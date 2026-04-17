# Loan Approval Process

## Requirements

- Java 21
- Docker + Docker Compose

## Environment Variables

Copy `.env.example` to `.env` before starting stack.

```env
POSTGRES_DB=loan_approval
POSTGRES_PORT=5432
DB_URL=jdbc:postgresql://localhost:5432/loan_approval
DB_USERNAME=loan_app
DB_PASSWORD=loan_app
LOAN_CUSTOMER_MAX_AGE=70
APP_PORT=8080
```

## Start With Docker

```bash
docker compose up --build
```

Services:

- Root URL: `http://localhost:8080` -> redirects to Swagger UI
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- PostgreSQL: `localhost:5432`

If `8080` already busy, set `APP_PORT` in `.env`, for example `APP_PORT=8081`.
If `5432` already busy, set `POSTGRES_PORT` in `.env`, for example `POSTGRES_PORT=5433`.

Stop stack:

```bash
docker compose down
```

Stop stack and remove database volume:

```bash
docker compose down -v
```

## Run Locally

Start PostgreSQL first, then run app:

```bash
./mvnw spring-boot:run
```

## Sample Requests

Create loan application:

```bash
 curl -X POST http://localhost:8080/api/loan-applications \
    -H "Content-Type: application/json" \
    -d '{
      "firstName": "Mari",
      "lastName": "Tamm",
      "personalCode": "39504121236",
      "loanTermMonths": 12,
      "interestMargin": 1.001,
      "baseInterestRate": 1.234,
      "loanAmount": 10000.00
    }'
```

Get application by id:

```bash
curl http://localhost:8080/api/loan-applications/1
```

Approve application:

```bash
curl -X POST http://localhost:8080/api/loan-applications/1/approve
```

Reject application:

```bash
curl -X POST http://localhost:8080/api/loan-applications/1/reject \
  -H "Content-Type: application/json" \
  -d '{
    "rejectionReason": "FAILED_CREDIT_POLICY"
  }'
```
