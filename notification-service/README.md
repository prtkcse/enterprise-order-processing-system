# Notification Service

## Overview

The Notification Service is a microservice within the Enterprise Order Processing System responsible for processing business events and generating customer notifications.

It consumes payment-related events from Apache Kafka, persists notifications in Oracle Database, and exposes REST APIs for retrieving notifications and managing their read status.

The service follows an event-driven architecture and operates independently of the Order, Inventory, and Payment services.

## Key Features

- Kafka-based event consumption
- Payment success notifications
- Payment failure notifications
- Persistent notification storage using Oracle Database
- Idempotent event processing
- Duplicate event prevention
- REST APIs for retrieving customer notifications
- Unread notification retrieval
- Mark notifications as read
- Eureka service discovery
- Spring Data JPA and Hibernate
- OpenAPI / Swagger documentation
- Spring Boot Actuator
- Prometheus metrics support

## Architecture

The Notification Service consumes events published by the Payment Service.

### Successful Payment Flow

1. Order Service creates an order.
2. Inventory Service reserves the required inventory.
3. Payment Service processes the payment.
4. Payment Service publishes `payment.completed` to Kafka.
5. Notification Service consumes the event.
6. Notification Service creates a payment success notification.
7. Notification is persisted in Oracle Database.
8. Customer can retrieve the notification through REST APIs.

### Failed Payment Flow

1. Payment Service fails to process a payment.
2. Payment Service publishes `payment.failed` to Kafka.
3. Notification Service consumes the event.
4. Notification Service creates a payment failure notification.
5. Notification is persisted in Oracle Database.
6. Customer can retrieve the notification through REST APIs.

## Technology Stack

| Technology | Purpose |
|---|---|
| Java 21 | Programming language |
| Spring Boot 3.5.x | Application framework |
| Spring Web | REST API development |
| Spring Data JPA | Persistence layer |
| Hibernate | ORM |
| Oracle Database | Notification storage |
| Apache Kafka | Event-driven communication |
| Spring Kafka | Kafka integration |
| Eureka | Service discovery |
| Maven | Build and dependency management |
| Spring Actuator | Health checks and metrics |
| Micrometer / Prometheus | Metrics collection |
| Springdoc OpenAPI | API documentation |

## Microservice Integration

### Kafka Consumers

| Topic | Event | Description |
|---|---|---|
| `payment.completed` | `PaymentCompletedEvent` | Creates a payment success notification |
| `payment.failed` | `PaymentFailedEvent` | Creates a payment failure notification |

### PaymentCompletedEvent

```java
PaymentCompletedEvent(
    UUID eventId,
    UUID paymentId,
    UUID orderId,
    BigDecimal amount,
    String currency,
    String transactionReference,
    LocalDateTime completedAt
)
```

### PaymentFailedEvent

```java
PaymentFailedEvent(
    UUID eventId,
    UUID paymentId,
    UUID orderId,
    BigDecimal amount,
    String currency,
    String failureReason,
    LocalDateTime failedAt
)
```

Events are shared through the `common-events` Maven module.

## Notification Lifecycle

Notifications have the following statuses:

| Status | Description |
|---|---|
| `UNREAD` | Notification has been created but not read |
| `READ` | Notification has been read by the recipient |

### Notification Types

| Type | Description |
|---|---|
| `PAYMENT_SUCCESS` | Payment completed successfully |
| `PAYMENT_FAILED` | Payment processing failed |

## Database Schema

### Table: `NOTIFICATIONS`

| Column | Type | Description |
|---|---|---|
| `ID` | `RAW(16)` | Notification primary key |
| `EVENT_ID` | `RAW(16)` | Source Kafka event identifier |
| `ORDER_ID` | `RAW(16)` | Associated order identifier |
| `PAYMENT_ID` | `RAW(16)` | Associated payment identifier |
| `RECIPIENT_ID` | `VARCHAR2(100)` | Notification recipient reference |
| `NOTIFICATION_TYPE` | `VARCHAR2(30)` | Notification type |
| `TITLE` | `VARCHAR2(200)` | Notification title |
| `MESSAGE` | `VARCHAR2(1000)` | Notification message |
| `STATUS` | `VARCHAR2(20)` | `UNREAD` or `READ` |
| `CREATED_AT` | `TIMESTAMP` | Notification creation time |
| `READ_AT` | `TIMESTAMP` | Notification read time |

### Idempotency

The `EVENT_ID` column has a unique constraint.

This prevents the same payment event from generating multiple notifications when Kafka redelivers an event.

The application also checks whether a notification for the event already exists before creating a new record.

## REST API

Base URL:

```text
http://localhost:8084/api/notifications
```

### 1. Retrieve All Notifications

```http
GET /api/notifications/customer/{recipientId}
```

Returns all notifications belonging to the specified recipient, ordered by creation time in descending order.

Example:

```http
GET http://localhost:8084/api/notifications/customer/550e8400-e29b-41d4-a716-446655440000
```

### 2. Retrieve Unread Notifications

```http
GET /api/notifications/customer/{recipientId}/unread
```

Returns only unread notifications for the specified recipient.

Example:

```http
GET http://localhost:8084/api/notifications/customer/550e8400-e29b-41d4-a716-446655440000/unread
```

### 3. Mark Notification as Read

```http
PATCH /api/notifications/{notificationId}/read?recipientId={recipientId}
```

Updates the notification status from `UNREAD` to `READ` and sets the `READ_AT` timestamp.

Example:

```http
PATCH http://localhost:8084/api/notifications/8d4a2d9b-6d4c-4e5f-9a1b-7c8d9e0f1234/read?recipientId=550e8400-e29b-41d4-a716-446655440000
```

## API Response

Example notification response:

