# 🏺 Craftistan - E-Commerce Platform for Pakistani Artisan Crafts

## 📋 Project Overview

**Craftistan** is a full-featured, production-ready e-commerce backend platform designed specifically for Pakistani artisan crafts marketplace. This robust backend system enables local artisans to showcase and sell their handcrafted products to a global audience, featuring a multi-role user system, AI-powered content translation, and comprehensive order management.

---

## 🎯 Business Problem Solved

Pakistani artisans produce world-class handicrafts but often lack the technical infrastructure to reach global markets. Craftistan bridges this gap by providing:

- **Artisan Empowerment**: Dedicated dashboard for artisans to manage their products and orders
- **Multi-Language Support**: AI-powered translation to 6 Pakistani languages (English, Urdu, Punjabi, Sindhi, Pashto, Balochi)
- **Seamless Commerce**: Complete order lifecycle management from cart to delivery
- **Trust Building**: Review and rating system for product credibility

---

## 🛠️ Technical Stack

| Technology | Purpose |
|------------|---------|
| **Java 21** | Latest LTS with modern language features |
| **Spring Boot 3.4.1** | Enterprise-grade microservices framework |
| **Spring Security + JWT** | Stateless authentication & authorization |
| **Spring Data JPA** | Database abstraction with Hibernate ORM |
| **PostgreSQL** | Production database (ACID compliant) |
| **H2 Database** | Development/testing in-memory database |
| **Flyway** | Database migration & version control |
| **Spring WebFlux** | Reactive HTTP client for external APIs |
| **Google Gemini API** | AI-powered content translation |
| **Lombok** | Boilerplate code reduction |
| **SpringDoc OpenAPI** | Auto-generated API documentation |
| **BCrypt** | Industry-standard password hashing |
| **Thymeleaf** | Email template engine |

---

## 🏗️ Architecture & Design Patterns

### Clean Architecture Implementation
```
src/main/java/com/craftistan/
├── CraftistanApplication.java    # Application entry point
├── config/                        # Security, JWT, CORS configurations
├── auth/                          # Authentication module
│   ├── controller/
│   ├── dto/
│   └── service/
├── user/                          # User management module
│   ├── controller/
│   ├── dto/
│   ├── entity/
│   ├── repository/
│   └── service/
├── product/                       # Product catalog module
├── order/                         # Order management module
├── wishlist/                      # Wishlist functionality
├── review/                        # Product reviews & ratings
├── artisan/                       # Artisan dashboard module
├── chat/                          # AI Chatbot integration
├── translation/                   # Multi-language translation
├── upload/                        # File upload management
└── common/                        # Shared utilities & exceptions
```

### Design Patterns Used
- **Repository Pattern**: Data access abstraction via Spring Data JPA
- **DTO Pattern**: Secure data transfer between layers
- **Service Layer Pattern**: Business logic encapsulation
- **Factory Pattern**: Object creation standardization
- **Builder Pattern**: Complex object construction (Lombok @Builder)
- **Strategy Pattern**: Configurable authentication strategies

---

## 🔐 Security Implementation

### Authentication & Authorization
- **JWT Token Authentication**: Stateless, scalable authentication mechanism
- **Role-Based Access Control (RBAC)**: BUYER, ARTISAN, ADMIN roles
- **Method-Level Security**: `@PreAuthorize` annotations for fine-grained control
- **BCrypt Password Hashing**: Industry-standard password security
- **CORS Configuration**: Configurable cross-origin policies

### Security Features
```java
// Sample Security Configuration Highlights
- CSRF protection (disabled for REST API)
- Stateless session management
- Custom JWT authentication filter
- Public vs Protected endpoint segregation
- H2 console protection in development
```

---

## 📡 API Endpoints (30+ REST Endpoints)

### 🔓 Authentication APIs
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | User registration with email verification |
| POST | `/api/auth/login` | JWT token generation |
| GET | `/api/auth/me` | Current user details (authenticated) |

### 📦 Product Management
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/products` | List products with filters, pagination, sorting |
| GET | `/api/products/{id}` | Product details with translations |
| GET | `/api/categories` | Product categories listing |

### 🛒 Order Management
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/orders` | Create new order |
| GET | `/api/orders` | User's order history |
| GET | `/api/orders/{id}` | Order details with items |
| PUT | `/api/orders/{id}/cancel` | Cancel pending order |

