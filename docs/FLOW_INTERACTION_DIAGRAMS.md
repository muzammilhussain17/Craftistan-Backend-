# Craftistan - Flow and User Interaction Diagrams

This document contains Mermaid diagrams illustrating the key user interactions and system flows within the Craftistan E-Commerce Platform.

## 1. User Registration & Authentication Flow

This sequence diagram shows how a user interacts with the system to register and authenticate, receiving a JWT token for subsequent requests.

```mermaid
sequenceDiagram
    actor User
    participant Client as Frontend / Mobile App
    participant Auth as AuthController
    participant Service as AuthService
    participant DB as PostgreSQL Database

    %% Registration Flow
    User->>Client: Enters registration details
    Client->>Auth: POST /api/auth/register
    Auth->>Service: registerUser(request)
    Service->>DB: Check if email exists
    DB-->>Service: Email available
    Service->>Service: Hash password (BCrypt)
    Service->>DB: Save User (Role: BUYER/ARTISAN)
    DB-->>Service: User saved
    Service-->>Auth: AuthResponse (Token, User details)
    Auth-->>Client: 200 OK
    Client-->>User: Registration Successful

    %% Login Flow
    User->>Client: Enters login credentials
    Client->>Auth: POST /api/auth/login
    Auth->>Service: login(request)
    Service->>DB: Fetch user by email
    DB-->>Service: User details with hashed password
    Service->>Service: Validate password
    Service->>Service: Generate JWT Token
    Service-->>Auth: AuthResponse (Token)
    Auth-->>Client: 200 OK + JWT Token
    Client->>Client: Store Token securely
    Client-->>User: Logged In successfully
```

## 2. Customer Shopping Flow (Browsing to Checkout)

This flowchart illustrates the end-to-end shopping experience for a Buyer interactively browsing products, managing their cart, and creating an order.

```mermaid
flowchart TD
    Start([User visits Application]) --> Browse[Browse Products / Home Page]
    Browse --> Search[Search / Filter by Category]
    Search --> ViewProduct[View Product Details]
    Browse --> ViewProduct
    
    ViewProduct -->|User wants to save| AddWishlist{Add to Wishlist?}
    AddWishlist -->|Yes| WishlistAPI[POST /api/wishlist]
    WishlistAPI --> ViewProduct
    AddWishlist -->|No| AddCart{Add to Cart?}
    
    AddCart -->|Yes| Cart[Update Local Cart]
    Cart --> CheckoutDecision{Checkout?}
    AddCart -->|No| Browse
    
    CheckoutDecision -->|Continue Shopping| Browse
    CheckoutDecision -->|Proceed| Checkout[Checkout Process]
    
    Checkout --> LoginCheck{Is Logged In?}
    LoginCheck -->|No| Login[Login / Register]
    Login --> Checkout
    LoginCheck -->|Yes| CreateOrder[POST /api/orders]
    
    CreateOrder --> OrderService[Order Service Processing]
    OrderService -.-> VerifyStock[Verify Stock Availability]
    OrderService -.-> ReduceStock[Deduct from Product Stock]
    OrderService -.-> SaveOrder[Save to Database]
    
    SaveOrder --> OrderSuccess([Order Confirmation Received])
```

## 3. Order Lifecycle (State Machine)

This state diagram shows the states an order goes through from placement to delivery, primarily driven by the Artisan and System updates.

```mermaid
stateDiagram-v2
    [*] --> PENDING: Order Created
    
    PENDING --> PROCESSING: Artisan accepts order
    PENDING --> CANCELLED: Buyer/Artisan cancels order
    
    PROCESSING --> SHIPPED: Artisan ships product
    PROCESSING --> CANCELLED: Artisan cancels (out of stock)
    
    SHIPPED --> DELIVERED: Buyer receives product
    
    DELIVERED --> [*]
    CANCELLED --> [*]
```

## 4. Artisan Product Management Interaction

This sequence diagram depicts an Artisan managing their products on the platform, including the automated translation feature.

```mermaid
sequenceDiagram
    actor Artisan
    participant Client as Artisan Dashboard
    participant API as ProductController
    participant Service as Product/Artisan Service
    participant Gemini as Google Gemini API
    participant DB as Database

    Artisan->>Client: Create/Edit Product
    Client->>API: POST /api/artisan/products + JWT
    API->>Service: createProduct(request)
    Service->>DB: Save Product details (en)
    DB-->>Service: Product saved
    
    %% Async Translation
    Service->>Service: Trigger Async Translation
    par Translation Process
        Service->>Gemini: Request translations (ur, pa, sd, ps, bal)
        Gemini-->>Service: Return Translated JSON
        Service->>DB: Update Product with translations
    end
    
    Service-->>API: Product Creation Response
    API-->>Client: 201 Created
    Client-->>Artisan: Product published successfully
```

## 5. User Review & Verification Flow

This diagram defines how a user leaves a review and how the system validates if they actually purchased the item to grant a "Verified Purchase" badge.

```mermaid
flowchart LR
    User([User]) -->|Submits Review & Rating| SubmitReview
    SubmitReview[POST /api/products/{id}/reviews] --> Validation{Has User Ordered Product?}
    
    Validation -->|Yes| CheckStatus{Is Order DELIVERED?}
    Validation -->|No| RejectReview[Reject Review\nReturn 403 Forbidden]
    
    CheckStatus -->|Yes| SaveVerified[Save Review\nSet Verified = True]
    CheckStatus -->|No| SaveUnverified[Save Review\nSet Verified = False]
    
    SaveVerified --> UpdateRating[Update Product Average Rating]
    SaveUnverified --> UpdateRating
    
    UpdateRating --> Success([Return Success to User])
    RejectReview --> Error([Show Error to User])
```

## 6. AI Chatbot Support Interaction

This sequence diagram shows how a user interacts with the AI-powered customer support chatbot feature available on Craftistan.

```mermaid
sequenceDiagram
    actor User
    participant ChatWidget as Chat Widget UI
    participant ChatAPI as ChatController
    participant ChatService as ChatService
    participant Gemini as Google Gemini API
    
    User->>ChatWidget: Sends message / query
    ChatWidget->>ChatAPI: POST /api/chat/message (msg, threadId)
    ChatAPI->>ChatService: processMessage()
    
    ChatService->>ChatService: Retrieve Chat History
    ChatService->>ChatService: Add Craftistan System Prompt Context
    
    ChatService->>Gemini: Stream Chat Request (Context + Message)
    Gemini-->>ChatService: AI Response
    
    ChatService->>ChatService: Save Interaction to History
    ChatService-->>ChatAPI: Return Response Text
    ChatAPI-->>ChatWidget: 200 OK (Message)
    
    ChatWidget-->>User: Display Reply
```
