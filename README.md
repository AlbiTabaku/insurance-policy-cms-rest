# insurance-policy-cms-rest
Rest Api Insurance Policy CMS

## What i have  used

- Java 21
- Spring Boot 3.2.0
- PostgreSQL
- Maven
- Flyway for database migrations
- Swagger/OpenAPI docs

## Getting Started

### First Steps that i have followed

1. Make sure Java 21 is installed:
```bash
java -version
```

2. Clone or navigate to the project:
```bash
cd insurance-policy-management
```

## Setup Options

### Option 1: Docker (Easiest)

If you have Docker installed, this is the quickest way:

```bash
docker-compose up -d
```

That's it. The app runs on `http://localhost:3000` and PostgreSQL on port 5432.

To stop:
```bash
docker-compose down
```

### Option 2: Manual Setup

#### Step 1: Setup PostgreSQL

Install PostgreSQL if you don't have it:
```bash
install postgress from pg installer exec file
#### Step 2: Create Database

```bash
psql postgres
```

Then run:
```sql
CREATE DATABASE insurance_db;
CREATE USER postgres WITH PASSWORD 'Albi1902';
GRANT ALL PRIVILEGES ON DATABASE insurance_db TO postgres;
\q
```

#### Step 3: Configure (if needed)

Database config is in `src/main/resources/application.properties`:
```properties
spring.datasource.username=postgres
spring.datasource.password=Albi1902
```

Change these if you used different credentials.

#### Step 4: Build and Run

```bash
mvn clean install
mvn spring-boot:run
```

App will start on `http://localhost:3000`

## Testing the API

### Swagger UI
Open in browser: `http://localhost:3000/swagger-ui/index.html`

## API Response Format

All `/api/policies` requests return:
```json
{
  "content": [
    {
      "id": 1,
      "policyNumber": "POL-2024-100001",
      "customerName": "Albi Tabaku",
      "customerEmail": "albi.tabaku@email.com",
      "policyType": "HEALTH",
      "status": "ACTIVE"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 50,
  "totalPages": 3,
  "first": true,
  "last": false
}
```

## Available Filters

- `page` - Page number (starts at 0)
- `size` - Items per page (max 100)
- `customerEmail` - Exact match
- `policyNumber` - Partial match
- `status` - ACTIVE, EXPIRED, or CANCELLED
- `policyType` - HEALTH, AUTO, HOME, or LIFE

Results are sorted by creation date (newest first).

## Running Tests

```bash
$env:JAVA_HOME = "C:\Users\User\.jdks\ms-21.0.10"  
mvn test
```

Tests use H2 in-memory database, no PostgreSQL required.

## Error Handling

The API uses a global exception handler (`GlobalExceptionHandler`) that catches all errors and returns consistent error responses.

### HTTP Status Codes

**404 - Not Found**
- Triggered when: Policy or claim doesn't exist
- Handler: `ResourceNotFoundException`
- Example: `GET /api/policies/999` (non-existent ID)
```json
{
  "timestamp": "2024-02-07T12:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Policy not found with id: 999",
  "path": "/api/policies/999"
}
```

**400 - Bad Request**
- Triggered when: Validation fails on request parameters or body
- Handlers: `ConstraintViolationException`, `MethodArgumentNotValidException`
- Examples:
    - Invalid page number: `GET /api/policies?page=-1`
    - Invalid email format: `POST /api/policies` with bad email
    - Size exceeds max: `GET /api/policies?size=101`

**409 - Conflict**
- Triggered when: Business rules are violated
- Handler: `BusinessRuleException`
- Examples:
    - Renewing a cancelled policy
    - Cancelling an already expired policy
    - Policy end date less than 6 months from start

**500 - Internal Server Error**
- Triggered when: Unexpected errors occur (database issues, null pointers, etc.)
- Handler: Generic `Exception` handler
- All unhandled exceptions are caught here and logged

## Notes

- Flyway runs migrations automatically on startup
- All API endpoints documented in Swagger UI