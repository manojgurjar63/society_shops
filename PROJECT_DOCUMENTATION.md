# Society Shops — Architecture & Documentation

## What is this project?
A platform connecting residents of a residential society with local shopkeepers.
Residents can browse shops, view inventory, and save favorites.
Shopkeepers manage their shop and inventory.
Admins approve or reject shop registrations.

---

## Tech Stack

| Layer          | Technology                          |
|----------------|-------------------------------------|
| Backend        | Java 17 + Spring Boot 3.2.3         |
| Database       | MySQL 8                             |
| Security       | Spring Security + JWT (jjwt 0.11.5) |
| ORM            | Spring Data JPA + Hibernate         |
| Web UI         | Thymeleaf + Thymeleaf Security      |
| Build Tool     | Maven                               |
| Testing        | JUnit 5 + Mockito + Spring Test     |

---

## User Roles

| Role       | What they can do                                      |
|------------|-------------------------------------------------------|
| RESIDENT   | View approved shops, browse inventory, save favorites |
| SHOPKEEPER | Register shop, manage inventory, toggle open/closed   |
| ADMIN      | Approve or reject shop registrations                  |

---

## How a Request Flows

```
HTTP Request
    │
    ▼
JwtFilter  ──── reads "Authorization: Bearer <token>" header
    │            validates token, sets user in Security Context
    ▼
SecurityConfig ── checks if route is permitted or requires a role
    │
    ▼
Controller ── handles HTTP method + path, calls service
    │
    ▼
Service ── business logic (ownership checks, state changes)
    │
    ▼
Repository ── Spring Data JPA queries → MySQL
    │
    ▼
Entity ── Java object mapped to DB table
```

Response travels back up the same chain.

---

## Project Folder Structure

