# 🚗 Vehicle Backend

A **Spring Boot** application developed as part of the Mühlbauer technical assignment.  
The project implements **Create, Read, Update, and Delete (CRUD)** functionality for managing vehicle records, following a clean, modular architecture and good development practices.

---

## 🎯 Purpose

The application enables basic management of vehicle data — creating, listing, updating, and deleting vehicles with attributes such as model, first registration year, cubic capacity, fuel type, and mileage.

This backend corresponds to the server-side requirements defined in the task specification document.

---

## 🧩 Tech Stack

- **Java 21**
- **Spring Boot 3.2**
  - Spring Web  
  - Spring Data JPA  
  - Hibernate Validator
- **H2 In-Memory Database**
- **Lombok**
- **JUnit 5 / MockMvc**
- **Swagger (OpenAPI 3.0)**
- **SLF4J Logging**

---

## ⚙️ Architecture Overview

```
com.muehlbauer.vehicle_backend
├── controller      → REST endpoints (CRUD operations)
├── service         → Business logic
├── domain          → JPA entities
├── dto             → Request/response models
├── repository      → Data access layer
├── exception       → Global exception handling
├── config          → CORS, Swagger, and Security configs
├── filters         → HTTP request logging
└── enums           → Enumeration types (Fuel)
```

The application follows a layered design ensuring separation of concerns and testability.

---

## 📋 Core Features

### Vehicle Management API
| Method | Endpoint | Description |
|--------|-----------|-------------|
| **GET** | `/api/vehicles` | Retrieve all vehicles |
| **GET** | `/api/vehicles/paged` | Retrieve paginated vehicles (supports `page`, `size`, `sort` query parameters) |
| **GET** | `/api/vehicles/{id}` | Retrieve a vehicle by ID |
| **POST** | `/api/vehicles` | Create a new vehicle |
| **POST** | `/api/vehicles/seed` | Generate and save 10 random vehicles with valid data |
| **PUT** | `/api/vehicles/{id}` | Update a vehicle by ID |
| **DELETE** | `/api/vehicles/{id}` | Delete a vehicle by ID |

### Data Validation
Implemented using **Jakarta Bean Validation**:
- `@NotBlank`, `@Size`, `@Pattern` for strings  
- `@NotNull`, `@Min`, `@Max`, `@Positive` for numbers  
- Enum validation for `Fuel` (`diesel`, `petrol`, `hybrid`)

#### Validation overview
| Field | Type | Constraints |
|------|------|-------------|
| model | String | Not blank, max length 40 |
| firstRegistrationYear | String | Exactly 4 digits (`\\d{4}`) |
| cubicCapacity | Long | Min 1, Max 9999 |
| fuel | Enum | One of: `diesel`, `petrol`, `hybrid` |
| mileage | Long | Min 0, Max 9,999,999 |

### Error Handling
- `NotFoundException` for missing IDs  
- Centralized exception handler returning consistent JSON responses  

---

## 🧠 Additional Enhancements

Although not required by the specification, several improvements were added for clarity and maintainability:

- **Swagger / OpenAPI documentation**  
- **Transactional service methods**  
- **Dedicated `test` profile** using H2  
- **Request logging filter** for method & duration  
- **Integration tests (MockMvc)** and context load test  
- **Simplified security configuration** (CSRF disabled, permit-all)  
- **CORS configuration** for frontend integration  
- **RFC 7807 Problem Details** standardized error format with `traceId` and stable `code`

Note (validation UX): The DTO uses Long type for numeric fields (`cubicCapacity`, `mileage`) so that even extremely large values pass JSON parsing and then return precise Bean Validation messages (e.g., "must be less than or equal to ...") instead of a generic "Malformed JSON or invalid types".

---

## 🚀 Run Instructions

### Prerequisites
- Java 21+
- Maven 3.6+ (or use the included Maven Wrapper)

### Start the Application
```bash
./mvnw spring-boot:run
```

On Windows (PowerShell/CMD):
```bash
mvnw.cmd spring-boot:run
```

Then open:  
👉 `http://localhost:8080/swagger-ui.html` — API documentation  
👉 `http://localhost:8080/api/vehicles` — main endpoint  
👉 `http://localhost:8080/h2-console` — in-memory DB console  

---

## 📚 API Examples

### Create Vehicle
```bash
curl -X POST http://localhost:8080/api/vehicles   -H "Content-Type: application/json"   -d '{
    "model": "BMW 320",
    "firstRegistrationYear": "2021",
    "cubicCapacity": 2000,
    "fuel": "petrol",
    "mileage": 50000
  }'
```

### Seed Vehicles
```bash
curl -X POST http://localhost:8080/api/vehicles/seed
```

