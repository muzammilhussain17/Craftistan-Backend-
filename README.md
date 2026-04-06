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

## Docker Deployment

### Prerequisites

- [Docker](https://docs.docker.com/get-docker/) & [Docker Compose](https://docs.docker.com/compose/install/) installed

### 1. Configure Environment Variables

```bash
# Copy the example env file
cp .env.example .env

# Edit .env and fill in your values
```

Required variables in `.env`:

| Variable | Description | Example |
|----------|-------------|---------|
| `DB_NAME` | PostgreSQL database name | `craftistan` |
| `DB_USERNAME` | PostgreSQL username | `postgres` |
| `DB_PASSWORD` | PostgreSQL password | `your_secure_password` |
| `JWT_SECRET` | JWT signing key (min 256-bit) | `your-256-bit-secret` |
| `GEMINI_API_KEY` | Google Gemini API key | `AIza...` |
| `BREVO_SMTP_USER` | Brevo SMTP username | `your_smtp_user` |
| `BREVO_SMTP_KEY` | Brevo SMTP key | `your_smtp_key` |
| `CORS_ALLOWED_ORIGINS` | Frontend URL(s), comma-separated | `https://your-frontend.com` |
| `APP_PORT` | Host port to expose (default `8080`) | `8080` |

### 2. Build & Run

```bash
# Build and start all services (PostgreSQL + App)
docker compose up --build -d

# View logs
docker compose logs -f app

# Stop all services
docker compose down

# Stop and remove volumes (⚠️ deletes database & uploads)
docker compose down -v
```

### 3. Verify

```bash
# Health check
curl http://localhost:8080/actuator/health

# Swagger UI
# Open http://localhost:8080/swagger-ui.html
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