```json
{
  "notificationId": "8d4a2d9b-6d4c-4e5f-9a1b-7c8d9e0f1234",
  "orderId": "550e8400-e29b-41d4-a716-446655440000",
  "paymentId": "6d4a2d9b-6d4c-4e5f-9a1b-7c8d9e0f1234",
  "recipientId": "550e8400-e29b-41d4-a716-446655440000",
  "notificationType": "PAYMENT_SUCCESS",
  "title": "Payment Successful",
  "message": "Your payment was successful.",
  "status": "UNREAD",
  "createdAt": "2026-11-26T20:15:30",
  "readAt": null
}
```

## Project Structure

```text
notification-service
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com.enterprise.notification
│   │   │       ├── config
│   │   │       ├── controller
│   │   │       ├── dto
│   │   │       ├── entity
│   │   │       ├── exception
│   │   │       ├── mapper
│   │   │       ├── messaging
│   │   │       ├── repository
│   │   │       ├── service
│   │   │       └── NotificationServiceApplication.java
│   │   └── resources
│   │       └── application.properties
└── pom.xml
```

## Prerequisites

Ensure the following components are installed and running:

- Java 21
- Maven
- Oracle Database
- Apache Kafka
- Eureka Discovery Server
- `common-events` module

## Database Setup

Create a dedicated Oracle user for the Notification Service.

Example:

```sql
CREATE USER NOTIFICATION_DB IDENTIFIED BY NOTIFICATION_DB;

GRANT CONNECT, RESOURCE TO NOTIFICATION_DB;

ALTER USER NOTIFICATION_DB QUOTA UNLIMITED ON USERS;
```

Execute the notification schema SQL script to create the `NOTIFICATIONS` table and associated indexes.

The service uses Hibernate schema validation:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

Hibernate validates the entity mappings against the existing database schema during application startup.

## Configuration

The application uses `application.properties`.

### Database

```properties
spring.datasource.url=${NOTIFICATION_DB_URL:jdbc:oracle:thin:@//localhost:1521/FREEPDB1}
spring.datasource.username=${NOTIFICATION_DB_USERNAME:NOTIFICATION_DB}
spring.datasource.password=${NOTIFICATION_DB_PASSWORD:NOTIFICATION_DB}
```

### Kafka

```properties
spring.kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
spring.kafka.consumer.group-id=notification-service-group
```

### Eureka

```properties
eureka.client.service-url.defaultZone=${EUREKA_URL:http://localhost:8761/eureka/}
```

## Build

From the root of the Maven multi-module project:

### Build all modules

```bash
mvn clean install -DskipTests
```

### Build Notification Service and dependencies

```bash
mvn -pl notification-service -am clean install -DskipTests
```

### Run tests

```bash
mvn -pl notification-service test
```

## Run the Service

```bash
mvn -pl notification-service spring-boot:run
```

The Notification Service runs on:

```text
http://localhost:8084
```

## Service Discovery

The service registers itself with Eureka.

Eureka Dashboard:

```text
http://localhost:8761
```

Registered service name:

```text
NOTIFICATION-SERVICE
```

## API Documentation

Swagger UI:

```text
http://localhost:8084/swagger-ui.html
```

OpenAPI JSON:

```text
http://localhost:8084/api-docs
```

## Actuator

Health check:

```text
http://localhost:8084/actuator/health
```

Metrics:

```text
http://localhost:8084/actuator/metrics
```

Prometheus:

```text
http://localhost:8084/actuator/prometheus
```

## Logging

Hibernate SQL logging is disabled to keep application logs clean.

```properties
spring.jpa.show-sql=false
logging.level.org.hibernate.SQL=OFF
logging.level.org.hibernate.orm.jdbc.bind=OFF
logging.level.org.hibernate=ERROR
logging.level.org.springframework.kafka=WARN
logging.level.org.apache.kafka=WARN
```

The application logs important business events, including:

- Payment events received
- Notification creation
- Event processing failures
- Kafka consumer errors

## End-to-End Testing

### Successful Payment

1. Start Eureka Discovery Server.
2. Start Kafka.
3. Start Order Service.
4. Start Inventory Service.
5. Start Payment Service.
6. Start Notification Service.
7. Create an order through Order Service.
8. Verify inventory reservation.
9. Verify payment success.
10. Verify that Payment Service publishes `payment.completed`.
11. Verify that Notification Service consumes the event.
12. Query the Notification Service REST API.

### Failed Payment

1. Configure the payment processor to simulate failure.
2. Create an order.
3. Verify that Payment Service publishes `payment.failed`.
4. Verify that Notification Service consumes the event.
5. Retrieve the failure notification using the REST API.

## Reliability

The Notification Service implements:

- Event-based processing
- Kafka manual acknowledgement
- Database persistence
- Duplicate event detection
- Unique event ID constraint
- Transactional notification creation

Notifications are acknowledged to Kafka only after the processing method successfully returns.

## Limitations

The current implementation provides database-backed in-app notifications.

It does not currently support:

- Email delivery
- SMS delivery
- Push notifications
- WebSocket real-time delivery
- Customer authentication and authorization
- Pagination
- Notification retention policies
- Notification delivery retry tracking

The current `recipientId` is derived from the order ID because customer identity is not included in the payment events yet. This is a temporary integration approach.

## Future Enhancements

- Add customer ID to shared event contracts
- Integrate email delivery using SMTP or an email provider
- Integrate SMS notifications
- Add push notifications
- Add WebSocket support for real-time updates
- Add pagination and filtering
- Implement authentication and authorization
- Add notification templates
- Add delivery status tracking
- Implement retry and dead-letter handling
- Add integration tests using Testcontainers
- Add distributed tracing

## License

This project is intended for learning, demonstration, and portfolio purposes.
