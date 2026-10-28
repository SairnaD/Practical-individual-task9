# Practical Task: Library Book Tracker API

Starter project for Data Architecture practical task: Build a REST API for managing books in a library using Spring Boot, Spring Data JPA and an H2 database.

## Task
Complete all of the 'TODO' portions of the code. The finished application should compile and all of the controller endpoints should be functional.

## Behavioural Requirements
- Books can be created
- All books can be retrieved
- A single book can be retrieved by ID
- Books can be deleted
- Books can be filtered and searched by their fields.
- Controller delegates to Service, which delegates to Repository
- DTOs are used for requests and responses

## Stretch goals
- Request validation
- Custom exception handling
- Searching by partial title
- Add 'borrow' and 'return' endpoints along with the new 'borrowedStatus' field to the Book entity

## Book state fields

- `available` — `true` if the book can be borrowed right now.
  `GET /api/books/available` returns only such books.
- `borrowedStatus` — `true` if the book is currently borrowed.
- The two fields change only through `POST /api/books/{id}/borrow` and
  `POST /api/books/{id}/return`. `PUT /api/books/{id}` updates only
  title, author, genre and publication year.

## Security

### Users (stored in H2, passwords hashed with BCrypt)

| Username | Password | Role  |
|----------|----------|-------|
| user     | user     | USER  |
| admin    | admin    | ADMIN |

Demo credentials only. They are created on startup by `DataInitializer`.

### REST API (`/api/**`): Basic Auth, stateless, CSRF disabled

- GET: any authenticated user
- POST, PUT, DELETE: ADMIN only
- No credentials: 401, authenticated without the role: 403

### Browser UI: form login, session, CSRF enabled

- `/login` is public, everything else requires login
- Page `/books`: USER sees the list, ADMIN can also add and delete
- Forms include the CSRF token automatically (`th:action`)
- A POST without the CSRF token is rejected with 403

### Outbound API call

`GET /api/external/status` calls an external service with `Authorization: Bearer <token>`.

The token is NOT in the source code. It is read from the environment variable:

    EXTERNAL_API_TOKEN=your-token

Optional overrides: `EXTERNAL_API_BASE_URL`, `EXTERNAL_API_PATH`.

| Situation                         | Response |
|-----------------------------------|----------|
| Token not configured              | 503      |
| External API rejects credentials  | 502      |
| External API returns another error| 502      |
| External API unreachable/timeout  | 504      |

### Tests

    mvn clean test

Covers 401, USER read (200), USER write (403), ADMIN write (2xx),
UI redirect to login, missing CSRF token (403) and external API failures.