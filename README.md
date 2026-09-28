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

## Run the application and UI

```powershell
mvn spring-boot:run
```

Open the UI at [http://localhost:8080](http://localhost:8080). The dashboard is
served by Spring Boot and calls the API through same-origin `/api/v1` URLs.

### Deploy to Vercel

This repository is deployed as one Spring Boot container. `Dockerfile.vercel` is
at the repository root, and Spring Boot serves the dashboard from
`src/main/resources/static`.

Before deploying, commit **all** of these files, including the dashboard assets:

```powershell
git add Dockerfile.vercel .dockerignore src/main/resources/static src/main/java src/main/resources pom.xml README.md
git commit -m "Prepare Spring Boot app for Vercel"
git push origin main
```

In Vercel, set the project Root Directory to the repository root and add this
Production environment variable:

```text
DATABASE_PASSWORD=<your Neon database password>
```

Do not add the password to `application.properties` or commit it to Git. Redeploy
the commit after the variable is saved. Verify these routes after deployment:

```text
GET /api/v1/health
GET /api/v1/analytics/summary
GET /
```

The dashboard calls same-origin `/api/v1/...` endpoints, so it needs no separate
frontend URL or CORS configuration.

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
