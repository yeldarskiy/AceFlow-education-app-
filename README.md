# AceFlow 

AI-powered exam preparation web application built with Spring MVC + JDBC.

## Tech Stack

- **Backend:** Java 17, Spring Core, Spring MVC, Servlets
- **Database:** PostgreSQL + pure JDBC (custom Connection Pool, no ORM)
- **Templates:** Thymeleaf (server-side rendering)
- **Security:** BCrypt password hashing, PreparedStatements (SQL injection protection)
- **i18n:** EN | RU | KZ (3 locales)
- **Testing:** JUnit 5 + Mockito + JaCoCo

## Architecture

```
Controller → Service → DAO → Database
kz.aceflow.controller  (Spring MVC @Controller)
kz.aceflow.service     (Business logic interfaces + implementations)
kz.aceflow.dao         (CRUD interfaces + JDBC implementations)
kz.aceflow.model       (Domain entities)
kz.aceflow.config      (Spring configuration)
kz.aceflow.interceptor (Auth + Locale interceptors)
kz.aceflow.exception   (Custom exceptions)
kz.aceflow.util        (ConnectionPool singleton)
```

## Design Patterns Used

1. **Singleton** — `ConnectionPool` (thread-safe double-checked locking)
2. **Factory Method** — `DaoFactory` (creates DAO instances)
3. **Strategy** — XP reward calculation based on goal priority
4. **Object Pool** — reusable database connections

## Quick Start

### 1. Database setup (PostgreSQL)

```bash
createdb aceflow
psql aceflow < src/main/resources/db/schema.sql
psql aceflow < src/main/resources/db/data.sql
```

### 2. Configure connection

Edit `src/main/resources/application.properties`:
```properties
db.url=jdbc:postgresql://localhost:5432/aceflow
db.username=your_user
db.password=your_password
```

### 3. Build and run

```bash
mvn clean package
# Deploy aceflow.war to Tomcat 10+
```

### 4. Run tests

```bash
mvn test
# Coverage report: target/site/jacoco/index.html
```

## Features

- User authentication (register/login/logout) with BCrypt
- Document upload (PDF, DOCX, TXT)
- Practice tests with scoring and XP rewards
- Spaced-repetition flashcards
- Goal tracking with deadlines
- Study plans for exams
- Achievements and leaderboard
- 3-language interface (EN/RU/KZ)
- Responsive design (Bootstrap-compatible CSS)

## Test Credentials

| Role    | Email              | Password   |
|---------|--------------------|------------|
| Student | yeldar@example.kz  | student123 |
| Admin   | admin@aceflow.kz   | admin123   |
