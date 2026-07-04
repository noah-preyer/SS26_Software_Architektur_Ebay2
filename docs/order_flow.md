# Order Flow

## Overview

The order flow handles order creation, status management, and order history.

## Sequence Diagram

```mermaid
sequenceDiagram
    participant Client as Client<br/>(Web/Mobile)
    participant GW as API Gateway<br/>:8080
    participant Order as Order Service<br/>:8087
    participant Product as Product Service<br/>:8082
    participant DB as Order DB

    Note over Client,DB: Create Order
    Client->>GW: POST /order<br/>{userId, productId, currency}
    GW->>Order: POST /order
    
    Order->>Product: GET /products/{productId}
    Product-->>Order: Product details<br/>{id, title, price}
    
    alt Product Found
        Order->>DB: INSERT order<br/>status=CREATED<br/>productTitle=title<br/>totalAmount=price
        DB-->>Order: Order saved
        
        Order-->>GW: 201 Created<br/>{orderId, productTitle, totalAmount}
        GW-->>Client: 201 Created
    else Product Not Found
        Order-->>GW: 404 Not Found<br/>{message: "Product not found"}
        GW-->>Client: 404 Not Found
    end

    Note over Client,DB: Get Order
    Client->>GW: GET /order/{orderId}
    GW->>Order: GET /order/{orderId}
    
    Order->>DB: SELECT order by id
    DB-->>Order: Order data
    
    alt Order Found
        Order-->>GW: 200 OK<br/>{order}
        GW-->>Client: 200 OK
    else Order Not Found
        Order-->>GW: 404 Not Found
        GW-->>Client: 404 Not Found
    end

    Note over Client,DB: Update Order Status
    Client->>GW: PUT /order/{orderId}/status<br/>{status: "SHIPPED"}
    GW->>Order: PUT /order/{orderId}/status
    
    Order->>DB: SELECT order
    DB-->>Order: Current order
    
    alt Valid Status Transition
        Order->>Order: Validate transition<br/>CREATED → SHIPPED
        Order->>DB: UPDATE order status
        DB-->>Order: Status updated
        Order-->>GW: 200 OK<br/>{order with new status}
        GW-->>Client: 200 OK
    else Invalid Transition
        Order-->>GW: 409 Conflict<br/>{message: "Invalid transition"}
        GW-->>Client: 409 Conflict
    end

    Note over Client,DB: Get User Orders
    Client->>GW: GET /order/user/{userId}
    GW->>Order: GET /order/user/{userId}
    
    Order->>DB: SELECT orders by userId<br/>ORDER BY createdAt DESC
    DB-->>Order: Order list
    
    Order-->>GW: 200 OK<br/>[{order1}, {order2}]
    GW-->>Client: 200 OK
```

## Refund Flow (User-Initiated)

```mermaid
sequenceDiagram
    participant Client as Client<br/>(Web/Mobile)
    participant GW as API Gateway<br/>:8080
    participant Order as Order Service<br/>:8087
    participant Payment as Payment Service<br/>:8086
    participant User as User Service<br/>:8082
    participant Email as Email Service<br/>:8084
    participant OrderDB as Order DB
    participant PayDB as Payment DB

    Client->>GW: POST /order/{orderId}/refund
    GW->>Order: POST /order/{orderId}/refund
    
    Order->>Payment: PUT /payment/order/{orderId}/refund
    Payment->>PayDB: SELECT payment for order
    PayDB-->>Payment: Payment data
    
    alt Payment Found and Completed
        Payment->>Payment: markRefunded()
        Payment->>PayDB: UPDATE payment<br/>status=REFUNDED
        PayDB-->>Payment: Payment updated
        Payment-->>Order: 200 OK
        
        Order->>OrderDB: UPDATE order<br/>status=REFUNDED
        OrderDB-->>Order: Order updated
        
        Order->>User: GET /user/{userId}
        User-->>Order: User email
        
        Order->>Email: POST /notification/send<br/>{recipientEmail,<br/>templateCode: "ORDER_CONFIRMATION"}
        Email-->>Order: 201 Created
        
        Order-->>GW: 200 OK<br/>{order with status REFUNDED}
        GW-->>Client: 200 OK
    else No Completed Payment Found
        Payment-->>Order: 404 Not Found
        Order-->>GW: 404 Not Found
        GW-->>Client: 404 Not Found
    end
```