```
society-shops/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/societyshops/
    │   │   ├── SocietyShopsApplication.java     ← Entry point (@SpringBootApplication)
    │   │   │
    │   │   ├── config/
    │   │   │   └── SecurityConfig.java          ← Two filter chains: REST (JWT) + Web (form login)
    │   │   │
    │   │   ├── security/
    │   │   │   ├── JwtUtil.java                 ← Generate / validate / parse JWT tokens
    │   │   │   ├── JwtFilter.java               ← Intercepts every request, validates token
    │   │   │   └── CustomUserDetailsService.java← Loads user from DB by email for Spring Security
    │   │   │
    │   │   ├── enums/
    │   │   │   ├── Role.java                    ← RESIDENT, SHOPKEEPER, ADMIN
    │   │   │   └── ShopStatus.java              ← OPEN, CLOSED
    │   │   │
    │   │   ├── entity/                          ← JPA entities (map to DB tables)
    │   │   │   ├── User.java                    ← users table
    │   │   │   ├── Shop.java                    ← shops table (FK: owner_id → users)
    │   │   │   ├── Inventory.java               ← inventory table (FK: shop_id → shops)
    │   │   │   └── Favorite.java                ← favorites table (FK: user_id + shop_id)
    │   │   │
    │   │   ├── repository/                      ← Spring Data JPA (auto-generates SQL)
    │   │   │   ├── UserRepository.java          ← findByEmail, existsByEmail
    │   │   │   ├── ShopRepository.java          ← findByIsApproved, findByOwnerId
    │   │   │   ├── InventoryRepository.java     ← findByShopId, findByShopIdAndIsAvailable
    │   │   │   └── FavoriteRepository.java      ← findByUserId, existsByUserIdAndShopId
    │   │   │
    │   │   ├── dto/                             ← API input/output shapes (not DB entities)
    │   │   │   ├── ApiResponse.java             ← Standard wrapper: {success, message, data}
    │   │   │   ├── AuthRequest.java             ← Login: email + password
    │   │   │   ├── AuthResponse.java            ← Login response: token + role + name
    │   │   │   ├── RegisterRequest.java         ← Register: name + email + password + role
    │   │   │   ├── ShopRequest.java             ← Create/update shop fields
    │   │   │   └── InventoryRequest.java        ← Add/update inventory item fields
    │   │   │
    │   │   ├── service/                         ← Business logic lives here
    │   │   │   ├── AuthService.java             ← register(), login()
    │   │   │   ├── ShopService.java             ← registerShop(), toggleStatus(), approve/reject
    │   │   │   ├── InventoryService.java        ← addItem(), updateItem(), toggleAvailability()
    │   │   │   └── FavoriteService.java         ← addFavorite(), removeFavorite(), getMyFavorites()
    │   │   │
    │   │   ├── controller/                      ← REST API endpoints (/api/**)
    │   │   │   ├── AuthController.java          ← POST /api/auth/register, /api/auth/login
    │   │   │   ├── ShopController.java          ← CRUD + approve/reject + toggle
    │   │   │   └── InventoryController.java     ← CRUD + toggle availability
    │   │   │
    │   │   ├── web/                             ← Thymeleaf web UI controllers (/web/**)
    │   │   │   ├── WebAuthController.java       ← GET /web/login, /web/dashboard (role redirect)
    │   │   │   ├── AdminWebController.java      ← GET /web/admin/dashboard
    │   │   │   └── ShopkeeperWebController.java ← GET /web/shopkeeper/dashboard + inventory
    │   │   │
    │   │   └── exception/
    │   │       └── GlobalExceptionHandler.java  ← Catches all errors, returns clean JSON
    │   │
    │   └── resources/
    │       ├── application.properties           ← DB config, JWT secret, server port
    │       ├── static/css/style.css             ← Shared CSS
    │       └── templates/
    │           ├── auth/login.html              ← Web login form
    │           ├── admin/dashboard.html         ← Admin: pending shops list
    │           └── shopkeeper/
    │               ├── dashboard.html           ← Shopkeeper: my shops
    │               └── inventory.html           ← Shopkeeper: manage inventory
    │
    └── test/
        └── java/com/societyshops/
            ├── service/
            │   ├── AuthServiceTest.java         ← 5 tests: register, login, error cases
            │   ├── ShopServiceTest.java         ← 5 tests: register, toggle, approve, reject
            │   └── InventoryServiceTest.java    ← 6 tests: add, update, toggle, delete
            └── controller/
                ├── AuthControllerTest.java      ← 3 tests: register, login, validation
                └── ShopControllerTest.java      ← 5 tests: role access, CRUD endpoints
```

---

## Security — Two Separate Chains

### Chain 1: REST API (`/api/**`) — JWT stateless
```
POST /api/auth/register  ← public
POST /api/auth/login     ← public
everything else          ← requires valid JWT in Authorization header
```
How to use:
```
Authorization: Bearer <token>
```

### Chain 2: Web UI (`/web/**`) — Session-based form login
```
GET /web/login           ← public (login form)
POST /web/login          ← form submit (Spring Security handles it)
/web/admin/**            ← requires ADMIN role
/web/shopkeeper/**       ← requires SHOPKEEPER role
```
After login → redirects to `/web/dashboard` → then to role-specific dashboard.

---

## JWT Token

- Contains: `email` + `role`
- Signed with: HMAC SHA-256
- Expiry: 24 hours
- Secret: configured in `application.properties` (`app.jwt.secret`)

Flow:
```
Login → AuthService generates token → client stores token
Next request → JwtFilter reads token → validates → sets SecurityContext
Controller → @PreAuthorize checks role from SecurityContext
```

---

## Database Tables

```
users
  id, name, email, password, phone, role, fcm_token, created_at

shops
  id, owner_id (FK→users), name, description, category,
  phone, address, open_time, close_time, status, is_approved, created_at

inventory
  id, shop_id (FK→shops), name, description, price, unit, is_available, created_at

favorites
  id, user_id (FK→users), shop_id (FK→shops), created_at
  UNIQUE(user_id, shop_id)
```

