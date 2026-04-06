# 📋 Technical Handover Document
## Craftistan E-Commerce Platform - Backend System

---

**Document Version:** 1.0  
**Document Date:** February 2026  
**Project Name:** Craftistan Backend  
**Project Version:** 1.0.0  
**Prepared By:** Development Team  
**Confidentiality:** Client Confidential  

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Project Overview](#2-project-overview)
3. [System Architecture](#3-system-architecture)
4. [Technology Stack](#4-technology-stack)
5. [Application Structure](#5-application-structure)
6. [Database Design](#6-database-design)
7. [API Specification](#7-api-specification)
8. [Authentication & Authorization](#8-authentication--authorization)
9. [Business Logic](#9-business-logic)
10. [Third-Party Integrations](#10-third-party-integrations)
11. [Configuration Management](#11-configuration-management)
12. [Deployment Guide](#12-deployment-guide)
13. [Testing](#13-testing)
14. [Security Considerations](#14-security-considerations)
15. [Performance Considerations](#15-performance-considerations)
16. [Maintenance & Support](#16-maintenance--support)
17. [Known Issues & Limitations](#17-known-issues--limitations)
18. [Appendices](#18-appendices)

---

# 1. Executive Summary

## 1.1 Project Description

**Craftistan** is a comprehensive e-commerce platform backend designed specifically for the Pakistani artisan crafts marketplace. The system enables local artisans to showcase and sell their handcrafted products to a global audience, featuring multi-role user management, AI-powered content translation, and a complete order lifecycle management system.

## 1.2 Key Features

| Feature | Description |
|---------|-------------|
| **User Authentication** | JWT-based stateless authentication with role-based access control |
| **Product Management** | Full CRUD operations with image uploads and category management |
| **Order Processing** | Complete order lifecycle from creation to delivery |
| **AI Translation** | Automatic content translation to 6 Pakistani languages via Google Gemini API |
| **Artisan Dashboard** | Dedicated portal for artisans to manage products and view analytics |
| **Review System** | Product reviews and ratings with verified purchase badges |
| **Wishlist** | User wishlist functionality |
| **Chat Support** | AI-powered customer support chatbot |

## 1.3 Delivery Checklist

| Item | Status | Notes |
|------|--------|-------|
| Source Code Repository | ✅ Delivered | Git repository with full history |
| API Documentation | ✅ Delivered | Swagger UI + Postman Collection |
| Database Migrations | ✅ Delivered | Flyway migration scripts |
| Configuration Files | ✅ Delivered | Environment-based configuration |
| Testing Documentation | ✅ Delivered | Testing guide included |
| Technical Documentation | ✅ Delivered | This document |

---

# 2. Project Overview

## 2.1 Business Context

Pakistani artisans produce world-class handicrafts but often lack the technical infrastructure to reach global markets. Craftistan addresses this gap by providing:

- **Digital Presence**: Online storefront for artisan products
- **Multi-Language Support**: Content available in regional languages
- **Secure Transactions**: Robust order and payment handling
- **Business Analytics**: Dashboard for artisans to track performance

## 2.2 Stakeholders

| Role | Description |
|------|-------------|
| **Buyers** | End customers purchasing artisan products |
| **Artisans** | Product creators who manage their inventory |
| **System Admin** | Platform operators (future role) |

## 2.3 Project Scope

### In Scope
- User registration and authentication
- Product catalog management
- Order processing and tracking
- Review and rating system
- Wishlist functionality
- AI-powered translation
- AI chatbot assistance
- File upload management
- API documentation

### Out of Scope (Future Enhancements)
- Payment gateway integration (currently COD only)
- Admin panel frontend
- Push notifications
- Real-time messaging
- Mobile application APIs

---

# 3. System Architecture

## 3.1 High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              CLIENT LAYER                                     │
├─────────────────────────────────────────────────────────────────────────────┤
│    Web Application (React/Vue)    │    Mobile App    │    Postman/Testing   │
└───────────────────────────────────┴──────────────────┴──────────────────────┘
                                        │
                                        │ HTTPS (REST API)
                                        ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           API GATEWAY LAYER                                   │
├─────────────────────────────────────────────────────────────────────────────┤
│                        Spring Boot Application                                │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │
│  │   CORS      │  │   JWT       │  │   Rate      │  │   Logging   │         │
│  │   Filter    │  │   Filter    │  │   Limiting  │  │   Filter    │         │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘         │
└─────────────────────────────────────────────────────────────────────────────┘
                                        │
                                        ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                          APPLICATION LAYER                                    │
├─────────────────────────────────────────────────────────────────────────────┤
│  ┌──────────────────────────────────────────────────────────────────────┐   │
│  │                         CONTROLLERS (REST)                            │   │
│  │  AuthController │ ProductController │ OrderController │ UserController│   │
│  │  ReviewController │ WishlistController │ ArtisanController │ ChatController│
│  └──────────────────────────────────────────────────────────────────────┘   │
│                                     │                                         │
│                                     ▼                                         │
│  ┌──────────────────────────────────────────────────────────────────────┐   │
│  │                         SERVICES (Business Logic)                     │   │
│  │  AuthService │ ProductService │ OrderService │ UserService │ ...     │   │
│  └──────────────────────────────────────────────────────────────────────┘   │
│                                     │                                         │
│                                     ▼                                         │
│  ┌──────────────────────────────────────────────────────────────────────┐   │
│  │                         REPOSITORIES (Data Access)                    │   │
│  │  UserRepository │ ProductRepository │ OrderRepository │ ...          │   │
│  └──────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
                                        │
                                        ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                            DATA LAYER                                         │
├───────────────────────────────┬─────────────────────────────────────────────┤
│     PostgreSQL Database       │           File Storage                        │
│     (Primary Data Store)      │           (./uploads)                         │
└───────────────────────────────┴─────────────────────────────────────────────┘
                                        │
                                        ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         EXTERNAL SERVICES                                     │
├─────────────────────────────────────────────────────────────────────────────┤
│    Google Gemini API (Translation)    │    Brevo SMTP (Email)                 │
└───────────────────────────────────────┴─────────────────────────────────────┘
```

## 3.2 Component Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        CRAFTISTAN BACKEND MODULES                             │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                               │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐              │
│  │      AUTH       │  │      USER       │  │     PRODUCT     │              │
│  │   • Login       │  │   • Profile     │  │   • Catalog     │              │
│  │   • Register    │  │   • Addresses   │  │   • Categories  │              │
│  │   • JWT Utils   │  │   • Password    │  │   • Search      │              │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘              │
│                                                                               │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐              │
│  │      ORDER      │  │     REVIEW      │  │    WISHLIST     │              │
│  │   • Create      │  │   • Ratings     │  │   • Add/Remove  │              │
│  │   • Status      │  │   • Comments    │  │   • List        │              │
│  │   • Cancel      │  │   • Verified    │  │                 │              │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘              │
│                                                                               │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐              │
│  │    ARTISAN      │  │  TRANSLATION    │  │      CHAT       │              │
│  │   • Dashboard   │  │   • Gemini API  │  │   • AI Chatbot  │              │
│  │   • Products    │  │   • Multi-lang  │  │   • History     │              │
│  │   • Orders      │  │   • Async       │  │                 │              │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘              │
│                                                                               │
│  ┌─────────────────┐  ┌─────────────────┐                                   │
│  │     UPLOAD      │  │     COMMON      │                                   │
│  │   • Images      │  │   • DTOs        │                                   │
│  │   • Validation  │  │   • Exceptions  │                                   │
│  │                 │  │   • BaseEntity  │                                   │
│  └─────────────────┘  └─────────────────┘                                   │
│                                                                               │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │                           CONFIG                                     │    │
│  │   SecurityConfig │ JwtAuthFilter │ JwtUtils │ WebConfig │ JpaConfig │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
│                                                                               │
└─────────────────────────────────────────────────────────────────────────────┘
```

## 3.3 Sequence Diagrams

### 3.3.1 User Authentication Flow

```
┌──────┐          ┌─────────────────┐          ┌─────────────┐          ┌────────────┐
│Client│          │ AuthController  │          │ AuthService │          │ JwtUtils   │
└──┬───┘          └───────┬─────────┘          └──────┬──────┘          └─────┬──────┘
   │                      │                           │                       │
   │  POST /api/auth/login│                           │                       │
   │  {email, password}   │                           │                       │
   │ ────────────────────>│                           │                       │
   │                      │                           │                       │
   │                      │  login(LoginRequest)      │                       │
   │                      │ ─────────────────────────>│                       │
   │                      │                           │                       │
   │                      │                           │  Validate credentials │
   │                      │                           │  (BCrypt compare)     │
   │                      │                           │                       │
   │                      │                           │  generateToken(user)  │
   │                      │                           │ ─────────────────────>│
   │                      │                           │                       │
   │                      │                           │       JWT token       │
   │                      │                           │ <─────────────────────│
   │                      │                           │                       │
   │                      │  AuthResponse(token,user) │                       │
   │                      │ <─────────────────────────│                       │
   │                      │                           │                       │
   │  200 OK              │                           │                       │
   │  {token, user}       │                           │                       │
   │ <────────────────────│                           │                       │
   │                      │                           │                       │
```

### 3.3.2 Order Creation Flow

```
┌──────┐       ┌─────────────────┐       ┌──────────────┐       ┌────────────────┐
│Client│       │OrderController  │       │ OrderService │       │ProductRepository│
└──┬───┘       └───────┬─────────┘       └──────┬───────┘       └───────┬────────┘
   │                   │                        │                       │
   │  POST /api/orders │                        │                       │
   │  + JWT Header     │                        │                       │
   │ ─────────────────>│                        │                       │
   │                   │                        │                       │
   │                   │ createOrder(request,   │                       │
   │                   │             user)      │                       │
   │                   │ ──────────────────────>│                       │
   │                   │                        │                       │
   │                   │                        │  Validate products    │
   │                   │                        │ ─────────────────────>│
   │                   │                        │                       │
   │                   │                        │  Products found       │
   │                   │                        │ <─────────────────────│
   │                   │                        │                       │
   │                   │                        │  Calculate totals     │
   │                   │                        │  Generate order ID    │
   │                   │                        │  (ORD-YYYY-XXXX)      │
   │                   │                        │                       │
   │                   │                        │  Save order           │
   │                   │                        │  Update stock         │
   │                   │                        │                       │
   │                   │  OrderDto              │                       │
   │                   │ <──────────────────────│                       │
   │                   │                        │                       │
   │  200 OK           │                        │                       │
   │  {order details}  │                        │                       │
   │ <─────────────────│                        │                       │
   │                   │                        │                       │
```

### 3.3.3 Product Translation Flow

```
┌──────────────┐       ┌──────────────────┐       ┌──────────────────┐       ┌──────────────┐
│ArtisanService│       │TranslationService│       │    WebClient     │       │ Gemini API   │
└──────┬───────┘       └────────┬─────────┘       └────────┬─────────┘       └──────┬───────┘
       │                        │                          │                        │
       │  @Async                │                          │                        │
       │  translateProductAsync │                          │                        │
       │  (product)             │                          │                        │
       │ ──────────────────────>│                          │                        │
       │                        │                          │                        │
       │                        │  For each language:      │                        │
       │                        │  (ur, pa, sd, ps, bal)  │                        │
       │                        │                          │                        │
       │                        │  POST /generateContent   │                        │
       │                        │ ────────────────────────>│                        │
       │                        │                          │                        │
       │                        │                          │  Request translation   │
       │                        │                          │ ──────────────────────>│
       │                        │                          │                        │
       │                        │                          │  Translated JSON       │
       │                        │                          │ <──────────────────────│
       │                        │                          │                        │
       │                        │  Response                │                        │
       │                        │ <────────────────────────│                        │
       │                        │                          │                        │
       │                        │  Parse JSON response     │                        │
       │                        │  Store in translations   │                        │
       │                        │  field as JSON           │                        │
       │                        │                          │                        │
       │  Product saved with    │                          │                        │
       │  translations          │                          │                        │
       │ <──────────────────────│                          │                        │
       │                        │                          │                        │
```

---

# 4. Technology Stack

## 4.1 Core Technologies

| Category | Technology | Version | Purpose |
|----------|------------|---------|---------|
| **Runtime** | Java | 21 (LTS) | Primary programming language |
| **Framework** | Spring Boot | 3.4.1 | Application framework |
| **Build Tool** | Maven | 3.9+ | Dependency management & build |
| **Database** | PostgreSQL | 15+ | Production database |
| **Database** | H2 | 2.2+ | Development/testing database |

## 4.2 Spring Ecosystem

| Module | Artifact | Purpose |
|--------|----------|---------|
| **Web** | spring-boot-starter-web | RESTful API development |
| **Security** | spring-boot-starter-security | Authentication & authorization |
| **Data JPA** | spring-boot-starter-data-jpa | ORM & database access |
| **Validation** | spring-boot-starter-validation | Request validation |
| **Mail** | spring-boot-starter-mail | Email notifications |
| **Actuator** | spring-boot-starter-actuator | Health monitoring |
| **WebFlux** | spring-boot-starter-webflux | Reactive HTTP client |
| **Thymeleaf** | spring-boot-starter-thymeleaf | Email templates |

## 4.3 Third-Party Libraries

| Library | Version | Purpose |
|---------|---------|---------|
| **JJWT** | 0.12.6 | JWT token generation & validation |
| **SpringDoc OpenAPI** | 2.7.0 | API documentation (Swagger) |
| **Lombok** | (managed) | Boilerplate code reduction |
| **Flyway** | (managed) | Database migrations |
| **PostgreSQL Driver** | (managed) | PostgreSQL connectivity |

## 4.4 Development Tools

| Tool | Purpose |
|------|---------|
| **Spring DevTools** | Hot reloading during development |
| **H2 Console** | Database inspection in development |
| **Swagger UI** | Interactive API documentation |
| **Postman** | API testing |

---

# 5. Application Structure

## 5.1 Project Structure

```
craftistan-backend/
├── pom.xml                              # Maven configuration
├── README.md                            # Quick start guide
├── docs/
│   ├── Craftistan_API.postman_collection.json
│   ├── TESTING_GUIDE.md
│   └── TECHNICAL_HANDOVER_DOCUMENT.md   # This document
├── uploads/                             # File upload directory
├── src/
│   ├── main/
│   │   ├── java/com/craftistan/
│   │   │   ├── CraftistanApplication.java
│   │   │   ├── config/
│   │   │   │   ├── DataInitializer.java
│   │   │   │   ├── JpaConfig.java
│   │   │   │   ├── JwtAuthFilter.java
│   │   │   │   ├── JwtUtils.java
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   └── WebConfig.java
│   │   │   ├── auth/
│   │   │   │   ├── controller/AuthController.java
│   │   │   │   ├── dto/
│   │   │   │   │   ├── AuthResponse.java
│   │   │   │   │   ├── LoginRequest.java
│   │   │   │   │   └── RegisterRequest.java
│   │   │   │   └── service/AuthService.java
│   │   │   ├── user/
│   │   │   │   ├── controller/
│   │   │   │   │   ├── ProfileController.java
│   │   │   │   │   └── AddressController.java
│   │   │   │   ├── dto/
│   │   │   │   ├── entity/
│   │   │   │   │   ├── User.java
│   │   │   │   │   ├── Address.java
│   │   │   │   │   └── Role.java
│   │   │   │   ├── repository/
│   │   │   │   └── service/
│   │   │   ├── product/
│   │   │   │   ├── controller/
│   │   │   │   │   ├── ProductController.java
│   │   │   │   │   └── CategoryController.java
│   │   │   │   ├── dto/
│   │   │   │   ├── entity/
│   │   │   │   │   ├── Product.java
│   │   │   │   │   └── Category.java
│   │   │   │   ├── repository/
│   │   │   │   └── service/
│   │   │   ├── order/
│   │   │   │   ├── controller/
│   │   │   │   ├── dto/
│   │   │   │   ├── entity/
│   │   │   │   │   ├── Order.java
│   │   │   │   │   ├── OrderItem.java
│   │   │   │   │   ├── OrderStatus.java
│   │   │   │   │   └── PaymentMethod.java
│   │   │   │   ├── repository/
│   │   │   │   └── service/
│   │   │   ├── review/
│   │   │   ├── wishlist/
│   │   │   ├── artisan/
│   │   │   ├── chat/
│   │   │   ├── translation/
│   │   │   ├── upload/
│   │   │   └── common/
│   │   │       ├── dto/ApiResponse.java
│   │   │       ├── entity/BaseEntity.java
│   │   │       └── exception/
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       └── db/migration/            # Flyway migrations
│   └── test/
│       └── java/com/craftistan/
└── target/                              # Build output
```

## 5.2 Module Descriptions

### 5.2.1 Config Module
| File | Description |
|------|-------------|
| `SecurityConfig.java` | Spring Security configuration, CORS, endpoint protection |
| `JwtAuthFilter.java` | JWT authentication filter |
| `JwtUtils.java` | JWT token generation and validation utilities |
| `WebConfig.java` | Web MVC configuration, resource handlers |
| `JpaConfig.java` | JPA auditing configuration |
| `DataInitializer.java` | Sample data seeding for development |

### 5.2.2 Feature Modules

| Module | Controllers | Key Entities | Description |
|--------|-------------|--------------|-------------|
| **auth** | AuthController | - | User login, registration, JWT generation |
| **user** | ProfileController, AddressController | User, Address | Profile management, address CRUD |
| **product** | ProductController, CategoryController | Product, Category | Product catalog, categories |
| **order** | OrderController | Order, OrderItem | Order creation and management |
| **review** | ReviewController | Review | Product reviews and ratings |
| **wishlist** | WishlistController | WishlistItem | Wishlist management |
| **artisan** | ArtisanController | - | Artisan dashboard and product management |
| **chat** | ChatController | - | AI chatbot integration |
| **translation** | - | - | AI-powered content translation |
| **upload** | UploadController | - | File upload handling |
| **common** | - | BaseEntity | Shared DTOs, exceptions, utilities |

---

# 6. Database Design

## 6.1 Entity Relationship Diagram (ERD)

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                                   DATABASE SCHEMA                                    │
└─────────────────────────────────────────────────────────────────────────────────────┘

                                    ┌────────────────────┐
                                    │       users        │
                                    ├────────────────────┤
                                    │ id (UUID) PK       │
                                    │ name               │
                                    │ email (UNIQUE)     │
                                    │ password (BCrypt)  │
                                    │ phone              │
                                    │ avatar             │
                                    │ role (ENUM)        │
                                    │ enabled            │
                                    │ created_at         │
                                    │ updated_at         │
                                    └────────┬───────────┘
                                             │
              ┌──────────────────────────────┼──────────────────────────────┐
              │                              │                              │
              ▼                              ▼                              ▼
┌─────────────────────┐      ┌─────────────────────┐      ┌─────────────────────┐
│     addresses       │      │    wishlist_items   │      │       orders        │
├─────────────────────┤      ├─────────────────────┤      ├─────────────────────┤
│ id (BIGINT) PK      │      │ id (BIGINT) PK      │      │ id (VARCHAR) PK     │
│ user_id FK          │      │ user_id FK          │      │ user_id FK          │
│ label               │      │ product_id FK       │      │ status (ENUM)       │
│ full_name           │      │ created_at          │      │ subtotal            │
│ phone               │      │ updated_at          │      │ shipping_cost       │
│ address             │      └─────────────────────┘      │ total               │
│ city                │                                   │ payment_method      │
│ postal_code         │                                   │ shipping_full_name  │
│ is_default          │                                   │ shipping_phone      │
│ created_at          │                                   │ shipping_address    │
│ updated_at          │                                   │ shipping_city       │
└─────────────────────┘                                   │ shipping_postal_code│
                                                          │ created_at          │
                                                          │ updated_at          │
                                                          └──────────┬──────────┘
                                                                     │
                                                                     ▼
                                                          ┌─────────────────────┐
                                                          │    order_items      │
                                                          ├─────────────────────┤
                                                          │ id (BIGINT) PK      │
                                                          │ order_id FK         │
                                                          │ product_id          │
                                                          │ product_name        │
                                                          │ product_image       │
                                                          │ price               │
                                                          │ quantity            │
                                                          │ artisan_id          │
                                                          │ created_at          │
                                                          │ updated_at          │
                                                          └─────────────────────┘

┌─────────────────────┐      ┌─────────────────────┐      ┌─────────────────────┐
│     categories      │      │      products       │      │   product_images    │
├─────────────────────┤      ├─────────────────────┤      ├─────────────────────┤
│ id (BIGINT) PK      │      │ id (BIGINT) PK      │      │ product_id FK       │
│ slug (UNIQUE)       │◄─────│ category            │─────►│ image_url           │
│ name                │      │ artisan_id FK       │      └─────────────────────┘
│ icon                │      │ artisan_name        │
│ description         │      │ name                │
│ sort_order          │      │ price               │      ┌─────────────────────┐
│ created_at          │      │ description         │      │       reviews       │
│ updated_at          │      │ style               │      ├─────────────────────┤
└─────────────────────┘      │ stock               │      │ id (BIGINT) PK      │
                             │ rating              │◄─────│ product_id FK       │
                             │ review_count        │      │ user_id FK          │
                             │ is_new              │      │ user_name           │
                             │ is_active           │      │ user_avatar         │
                             │ original_language   │      │ rating (1-5)        │
                             │ translations (JSON) │      │ comment             │
                             │ created_at          │      │ helpful             │
                             │ updated_at          │      │ verified            │
                             └─────────────────────┘      │ created_at          │
                                                          │ updated_at          │
                                                          └─────────────────────┘
```

## 6.2 Table Specifications

### 6.2.1 Users Table

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | VARCHAR(36) | PRIMARY KEY | UUID format |
| name | VARCHAR(255) | NOT NULL | User's display name |
| email | VARCHAR(255) | NOT NULL, UNIQUE | Login email |
| password | VARCHAR(255) | NOT NULL | BCrypt hashed |
| phone | VARCHAR(50) | NULLABLE | Phone number |
| avatar | VARCHAR(500) | NULLABLE | Avatar image URL |
| role | VARCHAR(20) | NOT NULL | BUYER, ARTISAN |
| enabled | BOOLEAN | DEFAULT true | Account status |
| created_at | TIMESTAMP | NOT NULL | JPA auditing |
| updated_at | TIMESTAMP | NULLABLE | JPA auditing |

### 6.2.2 Products Table

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO | Sequential ID |
| name | VARCHAR(255) | NOT NULL | Product name |
| price | DECIMAL(10,2) | NOT NULL | Price in PKR |
| description | VARCHAR(2000) | NULLABLE | Product description |
| category | VARCHAR(100) | NOT NULL | Category slug |
| style | VARCHAR(100) | NULLABLE | Product style |
| stock | INTEGER | DEFAULT 0 | Available quantity |
| artisan_id | VARCHAR(36) | NOT NULL | Creator's user ID |
| artisan_name | VARCHAR(255) | NULLABLE | Cached artisan name |
| rating | DOUBLE | DEFAULT 0.0 | Average rating |
| review_count | INTEGER | DEFAULT 0 | Total reviews |
| is_new | BOOLEAN | DEFAULT true | New product flag |
| is_active | BOOLEAN | DEFAULT true | Active status |
| original_language | VARCHAR(10) | DEFAULT 'en' | Source language |
| translations | TEXT | NULLABLE | JSON translations |
| created_at | TIMESTAMP | NOT NULL | Creation time |
| updated_at | TIMESTAMP | NULLABLE | Last update |

### 6.2.3 Orders Table

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | VARCHAR(20) | PRIMARY KEY | Format: ORD-YYYY-XXXX |
| user_id | VARCHAR(36) | NOT NULL, FK | Buyer's user ID |
| status | VARCHAR(20) | NOT NULL | Order status enum |
| subtotal | DECIMAL(10,2) | NOT NULL | Items total |
| shipping_cost | DECIMAL(10,2) | DEFAULT 0 | Shipping fee |
| total | DECIMAL(10,2) | NOT NULL | Grand total |
| payment_method | VARCHAR(20) | DEFAULT 'COD' | Payment type |
| shipping_full_name | VARCHAR(255) | NULLABLE | Recipient name |
| shipping_phone | VARCHAR(50) | NULLABLE | Delivery phone |
| shipping_address | VARCHAR(500) | NULLABLE | Street address |
| shipping_city | VARCHAR(100) | NULLABLE | City |
| shipping_postal_code | VARCHAR(20) | NULLABLE | Postal code |
| created_at | TIMESTAMP | NOT NULL | Order date |
| updated_at | TIMESTAMP | NULLABLE | Last update |

## 6.3 Enumerations

### Role Enum
```java
public enum Role {
    BUYER,    // Regular customer
    ARTISAN   // Product seller
}
```

### OrderStatus Enum
```java
public enum OrderStatus {
    PENDING,     // Order placed, awaiting processing
    PROCESSING,  // Being prepared
    SHIPPED,     // In transit
    DELIVERED,   // Successfully delivered
    CANCELLED    // Order cancelled
}
```

### PaymentMethod Enum
```java
public enum PaymentMethod {
    COD,        // Cash on Delivery
    JAZZCASH,   // JazzCash mobile wallet
    EASYPAISA,  // Easypaisa mobile wallet
    CARD        // Debit/Credit card
}
```

## 6.4 Database Migrations

Flyway is used for database version control. Migration scripts are located in `src/main/resources/db/migration/`.

| Migration | Description |
|-----------|-------------|
| V1__initial_schema.sql | Initial database schema |
| V2__add_translations.sql | Add translation columns |
| ... | Additional migrations |

---

# 7. API Specification

## 7.1 API Overview

| Property | Value |
|----------|-------|
| **Base URL** | `http://localhost:8080` |
| **API Version** | v1 (implicit) |
| **Protocol** | HTTP/HTTPS |
| **Format** | JSON |
| **Authentication** | Bearer JWT Token |

## 7.2 Standard Response Format

### Success Response
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... }
}
```

### Error Response
```json
{
  "success": false,
  "message": "Error description",
  "errors": ["Field-level errors if applicable"]
}
```

### Paginated Response
```json
{
  "success": true,
  "data": {
    "content": [...],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 20,
      "sort": { ... }
    },
    "totalPages": 5,
    "totalElements": 100,
    "last": false,
    "first": true
  }
}
```

## 7.3 API Endpoints Reference

### 7.3.1 Authentication APIs

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/auth/register` | ❌ | Register new user |
| POST | `/api/auth/login` | ❌ | Login and get JWT |
| GET | `/api/auth/me` | ✅ | Get current user |

#### POST /api/auth/register
**Request Body:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "role": "BUYER"  // or "ARTISAN"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Registration successful",
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "user": {
    "id": "uuid",
    "name": "John Doe",
    "email": "john@example.com",
    "role": "BUYER"
  }
}
```

#### POST /api/auth/login
**Request Body:**
```json
{
  "email": "john@example.com",
  "password": "password123"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Login successful",
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "user": { ... }
}
```

### 7.3.2 Product APIs

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/products` | ❌ | List products (paginated) |
| GET | `/api/products/{id}` | ❌ | Get product details |
| GET | `/api/products/category/{category}` | ❌ | Products by category |
| GET | `/api/categories` | ❌ | List all categories |

#### GET /api/products
**Query Parameters:**
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| lang | string | "en" | Language code for translations |
| category | string | - | Filter by category slug |
| style | string | - | Filter by style |
| minPrice | number | - | Minimum price filter |
| maxPrice | number | - | Maximum price filter |
| search | string | - | Search in name/description |
| page | integer | 0 | Page number |
| size | integer | 20 | Page size |
| sort | string | createdAt,desc | Sort field and direction |

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "name": "Handcrafted Pottery",
        "price": 2500.00,
        "description": "Beautiful handmade pottery...",
        "category": "pottery",
        "style": "Traditional",
        "images": ["url1", "url2"],
        "stock": 10,
        "artisanId": "uuid",
        "artisanName": "Ahmed Crafts",
        "rating": 4.5,
        "reviewCount": 12,
        "isNew": true,
        "isActive": true
      }
    ],
    "totalPages": 5,
    "totalElements": 100,
    "number": 0,
    "size": 20
  }
}
```

### 7.3.3 Order APIs

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/orders` | ✅ | Create new order |
| GET | `/api/orders` | ✅ | Get user's orders |
| GET | `/api/orders/{id}` | ✅ | Get order details |
| PUT | `/api/orders/{id}/cancel` | ✅ | Cancel order |

#### POST /api/orders
**Request Body:**
```json
{
  "items": [
    {
      "productId": 1,
      "quantity": 2
    }
  ],
  "shippingAddressId": "uuid",
  "paymentMethod": "COD"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Order placed successfully",
  "data": {
    "id": "ORD-2026-0001",
    "status": "PENDING",
    "items": [...],
    "subtotal": 5000.00,
    "shippingCost": 200.00,
    "total": 5200.00,
    "paymentMethod": "COD",
    "shippingAddress": { ... },
    "createdAt": "2026-02-01T10:30:00"
  }
}
```

### 7.3.4 User Profile APIs

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/profile` | ✅ | Get profile |
| PUT | `/api/profile` | ✅ | Update profile |
| PUT | `/api/profile/password` | ✅ | Change password |
| POST | `/api/profile/avatar` | ✅ | Upload avatar |

### 7.3.5 Address APIs

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/addresses` | ✅ | List addresses |
| POST | `/api/addresses` | ✅ | Create address |
| PUT | `/api/addresses/{id}` | ✅ | Update address |
| DELETE | `/api/addresses/{id}` | ✅ | Delete address |
| PUT | `/api/addresses/{id}/default` | ✅ | Set as default |

### 7.3.6 Wishlist APIs

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/wishlist` | ✅ | Get wishlist |
| POST | `/api/wishlist/{productId}` | ✅ | Add to wishlist |
| DELETE | `/api/wishlist/{productId}` | ✅ | Remove from wishlist |

### 7.3.7 Review APIs

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/products/{id}/reviews` | ❌ | Get product reviews |
| POST | `/api/products/{id}/reviews` | ✅ | Add review |
| PUT | `/api/reviews/{id}` | ✅ | Update review |
| DELETE | `/api/reviews/{id}` | ✅ | Delete review |

### 7.3.8 Artisan APIs (ROLE_ARTISAN only)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/artisan/dashboard` | ✅ | Dashboard statistics |
| GET | `/api/artisan/products` | ✅ | List artisan's products |
| POST | `/api/artisan/products` | ✅ | Create product |
| PUT | `/api/artisan/products/{id}` | ✅ | Update product |
| DELETE | `/api/artisan/products/{id}` | ✅ | Delete product |
| GET | `/api/artisan/orders` | ✅ | Orders containing artisan's products |

### 7.3.9 Chat APIs

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/chat/message` | ❌ | Send chat message |
| GET | `/api/chat/history` | ❌ | Get chat history |

### 7.3.10 Upload APIs

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/upload/image` | ✅ | Upload single image |
| POST | `/api/upload/images` | ✅ | Upload multiple images (max 5) |

## 7.4 Error Codes

| HTTP Code | Description | Usage |
|-----------|-------------|-------|
| 200 | OK | Successful request |
| 201 | Created | Resource created successfully |
| 400 | Bad Request | Validation errors, invalid input |
| 401 | Unauthorized | Missing or invalid JWT token |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource not found |
| 409 | Conflict | Duplicate resource (email exists) |
| 500 | Internal Server Error | Server-side error |

---

# 8. Authentication & Authorization

## 8.1 JWT Authentication

### Token Structure
```
Header.Payload.Signature

Header: {
  "alg": "HS256",
  "typ": "JWT"
}

Payload: {
  "sub": "user@example.com",
  "iat": 1706789400,
  "exp": 1706875800
}
```

### Token Configuration
| Property | Value |
|----------|-------|
| Algorithm | HMAC-SHA256 |
| Expiration | 24 hours (86400000 ms) |
| Header | Authorization: Bearer {token} |

## 8.2 Security Configuration

```java
// Public endpoints (no authentication required)
/api/auth/**              // Login, register
/api/products/**          // GET only - product browsing
/api/categories/**        // GET only - category listing
/api/artisans/**          // GET only - artisan profiles
/api/chat/**              // Chatbot (public access)

// Role-based endpoints
/api/artisan/**           // ROLE_ARTISAN only

// Authenticated endpoints (any role)
/api/profile/**           // Profile management
/api/addresses/**         // Address management
/api/orders/**            // Order management
/api/wishlist/**          // Wishlist management
/api/upload/**            // File uploads
/api/reviews/** (POST)    // Create reviews
```

## 8.3 Password Security

- **Hashing**: BCrypt with default strength (10 rounds)
- **Storage**: Only hashed passwords stored
- **Validation**: Minimum 6 characters (configurable)

## 8.4 CORS Configuration

```yaml
app:
  cors:
    allowed-origins: http://localhost:5173,http://localhost:3000
    allowed-methods: GET, POST, PUT, DELETE, OPTIONS
    allowed-headers: *
    allow-credentials: true
```

---

# 9. Business Logic

## 9.1 User Registration

1. Validate email uniqueness
2. Hash password with BCrypt
3. Assign default role (BUYER)
4. Generate JWT token
5. Return token and user details

## 9.2 Order Processing

### Order Creation Flow
1. Validate all product IDs exist
2. Check product availability (stock)
3. Generate order ID (format: ORD-YYYY-XXXX)
4. Calculate subtotal, shipping, total
5. Create order and order items
6. Deduct stock from products
7. Return order confirmation

### Order Status Transitions
```
PENDING → PROCESSING → SHIPPED → DELIVERED
    ↓
CANCELLED (only from PENDING)
```

## 9.3 Product Translation

### Translation Workflow
1. Artisan creates/updates product
2. @Async triggers translation service
3. For each supported language (ur, pa, sd, ps, bal):
   - Call Gemini API with translation prompt
   - Parse JSON response
   - Store in translations field
4. Front-end requests product with `?lang=xx`
5. Service returns translated content if available

### Supported Languages
| Code | Language |
|------|----------|
| en | English (default) |
| ur | Urdu |
| pa | Punjabi |
| sd | Sindhi |
| ps | Pashto |
| bal | Balochi |

## 9.4 Review & Rating

1. Verify user hasn't already reviewed product
2. Create review with optional "verified" badge
3. Recalculate product's average rating
4. Update product's review count

---

# 10. Third-Party Integrations

## 10.1 Google Gemini API

### Purpose
AI-powered content translation for product names and descriptions.

### Configuration
```yaml
app:
  gemini:
    api-key: ${GEMINI_API_KEY}
    model: gemini-2.5-flash
    max-tokens: 1024
```

### API Endpoint
```
POST https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent
```

### Request Format
```json
{
  "contents": [{ "parts": [{ "text": "translation prompt" }] }],
  "generationConfig": {
    "maxOutputTokens": 500,
    "temperature": 0.3
  }
}
```

## 10.2 Brevo SMTP (Email)

### Purpose
Email notifications (welcome emails, order confirmations, etc.)

### Configuration
```yaml
spring:
  mail:
    host: smtp-relay.brevo.com
    port: 587
    username: ${BREVO_SMTP_USER}
    password: ${BREVO_SMTP_KEY}
    properties:
      mail.smtp:
        auth: true
        starttls.enable: true
```

---

# 11. Configuration Management

## 11.1 Configuration Files

| File | Purpose |
|------|---------|
| `application.yml` | Common configuration |
| `application-dev.yml` | Development settings (H2, debug) |
| `application-prod.yml` | Production settings (PostgreSQL) |

## 11.2 Environment Variables

### Required for Production
| Variable | Description | Example |
|----------|-------------|---------|
| `DB_HOST` | PostgreSQL host | `localhost` |
| `DB_PORT` | PostgreSQL port | `5432` |
| `DB_NAME` | Database name | `craftistan` |
| `DB_USERNAME` | Database user | `postgres` |
| `DB_PASSWORD` | Database password | `secret` |
| `JWT_SECRET` | JWT signing key | `256-bit-secret` |

### Optional
| Variable | Description | Default |
|----------|-------------|---------|
| `GEMINI_API_KEY` | Gemini API key | - |
| `BREVO_SMTP_USER` | Brevo SMTP user | - |
| `BREVO_SMTP_KEY` | Brevo SMTP key | - |

## 11.3 Profile Activation

```bash
# Development (default)
mvn spring-boot:run

# Production
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# Or via environment variable
export SPRING_PROFILES_ACTIVE=prod
java -jar craftistan-backend.jar
```

---

# 12. Deployment Guide

## 12.1 Prerequisites

- Java 21 (LTS)
- Maven 3.9+
- PostgreSQL 15+ (production)
- 512MB minimum RAM

## 12.2 Build Process

```bash
# Clone repository
git clone <repository-url>
cd craftistan-backend

# Build JAR
mvn clean package -DskipTests

# JAR location
target/craftistan-backend-1.0.0.jar
```

## 12.3 Database Setup

```sql
-- Create database
CREATE DATABASE craftistan;

-- Create user (optional)
CREATE USER craftistan_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE craftistan TO craftistan_user;
```

## 12.4 Running the Application

### Development
```bash
mvn spring-boot:run
```

### Production
```bash
# Set environment variables
export DB_HOST=your-db-host
export DB_PORT=5432
export DB_NAME=craftistan
export DB_USERNAME=your-username
export DB_PASSWORD=your-password
export JWT_SECRET=your-256-bit-secret

# Run with production profile
java -jar -Dspring.profiles.active=prod target/craftistan-backend-1.0.0.jar
```

### Docker (Optional)
```dockerfile
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/craftistan-backend-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## 12.5 Health Check

```bash
curl http://localhost:8080/actuator/health
```

Expected response:
```json
{ "status": "UP" }
```

---

# 13. Testing

## 13.1 Testing Tools

| Tool | Purpose |
|------|---------|
| **JUnit 5** | Unit testing |
| **Spring Test** | Integration testing |
| **Postman** | API testing |
| **H2 Database** | Test database |

## 13.2 Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=AuthServiceTest

# Generate test report
mvn surefire-report:report
```

## 13.3 Postman Collection

A comprehensive Postman collection is provided at:
```
docs/Craftistan_API.postman_collection.json
```

### Importing Collection
1. Open Postman
2. Click Import → Select file
3. Navigate to `docs/Craftistan_API.postman_collection.json`
4. Configure environment variables:
   - `baseUrl`: http://localhost:8080
   - `token`: (auto-populated after login)

## 13.4 Demo Credentials

| Role | Email | Password |
|------|-------|----------|
| Buyer | buyer@example.com | password123 |
| Artisan | artisan@example.com | password123 |

---

# 14. Security Considerations

## 14.1 Implemented Security Measures

| Measure | Implementation |
|---------|----------------|
| **Authentication** | JWT with HMAC-SHA256 |
| **Password Storage** | BCrypt hashing |
| **Authorization** | Role-based access control |
| **Input Validation** | Jakarta Bean Validation |
| **CORS** | Configured allowed origins |
| **XSS Prevention** | JSON-only responses |
| **CSRF** | Disabled (stateless API) |
| **SQL Injection** | JPA parameterized queries |

## 14.2 Security Recommendations

1. **Production JWT Secret**: Use a cryptographically secure 256-bit key
2. **HTTPS**: Deploy behind HTTPS-enabled reverse proxy
3. **Rate Limiting**: Implement rate limiting for login attempts
4. **API Keys**: Rotate Gemini API key periodically
5. **Database**: Use read-only replica for public queries
6. **Logging**: Implement audit logging for sensitive operations
7. **Updates**: Keep dependencies updated for security patches

## 14.3 Sensitive Files

| File/Setting | Protection |
|--------------|------------|
| application*.yml | Store secrets in env vars, not files |
| JWT Secret | Never commit to version control |
| API Keys | Use environment variables |
| Database credentials | Use secret management (e.g., Vault) |

---

# 15. Performance Considerations

## 15.1 Current Optimizations

| Area | Optimization |
|------|--------------|
| **Database** | JPA lazy loading for relationships |
| **Pagination** | Server-side pagination for lists |
| **Async** | @Async for translation operations |
| **Caching** | Translation results stored in DB |
| **Connection Pool** | HikariCP (Spring default) |

## 15.2 Recommended Optimizations

1. **Redis Caching**: Cache frequently accessed products/categories
2. **CDN**: Serve product images via CDN
3. **Database Indexes**: Add indexes on frequently queried columns
4. **Connection Pooling**: Tune HikariCP settings for load
5. **Query Optimization**: Monitor and optimize slow queries

## 15.3 Monitoring

### Actuator Endpoints
- `GET /actuator/health` - Application health
- `GET /actuator/info` - Application info

---

# 16. Maintenance & Support

## 16.1 Log Files

```yaml
logging:
  level:
    com.craftistan: INFO
    org.springframework.security: INFO
```

### Log Locations
- **Console**: Standard output (default)
- **File**: Configure via `logging.file.name` property

## 16.2 Database Maintenance

```sql
-- Analyze tables for query optimization
ANALYZE;

-- Check table sizes
SELECT relname, pg_size_pretty(pg_total_relation_size(relid))
FROM pg_catalog.pg_statio_user_tables
ORDER BY pg_total_relation_size(relid) DESC;
```

## 16.3 Common Issues & Solutions

| Issue | Solution |
|-------|----------|
| JWT token expired | Login again to get new token |
| Database connection failed | Check DB credentials and network |
| Translation not working | Verify Gemini API key is valid |
| File upload fails | Check max file size configuration |
| CORS errors | Verify allowed origins in config |

---

# 17. Known Issues & Limitations

## 17.1 Current Limitations

| Limitation | Impact | Workaround |
|------------|--------|------------|
| Payment integration pending | COD only | Implement payment gateway |
| No admin panel | Manual DB operations | Build admin frontend |
| Single server deployment | Limited scalability | Use container orchestration |
| No real-time notifications | Polling required | Implement WebSockets |

## 17.2 Technical Debt

| Item | Priority | Notes |
|------|----------|-------|
| Additional unit tests | Medium | Increase test coverage |
| API versioning | Low | Consider /v1/api path prefix |
| Error standardization | Medium | Implement RFC 7807 problem details |
| Audit logging | High | Log user actions for compliance |

---

# 18. Appendices

## Appendix A: Glossary

| Term | Definition |
|------|------------|
| **Artisan** | Product creator/seller on the platform |
| **Buyer** | Customer purchasing products |
| **JWT** | JSON Web Token for authentication |
| **DTO** | Data Transfer Object |
| **JPA** | Java Persistence API |
| **CRUD** | Create, Read, Update, Delete operations |

## Appendix B: Contact Information

| Role | Contact |
|------|---------|
| **Developer** | [Your Contact Info] |
| **Technical Support** | [Support Email/Phone] |

## Appendix C: Document History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | February 2026 | Development Team | Initial release |

---

## Appendix D: Quick Reference

### API Base URLs
```
Development: http://localhost:8080
Swagger UI:  http://localhost:8080/swagger-ui.html
H2 Console:  http://localhost:8080/h2-console
Health:      http://localhost:8080/actuator/health
```

### Common Commands
```bash
# Start application
mvn spring-boot:run

# Run tests
mvn test

# Build JAR
mvn clean package

# Check dependencies
mvn dependency:tree
```

---

**End of Technical Handover Document**

*This document is confidential and intended for the recipient client only.*
