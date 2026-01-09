# API Documentation

## Overview

The EXAM System API provides REST endpoints for managing reports. The API follows RESTful principles and uses JSON for request/response payloads.

## Base URL

- **Local Development**: `http://localhost:9000`
- **Production**: `https://api.example.com` (configure as needed)

## OpenAPI Specification

The complete API specification is available in [OpenAPI 3.0 format](openapi.yaml).

### Viewing the API Spec

You can view and interact with the API specification using:

1. **Swagger Editor**: https://editor.swagger.io/
   - Copy the contents of `openapi.yaml` into the editor

2. **Swagger UI** (if deployed):
   - Visit `/api-docs` endpoint (if configured)

3. **Postman/Insomnia**: Import the `openapi.yaml` file

## Authentication

### Production
- Uses Shibboleth/HAKA authentication
- Authentication headers are handled by the reverse proxy

### Development
- Dev auth mode can be enabled (see [QUICKSTART.md](../QUICKSTART.md))
- Set `DEV_AUTH_ENABLED=true` in environment variables
- Mock user headers are injected automatically

## Endpoints

### Reports API

#### List Reports
```
GET /api/v1/reports?page=1&pageSize=20&filters=searchterm
```

**Query Parameters:**
- `page` (optional): Page number, default: 1
- `pageSize` (optional): Items per page (max 100), default: 20
- `filters` (optional): Filter by title or content (case-insensitive partial match)

**Response:**
```json
{
  "data": [
    {
      "id": 1,
      "title": "Q1 2024 Report",
      "content": "First quarter results...",
      "createdAt": "2024-01-15T10:30:00Z"
    }
  ],
  "pagination": {
    "page": 1,
    "pageSize": 20,
    "total": 42,
    "totalPages": 3
  }
}
```

#### Get Report by ID
```
GET /api/v1/reports/{id}
```

**Response:**
```json
{
  "id": 1,
  "title": "Q1 2024 Report",
  "content": "First quarter results...",
  "createdAt": "2024-01-15T10:30:00Z"
}
```

#### Create Report
```
POST /api/v1/reports
Content-Type: application/json

{
  "title": "Q2 2024 Report",
  "content": "Second quarter analysis..."
}
```

**Response:** 201 Created
```json
{
  "id": 2,
  "title": "Q2 2024 Report",
  "content": "Second quarter analysis...",
  "createdAt": "2024-04-15T14:20:00Z"
}
```

**Headers:**
- `Location: /api/v1/reports/2`

#### Update Report
```
PUT /api/v1/reports/{id}
Content-Type: application/json

{
  "title": "Q1 2024 Report (Updated)",
  "content": "Updated first quarter results..."
}
```

**Note:** This operation is idempotent - multiple identical requests have the same effect.

**Response:** 200 OK
```json
{
  "id": 1,
  "title": "Q1 2024 Report (Updated)",
  "content": "Updated first quarter results...",
  "createdAt": "2024-01-15T10:30:00Z"
}
```

#### Delete Report
```
DELETE /api/v1/reports/{id}
```

**Note:** This operation is idempotent - deleting a non-existent resource returns 404 but is safe to retry.

**Response:** 204 No Content

## Error Responses

All errors follow a consistent format:

```json
{
  "error": "ERROR_CODE",
  "message": "Human-readable error message",
  "details": "Additional details (optional)"
}
```

### Error Codes

- `VALIDATION_ERROR`: Request body validation failed
- `NOT_FOUND`: Resource not found
- `INTERNAL_ERROR`: Server error

### HTTP Status Codes

- `200 OK`: Successful GET or PUT request
- `201 Created`: Successful POST request
- `204 No Content`: Successful DELETE request
- `400 Bad Request`: Validation error
- `404 Not Found`: Resource not found
- `500 Internal Server Error`: Server error

## Pagination

List endpoints support pagination:

- **Page numbering**: 1-based (first page is 1)
- **Page size limits**: Minimum 1, maximum 100
- **Default values**: page=1, pageSize=20

The pagination response includes:
- `page`: Current page number
- `pageSize`: Items per page
- `total`: Total number of items
- `totalPages`: Total number of pages

## Filtering

The `filters` query parameter performs a case-insensitive partial match on:
- Report title
- Report content

Example: `?filters=quarterly` will match reports containing "quarterly" in either title or content.

## Idempotency

- **PUT operations**: Idempotent - multiple identical requests have the same effect
- **DELETE operations**: Idempotent - safe to retry even if resource doesn't exist

## Examples

### cURL Examples

**List reports:**
```bash
curl -X GET "http://localhost:9000/api/v1/reports?page=1&pageSize=10"
```

**Get a report:**
```bash
curl -X GET "http://localhost:9000/api/v1/reports/1"
```

**Create a report:**
```bash
curl -X POST "http://localhost:9000/api/v1/reports" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Monthly Report",
    "content": "This month we achieved..."
  }'
```

**Update a report:**
```bash
curl -X PUT "http://localhost:9000/api/v1/reports/1" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Updated Monthly Report",
    "content": "Updated content..."
  }'
```

**Delete a report:**
```bash
curl -X DELETE "http://localhost:9000/api/v1/reports/1"
```

### JavaScript/TypeScript Example

```javascript
// List reports
const response = await fetch('http://localhost:9000/api/v1/reports?page=1&pageSize=20');
const data = await response.json();
console.log(data.data); // Array of reports
console.log(data.pagination); // Pagination info

// Create a report
const newReport = await fetch('http://localhost:9000/api/v1/reports', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    title: 'New Report',
    content: 'Report content...'
  })
});
const created = await newReport.json();
console.log(created);
```

## Rate Limiting

Rate limiting may be applied in production. Check response headers for rate limit information:
- `X-RateLimit-Limit`: Maximum requests per time window
- `X-RateLimit-Remaining`: Remaining requests in current window
- `X-RateLimit-Reset`: Time when the rate limit resets

## Versioning

The API is versioned using URL paths: `/api/v1/...`

Future versions will use `/api/v2/`, etc. The current version is v1.

## Support

For API support or questions:
- Check the [OpenAPI specification](openapi.yaml) for detailed schema information
- Review [DEVELOPMENT.md](../DEVELOPMENT.md) for development guidelines
- Contact: support@example.com