---

## REST API Endpoints

### Auth (Public)
| Method | URL                  | Description       |
|--------|----------------------|-------------------|
| POST   | /api/auth/register   | Register new user |
| POST   | /api/auth/login      | Login, get JWT    |

### Shops
| Method | URL                       | Role       | Description         |
|--------|---------------------------|------------|---------------------|
| GET    | /api/shops                | RESIDENT   | All approved shops  |
| GET    | /api/shops/filter?status= | RESIDENT   | Filter by status    |
| POST   | /api/shops                | SHOPKEEPER | Register shop       |
| PUT    | /api/shops/{id}           | SHOPKEEPER | Update shop         |
| PUT    | /api/shops/{id}/toggle    | SHOPKEEPER | Toggle OPEN/CLOSED  |
| GET    | /api/shops/my             | SHOPKEEPER | My shops            |
| GET    | /api/shops/pending        | ADMIN      | Pending approvals   |
| PUT    | /api/shops/{id}/approve   | ADMIN      | Approve shop        |
| DELETE | /api/shops/{id}/reject    | ADMIN      | Reject/delete shop  |

### Inventory
| Method | URL                              | Role       | Description          |
|--------|----------------------------------|------------|----------------------|
| GET    | /api/inventory/shop/{id}         | RESIDENT   | Available items      |
| GET    | /api/inventory/shop/{id}/all     | SHOPKEEPER | All items            |
| POST   | /api/inventory/shop/{id}         | SHOPKEEPER | Add item             |
| PUT    | /api/inventory/{itemId}          | SHOPKEEPER | Update item          |
| PUT    | /api/inventory/{itemId}/toggle   | SHOPKEEPER | Toggle availability  |
| DELETE | /api/inventory/{itemId}          | SHOPKEEPER | Delete item          |

---

## Web UI Pages

| URL                         | Role       | Page                        |
|-----------------------------|------------|-----------------------------|
| /web/login                  | Public     | Login form                  |
| /web/admin/dashboard        | ADMIN      | Pending shops, approve/reject|
| /web/shopkeeper/dashboard   | SHOPKEEPER | My shops list               |
| /web/shopkeeper/inventory   | SHOPKEEPER | Manage inventory items      |

---

## Tests (24 total — all use Mockito, no DB needed)

| File                    | Tests | What's covered                              |
|-------------------------|-------|---------------------------------------------|
| AuthServiceTest         | 5     | register, duplicate email, login, bad password, user not found |
| ShopServiceTest         | 5     | register shop, toggle status, wrong owner, approve, reject |
| InventoryServiceTest    | 6     | add item, wrong owner, toggle, delete, wrong owner, update |
| AuthControllerTest      | 3     | register 200, login 200, invalid email 400  |
| ShopControllerTest      | 5     | role access (allowed/forbidden), CRUD, approve |

Run all tests:
```bash
./mvnw test
```

Run a specific test:
```bash
./mvnw test -Dtest=AuthServiceTest
```

---

## How to Run the App

```bash
# 1. Start MySQL and create DB
mysql -u manoj -p
CREATE DATABASE society_shops;

# 2. Run the app
cd /home/manoj/my-project/society-shops
./mvnw spring-boot:run

# 3. App is live at
http://localhost:8080
```

First time setup — create an admin user:
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Admin","email":"admin@example.com","password":"admin123","role":"ADMIN"}'
```

Then login at: `http://localhost:8080/web/login`

---

## What's Pending

| Feature                        | Status      |
|--------------------------------|-------------|
| FavoriteController (REST)      | Pending     |
| FavoriteServiceTest            | Pending     |
| Repository tests (@DataJpaTest)| Pending     |
| JwtUtilTest                    | Pending     |
| Integration tests              | Pending     |
| Android app (Resident)         | Not started |
| FCM Push Notifications         | Not started |
| Docker setup                   | Not started |
