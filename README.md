# DevTrack

Backend application to track coding problems, algorithms, difficulty, solved status and review notes.

The project is focused on learning and applying backend development concepts using Spring Boot and PostgreSQL.


## Features
- Create coding problems
- Get all problems
- Get a problem by ID
- Update existing problems
- Delete problems
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
- Bean Validation
- JUnit 5
- Mockito
- MockMvc
- Maven


## API Endpoints
| Method | Endpoint | Description          |
|--------|----------|----------------------|
| GET | `/problems` | Get all problems     |
| GET | `/problems/{id}` | Get a problem by ID  |
| POST | `/problems` | Create a new problem |
| PUT | `/problems/{id}` | Update an existing problem |
| DELETE | `/problems/{id}` | Delete a problem |


## API Example

### Create a problem

```http
POST /problems
Content-Type: application/json
Accept: application/json

{
    "title": "Two Sum",
    "difficulty": "Easy",
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
    "difficulty": "Easy",
    "algorithm": "Hash Map",
    "solved": true,
    "notes": "Review the O(n) solution",
    "url": "https://leetcode.com/problems/two-sum/"
}
```

## Project Status

The basic CRUD API is complete and covered with service and controller tests.

Next steps include adding filters, pagination and more advanced backend features.
