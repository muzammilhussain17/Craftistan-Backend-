# Craftistan API Testing Guide

## Prerequisites

- **Java 21+** installed
- **Maven** or **IntelliJ IDEA** (recommended)
- **Postman** for API testing

---

## 1. Starting the Backend

### Option A: Using IntelliJ IDEA (Recommended)
1. Open `d:/backend` in IntelliJ IDEA
2. Wait for Maven dependencies to download
3. Right-click `CraftistanApplication.java` → **Run**
4. Wait for "Started CraftistanApplication" message

### Option B: Using Command Line
```bash
cd d:/backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Verify Backend is Running
- Open: http://localhost:8080/actuator/health
- Expected: `{"status":"UP"}`

---

## 2. Available URLs

| Service | URL |
|---------|-----|
| **API Base** | http://localhost:8080 |
| **Swagger UI** | http://localhost:8080/swagger-ui.html |
| **H2 Console** | http://localhost:8080/h2-console |

### H2 Console Login
- **JDBC URL**: `jdbc:h2:mem:craftistan`
- **Username**: `sa`
- **Password**: *(leave empty)*

---

## 3. Demo Users

The dev profile automatically creates these users:

| Role | Email | Password |
|------|-------|----------|
| Buyer | buyer@example.com | password123 |
| Artisan | artisan@example.com | password123 |

---

## 4. Testing with Postman

### Step 1: Import Collection
1. Open Postman
2. Click **Import**
3. Select `d:/backend/docs/Craftistan_API.postman_collection.json`

### Step 2: Get Access Token
1. Open **Authentication → Login (Buyer)** request
2. Click **Send**
3. Copy the `accessToken` from response
4. The token is auto-saved to collection variables

### Step 3: Test Protected Endpoints
- Token is automatically included via `{{accessToken}}` variable
- All protected endpoints use `Authorization: Bearer {{accessToken}}`

---

## 5. API Endpoint Quick Reference

### Public Endpoints (No Auth Required)
```
POST /api/auth/register     - Register new user
POST /api/auth/login        - Login and get token
GET  /api/products          - List all products
GET  /api/products/{id}     - Get product details
GET  /api/categories        - List categories
GET  /api/products/{id}/reviews - Get reviews
```

### Authenticated Endpoints (Token Required)
```
GET  /api/auth/me           - Current user info
GET  /api/profile           - Get profile
PUT  /api/profile           - Update profile
GET  /api/addresses         - List addresses
POST /api/addresses         - Add address
GET  /api/wishlist          - Get wishlist
POST /api/wishlist/{id}     - Add to wishlist
POST /api/orders            - Create order
GET  /api/orders            - List orders
POST /api/products/{id}/reviews - Add review
```

### Artisan Only (ARTISAN Role Required)
```
GET  /api/artisan/dashboard     - Dashboard stats
GET  /api/artisan/products      - My products
POST /api/artisan/products      - Create product
PUT  /api/artisan/products/{id} - Update product
DELETE /api/artisan/products/{id} - Delete product
GET  /api/artisan/orders        - Orders for my products
```

---

## 6. Testing Workflow

### Test Authentication Flow
```
1. POST /api/auth/register (create new user)
2. POST /api/auth/login (get token)
3. GET /api/auth/me (verify token works)
```

### Test Buyer Flow
```
1. Login as buyer@example.com
2. GET /api/products (browse products)
3. POST /api/wishlist/1 (add to wishlist)
4. POST /api/addresses (add shipping address)
5. POST /api/orders (place order)
6. GET /api/orders (view orders)
```

### Test Artisan Flow
```
1. Login as artisan@example.com
2. GET /api/artisan/dashboard (view stats)
3. POST /api/artisan/products (create product)
4. GET /api/artisan/products (list my products)
5. GET /api/artisan/orders (view incoming orders)
```

---

## 7. Sample API Requests

### Register User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "password": "password123",
    "role": "BUYER"
  }'
```

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "buyer@example.com",
    "password": "password123"
  }'
```

### Get Products (Public)
```bash
curl http://localhost:8080/api/products
```

### Create Order (Authenticated)
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "items": [{"productId": 1, "quantity": 1}],
    "shippingAddress": {
      "fullName": "Test User",
      "phone": "03001234567",
      "address": "123 Main St",
      "city": "Lahore",
      "postalCode": "54000"
    },
    "paymentMethod": "COD"
  }'
```

---

## 8. Response Format

### Success Response
```json
{
  "success": true,
  "data": { ... },
  "message": "Operation successful"
}
```

### Error Response
```json
{
  "success": false,
  "error": "Error message",
  "details": { ... }
}
```

### Paginated Response
```json
{
  "success": true,
  "data": {
    "content": [...],
    "totalElements": 10,
    "totalPages": 1,
    "size": 20,
    "number": 0
  }
}
```

---

## 9. Troubleshooting

### Port 8080 Already in Use
```bash
# Find and kill the process
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### Bean/Compilation Errors
1. Stop the application
2. **Build → Rebuild Project** in IntelliJ
3. Run again

### Token Expired
- JWT tokens expire after 24 hours
- Login again to get a new token

---

## 10. Database Schema

Tables created automatically:
- `users` - User accounts
- `products` - Product listings
- `product_images` - Product images
- `categories` - Product categories
- `orders` - Customer orders
- `order_items` - Items in orders
- `addresses` - User addresses
- `wishlist_items` - Wishlist
- `reviews` - Product reviews
