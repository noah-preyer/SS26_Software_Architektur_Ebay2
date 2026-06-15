# System Architecture

## Services Overview

| Service | Port | Container | Database | Description |
|---------|------|-----------|----------|-------------|
| API Gateway | 8080 | api-gateway | - | Routes requests to services |
| Auth Service | 8081 | auth-service | auth-service-db | Authentication & authorization |
| User Service | 8082 | user-service | user-service-db | User profile management |
| Product Service | 8082 | product-service | product-db | Product catalog |
| Order Service | 8087 | order-service | order-service-db | Order management |
| Payment Service | 8086 | payment-service | payment-service-db | Payment processing |
| Email Service | 8084 | notification-service | notification-service-db | Email notifications |
| Image Service | 8088 | image-service | image-service-db | Image upload/storage |

## Architecture Diagram

```mermaid
graph TB
    subgraph "External"
        Client[Client<br/>Web/Mobile]
    end
    
    subgraph "API Layer"
        GW[API Gateway<br/>:8080]
    end
    
    subgraph "Core Services"
        Auth[Auth Service<br/>:8081]
        User[User Service<br/>:8082]
        Product[Product Service<br/>:8082]
        Order[Order Service<br/>:8087]
        Payment[Payment Service<br/>:8086]
    end
    
    subgraph "Support Services"
        Email[Email Service<br/>:8084]
        Image[Image Service<br/>:8088]
    end
    
    subgraph "Databases"
        AuthDB[(auth-service-db)]
        UserDB[(user-service-db)]
        ProductDB[(product-db)]
        OrderDB[(order-service-db)]
        PaymentDB[(payment-service-db)]
        EmailDB[(notification-service-db)]
        ImageDB[(image-service-db)]
    end
    
    Client --> GW
    
    GW --> Auth
    GW --> User
    GW --> Product
    GW --> Order
    GW --> Payment
    GW --> Email
    GW --> Image
    
    Payment --> Order
    Payment --> User
    Payment --> Email
    
    Auth --> AuthDB
    User --> UserDB
    Product --> ProductDB
    Order --> OrderDB
    Payment --> PaymentDB
    Email --> EmailDB
    Image --> ImageDB
    
    style GW fill:#f9f,stroke:#333,stroke-width:2px
    style Auth fill:#bbf,stroke:#333,stroke-width:2px
    style User fill:#bfb,stroke:#333,stroke-width:2px
    style Product fill:#fbb,stroke:#333,stroke-width:2px
    style Order fill:#bdf,stroke:#333,stroke-width:2px
    style Payment fill:#fdb,stroke:#333,stroke-width:2px
    style Email fill:#dfd,stroke:#333,stroke-width:2px
    style Image fill:#ffd,stroke:#333,stroke-width:2px
```

## API Gateway Security

### Public Endpoints (No Authentication Required)

| Endpoint | Method | Description |
|----------|--------|-------------|
| /auth/** | ALL | Login, register, token validation |
| /products/** | GET | Browse products (view only) |
| /image/** | GET | View images |

### Protected endpoints (JWT Token Required)

All other endpoints require a valid JWT token in the `Authorization: Bearer {token}` header.

The API Gateway:
1. Validates the JWT token
2. Extracts `userId` from the token
3. Adds `X-User-Id` header to downstream service requests

## Request Flow

```mermaid
sequenceDiagram
    participant C as Client
    participant GW as API Gateway
    participant S as Service

    C->>GW: HTTP Request
    GW->>GW: Check if public endpoint
    GW->>GW: Validate JWT Token (if protected)
    GW->>GW: Route to Service
    
    alt Valid Route
        GW->>S: Forward Request<br/>(with X-User-Id header)
        S-->>GW: Response
        GW-->>C: Response
    else Invalid Route
        GW-->>C: 404 Not Found
    end
```

## Inter-Service Communication

```mermaid
graph LR
    subgraph "Synchronous (REST)"
        P-->|GET /order/{id}| O
        P-->|PUT /order/{id}/paid| O
        O-->|PUT /payment/order/{id}/refund| P
        O-->|GET /products/{id}| Pr
        O-->|GET /user/{id}| U
        O-->|POST /notification/send| E
    end
    
    subgraph "Service Names"
        P[Payment Service]
        O[Order Service]
        Pr[Product Service]
        U[User Service]
        E[Email Service]
    end
```

## Port Mapping

```mermaid
graph TB
    subgraph "Host Ports"
        H8080[8080]
        H8081[8081]
        H8082[8082]
        H8084[8084]
        H8086[8086]
        H8087[8087]
        H8088[8088]
    end
    
    subgraph "Container Ports"
        C8080[8080]
    end
    
    H8080 --> C8080
    H8081 --> C8080
    H8082 --> C8080
    H8084 --> C8080
    H8086 --> C8080
    H8087 --> C8080
    H8088 --> C8080
    
    style H8080 fill:#f9f,stroke:#333
    style H8081 fill:#bbf,stroke:#333
    style H8082 fill:#bfb,stroke:#333
    style H8084 fill:#dfd,stroke:#333
    style H8086 fill:#fdb,stroke:#333
    style H8087 fill:#bdf,stroke:#333
    style H8088 fill:#ffd,stroke:#333
```

## Database Schema

```mermaid
erDiagram
    USERS {
        uuid id PK
        uuid authUserId
        string username
        string email
        string firstName
        string lastName
        string phoneNumber
    }
    
    ORDERS {
        uuid id PK
        uuid userId FK
        string productId
        string status
        decimal totalAmount
        string currency
        timestamp createdAt
        timestamp updatedAt
    }
    
    PAYMENTS {
        uuid id PK
        uuid orderId FK
        string productId
        uuid userId FK
        decimal amount
        string currency
        string status
        string paymentMethodType
        string transactionId
        timestamp paidAt
    }
    
    USERS ||--o{ ORDERS : places
    USERS ||--o{ PAYMENTS : makes
    ORDERS ||--o{ PAYMENTS : has
```

## Email Notifications

The Order Service sends email notifications for the following events:

| Event | Template Code | When |
|-------|---------------|------|
| Payment Confirmation | ORDER_CONFIRMATION | When order status → PAID |
| Shipping Confirmation | SHIPPING_CONFIRMATION | When order status → SHIPPED |
| Delivery Confirmation | DELIVERY_CONFIRMATION | When order status → DELIVERED |
| Refund Confirmation | ORDER_CONFIRMATION | When order status → REFUNDED |

```mermaid
graph LR
    O[Order Service] -->|POST /notification/send| E[Email Service]
    
    subgraph "Email Templates"
        T1[ORDER_CONFIRMATION]
        T2[SHIPPING_CONFIRMATION]
        T3[DELIVERY_CONFIRMATION]
    end
    
    E --> T1
    E --> T2
    E --> T3
```

## Environment Variables

| Service | Variable | Default | Description |
|---------|----------|---------|-------------|
| Payment Service | ORDER_SERVICE_URL | http://order-service:8080 | Order service endpoint |
| Order Service | USER_SERVICE_URL | http://user-service:8080 | User service endpoint |
| Order Service | EMAIL_SERVICE_URL | http://notification-service:8080 | Email service endpoint |
| Order Service | PAYMENT_SERVICE_URL | http://payment-service:8080 | Payment service endpoint |
| Order Service | PRODUCT_SERVICE_URL | http://product-service:8082 | Product service endpoint |
| Auth Service | USER_SERVICE_URL | http://user-service:8080 | User service endpoint |
| Product Service | PAYMENT_SERVICE_URL | http://payment-service:8080 | Payment service endpoint |