### 👤 User Profile & Addresses
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/profile` | Get user profile |
| PUT | `/api/profile` | Update profile information |
| PUT | `/api/profile/password` | Change password |
| POST | `/api/profile/avatar` | Upload profile picture |
| CRUD | `/api/addresses/*` | Full address management |

### ⭐ Reviews & Wishlist
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET/POST | `/api/products/{id}/reviews` | Review management |
| CRUD | `/api/wishlist/*` | Wishlist operations |

### 🎨 Artisan Dashboard (ROLE_ARTISAN only)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/artisan/dashboard` | Sales statistics & analytics |
| CRUD | `/api/artisan/products/*` | Product CRUD operations |
| GET | `/api/artisan/orders` | Orders for artisan's products |

### 🤖 AI Chatbot
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/chat/message` | Send message to AI assistant |
| GET | `/api/chat/history` | Retrieve chat history |

### 📁 File Upload
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/upload/image` | Single image upload |
| POST | `/api/upload/images` | Batch upload (max 5 images) |

---

## 🌐 AI-Powered Translation Feature

### Multi-Language Support
A unique feature that leverages **Google Gemini API** for real-time content translation:

```
Supported Languages:
• English (en)     • Urdu (ur)      • Punjabi (pa)
• Sindhi (sd)      • Pashto (ps)    • Balochi (bal)
```

### Implementation Highlights
- **Asynchronous Translation**: Non-blocking translation using `@Async`
- **Language Detection**: Auto-detect source language
- **Cached Translations**: Stored as JSON for fast retrieval
- **Graceful Degradation**: Falls back to original content if translation fails

---

## 📊 Database Schema

### Core Entities
```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│    User      │────→│   Address    │     │   Product    │
│  - id        │     │  - id        │←────│  - id        │
│  - email     │     │  - userId    │     │  - name      │
│  - password  │     │  - street    │     │  - price     │
│  - role      │     │  - city      │     │  - artisanId │
│  - firstName │     │  - isDefault │     │  - categoryId│
└──────────────┘     └──────────────┘     └──────────────┘
                            │                    │
                            │                    │
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│    Order     │────→│  OrderItem   │←────│   Review     │
│  - id        │     │  - quantity  │     │  - rating    │
│  - userId    │     │  - price     │     │  - comment   │
│  - status    │     │  - productId │     │  - userId    │
│  - total     │     │  - orderId   │     │  - productId │
└──────────────┘     └──────────────┘     └──────────────┘

┌──────────────┐     ┌──────────────┐
│   Category   │     │   Wishlist   │
│  - id        │     │  - userId    │
│  - name      │     │  - productId │
│  - slug      │     │  - addedAt   │
└──────────────┘     └──────────────┘
```

---

## ⚙️ Configuration & DevOps

### Environment Profiles
- **Development**: H2 in-memory database, debug logging
- **Production**: PostgreSQL, optimized settings

### Deployment Ready
```yaml
# Production configuration example
DB_HOST: ${DATABASE_HOST}
DB_PORT: 5432
DB_NAME: craftistan
JWT_SECRET: ${JWT_SECRET_KEY}
GEMINI_API_KEY: ${GEMINI_API_KEY}

# Flyway migrations for zero-downtime deployments
# Spring Actuator for health monitoring
# OpenAPI/Swagger for API documentation
```

---

## 📚 Documentation & Testing

### Available Resources
- **Swagger UI**: Auto-generated interactive API docs at `/swagger-ui.html`
- **Postman Collection**: Complete API testing suite (47KB+)
- **Testing Guide**: Step-by-step testing documentation
- **H2 Console**: Database inspection at `/h2-console`

### Demo Credentials
```
Buyer Account:   buyer@example.com / password123
Artisan Account: artisan@example.com / password123
```

---

## 💡 Key Technical Achievements

### 1. **Scalable Architecture**
   - Stateless JWT authentication for horizontal scaling
   - Layered architecture for maintainability
   - Database migrations with Flyway

### 2. **Security Best Practices**
   - BCrypt password hashing
   - Role-based access control
   - Method-level security annotations
   - Configured CORS policies

### 3. **AI Integration**
   - Google Gemini API for intelligent translations
   - Async processing for non-blocking operations
   - Graceful error handling

### 4. **Developer Experience**
   - SpringDoc OpenAPI for API documentation
   - Lombok for cleaner code
   - Postman collection for testing
   - Development/Production profiles

### 5. **Production Ready**
   - Environment-based configuration
   - Health monitoring via Actuator
   - Comprehensive error handling
   - Logging with SLF4J

---

## 📈 Project Metrics

| Metric | Value |
|--------|-------|
| **API Endpoints** | 30+ |
| **Entity Models** | 10+ |
| **Code Modules** | 12 |
| **Supported Languages** | 6 |
| **Test Coverage** | Comprehensive |

---

## 🔗 Technical Skills Demonstrated

- **Backend Development**: Spring Boot, RESTful API Design
- **Database Design**: PostgreSQL, JPA/Hibernate, Database Migrations
- **Security**: JWT, Spring Security, BCrypt, RBAC
- **API Documentation**: OpenAPI/Swagger
- **AI/ML Integration**: Google Gemini API
- **DevOps**: Environment Profiles, Health Monitoring
- **Clean Code**: Design Patterns, SOLID Principles

---

## 🚀 Live Demo & Documentation

- **API Documentation**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health
- **Postman Collection**: Available in `/docs/` directory

---

## 📞 Contact & Availability

**Ready for similar projects including:**
- E-commerce Backend Development
- Spring Boot API Development
- JWT Authentication Systems
- AI/ML API Integration
- Database Design & Migration
- Microservices Architecture

---

*This project demonstrates expertise in building enterprise-grade backend systems with modern Java technologies, security best practices, and AI integration capabilities.*
