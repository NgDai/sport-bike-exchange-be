# 🚲 Sport Bike Exchange — Backend

A full-featured **peer-to-peer sport bike marketplace** backend, built with Spring Boot. Users can list, discover, and transact sport bicycles in a secure, inspection-verified environment, with integrated digital wallet and VNPay payment processing.

> **Portfolio project** — Part of a capstone project at FPT University (SWP391).

---

## 📌 Features

- **Authentication** — Username/password login & Google OAuth2 (JWT-based, HS256)
- **Bike Listings** — Post, browse, and manage sport bike listings with Cloudinary image uploads
- **Reservation & Escrow Flow** — Buyers deposit funds via VNPay; an inspector is assigned to verify the bike condition before finalizing the deal
- **Inspection System** — Inspectors submit reports; outcomes (pass/fail/no-show) trigger automated wallet settlements
- **Dispute Management** — Either party can raise a dispute that admins resolve
- **Digital Wallet** — Internal wallet for holding deposits, receiving payouts, and top-ups via VNPay
- **Event Bikes** — Special event-based bicycle sales with a separate reservation and payment flow
- **Admin Dashboard APIs** — Full CRUD for users, categories, brands, system config, and transaction management
- **QR Code Generation** — Used for buyer/seller check-in at inspection appointments

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0 |
| Security | Spring Security + JWT (Nimbus JOSE) + OAuth2 |
| ORM | Spring Data JPA (Hibernate) |
| Database | Microsoft SQL Server |
| Mapping | MapStruct 1.6 + Lombok |
| Payment Gateway | VNPay (v2.1.0) |
| File Storage | Cloudinary |
| Containerization | Docker |
| Build Tool | Maven |

---

## 🏗️ Architecture

```
src/main/java/com/bicycle/marketplace/
├── config/          # Security, VNPay, Cloudinary configuration
├── controller/      # REST API layer (21 controllers)
├── dto/             # Request / Response DTOs
├── entities/        # JPA entities
├── enums/           # Application enums
├── exception/       # Global exception handling
├── mapper/          # MapStruct mappers
├── repository/      # Spring Data JPA repositories
├── services/        # Business logic layer
└── util/            # Utility helpers
```

### Core Transaction Flow

```
Buyer places reservation
        │
        ▼
Deposit via VNPay ──► VNPay Callback confirms deposit
        │
        ▼
Admin schedules inspection (assigns inspector, time, location)
        │
        ▼
Inspector submits Inspection Report
        │
   ┌────┴────┐
 Pass      Fail / No-show
   │              │
   ▼              ▼
Buyer pays    Deposit refunded
final amount  or transferred (penalty)
   │
   ▼
Seller receives payout → Wallet settled
```

---

## 🚀 Getting Started

### Prerequisites

- Java 21+
- Maven 3.9+
- Microsoft SQL Server
- Docker (optional)

### Environment Variables

Create an `application.properties` (or use environment variables) with:

```properties
# Database
spring.datasource.url=jdbc:sqlserver://<host>:<port>;databaseName=<db>
spring.datasource.username=<username>
spring.datasource.password=<password>

# JWT
jwt.signer.key=<your-secret-key-min-32-chars>

# Google OAuth2
GOOGLE_CLIENT_ID=<your-google-client-id>

# VNPay
vnpay.tmn-code=<your-tmn-code>
vnpay.secret-key=<your-secret-key>
vnpay.pay-url=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
vnpay.return-url=/payments/vnpay-payment

# Cloudinary
cloudinary.cloud-name=<cloud-name>
cloudinary.api-key=<api-key>
cloudinary.api-secret=<api-secret>

# Frontend URL
frontend.url=http://localhost:5173
frontend.prod-url=https://your-frontend.vercel.app
```

### Run Locally

```bash
# Clone the repository
git clone https://github.com/your-username/sport-bike-exchange-be.git
cd sport-bike-exchange-be

# Build and run
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`.

### Run with Docker

```bash
docker build -t sport-bike-exchange-be .
docker run -p 8080:8080 --env-file .env sport-bike-exchange-be
```

---

## 📡 API Overview

| Module | Base Path | Description |
|---|---|---|
| Authentication | `/auth` | Login (username/email/Google), token introspect |
| Users | `/users` | User profile management |
| Bike Listings | `/listings` | Post and browse bikes |
| Reservations | `/reservations` | Full reservation lifecycle |
| Deposits | `/deposits` | Deposit creation and management |
| Payments | `/payments` | VNPay integration (top-up, deposit, final payment) |
| Inspection Reports | `/inspection-reports` | Submit and retrieve inspection outcomes |
| Disputes | `/disputes` | Raise and resolve disputes |
| Events | `/events` | Event-based bike sales |
| Wallet | `/wallet` | Wallet balance and transactions |
| Brands / Categories | `/brands`, `/categories` | Catalog management |
| System Config | `/system-config` | Platform fee and configuration |

> All protected endpoints require a `Bearer <JWT>` header.

---

## 🔐 Security

- JWT tokens are signed with HMAC-SHA256 and expire after **5 hours**
- Role-based access control via Spring Security `@PreAuthorize`
- Roles: `USER`, `ADMIN`, `INSPECTOR`
- Google ID Token verification using the official Google API Client library
- VNPay callback signatures are verified using HMAC-SHA512 before processing any financial operation

---

## 🧪 Testing

```bash
./mvnw test
```

---

## 📁 Related Repository

- **Frontend**: [sport-bike-exchange-fe](https://github.com/your-username/sport-bike-exchange-fe) — React + Vite

---

## 👥 Team

Developed as a Software Project (SWP391) capstone at **FPT University**.

---

## 📄 License

This project is for educational and portfolio purposes.
