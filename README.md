# ApplyTrack

ApplyTrack is a beginner-friendly Spring Boot project for tracking job applications.
This first backend milestone provides CRUD, filtering, and database-backed analytics.

## Stack

- Java 21 and Spring Boot 3
- PostgreSQL 16
- Spring Data JPA / Hibernate
- Flyway migrations
- Maven

## Database configuration

The application requires the configured Neon PostgreSQL target. Set
`DATABASE_URL`, `DATABASE_USERNAME`, and `DATABASE_PASSWORD` in the process
environment before starting it. Startup rejects missing or malformed URLs and
URLs that do not point to the configured Neon host and `neondb` database.
For local runs, Spring Boot loads untracked `.env` and `.env.local` files from
the repository root. Use plain `KEY=value` lines; keep these files out of Git.

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

In Vercel, set the project Root Directory to the repository root and configure
these Production environment variables in Project Settings. A local `.env` file
is not automatically uploaded or read by a Vercel deployment:

```text
DATABASE_URL=jdbc:postgresql://ep-broad-heart-ay53aflf-pooler.c-5.us-east-2.aws.neon.tech/neondb?sslmode=require&channel_binding=require
DATABASE_USERNAME=neondb_owner
DATABASE_PASSWORD=<your Neon database password>
JWT_SECRET=<at least 32 random bytes>
```

`DATABASE_URL` is required and startup rejects a non-PostgreSQL URL or any host
or database other than the configured Neon `neondb`. Keep the password in
`DATABASE_PASSWORD`; do not embed it in the URL, add it to
`application.properties`, or commit it to Git. Redeploy the commit after the
variables are saved. Verify these routes after deployment:

```text
GET /api/v1/health
GET /api/v1/analytics/summary
GET /
```

The dashboard calls same-origin `/api/v1/...` endpoints, so it needs no separate
frontend URL or CORS configuration.

## Authentication and referrals

Registration and login return a JWT. Send it as a Bearer token on protected API
requests. Google and LinkedIn provider login are currently disabled. Set
CORS_ALLOWED_ORIGINS to the comma-separated list of allowed frontend origins
only when the dashboard is hosted on a different origin.

DATABASE_URL, DATABASE_PASSWORD, and JWT_SECRET are required to start the app.
JWT_SECRET must contain at least 32 UTF-8 bytes. SMTP uses MAIL_HOST, MAIL_PORT,
MAIL_USERNAME, MAIL_PASSWORD, and MAIL_FROM.

Password recovery sends a six-digit, ten-minute code and allows at most five
verification attempts. It is rate-limited to one request per minute and five
requests per account per hour. To send recovery mail through the requested Gmail
account, configure `MAIL_HOST=smtp.gmail.com`, `MAIL_PORT=587`,
`MAIL_USERNAME=umasankar.varadati@gmail.com`, `MAIL_FROM=umasankar.varadati@gmail.com`,
and `MAIL_PASSWORD` as a Google App Password. Enable 2-Step Verification in the
Google account and create an App Password; do not use the normal account password.
Set these values in local `.env` and Vercel Production environment variables,
then restart or redeploy the app.

Referral routes are nested under an application. They support contact list,
create, update, delete, message preview, send, send-all, mark-sent, and
mark-referred operations.

Each account has one reusable referral message template, editable in the
dashboard above the application list. The authenticated
`GET /api/v1/referral-template` and `PUT /api/v1/referral-template` endpoints
read and update it. New contacts copy the current template unless a custom
message is entered; later template edits do not overwrite saved contact copies.
Supported placeholders include `{{name}}`, `{{job_ID}}`, `{{role}}`,
`{{company}}`, `{{jobTitle}}`, `{{jobId}}`, `{{jobUrl}}`, and `{{senderName}}`.

V4 assigns existing ownerless applications to the earliest account, ordered by
account creation time and UUID. If there are no accounts at migration time, the
first account created by local registration claims those applications.
The migration and account creation update only rows whose owner is NULL.

### Protecting production data during deployment

Before each production deployment, create a Neon backup or branch from the
production database in the Neon console. Record the current application row
count and applied migration version:

```sql
SELECT count(*) AS application_rows FROM job_applications;
SELECT version FROM flyway_schema_history WHERE success = true ORDER BY installed_rank DESC LIMIT 1;
```

After deployment, verify the Vercel `DATABASE_URL` still targets the Neon host
and `neondb` above, then run the same queries against that database. Compare the
row count and confirm Flyway applied the pending migrations (currently through
version `6`). Sign in with the first account and confirm the existing
applications are visible. A Flyway failure should stop startup; do not point
the deployment at a replacement or empty database to work around it.

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
