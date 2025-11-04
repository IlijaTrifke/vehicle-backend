# 🚗 Vehicle Backend

A **Spring Boot** application developed as part of the Mühlbauer technical assignment.  
The project implements the required **Create, Read, and Delete (CRD)** functionality for managing vehicle records, following a clean, modular architecture and good development practices.

---

## 🎯 Purpose

The application enables basic management of vehicle data — creating, listing, and deleting vehicles with attributes such as model, first registration year, cubic capacity, fuel type, and mileage.

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
├── controller      → REST endpoints (CRD operations)
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
| **POST** | `/api/vehicles` | Create a new vehicle |
| **DELETE** | `/api/vehicles/{id}` | Delete a vehicle by ID |

### Data Validation
Implemented using **Jakarta Bean Validation**:
- `@NotBlank`, `@Size`, `@Pattern` for strings  
- `@NotNull`, `@Min`, `@Max`, `@Positive` for numbers  
- Enum validation for `Fuel` (`diesel`, `petrol`, `hybrid`)

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

---

## 🚀 Run Instructions

### Prerequisites
- Java 21+
- Maven 3.6+ (or use the included Maven Wrapper)

### Start the Application
```bash
./mvnw spring-boot:run
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

### Get All Vehicles
```bash
curl -X GET http://localhost:8080/api/vehicles
```

### Delete Vehicle
```bash
curl -X DELETE http://localhost:8080/api/vehicles/1
```

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
- Features such as authentication, update operations, or persistent storage were **excluded by design** to align with the project scope.
- Some additional improvements were made for code quality and developer experience.

---

© 2025 — Developed by **Ilija Trifunović** as part of the Mühlbauer interview assignment.
