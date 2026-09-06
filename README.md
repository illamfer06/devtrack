# DevTrack

DevTrack is a backend application for organizing programming practice and tracking coding problems.

The application organizes coding problems into **study blocks**, allowing users to group problems by topic or learning area. Each problem belongs to a study block and can be classified by difficulty and algorithm, marked as solved, annotated with notes, and retrieved using filtering, pagination and sorting.

The project is focused on learning and applying backend development concepts using Spring Boot and PostgreSQL.


## Features

### Study Blocks
- Create study blocks
- Get a study block by ID
- Update existing study blocks
- Delete study blocks
- Filter study blocks by active status
- Paginated study block retrieval
- Sorting by study block fields
- Automatic creation and update timestamps

### Problems
- Create problems inside a study block
- Get problems belonging to a study block
- Get a specific problem by ID within its study block
- Update existing problems
- Delete problems
- Filter problems by difficulty
- Filter problems by solved status
- Combine multiple filters
- Paginated problem retrieval
- Sorting by problem fields
- Automatic creation and update timestamps

### Backend
- Request validation
- Global exception handling
- PostgreSQL persistence
- Service layer unit tests
- Controller tests
- OpenAPI / Swagger documentation
- Continuous Integration with GitHub Actions

## Tech Stack
- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- PostgreSQL
- Hibernate
- Bean Validation
- OpenAPI / Swagger
- JUnit 5
- Mockito
- MockMvc
- Maven
- GitHub Actions

## API Endpoints

### Study Blocks

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/study-blocks` | Get study blocks with optional filtering, pagination and sorting |
| GET | `/study-blocks/{studyBlockId}` | Get a study block by ID |
| POST | `/study-blocks` | Create a new study block |
| PUT | `/study-blocks/{studyBlockId}` | Update an existing study block |
| DELETE | `/study-blocks/{studyBlockId}` | Delete a study block |

### Problems

Problems are accessed through the study block they belong to.

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/study-blocks/{studyBlockId}/problems` | Get problems from a study block with optional filtering, pagination and sorting |
| GET | `/study-blocks/{studyBlockId}/problems/{problemId}` | Get a problem by ID |
| POST | `/study-blocks/{studyBlockId}/problems` | Create a problem inside a study block |
| PUT | `/study-blocks/{studyBlockId}/problems/{problemId}` | Update a problem |
| DELETE | `/study-blocks/{studyBlockId}/problems/{problemId}` | Delete a problem |

### Health

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/health` | Check the application health status |

## Difficulty Values

- `EASY`
- `MEDIUM`
- `HARD`

## Filtering and Pagination

### Study Blocks

The `/study-blocks` endpoint supports:

| Parameter | Example | Description |
|-----------|---------|-------------|
| `active` | `true` | Filter by active status |
| `page` | `0` | Page number (zero-based) |
| `size` | `10` | Number of elements per page |
| `sort` | `id,desc` | Sort field and direction |

### Problems

The `/study-blocks/{studyBlockId}/problems` endpoint supports optional query parameters:

| Parameter | Example | Description |
|-----------|---------|-------------|
| `difficulty` | `EASY` | Filter by problem difficulty |
| `solved` | `true` | Filter by solved status |
| `page` | `0` | Page number (zero-based) |
| `size` | `10` | Number of elements per page |
| `sort` | `id,desc` | Sort field and direction |


## API Examples

### Create a study block

```http
POST /study-blocks
Content-Type: application/json

{
    "title": "Algorithms",
    "active": true
}
```

### Create a problem inside a study block

```http
POST /study-blocks/1/problems
Content-Type: application/json

{
    "title": "Two Sum",
    "difficulty": "EASY",
    "algorithm": "Hash Map",
    "solved": true,
    "notes": "Review the O(n) solution",
    "url": "https://leetcode.com/problems/two-sum/"
}
```

### Filter, paginate and sort problems
```http
GET /study-blocks/1/problems?difficulty=EASY&solved=false&page=0&size=10&sort=id,desc
```
sponse

### Example Problem Response

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
````
## API Documentation

The API is documented using OpenAPI and Swagger UI.

When the application is running locally, the interactive documentation is available at:

```text
http://localhost:8080/swagger-ui/index.html
```

The generated OpenAPI specification is available at:

```text
http://localhost:8080/v3/api-docs
```

Swagger UI can be used to explore the available endpoints, inspect request and response schemas, and execute API requests directly from the browser.

## Testing

The project includes:

- Unit tests for the service layer using JUnit 5 and Mockito
- Controller tests using MockMvc
- Tests for validation and exception handling
- Tests for filtering, pagination and sorting
- Tests for Study Block and Problem CRUD operations
- Tests verifying that problems are accessed through their corresponding study blocks

## Continuous Integration

GitHub Actions is configured to automatically run the Maven test suite on pushes and pull requests to the `main` branch.

This ensures that changes are automatically validated before being integrated into the project.

## Project Status

The core Study Block and Problem management API is complete.

Current functionality includes:

- Study Block CRUD operations
- Problem CRUD operations scoped to Study Blocks
- Validation and global exception handling
- Filtering, pagination and sorting
- Automatic timestamps
- PostgreSQL persistence
- Automated service and controller tests
- Continuous Integration with GitHub Actions
- OpenAPI / Swagger documentation

The next major development phase will focus on the **study and review system**, including review history, scheduling and automatic calculation of future review dates.

Future phases will include:

- Study and spaced-review system
- User registration and authentication
- Database migrations
- Docker
- Deployment
- Frontend application