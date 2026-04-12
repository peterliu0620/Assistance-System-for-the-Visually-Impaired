# homework

## Project Structure

- `admin/backend`: Spring Boot admin service
- `admin/frontend`: admin frontend
- `user/backend`: Spring Boot user service
- `user/frontend`: user frontend

## Backend Run

Admin service:

```bash
cd admin/backend
mvn spring-boot:run
```

User service:

```bash
cd user/backend
mvn spring-boot:run
```

## Frontend Run

Admin frontend:

```bash
cd admin/frontend
npm install
npm run dev:h5
```

User frontend:

```bash
cd user/frontend
npm install
npm run dev:h5
```

## Database Migration

Backend modules use Flyway for database version management.
Migration scripts are stored under:

- `admin/backend/src/main/resources/db/migration`
- `user/backend/src/main/resources/db/migration`