This endpoint generates and saves 10 random vehicles with valid data. Each vehicle will have:
- Random model from a predefined list (Audi A4, BMW 320, Mercedes C-Class, VW Golf, Toyota Corolla, Ford Focus, Opel Astra, Škoda Octavia, Peugeot 308, Renault Clio)
- Random registration year between 2000 and 2024
- Random cubic capacity between 1000 and 5000
- Random fuel type (diesel, petrol, or hybrid)
- Random mileage between 0 and 500000

### Get All Vehicles
```bash
curl -X GET http://localhost:8080/api/vehicles
```

### Get Vehicle by ID
```bash
curl -X GET http://localhost:8080/api/vehicles/1
```

### Get Vehicles (Paginated)
```bash
# Default pagination (page 0, size 20)
curl -X GET http://localhost:8080/api/vehicles/paged

# Custom page and size
curl -X GET "http://localhost:8080/api/vehicles/paged?page=0&size=10"

# With sorting (ascending by model)
curl -X GET "http://localhost:8080/api/vehicles/paged?page=0&size=10&sort=model,asc"

# With sorting (descending by model)
curl -X GET "http://localhost:8080/api/vehicles/paged?page=0&size=10&sort=model,desc"
```

**Query Parameters:**
- `page` - Page number (0-indexed, default: 0)
- `size` - Number of items per page (default: 20)
- `sort` - Sort criteria (format: `field,direction`, e.g., `sort=model,asc` or `sort=model,desc`)

**Response Format:**
The paginated response includes:
- `content` - Array of vehicles
- `totalElements` - Total number of vehicles
- `totalPages` - Total number of pages
- `number` - Current page number
- `size` - Page size
- `first` - Whether this is the first page
- `last` - Whether this is the last page

### Update Vehicle
```bash
curl -X PUT http://localhost:8080/api/vehicles/1   -H "Content-Type: application/json"   -d '{
    "model": "BMW 330",
    "firstRegistrationYear": "2022",
    "cubicCapacity": 3000,
    "fuel": "hybrid",
    "mileage": 75000
  }'
```

### Delete Vehicle
```bash
curl -X DELETE http://localhost:8080/api/vehicles/1
```

---

## ❗ Standardized error format (RFC 7807)

All error responses use RFC 7807 `application/problem+json` format with stable fields:

- `status` (HTTP status)
- `title` (status reason)
- `detail` (brief description, without internal details)
- `instance` (route URI)
- `code` (stable application-specific code, enum)
- `traceId` (for correlation in logs)
- `errors` (map of specific validation errors)

Example 404:

```json
{
  "type": "about:blank",
  "title": "Not Found",
  "status": 404,
  "detail": "Resource not found",
  "instance": "/api/vehicles/123",
  "code": "NOT_FOUND",
  "resource": "vehicle",
  "resourceId": "123",
  "traceId": "e7f2b6f8-2a3e-4b0b-9a4c-2d8e9d1c5a11"
}
```

Example 400 (body validation):

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Validation failed",
  "instance": "/api/vehicles",
  "code": "VALIDATION_ERROR",
  "errors": {
    "model": "must not be blank",
    "mileage": "must be greater than or equal to 0"
  },
  "traceId": "c1a9c7b1-7b7f-4b3a-9a3e-f9a1b2c3d4e5"
}
```

#### Example 400 (numeric range validation)

```json
{
  "type": "https://api.example.com/problems/400",
  "title": "Bad Request",
  "status": 400,
  "detail": "Validation failed",
  "instance": "/api/vehicles",
  "code": "VALIDATION_ERROR",
  "errors": {
    "cubicCapacity": "must be less than or equal to 9999"
  },
  "traceId": "..."
}
```

### Error codes (stable)

Generic codes (enum): `BAD_REQUEST`, `VALIDATION_ERROR`, `TYPE_MISMATCH`, `NOT_FOUND`, `CONFLICT`, `UNAUTHORIZED`, `FORBIDDEN`, `INTERNAL_ERROR`.

---

## 🧪 Testing

Run the full test suite:
```bash
./mvnw test
```

Includes:
- **Integration tests** for REST endpoints  
- **Context smoke test** ensuring Spring Boot configuration loads  

All tests run against the H2 in-memory database.

---

## 📄 Notes

- The application scope strictly follows the **Create, Read, and Delete** requirements defined in the specification.
- **Update operation** was added as an additional feature beyond the original requirements.
- Features such as authentication or persistent storage were **excluded by design** to align with the project scope.
- Some additional improvements were made for code quality and developer experience.

---

© 2025 — Developed by **Ilija Trifunović** as part of the Mühlbauer interview assignment.
