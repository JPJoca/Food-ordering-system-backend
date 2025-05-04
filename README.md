# RAF Food Ordering System

## Project Overview

This is a **student project** developed as part of the *Advanced Web Programming* course at the Faculty of Computer Science (RAF).

The aim of the project is to implement a **web application** that simulates food ordering and user management, including features such as placing and scheduling orders, tracking order statuses in real time, CRUD operations on users, and handling system errors. The system supports permission-based access control, JWT-based authentication and authorization, automatic background status updates, and WebSocket communication between the frontend and backend.

---

## Backend (Spring Boot + Spring Security + WebSocket)

### Technologies Used:
- Java 17
- Spring Boot
- Spring Security (with JWT authentication and authorization)
- WebSocket + STOMP (for real-time communication)
- JPA / Hibernate (ORM)
- MySQL / PostgreSQL (relational database)
- ScheduledExecutorService (for delayed background tasks)

### Key Functionalities:
- **PLACE_ORDER** – Creates a new order with a list of dishes.
- **CANCEL** – Cancels an order in `ORDERED` status.
- **SEARCH** – Filters orders by status, date range, and user.
- **SCHEDULE** – Allows a user to schedule an order to be created at a specific time.
- **TRACK** – Returns the current status of an order.

### Automatic Order Status Transitions:
- `ORDERED → PREPARING` after ~10 seconds (with randomized deviation)
- `PREPARING → IN_DELIVERY` after ~15 seconds
- `IN_DELIVERY → DELIVERED` after ~20 seconds
- All transitions are handled asynchronously in the background.

### Permissions and Roles:
The system implements fine-grained permission checks. Each order-related action is bound to a specific permission:
- `can_search_order`
- `can_place_order`
- `can_cancel_order`
- `can_track_order`
- `can_schedule_order`
- `can_create_users`
- `can_read_users`
- `can_update_users`
- `can_delete_users`

Users without the corresponding permission cannot perform or view those actions, either via API or frontend UI.

### Error Handling:
When a scheduled order cannot be created due to exceeding the maximum number of active orders (3), the system logs an error in the `ErrorMessage` entity, which includes:
- Timestamp
- Order ID
- Operation attempted
- Human-readable error message

---

## Frontend (Angular)

### Technologies Used:
- Angular 17+
- RxJS
- Angular Material
- WebSocket (STOMP protocol)
- ngx-pagination
- jwt-decode

### Key Views and Features:

#### 1. Order Search Page
- Displays a list of orders relevant to the logged-in user.
  - Regular users see only their own orders.
  - Admins can see all users' orders.
- Includes a filter form for status, date range, and user ID (admin only).
- Real-time updates of order statuses via WebSocket.

#### 2. Create Order Page
- A form allowing users to select dishes and place an order.
- The backend automatically associates the logged-in user.
- Also supports scheduling future orders.

#### 3. Error History Page
- Displays errors (from the `ErrorMessage` entity) caused by scheduled order failures.
- Regular users see only their own; admins see all.
- Paginated list with detailed messages.

#### 4. Permission Enforcement
- The UI dynamically adapts to the user’s permissions.
- If a user has no permissions, a warning message appears after login.

---

## Frontend-Backend Communication

- **REST API** – used for CRUD operations, filters, and state changes
- **WebSocket (STOMP)** – used for real-time updates of order statuses

All secure routes require a valid JWT token sent in the `Authorization` header. WebSocket connections also validate the token during the handshake.

---

## Deployment and Architecture

- The project is divided into a backend and frontend module.
- The system uses token-based authentication.
- Backend routes are protected by Spring Security.
- WebSocket endpoints are authenticated and provide push-based updates.

---

## Notes

This project was developed as part of a course assignment and is not intended for production use. It showcases the integration of modern Java-based backend frameworks with a reactive Angular frontend and real-time communication mechanisms.