## Internal Status Updates (from Payment Service)

```mermaid
sequenceDiagram
    participant PS as Payment Service<br/>:8086
    participant Order as Order Service<br/>:8087
    participant User as User Service<br/>:8082
    participant Email as Email Service<br/>:8084
    participant DB as Order DB

    Note over PS,DB: Payment Completed
    PS->>Order: PUT /order/{orderId}/paid
    Order->>DB: SELECT order
    DB-->>Order: Order data
    
    alt Order Exists
        Order->>Order: markPaid()<br/>status → PAID
        Order->>DB: UPDATE order status
        DB-->>Order: Status updated
        
        Order->>User: GET /user/{userId}
        User-->>Order: User email
        
        Order->>Email: POST /notification/send<br/>{recipientEmail,<br/>templateCode: "ORDER_CONFIRMATION"}
        Email-->>Order: 201 Created
        
        Order-->>PS: 200 OK
    else Order Not Found
        Order-->>PS: 404 Not Found
    end
```

## Automatic Status Updates

The Order Service includes a scheduler that automatically transitions order statuses:

```mermaid
sequenceDiagram
    participant Scheduler as Order Status Scheduler<br/>(every 1s)
    participant Order as Order Service<br/>:8087
    participant Email as Email Service<br/>:8084
    participant User as User Service<br/>:8082
    participant DB as Order DB

    Note over Scheduler,DB: Check for orders to update (every second)

    Scheduler->>DB: Find PAID orders updated >10s ago
    DB-->>Scheduler: [order1, order2]
    
    loop For each PAID order
        Scheduler->>Order: markOrderShipped(orderId)
        Order->>DB: UPDATE status = SHIPPED
        Order->>User: GET /user/{userId}
        User-->>Order: User email
        Order->>Email: POST /notification/send<br/>templateCode: "SHIPPING_CONFIRMATION"
        Email-->>Order: 201 Created
    end

    Scheduler->>DB: Find SHIPPED orders updated >10s ago
    DB-->>Scheduler: [order3]
    
    loop For each SHIPPED order
        Scheduler->>Order: markOrderDelivered(orderId)
        Order->>DB: UPDATE status = DELIVERED
        Order->>User: GET /user/{userId}
        User-->>Order: User email
        Order->>Email: POST /notification/send<br/>templateCode: "DELIVERY_CONFIRMATION"
        Email-->>Order: 201 Created
    end
```

**Timeline:**
```
0s   - Order created (status: CREATED)
0s   - Payment processed (status: PAID) → Email: Payment Confirmation
10s  - Auto-updated (status: SHIPPED) → Email: Shipping Confirmation
20s  - Auto-updated (status: DELIVERED) → Email: Delivery Confirmation
```

## Error Handling

| Scenario | HTTP Code | Message |
|----------|-----------|---------|
| Order not found | 404 | "Order not found: {orderId}" |
| Invalid status transition | 409 | "Invalid status transition: {status}" |
| Missing required fields | 400 | "userId: required, totalAmount: required" |

## Service Communication

| From | To | Method | Endpoint | Purpose |
|------|-----|--------|----------|---------|
| API Gateway | Order Service | POST | /order | Create order |
| API Gateway | Order Service | GET | /order/{id} | Get order details |
| API Gateway | Order Service | PUT | /order/{id}/status | Update status |
| API Gateway | Order Service | GET | /order/user/{id} | List user orders |
| API Gateway | Order Service | POST | /order/{id}/refund | Refund order (orchestrated) |
| Order Service | Product Service | GET | /products/{id} | Get product details |
| Order Service | Payment Service | PUT | /payment/order/{id}/refund | Process refund |
| Order Service | User Service | GET | /user/{userId} | Get user email |
| Order Service | Email Service | POST | /notification/send | Send payment confirmation |
| Order Service | Email Service | POST | /notification/send | Send shipping confirmation |
| Order Service | Email Service | POST | /notification/send | Send delivery confirmation |
| Order Service | Email Service | POST | /notification/send | Send refund confirmation |
| Payment Service | Order Service | PUT | /order/{id}/paid | Mark as paid |

## Database Operations

| Operation | Table | Description |
|-----------|-------|-------------|
| INSERT | orders | Create new order |
| SELECT | orders | Get order by ID |
| SELECT | orders | Get orders by user ID |
| UPDATE | orders | Update order status |
