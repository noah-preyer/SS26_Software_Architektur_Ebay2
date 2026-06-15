# API Examples

## Quick Start

### 1. Register a User

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "password123",
    "username": "john_doe"
  }'
```

### 2. Login

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "password123"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": "11111111-1111-1111-1111-111111111111"
}
```

**Save the token for subsequent requests:**
```bash
export TOKEN="eyJhbGciOiJIUzI1NiJ9..."
export USER_ID="11111111-1111-1111-1111-111111111111"
```

---

## Product Service

### Get All Products (Public - No Auth Required)

```bash
curl http://localhost:8080/products
```

### Get Product by ID (Public - No Auth Required)

```bash
curl http://localhost:8080/products/PRODUCT_ID_HERE
```

### Create Product (Requires Auth)

```bash
curl -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "title": "iPhone 15 Pro",
    "description": "Latest Apple smartphone with A17 Pro chip",
    "price": 999.99,
    "category": "Electronics",
    "quantity": 10
  }'
```

**Response:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "title": "iPhone 15 Pro",
  "price": 999.99,
  "category": "Electronics",
  "sellerId": "11111111-1111-1111-1111-111111111111",
  "quantity": 10,
  "status": "AVAILABLE"
}
```

**Save the product ID:**
```bash
export PRODUCT_ID="550e8400-e29b-41d4-a716-446655440000"
```

---

## Order Service

### Create Order (Requires Auth)

```bash
curl -X POST http://localhost:8080/order \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "userId": "'$USER_ID'",
    "productId": "'$PRODUCT_ID'",
    "currency": "USD"
  }'
```

**Response:**
```json
{
  "id": "660e8400-e29b-41d4-a716-446655440000",
  "userId": "11111111-1111-1111-1111-111111111111",
  "productId": "550e8400-e29b-41d4-a716-446655440000",
  "productTitle": "iPhone 15 Pro",
  "status": "CREATED",
  "totalAmount": 999.99,
  "currency": "USD"
}
```

**Save the order ID:**
```bash
export ORDER_ID="660e8400-e29b-41d4-a716-446655440000"
```

### Get Order by ID

```bash
curl http://localhost:8080/order/$ORDER_ID \
  -H "Authorization: Bearer $TOKEN"
```

### Get User Orders

```bash
curl http://localhost:8080/order/user/$USER_ID \
  -H "Authorization: Bearer $TOKEN"
```

---

## Payment Service

### Process Payment

```bash
curl -X POST http://localhost:8080/payment/process \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "orderId": "'$ORDER_ID'",
    "productId": "'$PRODUCT_ID'",
    "userId": "'$USER_ID'",
    "amount": 999.99,
    "currency": "USD",
    "paymentMethodType": "CREDIT_CARD"
  }'
```

**Response:**
```json
{
  "id": "770e8400-e29b-41d4-a716-446655440000",
  "orderId": "660e8400-e29b-41d4-a716-446655440000",
  "status": "COMPLETED",
  "transactionId": "txn_abc123...",
  "amount": 999.99,
  "currency": "USD",
  "paymentMethodType": "CREDIT_CARD"
}
```

**Save the payment ID:**
```bash
export PAYMENT_ID="770e8400-e29b-41d4-a716-446655440000"
```

### Get Payment by ID

```bash
curl http://localhost:8080/payment/$PAYMENT_ID \
  -H "Authorization: Bearer $TOKEN"
```

### Get Payments by Order

```bash
curl http://localhost:8080/payment/order/$ORDER_ID \
  -H "Authorization: Bearer $TOKEN"
```

---

## Order Status Updates

### Update Order Status to SHIPPED

```bash
curl -X PUT http://localhost:8080/order/$ORDER_ID/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "status": "SHIPPED"
  }'
```

### Update Order Status to DELIVERED

```bash
curl -X PUT http://localhost:8080/order/$ORDER_ID/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "status": "DELIVERED"
  }'
```

---

## Refund Flow

### Refund Order (Orchestrated)

This endpoint orchestrates the refund process:
1. Calls Payment Service to process refund
2. Marks order as REFUNDED
3. Sends refund confirmation email

```bash
curl -X POST http://localhost:8080/order/$ORDER_ID/refund \
  -H "Authorization: Bearer $TOKEN"
```

---

## Complete Flow Example

```bash
# 1. Register
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"pass123","username":"testuser"}'

# 2. Login and save token
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"pass123"}' | jq -r '.token')

# 3. Create product
PRODUCT_ID=$(curl -s -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"title":"Test Product","price":29.99,"category":"Test","quantity":5}' | jq -r '.id')

# 4. Create order
ORDER_ID=$(curl -s -X POST http://localhost:8080/order \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"userId":"'$USER_ID'","productId":"'$PRODUCT_ID'","currency":"USD"}' | jq -r '.id')

# 5. Process payment (this triggers automatic status updates)
curl -X POST http://localhost:8080/payment/process \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"orderId":"'$ORDER_ID'","productId":"'$PRODUCT_ID'","userId":"'$USER_ID'","amount":29.99,"currency":"USD","paymentMethodType":"CREDIT_CARD"}'

# 6. Wait and check order status (auto-updates: PAID → SHIPPED → DELIVERED)
sleep 25
curl http://localhost:8080/order/$ORDER_ID -H "Authorization: Bearer $TOKEN"
```

---

## Email Notifications

The following emails are automatically sent:

| Event | Email Template | When |
|-------|----------------|------|
| Payment | ORDER_CONFIRMATION | When payment is processed |
| Shipping | SHIPPING_CONFIRMATION | When order is shipped (auto: +10s) |
| Delivery | DELIVERY_CONFIRMATION | When order is delivered (auto: +20s) |
| Refund | ORDER_CONFIRMATION | When order is refunded |
