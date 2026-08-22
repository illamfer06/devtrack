# DevTrack

DevTrack is a backend application for tracking coding problems and organizing programming practice.

It allows users to store coding problems, classify them by difficulty and algorithm, track their solved status, add review notes, and retrieve them using filtering, pagination and sorting.

The project is focused on learning and applying backend development concepts using Spring Boot and PostgreSQL.


## Features
- Create coding problems
- Get a problem by ID
- Update existing problems
- Delete problems
- Filter problems by difficulty
- Filter problems by solved status
- Combine multiple filters
- Paginated problem retrieval
- Sorting by problem fields
- Automatic creation and update timestamps
- Request validation
- Global exception handling
- PostgreSQL persistence
- Service layer unit tests
- Controller tests


## Tech Stack
- Java
- Spring Boot
- Spring Web
- Spring Data JPA
- PostgreSQL
- Hibernate
- Bean Validation
- JUnit 5
- Mockito
- MockMvc
- Maven


## API Endpoints
| Method | Endpoint | Description                                                   |
|--------|----------|---------------------------------------------------------------|
| GET | `/problems` | Get problems with optional filtering, pagination and sorting  |
| GET | `/problems/{id}` | Get a problem by ID                                           |
| POST | `/problems` | Create a new problem                                          |
| PUT | `/problems/{id}` | Update an existing problem                                    |
| DELETE | `/problems/{id}` | Delete a problem                                              |

## Difficulty Values

- `EASY`
- `MEDIUM`
- `HARD`

## Filtering and Pagination

The `/problems` endpoint supports optional query parameters:

| Parameter | Example | Description |
|-----------|---------|-------------|
| `difficulty` | `EASY` | Filter by problem difficulty |
| `solved` | `true` | Filter by solved status |
| `page` | `0` | Page number (zero-based) |
| `size` | `10` | Number of elements per page |
| `sort` | `id,desc` | Sort field and direction |


## API Examples

### Filter, paginate and sort problems
```http
GET /problems?difficulty=EASY&solved=false&page=0&size=10&sort=id,desc
```
### Create a problem

```http
POST /problems
Content-Type: application/json
Accept: application/json

{
    "title": "Two Sum",
    "difficulty": "EASY",
    "algorithm": "Hash Map",
    "solved": true,
    "notes": "Review the O(n) solution",
    "url": "https://leetcode.com/problems/two-sum/"
}
```

### Example Response

```json
{
  "id": 1,
  "title": "Two Sum",
  "difficulty": "EASY",
  "algorithm": "Hash Map",
  "solved": true,
  "notes": "Review the O(n) solution",
  "url": "https://leetcode.com/problems/two-sum/",
  "createdAt": "2026-08-22T18:00:00",
  "updatedAt": "2026-08-22T18:00:00"
}
```
## Testing

The project includes:

- Unit tests for the service layer using JUnit 5 and Mockito
- Controller tests using MockMvc
- Tests for validation and exception handling
- Tests for filtering, pagination and sorting

## Project Status

The core problem management API is complete.

Current functionality includes CRUD operations, validation, exception handling, filtering, pagination, sorting, timestamps and automated service/controller tests.

The next major development phase will focus on the study and review system, including review history, scheduling and automatic calculation of future review dates.

Future phases will include:

- Study and spaced-review system
- User registration and authentication
- API documentation with OpenAPI / Swagger
- Database migrations
- Docker
- AWS deployment
- Frontend application