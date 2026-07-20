The Vaping Duty Stamps - Approval Status Check API uses standard HTTP status codes to indicate the outcome of a request.

Status codes are returned containing additional error information, where applicable.

Refer to the [API documentation reference guide](https://developer.service.hmrc.gov.uk/api-documentation/docs/reference-guide#errors) for general error-specific information.

The HTTP status code-specific information is detailed in the following sub-sections:
  - [success responses](#success-responses)
  - [client error responses](#client-error-responses)
  - [server error responses](#server-error-responses)

### Success responses

The status codes and corresponding replies for a successful response are:
  - 200 to 2xx, except 202 - The request was successfully processed and approval status was returned
  - 202 - The request was accepted for processing and additional processing may be required

### Client error responses

Status codes in the 400 to 4xx range indicate that the server cannot process the request due to an error on the client application.

### Server error responses

Status codes in the 500 to 5xx range indicate that the server cannot process the request due to an unexpected error on the server application.


Additional endpoint-specific errors, validation rules, and error response structures are compiled in the Vaping Duty Stamps - Approval Status Check API (1.0) OAS document.
