# Society Shops - Project Documentation

## What is this project?
A platform that connects residents of a residential society (~5000 users)
with local shopkeepers inside the society.

---

## Tech Stack

| Layer        | Technology                        |
|--------------|-----------------------------------|
| Backend      | Java 17 + Spring Boot 3.2.3       |
| Database     | MySQL 8                           |
| Security     | Spring Security + JWT (jjwt 0.11.5)|
| ORM          | Spring Data JPA + Hibernate       |
| Build Tool   | Maven                             |
| Mobile (TBD) | Android (Java)                    |
| Notifications (TBD) | Firebase Cloud Messaging (FCM) |

---

## User Roles

| Role        | Access                                      |
|-------------|---------------------------------------------|
| RESIDENT    | View shops, browse inventory, save favorites |
| SHOPKEEPER  | Register shop, manage inventory, toggle status |
| ADMIN       | Approve/reject shop registrations           |

---

## What I Built (Step by Step)

### Step 1 — Project Setup
- Created Spring Boot project using Spring Initializr (start.spring.io)
- Added dependencies: Spring Web, Spring Data JPA, Spring Security,
  MySQL Driver, Lombok, Validation
- Configured pom.xml with JWT (jjwt) dependency

### Step 2 — Database Setup
- Created MySQL database: `society_shops`
- Configured `application.properties` with DB URL, username, password
- Used `spring.jpa.hibernate.ddl-auto=update` so Hibernate auto-creates tables

### Step 3 — Enums
- `Role.java` → RESIDENT, SHOPKEEPER, ADMIN
- `ShopStatus.java` → OPEN, CLOSED
- Why: Enums prevent invalid values in DB and code

### Step 4 — JPA Entities (Database Tables)
- `User.java` → maps to `users` table
- `Shop.java` → maps to `shops` table (FK: owner_id → users)
- `Inventory.java` → maps to `inventory` table (FK: shop_id → shops)
- `Favorite.java` → maps to `favorites` table (FK: user_id + shop_id)
- Used @Entity, @ManyToOne, @OneToMany, @JoinColumn annotations
- Used Lombok (@Getter, @Setter, @Builder) to reduce boilerplate
- Used @JsonIgnore to prevent circular references in API responses

### Step 5 — Repository Layer
- `UserRepository` → findByEmail, existsByEmail, findByRole
- `ShopRepository` → findByIsApproved, findByOwnerId, findByIsApprovedFalse
- `InventoryRepository` → findByShopId, findByShopIdAndIsAvailable
- `FavoriteRepository` → findByUserId, existsByUserIdAndShopId
- Why: Spring Data JPA generates SQL automatically from method names

### Step 6 — DTOs (Data Transfer Objects)
- `AuthRequest.java` → login request (email + password)
- `AuthResponse.java` → login response (token + role + name)
- `RegisterRequest.java` → registration request
- `ShopRequest.java` → create/update shop
- `InventoryRequest.java` → add/update inventory item
- `ApiResponse.java` → standard wrapper for all API responses
- Why: DTOs separate API contract from DB structure

### Step 7 — JWT Security
- `JwtUtil.java` → generates and validates JWT tokens
  - Token contains: email + role
  - Expiry: 24 hours
  - Algorithm: HMAC SHA-256
- `JwtFilter.java` → intercepts every HTTP request
  - Reads "Authorization: Bearer <token>" header
  - Validates token and sets user identity in Spring Security context
- `CustomUserDetailsService.java` → loads user from DB by email
  - Required by Spring Security for authentication

