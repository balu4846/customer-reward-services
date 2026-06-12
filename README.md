#  Customer Reward Service (Spring Boot)

##  Project Overview
This is a Spring Boot REST API that calculates reward points for customers based on their transaction history.

The system fetches customer transactions from the database and calculates:
- Monthly reward points
- Total reward points

---

##  Architecture

Controller → Service (Interface) → Service Implementation → Repository → Utility

### Layers:
- **RewardController** → Handles REST API requests
- **RewardService (Interface)** → Defines service contract
- **RewardServiceImpl** → Implements business logic for reward calculation
- **TransactionRepository** → Fetches transactions from database using JPA query
- **RewardHelperUtil** → Utility class for reward points calculation
- **RewardRequestValidator** → Validates input request parameters before processing the request

---

##  Reward Points Logic

Reward points are calculated using the following rules:

- 2 points for every dollar spent above $100
- 1 point for every dollar spent between $50 and $100
- No points for $50 or below

### Example:
Transaction = $120  
→ (20 × 2) + (50 × 1) = 40 + 50 = **90 points**

---
##  How to Run the Project

### 1. Clone the repository
git clone https://github.com/balu4846/customer-reward-services

### 2. Build the project
mvn clean install
### 3. Run the application
mvn spring-boot:run
### 4. Access the application
http://localhost:8081

## 📡 API Documentation

### 🔹 Get Customer Reward Points

**Endpoint:**
GET /api/rewards/{customerId}
### 🔹 Optional Query Parameters

| Parameter   | Description |
|------------|-------------|
| startDate  | Start date of range |
| endDate    | End date of range |
| months     | Number of months (default = 3) |

---

## 🔹 Example Requests

```http
GET /api/rewards/101
```

```http
GET /api/rewards/101?months=6
```

```http
GET /api/rewards/101?startDate=2026-03-01&endDate=2026-05-30
```

## 📥 Sample Success Response (200 OK)

```json
{
    "customerId": 101,
    "customerName": "John",
    "monthlyRewards": {
        "2026-03": 45.0,
        "2026-04": 250.0,
        "2026-05": 115.0
    },
    "totalRewards": 410.0
}
```

## ❌ Error Responses

### 400 Bad Request

Returned when request parameters are invalid.

#### Example: Invalid months value

```http
GET /api/rewards/101?months=-1
```

Response:

```json
{
    "status": 400,
    "message": "Months must be greater than 0",
    "timestamp": "2026-06-03T10:30:00"
}
```

#### Example: Months cannot exceed 36

```http
GET /api/rewards/101?months=9999
```

Response:

```json
{
    "status": 400,
    "message": "Months cannot exceed 36",
    "timestamp": "2026-06-03T10:30:00"
}
```

#### Example: Missing endDate

```http
GET /api/rewards/101?startDate=2026-03-01
```

Response:

```json
{
    "status": 400,
    "message": "Both startDate and endDate must be provided together",
    "timestamp": "2026-06-03T10:30:00"
}
```

#### Example: Date range and months provided together

```http
GET /api/rewards/101?startDate=2026-03-01&endDate=2026-05-30&months=3
```

Response:

```json
{
    "status": 400,
    "message": "Provide either date range or months, not both",
    "timestamp": "2026-06-03T10:30:00"
}
```

#### Example: Invalid date format

```http
GET /api/rewards/101?startDate=test&endDate=2026-05-30
```

Response:

```json
{
    "status": 400,
    "message": "Invalid value 'test' for parameter 'startDate'",
    "timestamp": "2026-06-03T10:30:00"
}
```

#### Example: Invalid date range

```http
GET /api/rewards/101?startDate=2026-05-30&endDate=2026-03-01
```

Response:

```json
{
    "status": 400,
    "message": "endDate cannot be before startDate",
    "timestamp": "2026-06-03T10:30:00"
}
```

### 404 Not Found

Returned when no transactions exist for the given customer and date range.

```http
GET /api/rewards/999?months=3
```

Response:

```json
{
    "status": 404,
    "message": "No transactions found for the given date range",
    "timestamp": "2026-06-03T10:30:00"
}
```

### 500 Internal Server Error

Returned when an unexpected server error occurs.

Response:

```json
{
    "status": 500,
    "message": "An unexpected error occurred",
    "timestamp": "2026-06-03T10:30:00"
}
```


