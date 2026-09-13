# ApplyTrack

ApplyTrack is a beginner-friendly Spring Boot project for tracking job applications.
This first backend milestone provides CRUD, filtering, and database-backed analytics.

## Stack

- Java 21 and Spring Boot 3
- PostgreSQL 16
- Spring Data JPA / Hibernate
- Flyway migrations
- Maven

## Start PostgreSQL

```powershell
docker compose up -d
```

The development database uses `localhost:5432`, database `applytrack`, user
`applytrack`, and password `applytrack`. These defaults can be replaced with the
`DATABASE_URL`, `DATABASE_USERNAME`, and `DATABASE_PASSWORD` environment variables.

## Run the API

```powershell
mvn spring-boot:run
```

The API runs at `http://localhost:8080`.

## API endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| POST | `/api/v1/applications` | Create a job application |
| GET | `/api/v1/applications` | List, search, filter, sort, and paginate applications |
| GET | `/api/v1/applications/{id}` | Get one application |
| PUT | `/api/v1/applications/{id}` | Replace an application |
| DELETE | `/api/v1/applications/{id}` | Delete an application |
| GET | `/api/v1/analytics/summary` | Get headline counts |
| GET | `/api/v1/analytics/status-distribution` | Get counts grouped by status |

### Create an application

```json
{
  "companyName": "Acme Labs",
  "jobTitle": "Junior Java Developer",
  "jobUrl": "https://careers.example.com/jobs/123",
  "location": "Hyderabad",
  "applicationSource": "LINKEDIN",
  "status": "APPLIED",
  "applicationDate": "2026-09-12",
  "notes": "Applied with the Java resume."
}
```

### Filtering examples

```text
GET /api/v1/applications?status=APPLIED
GET /api/v1/applications?search=java&location=Hyderabad
GET /api/v1/applications?source=LINKEDIN&fromDate=2026-09-01&toDate=2026-09-30
GET /api/v1/applications?page=0&size=10&sort=companyName,asc
```

Valid statuses are `SAVED`, `APPLIED`, `SCREENING`, `ASSESSMENT`, `INTERVIEW`,
`OFFER`, `ACCEPTED`, `REJECTED`, and `WITHDRAWN`.

## Run tests

```powershell
mvn test
```

Tests use an in-memory H2 database; the running application always uses PostgreSQL.

## Next learning steps

1. Add authentication and a `User` owner to every application.
2. Add companies as their own entity.
3. Add controlled status transitions and status history.
4. Add interviews, follow-ups, and dashboard charts.
The right opportunity at the right time.
