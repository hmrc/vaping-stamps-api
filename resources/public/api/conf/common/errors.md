The Vaping Duty Stamps - Approval Status Check API uses standard HTTP status codes to indicate the outcome of a request.

Status codes are returned containing additional error information, where applicable.

Refer to the [API documentation reference guide](https://developer.service.hmrc.gov.uk/api-documentation/docs/reference-guide#errors) for general error-specific information.

The HTTP status code-specific information is detailed in the following sub-sections:
  - [success responses](#success-responses)
  - [client error responses](#client-error-responses)
  - [server error responses](#server-error-responses)

### Success responses

| Status code                                     | Description                                                                         |
|-------------------------------------------------|-------------------------------------------------------------------------------------|
| 200 to 2xx, except 202 - Success                | The request failed because of a client error by your application.                   |
| 202 Accepted but requires additional processing | The request was accepted for processing, and additional processing may be required. |

### Client error responses

| Status code                           | Description                                                      |
|---------------------------------------|------------------------------------------------------------------|
| 400 to 4xx Failed due to client error | The request failed because of a client error by your application |

### Server error responses

| Status code                      | Description                                                |
|----------------------------------|------------------------------------------------------------|
| 500 to 5xx Internal server error | An unexpected error occurred while processing the request. |


Additional endpoint-specific errors, validation rules, and error response structures are compiled in the Vaping Duty Stamps - Approval Status Check API (1.0) OAS document.
