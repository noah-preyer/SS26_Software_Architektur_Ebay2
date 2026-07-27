```mermaid
sequenceDiagram
    participant F as Frontend
    participant G as API Gateway
    participant O as Order Service
    participant P as Product Service
    participant U as User Service
    participant M as MQTT Broker
    participant E as Email Service
    participant S as Scheduler

    F->>G: POST /order/checkout
    Note over F,G: { items: [{productId, quantity}] }

    Note over G: Validate JWT, extract userId
    G->>O: Forward + X-User-Id header

    loop For each item
        O->>P: GET /products/{id}
        P-->>O: price, title, sellerId, status
        Note over O: Check: exists, AVAILABLE,<br>buyerId != sellerId
    end

    loop Reserve (mark SOLD) for each item
        O->>P: PUT /products/{id}/reserve
        P-->>O: OK (marked SOLD)
        Note over O: On failure: rollback<br>previous reserves
    end

    Note over O: Create Order, status = CREATED

    O->>U: GET /user/{userId}
    U-->>O: email, username

    O->>M: publish "order/complete"
    Note right of M: Async event

    O-->>G: 201 { orderId, status: "CREATED", total }
    G-->>F: 201 { orderId, status: "CREATED", total }

    M->>E: deliver "order/complete"
    Note over E: Parse JSON<br>Render template<br>Send SMTP email<br>Save to DB

    Note over S: @Scheduled(every 10s)

    S->>O: find CREATED orders<br>older than 10 seconds
    O->>O: markPaid() → PAID

    Note over S: Future: PAID → SHIPPED → DELIVERED
```

```mermaid
stateDiagram-v2
    [*] --> CREATED
    CREATED --> PAID : timer (10s)
    PAID --> SHIPPED : timer (future)
    SHIPPED --> DELIVERED : timer (future)
    CREATED --> CANCELLED
    PAID --> CANCELLED

    note right of CREATED : Email sent here
```

## Event-Status Mapping

| Order Status | MQTT Event | Email Template | When |
|-------------|------------|----------------|------|
| CREATED | `order/complete` | ORDER_CONFIRMATION | Immediately after creation |
| PAID | *(future)* | PAYMENT_CONFIRMATION | 10s after CREATED |
| SHIPPED | *(future)* | SHIPPING_CONFIRMATION | 10s after PAID |
| DELIVERED | *(future)* | DELIVERY_CONFIRMATION | 10s after SHIPPED |
