# Flash Sale Ticket Booking System

> A high-concurrency ticket booking engine designed to handle **10,000+ concurrent requests/second** with zero overselling — built for real-world flash sale scenarios.

![Java](https://img.shields.io/badge/Java_17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_3-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-FF6600?style=for-the-badge&logo=rabbitmq&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)

---


## The Problem

Imagine 50,000 users simultaneously trying to buy the last 100 concert tickets the moment a flash sale opens. Traditional REST APIs collapse under this load — databases get overwhelmed, race conditions cause overselling, and the user experience degrades to timeouts and errors.

This project is an engineering solution to that exact problem.

---

## System Architecture

The system uses an **Asynchronous Queue-based Architecture** that decouples request ingestion from database writes:

```
Client Request
     │
     ▼
[ Nginx — Rate Limiting & Load Balancing ]
     │
     ▼
[ Spring Boot API — JWT Auth + RBAC ]
     │
     ├── REJECT ◄─── [ Redis — Atomic Stock Check via Lua Script ]
     │                         (Stock = 0 → HTTP 400 SoldOut)
     │
     ▼ (Stock > 0)
[ RabbitMQ — Message Queue ]
     │           └── HTTP 202 Accepted → Client
     │
     ▼
[ Background Consumer — Controlled write rate ]
     │
     ▼
[ PostgreSQL — Persistent Storage + Optimistic Lock ]
```

**Key design principle:** Redis absorbs 99% of traffic at the memory layer. PostgreSQL only processes a controlled, steady stream of confirmed orders from the queue.

---

## Key Features & Engineering Decisions

### 1. Multi-Layer Overselling Prevention
| Layer | Technology | Mechanism |
|-------|-----------|-----------|
| Layer 1 (Memory) | Redis + Lua Script | Atomic `DECR` — blocks invalid requests before they touch the DB |
| Layer 2 (Disk) | PostgreSQL `@Version` | Optimistic Locking catches any remaining write conflicts |

### 2. Queue-based Load Leveling
- **RabbitMQ** decouples the 10k/s ingestion rate from the database write rate
- Consumer processes messages at a configurable, safe rate (e.g., 50 msg/s)
- Database is protected from connection pool exhaustion

### 3. Role-Based Access Control (RBAC)
```
ADMIN  → Create campaigns, manage ticket inventory, trigger cache pre-heat
CUSTOMER → Browse events, place bookings
```
Admin-only APIs (pre-heat, inventory management) are enforced at the Spring Security filter level — not just business logic.

### 4. Financial Data Integrity
- Prices stored as `DECIMAL` / `BigDecimal` — never `Float` or `Double`
- Order IDs use **UUID v4** (random) instead of auto-increment integers
    - Prevents business intelligence leakage (competitors cannot estimate order volume by probing sequential IDs)

### 5. Async Booking with Client Polling
```
Client                    Server
  │──── POST /bookings ───►│  HTTP 202 + orderId
  │                        │  (enqueued to RabbitMQ)
  │                        │
  │◄─── GET /orders/{id} ──│  status: PENDING
  │   (poll every 2s)      │
  │◄─── GET /orders/{id} ──│  status: SUCCESS ✓
```

---

## Database Schema

```sql
-- Users with RBAC
users (id BIGINT PK, email, password_hash, role ENUM('ADMIN','CUSTOMER'))

-- Flash Sale Events
campaigns (id BIGINT PK, name, start_time, end_time, status ENUM('UPCOMING','ONGOING','ENDED'))

-- Ticket Inventory with Optimistic Lock
tickets (
  id          BIGINT PK,
  campaign_id BIGINT FK,
  ticket_type VARCHAR,
  price       DECIMAL,       -- Never Float/Double
  stock       INT CHECK (stock >= 0),
  version     BIGINT         -- Hibernate @Version for Optimistic Locking
)

-- Orders with UUID
orders (
  id          VARCHAR PK,    -- UUID v4, not auto-increment
  user_id     BIGINT FK,
  ticket_id   BIGINT FK,
  quantity    INT,
  total_price DECIMAL,
  status      ENUM('PENDING','SUCCESS','FAILED')
)
```

---

## Core Technical Flows

### Admin: Cache Pre-heat (Before Flash Sale Opens)
```
1. Admin calls POST /admin/campaigns/{id}/preheat
2. Server reads total stock from PostgreSQL
3. Writes to Redis: SET ticket_stock:{ticketId} {stock}
4. Campaign status → ONGOING
5. System is now ready to handle the surge
```

### Customer: High-Concurrency Booking
```
1. Nginx rate-limits by IP (blocks spam bots)
2. Spring Security validates JWT token
3. Redis Lua Script: atomically check & decrement stock
   → stock < 0: throw SoldOutException (HTTP 400) — no DB hit
   → stock ≥ 0: generate UUID order ID, push to RabbitMQ
4. Return HTTP 202 Accepted immediately
5. Consumer reads from queue → writes to PostgreSQL
6. Client polls GET /orders/status/{orderId} until SUCCESS/FAILED
```

### Optimistic Lock Conflict Resolution
```java
// Hibernate detects version mismatch → throws OptimisticLockingFailureException
// Consumer retries up to N times, then marks order as FAILED
// Stock is restored in Redis to maintain consistency
```

---

## Risk & Mitigation Matrix

| Risk | Mitigation |
|------|-----------|
| Database crash under load | Queue-based load leveling — DB only handles controlled throughput from RabbitMQ |
| Overselling (race condition) | Two-layer defense: Redis Lua Script (atomic) + PostgreSQL Optimistic Lock |
| Business data leakage via order IDs | UUID v4 randomizes all transaction identifiers |
| Privilege escalation attacks | RBAC enforced at Spring Security layer — admin APIs require `ROLE_ADMIN` |
| Financial calculation errors | `BigDecimal` used exclusively for all monetary values |

---

## Tech Stack

| Category | Technology                  | Purpose |
|----------|-----------------------------|---------|
| Language | Java 17                     | Application runtime |
| Framework | Spring Boot 3               | Web, Security, Data JPA |
| Database | PostgreSQL                  | Persistent storage, ACID transactions |
| Cache | Redis                       | Atomic stock management, session store |
| Message Broker | RabbitMQ                    | Async order queue |
| API Gateway | Nginx                       | Rate limiting, load balancing |
| Containerization | Docker                      | Local development & deployment |
| Cloud | Microsoft Azure (Ubuntu VM) | Production hosting |

---

## Getting Started

### Prerequisites
- Docker & Docker Compose
- Java 17+
- Maven 3.8+

### Run with Docker Compose
```bash
# Clone the repository
git clone https://github.com/Luctran12/Ticket-Booking-System.git
cd Ticket-Booking-System

# Start all infrastructure services
docker-compose up -d

# Run the application
./mvnw spring-boot:run
```

### Environment Variables
```env
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/ticket-booking-system
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=your_password
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
JWT_SECRET=your_jwt_secret_key
```

---

## API Reference

### Auth
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/auth/register` | Public | Register new account |
| POST | `/auth/login` | Public | Login, returns JWT |

### Campaigns
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/campaigns` | Customer | List all active flash sales |
| POST | `/admin/campaigns` | Admin | Create a new campaign |
| POST | `/admin/campaigns/{id}/preheat` | Admin | Load stock into Redis cache |

### Bookings
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/bookings` | Customer | Place a booking (async) |
| GET | `/orders/status/{orderId}` | Customer | Poll order status |

---

## Performance Targets (SLA)

| Metric | Target |
|--------|--------|
| Concurrent requests | 10,000 req/s |
| Overselling incidents | 0 (zero tolerance) |
| Booking API response time | < 100ms (HTTP 202) |
| Data consistency | ACID-compliant |
| Availability | 99.9% uptime |

---

## Roadmap

- [ ] Core booking engine implementation
- [ ] Redis Lua Script for atomic stock management
- [ ] RabbitMQ consumer with configurable throughput
- [ ] JWT authentication + RBAC
- [ ] Admin campaign management APIs
- [ ] Docker Compose setup
- [ ] WebSocket / SSE push notifications (replace polling)
- [ ] Dead Letter Queue for failed order recovery
- [ ] Idempotency key support (prevent duplicate bookings)
- [ ] Prometheus + Grafana monitoring dashboard
- [ ] Load testing with k6 / JMeter (target: 10k req/s)

---

##  Design Document

For full system design details including architecture diagrams, database schema rationale, and risk analysis, see [`SYSTEM_DESIGN.md`](./SYSTEM_DESIGN.md).

---

## Author

**[Your Name]**
- GitHub: [Luctran12](https://github.com/Luctran12)
- LinkedIn: [linkedin.com/in/Luctran](https://linkedin.com/in/Luctran)

---

> *This project is designed as a portfolio piece demonstrating production-grade system design for high-concurrency scenarios. Architecture decisions prioritize correctness and scalability over simplicity.*