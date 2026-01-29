# Craftistan Backend

E-commerce backend for Pakistani artisan crafts marketplace.

## Tech Stack

- **Java 21** - Latest LTS
- **Spring Boot 3.4.1** - Framework
- **Spring Security + JWT** - Authentication
- **Spring Data JPA** - Database ORM
- **H2 / PostgreSQL** - Database
- **Lombok** - Boilerplate reduction
- **SpringDoc OpenAPI** - API Documentation

## Quick Start

### Development (H2 Database)

```bash
mvn spring-boot:run
```

### Production (PostgreSQL)

```bash
# Set environment variables
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=craftistan
export DB_USERNAME=postgres
export DB_PASSWORD=yourpassword
export JWT_SECRET=your-256-bit-secret-key

# Run with prod profile
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## API Endpoints

### Base URL: `http://localhost:8080`

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login (returns JWT) |
| GET | `/api/auth/me` | Get current user |

### Products (Public)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/products` | List products (with filters) |
| GET | `/api/products/{id}` | Get product details |
| GET | `/api/categories` | List categories |

### User Profile (Authenticated)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/profile` | Get profile |
| PUT | `/api/profile` | Update profile |
| PUT | `/api/profile/password` | Change password |
| POST | `/api/profile/avatar` | Upload avatar |

### Addresses (Authenticated)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/addresses` | List addresses |
| POST | `/api/addresses` | Add address |
| PUT | `/api/addresses/{id}` | Update address |
| DELETE | `/api/addresses/{id}` | Delete address |
| PUT | `/api/addresses/{id}/default` | Set default |

### Orders (Authenticated)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/orders` | Create order |
| GET | `/api/orders` | List orders |
| GET | `/api/orders/{id}` | Get order details |
| PUT | `/api/orders/{id}/cancel` | Cancel order |

### Wishlist (Authenticated)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/wishlist` | Get wishlist |
| POST | `/api/wishlist/{productId}` | Add to wishlist |
| DELETE | `/api/wishlist/{productId}` | Remove from wishlist |

### Reviews
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/products/{id}/reviews` | Get reviews (public) |
| POST | `/api/products/{id}/reviews` | Add review |
| PUT | `/api/reviews/{id}` | Update review |
| DELETE | `/api/reviews/{id}` | Delete review |

### Artisan Only (ROLE_ARTISAN)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/artisan/dashboard` | Dashboard stats |
| GET | `/api/artisan/products` | My products |
| POST | `/api/artisan/products` | Create product |
| PUT | `/api/artisan/products/{id}` | Update product |
| DELETE | `/api/artisan/products/{id}` | Delete product |
| GET | `/api/artisan/orders` | Orders for my products |

### File Upload (Authenticated)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/upload/image` | Upload single image |
| POST | `/api/upload/images` | Upload multiple (max 5) |

## Demo Credentials

Run in dev mode to get sample data:

- **Buyer**: `buyer@example.com` / `password123`
- **Artisan**: `artisan@example.com` / `password123`

## Swagger UI

Access API documentation at: `http://localhost:8080/swagger-ui.html`

## H2 Console (Dev only)

Access at: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:craftistan`
- Username: `sa`
- Password: (empty)

## Project Structure

```
src/main/java/com/craftistan/
├── CraftistanApplication.java
├── config/          # Security, JWT, Web configs
├── auth/            # Authentication (login, register)
├── user/            # User, Address, Profile
├── product/         # Product, Category
├── order/           # Order, OrderItem
├── wishlist/        # Wishlist
├── review/          # Reviews
├── artisan/         # Artisan dashboard
├── upload/          # File upload
└── common/          # ApiResponse, Exceptions
```
