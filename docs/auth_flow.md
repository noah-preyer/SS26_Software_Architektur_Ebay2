# Authentication Flow

## Overview

The authentication flow handles user login, token generation, and token validation.

## Public Endpoints

The following endpoints are accessible without a valid bearer token:

| Endpoint | Method | Description |
|----------|--------|-------------|
| /auth/** | ALL | Login, register, token validation |
| /products/** | GET | Browse products (view only) |
| /image/** | GET | View images |

All other endpoints require a valid JWT token in the `Authorization: Bearer {token}` header.

## Sequence Diagram

```mermaid
sequenceDiagram
    participant Client as Client<br/>(Web/Mobile)
    participant GW as API Gateway<br/>:8080
    participant Auth as Auth Service<br/>:8081
    participant User as User Service<br/>:8082
    participant DB as Auth DB

    Note over Client,DB: Login Flow (Public)
    Client->>GW: POST /auth/login<br/>{email, password}
    GW->>GW: Check if public endpoint<br/>(/auth/** → public)
    GW->>Auth: POST /auth/login
    
    Auth->>DB: SELECT user by email
    DB-->>Auth: User credentials
    
    alt Valid Password
        Auth->>Auth: Generate JWT Token
        Auth-->>GW: 200 OK<br/>{token, userId}
        GW-->>Client: 200 OK<br/>{token, userId}
    else Invalid Password
        Auth-->>GW: 401 Unauthorized
        GW-->>Client: 401 Unauthorized
    end

    Note over Client,DB: Browse Products (Public)
    Client->>GW: GET /products
    GW->>GW: Check if public endpoint<br/>(/products/** GET → public)
    GW->>Product: GET /products
    Product-->>GW: Product List
    GW-->>Client: 200 OK

    Note over Client,DB: Create Order (Protected)
    Client->>GW: POST /order<br/>{userId, productId, currency}
    GW->>GW: Check if public endpoint<br/>(/order/** POST → protected)
    GW->>GW: Validate JWT Token
    
    alt Valid Token
        GW->>Auth: POST /auth/validate<br/>{token}
        Auth->>DB: Check token validity
        DB-->>Auth: Token valid
        Auth-->>GW: 200 OK<br/>{userId, roles}
        GW->>Order: POST /order<br/>X-User-Id: {userId}
        Order-->>GW: 201 Created
        GW-->>Client: 201 Created
    else Invalid Token
        Auth-->>GW: 401 Unauthorized
        GW-->>Client: 401 Unauthorized
    end

    Note over Client,DB: Token Validation (on protected routes)
    Client->>GW: GET /user/{id}<br/>Authorization: Bearer {token}
    GW->>GW: Check if public endpoint<br/>(/user/** GET → protected)
    GW->>GW: Validate JWT Token
    
    alt Valid Token
        GW->>Auth: POST /auth/validate<br/>{token}
        Auth->>DB: Check token validity
        DB-->>Auth: Token valid
        Auth-->>GW: 200 OK<br/>{userId, roles}
        GW->>User: GET /user/{id}
        User-->>GW: User Profile
        GW-->>Client: 200 OK<br/>{userProfile}
    else Invalid Token
        Auth-->>GW: 401 Unauthorized
        GW-->>Client: 401 Unauthorized
    end

    Note over Client,DB: Registration Flow (Public)
    Client->>GW: POST /auth/register<br/>{email, password, username}
    GW->>GW: Check if public endpoint<br/>(/auth/** → public)
    GW->>Auth: POST /auth/register
    
    Auth->>DB: Check if email exists
    DB-->>Auth: Email available
    
    Auth->>Auth: Hash password
    Auth->>DB: INSERT new user
    DB-->>Auth: User created
    
    Auth->>GW: POST /user<br/>{authUserId, email, username}
    GW->>User: POST /user
    User-->>GW: 201 Created
    
    Auth-->>GW: 201 Created<br/>{token, userId}
    GW-->>Client: 201 Created<br/>{token, userId}
```

## Error Handling

```mermaid
sequenceDiagram
    participant Client
    participant GW as API Gateway
    participant Auth as Auth Service

    Note over Client,Auth: Error Scenarios
    
    rect rgb(255, 230, 230)
        Note right of Client: Missing Token on Protected Route
        Client->>GW: GET /user/{id}<br/>(no Authorization header)
        GW->>GW: Check if public endpoint<br/>(/user/** GET → protected)
        GW-->>Client: 401 Unauthorized<br/>{message: "Missing token"}
    end
    
    rect rgb(255, 230, 230)
        Note right of Client: Expired Token
        Client->>GW: GET /user/{id}<br/>Authorization: Bearer {expired}
        GW->>GW: Check if public endpoint<br/>(/user/** GET → protected)
        GW->>Auth: POST /auth/validate
        Auth-->>GW: 401 Unauthorized<br/>{message: "Token expired"}
        GW-->>Client: 401 Unauthorized
    end
    
    rect rgb(255, 230, 230)
        Note right of Client: Service Unavailable
        Client->>GW: POST /auth/login
        GW->>GW: Check if public endpoint<br/>(/auth/** → public)
        GW->>Auth: POST /auth/login
        Auth-->>GW: 503 Service Unavailable
        GW-->>Client: 503 Service Unavailable
    end
```

## Service Communication

| From | To | Method | Endpoint | Purpose |
|------|-----|--------|----------|---------|
| API Gateway | Auth Service | POST | /auth/login | User login |
| API Gateway | Auth Service | POST | /auth/register | User registration |
| API Gateway | Auth Service | POST | /auth/validate | Token validation |
| Auth Service | User Service | POST | /user | Create user profile |

## Database Operations

| Operation | Table | Description |
|-----------|-------|-------------|
| SELECT | users | Get user by email |
| INSERT | users | Create new user |
| SELECT | tokens | Validate JWT token |