### Step 8 — Spring Security Config
- `SecurityConfig.java`
  - Disabled CSRF (not needed for REST APIs)
  - Stateless sessions (JWT handles state, not server)
  - Public routes: /api/auth/** (login, register)
  - All other routes: require valid JWT token
  - Used @EnableMethodSecurity for @PreAuthorize on controllers
  - BCryptPasswordEncoder for password hashing

### Step 9 — Service Layer (Business Logic)
- `AuthService.java` → register + login logic
- `ShopService.java` → register shop, toggle status, approve/reject, CRUD
- `InventoryService.java` → add/update/delete items, toggle availability
- `FavoriteService.java` → add/remove/list favorites
- Why: Controllers never touch DB directly, all logic lives in services

### Step 10 — REST Controllers (API Endpoints)
- `AuthController.java` → POST /api/auth/register, POST /api/auth/login
- `ShopController.java` → CRUD + approve/reject + toggle
- `InventoryController.java` → CRUD + toggle availability
- Used @PreAuthorize("hasRole('ADMIN')") for role-based access

### Step 11 — Global Exception Handler
- `GlobalExceptionHandler.java` using @RestControllerAdvice
- Catches: RuntimeException, validation errors, AccessDeniedException
- Returns clean JSON error responses instead of stack traces

### Step 12 — GitHub
- Initialized git repository
- Pushed code to: https://github.com/manojgurjar63/society_shops
- Branch: manoj

---

## API Endpoints Summary

### Auth (Public)
| Method | URL                    | Description        |
|--------|------------------------|--------------------|
| POST   | /api/auth/register     | Register user      |
| POST   | /api/auth/login        | Login + get token  |

### Shops
| Method | URL                        | Role        | Description           |
|--------|----------------------------|-------------|-----------------------|
| GET    | /api/shops                 | RESIDENT    | All approved shops    |
| GET    | /api/shops/filter?status=  | RESIDENT    | Filter by status      |
| POST   | /api/shops                 | SHOPKEEPER  | Register shop         |
| PUT    | /api/shops/{id}            | SHOPKEEPER  | Update shop           |
| PUT    | /api/shops/{id}/toggle     | SHOPKEEPER  | Toggle OPEN/CLOSED    |
| GET    | /api/shops/my              | SHOPKEEPER  | My shops              |
| GET    | /api/shops/pending         | ADMIN       | Pending approvals     |
| PUT    | /api/shops/{id}/approve    | ADMIN       | Approve shop          |
| DELETE | /api/shops/{id}/reject     | ADMIN       | Reject shop           |

### Inventory
| Method | URL                            | Role       | Description            |
|--------|--------------------------------|------------|------------------------|
| GET    | /api/inventory/shop/{id}       | RESIDENT   | Available items        |
| GET    | /api/inventory/shop/{id}/all   | SHOPKEEPER | All items              |
| POST   | /api/inventory/shop/{id}       | SHOPKEEPER | Add item               |
| PUT    | /api/inventory/{itemId}        | SHOPKEEPER | Update item            |
| PUT    | /api/inventory/{itemId}/toggle | SHOPKEEPER | Toggle availability    |
| DELETE | /api/inventory/{itemId}        | SHOPKEEPER | Delete item            |

---

## Project Folder Structure

```
society-shops/
├── pom.xml
└── src/main/java/com/societyshops/
    ├── SocietyShopsApplication.java   ← Entry point
    ├── config/
    │   └── SecurityConfig.java        ← Spring Security + JWT config
    ├── controller/
    │   ├── AuthController.java
    │   ├── ShopController.java
    │   └── InventoryController.java
    ├── dto/
    │   ├── ApiResponse.java
    │   ├── AuthRequest.java
    │   ├── AuthResponse.java
    │   ├── RegisterRequest.java
    │   ├── ShopRequest.java
    │   └── InventoryRequest.java
    ├── entity/
    │   ├── User.java
    │   ├── Shop.java
    │   ├── Inventory.java
    │   └── Favorite.java
    ├── enums/
    │   ├── Role.java
    │   └── ShopStatus.java
    ├── exception/
    │   └── GlobalExceptionHandler.java
    ├── repository/
    │   ├── UserRepository.java
    │   ├── ShopRepository.java
    │   ├── InventoryRepository.java
    │   └── FavoriteRepository.java
    ├── security/
    │   ├── JwtUtil.java
    │   ├── JwtFilter.java
    │   └── CustomUserDetailsService.java
    └── service/
        ├── AuthService.java
        ├── ShopService.java
        ├── InventoryService.java
        └── FavoriteService.java
```

---

## What's Pending

| Task                        | Status      |
|-----------------------------|-------------|
| FavoriteController          | Pending     |
| Run & test APIs in Postman  | In Progress |
| Dockerize the application   | Pending     |
| Android app (Resident)      | Not Started |
| Admin/Shopkeeper Web Dashboard | Not Started |
| FCM Push Notifications      | Not Started |

---

## How to Run

```bash
# 1. Make sure MySQL is running and database exists
mysql -u manoj -p
CREATE DATABASE society_shops;

# 2. Run the Spring Boot app
cd /home/manoj/my-project/society-shops
mvn spring-boot:run

# 3. App runs at
http://localhost:8080
```

---

## Architecture Pattern Used

```
Request → Controller → Service → Repository → Database
Response ← Controller ← Service ← Repository ← Database
```

- Controller: handles HTTP, calls service
- Service: business logic, calls repository
- Repository: DB queries via Spring Data JPA
- Entity: maps Java class to DB table
- DTO: shapes data for API input/output
- Security: JWT filter validates token on every request
