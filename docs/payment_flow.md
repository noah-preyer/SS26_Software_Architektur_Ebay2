# Payment Flow

## Overview

The payment flow handles payment processing, refunds, and email notifications.

## Sequence Diagram

```mermaid
sequenceDiagram
    participant Client as Client<br/>(Web/Mobile)
    participant GW as API Gateway<br/>:8080
    participant Payment as Payment Service<br/>:8086
    participant Order as Order Service<br/>:8087
    participant User as User Service<br/>:8082
    participant Email as Email Service<br/>:8084
    participant PayDB as Payment DB
    participant OrderDB as Order DB

    Note over Client,OrderDB: Process Payment
    Client->>GW: POST /payment/process<br/>{orderId, userId,<br/>amount, currency,<br/>paymentMethodType}
    GW->>Payment: POST /payment/process
    
    Payment->>Order: GET /order/{orderId}
    Order->>OrderDB: SELECT order
    OrderDB-->>Order: Order data<br/>{productId, productTitle, totalAmount}
    Order-->>Payment: Order details
    
    alt Order Status = CREATED
        Payment->>PayDB: INSERT payment<br/>status=PENDING
        PayDB-->>Payment: Payment saved
        
        Payment->>Payment: markCompleted()<br/>Generate transactionId
        
        Payment->>PayDB: UPDATE payment<br/>status=COMPLETED
        PayDB-->>Payment: Payment updated
        
        Payment->>Order: PUT /order/{orderId}/paid
        Order->>OrderDB: UPDATE order<br/>status=PAID
        OrderDB-->>Order: Order updated
        
        Order->>User: GET /user/{userId}
        User-->>Order: User email
        
        Order->>Email: POST /notification/send<br/>{recipientEmail,<br/>templateCode: "ORDER_CONFIRMATION",<br/>placeholders: {orderId, productTitle,<br/>amount, currency}}
        Email-->>Order: 201 Created
        
        Order-->>Payment: 200 OK
        
        Payment-->>GW: 201 Created<br/>{payment}
        GW-->>Client: 201 Created
    else Order Status != CREATED
        Payment-->>GW: 409 Conflict<br/>{message: "Cannot pay for order<br/>with status: {status}"}
        GW-->>Client: 409 Conflict
    end

    Note over Client,OrderDB: Process Refund
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
        
        Order->>Email: POST /notification/send<br/>{recipientEmail,<br/>templateCode: "ORDER_CONFIRMATION",<br/>placeholders: {orderId, amount, currency}}
        Email-->>Order: 201 Created
        
        Order-->>GW: 200 OK<br/>{order with status REFUNDED}
        GW-->>Client: 200 OK
    else No Completed Payment Found
        Payment-->>Order: 404 Not Found
        Order-->>GW: 404 Not Found<br/>{message: "No completed payment found"}
        GW-->>Client: 404 Not Found
    end
```

## Error Handling

```mermaid
sequenceDiagram
    participant Client
    participant GW as API Gateway
    participant Payment as Payment Service
    participant Order as Order Service

    Note over Client,Order: Error Scenarios
    
    rect rgb(255, 230, 230)
        Note right of Client: Order Not Found
        Client->>GW: POST /payment/process
        GW->>Payment: POST /payment/process
        Payment->>Order: GET /order/{orderId}
        Order-->>Payment: 404 Not Found
        Payment-->>GW: 404 Not Found<br/>{message: "Order not found"}
        GW-->>Client: 404 Not Found
    end
    
    rect rgb(255, 230, 230)
        Note right of Client: Order Already Paid
        Client->>GW: POST /payment/process
        GW->>Payment: POST /payment/process
        Payment->>Order: GET /order/{orderId}
        Order-->>Payment: Order (status=PAID)
        Payment-->>GW: 409 Conflict<br/>{message: "Cannot pay for order<br/>with status: PAID"}
        GW-->>Client: 409 Conflict
    end
    
    rect rgb(255, 230, 230)
        Note right of Client: Payment Already Refunded
        Client->>GW: POST /payment/{id}/refund
        GW->>Payment: POST /payment/{id}/refund
        Payment-->>GW: 409 Conflict<br/>{message: "Payment already refunded"}
        GW-->>Client: 409 Conflict
    end
    
    rect rgb(255, 230, 230)
        Note right of Client: Order Service Unavailable
        Client->>GW: POST /payment/process
        GW->>Payment: POST /payment/process
        Payment->>Order: GET /order/{orderId}
        Order-->>Payment: 503 Service Unavailable
        Payment-->>GW: 500 Internal Server Error
        GW-->>Client: 500 Internal Server Error
    end
```

## Payment States

```mermaid
stateDiagram-v2
    [*] --> PENDING: Payment Created
    PENDING --> COMPLETED: Payment Successful
    PENDING --> FAILED: Payment Failed
    COMPLETED --> REFUNDED: Refund Processed
    FAILED --> [*]
    COMPLETED --> [*]
    REFUNDED --> [*]
    
    note right of PENDING: Initial state
    note right of COMPLETED: Payment confirmed
    note right of REFUNDED: Full refund processed
```

## Service Communication

| From | To | Method | Endpoint | Purpose |
|------|-----|--------|----------|---------|
| API Gateway | Payment Service | POST | /payment/process | Process payment |
| API Gateway | Payment Service | GET | /payment/{id} | Get payment details |
| API Gateway | Payment Service | POST | /payment/{id}/refund | Refund payment (legacy) |
| API Gateway | Payment Service | GET | /payment/order/{orderId} | List payments for order |
| API Gateway | Order Service | POST | /order/{orderId}/refund | Refund order (orchestrated) |
| Payment Service | Order Service | GET | /order/{orderId} | Get order details |
| Payment Service | Order Service | PUT | /order/{orderId}/paid | Mark order as paid |
| Order Service | Payment Service | PUT | /payment/order/{orderId}/refund | Process refund |
| Order Service | User Service | GET | /user/{userId} | Get user email |
| Order Service | Email Service | POST | /notification/send | Send confirmation email |

## Database Operations

| Operation | Table | Description |
|-----------|-------|-------------|
| INSERT | payments | Create new payment |
| SELECT | payments | Get payment by ID |
| SELECT | payments | Get payments by order ID |
| UPDATE | payments | Update payment status |
