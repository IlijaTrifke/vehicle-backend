package com.meuhlbauer.vehicle_backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        Schema<?> problemDetailSchema = createProblemDetailSchema();
        ApiResponse badRequestResponse = createBadRequestResponse(problemDetailSchema);
        ApiResponse internalErrorResponse = createInternalErrorResponse(problemDetailSchema);
        ApiResponse notFoundResponse = createNotFoundResponse(problemDetailSchema);
        ApiResponse typeMismatchResponse = createTypeMismatchResponse(problemDetailSchema);
        ApiResponse validationErrorResponse = createValidationErrorResponse(problemDetailSchema);

        return new OpenAPI()
                .info(new Info()
                        .title("Vehicle Backend API")
                        .version("1.0.0")
                        .description(
                                """
                                        API documentation for Vehicle Backend application.
                                        
                                        ## Error Handling
                                        
                                        All error responses follow **RFC 7807 Problem Details** format (`application/problem+json`).
                                        
                                        ### Error Response Structure
                                        - `type` (URI): Problem type identifier
                                        - `title` (string): Short, human-readable summary
                                        - `status` (integer): HTTP status code
                                        - `detail` (string): Human-readable explanation
                                        - `instance` (URI): URI that identifies the specific occurrence of the problem
                                        - `code` (string): Application-specific error code (see Error Codes below)
                                        - `traceId` (string): Request correlation ID for logging/debugging
                                        - `errors` (object, optional): Field-specific validation errors (map: field → message)
                                        - `resource` (string, optional): Resource type (for 404 errors)
                                        - `resourceId` (string, optional): Resource identifier (for 404 errors)
                                        
                                        ### Error Codes
                                        
                                        | Code | Description | HTTP Status |
                                        |------|-------------|-------------|
                                        | `BAD_REQUEST` | Malformed request or invalid format | 400 |
                                        | `VALIDATION_ERROR` | Validation failed (body/query/path parameters) | 400 |
                                        | `TYPE_MISMATCH` | Invalid parameter type (e.g., string instead of number) | 400 |
                                        | `NOT_FOUND` | Resource not found | 404 |
                                        | `CONFLICT` | Resource conflict (e.g., duplicate) | 409 |
                                        | `UNAUTHORIZED` | Authentication required | 401 |
                                        | `FORBIDDEN` | Insufficient permissions | 403 |
                                        | `INTERNAL_ERROR` | Internal server error | 500 |
                                        
                                        ### Example Error Response
                                        
                                        ```json
                                        {
                                          "type": "about:blank",
                                          "title": "Bad Request",
                                          "status": 400,
                                          "detail": "Validation failed",
                                          "instance": "/api/vehicles",
                                          "code": "VALIDATION_ERROR",
                                          "traceId": "e7f2b6f8-2a3e-4b0b-9a4c-2d8e9d1c5a11",
                                          "errors": {
                                            "model": "must not be blank",
                                            "mileage": "must be greater than or equal to 0"
                                          }
                                        }
                                        ```
                                        """)
                        .contact(new Contact()
                                .name("Vehicle Backend Team")))
                .components(new Components()
                        .addSchemas("ProblemDetail", problemDetailSchema)
                        .addResponses("BadRequestProblem", badRequestResponse)
                        .addResponses("InternalErrorProblem", internalErrorResponse)
                        .addResponses("NotFoundProblem", notFoundResponse)
                        .addResponses("TypeMismatchProblem", typeMismatchResponse)
                        .addResponses("ValidationErrorProblem", validationErrorResponse));
    }

    private Schema<?> createProblemDetailSchema() {
        Schema<Object> problemDetail = new Schema<>();
        problemDetail.setType("object");
        problemDetail.setTitle("Problem Detail (RFC 7807)");
        problemDetail.setDescription("Standard error response format following RFC 7807");

        problemDetail.addProperty("type", new Schema<>().type("string").format("uri")
                .description("Problem type identifier (URI)")
                .example("about:blank"));
        problemDetail.addProperty("title", new Schema<>().type("string")
                .description("Short, human-readable summary")
                .example("Bad Request"));
        problemDetail.addProperty("status", new Schema<>().type("integer")
                .description("HTTP status code")
                .example(400));
        problemDetail.addProperty("detail", new Schema<>().type("string")
                .description("Human-readable explanation")
                .example("Validation failed"));
        problemDetail.addProperty("instance", new Schema<>().type("string").format("uri")
                .description("URI that identifies the specific occurrence")
                .example("/api/vehicles"));

        Schema<Object> errorsSchema = new Schema<>();
        errorsSchema.setType("object");
        errorsSchema.setAdditionalProperties(new Schema<>().type("string"));
        errorsSchema.setDescription("Field-specific validation errors (field name → error message)");

        Schema<Object> codeSchema = new Schema<>();
        codeSchema.setType("string");
        codeSchema.setDescription("Application-specific error code");
        codeSchema.setEnum(java.util.Arrays.asList("BAD_REQUEST", "VALIDATION_ERROR", "TYPE_MISMATCH",
                "NOT_FOUND", "CONFLICT", "UNAUTHORIZED", "FORBIDDEN", "INTERNAL_ERROR"));
        codeSchema.setExample("VALIDATION_ERROR");
        problemDetail.addProperty("code", codeSchema);
        problemDetail.addProperty("traceId", new Schema<>().type("string")
                .description("Request correlation ID for logging/debugging")
                .example("e7f2b6f8-2a3e-4b0b-9a4c-2d8e9d1c5a11"));
        problemDetail.addProperty("errors", errorsSchema);
        problemDetail.addProperty("resource", new Schema<>().type("string")
                .description("Resource type (for 404 errors)")
                .example("vehicle"));
        problemDetail.addProperty("resourceId", new Schema<>().type("string")
                .description("Resource identifier (for 404 errors)")
                .example("123"));

        return problemDetail;
    }

    private ApiResponse createBadRequestResponse(Schema<?> problemDetailSchema) {
        MediaType mediaType = new MediaType();
        mediaType.setSchema(problemDetailSchema);
        mediaType.setExample("""
                {
                  "type": "https://api.example.com/problems/400",
                  "title": "Bad Request",
                  "status": 400,
                  "detail": "Malformed JSON or invalid types",
                  "instance": "/api/vehicles",
                  "traceId": "abc123-def456-ghi789",
                  "code": "BAD_REQUEST"
                }
                """);

        ApiResponse response = new ApiResponse();
        response.setDescription("Bad Request - Malformed request or invalid format");
        response.setContent(new io.swagger.v3.oas.models.media.Content()
                .addMediaType("application/problem+json", mediaType));

        return response;
    }

    private ApiResponse createInternalErrorResponse(Schema<?> problemDetailSchema) {
        MediaType mediaType = new MediaType();
        mediaType.setSchema(problemDetailSchema);
        mediaType.setExample("""
                {
                  "type": "https://api.example.com/problems/500",
                  "title": "Internal Server Error",
                  "status": 500,
                  "detail": "Unexpected error",
                  "instance": "/api/vehicles",
                  "traceId": "abc123-def456-ghi789",
                  "code": "INTERNAL_ERROR"
                }
                """);

        ApiResponse response = new ApiResponse();
        response.setDescription("Unexpected server error");
        response.setContent(new io.swagger.v3.oas.models.media.Content()
                .addMediaType("application/problem+json", mediaType));

        return response;
    }

    private ApiResponse createNotFoundResponse(Schema<?> problemDetailSchema) {
        MediaType mediaType = new MediaType();
        mediaType.setSchema(problemDetailSchema);
        mediaType.setExample("""
                {
                  "type": "https://api.example.com/problems/404",
                  "title": "Not Found",
                  "status": 404,
                  "detail": "Resource not found",
                  "instance": "/api/vehicles/123",
                  "traceId": "9d3bbece-297f-470b-9476-c947885f9558",
                  "code": "NOT_FOUND",
                  "resource": "vehicle",
                  "resourceId": "123"
                }
                """);

        ApiResponse response = new ApiResponse();
        response.setDescription("Resource not found");
        response.setContent(new io.swagger.v3.oas.models.media.Content()
                .addMediaType("application/problem+json", mediaType));

        return response;
    }

    private ApiResponse createTypeMismatchResponse(Schema<?> problemDetailSchema) {
        MediaType mediaType = new MediaType();
        mediaType.setSchema(problemDetailSchema);
        mediaType.setExample("""
                {
                  "type": "https://api.example.com/problems/type-mismatch",
                  "title": "Bad Request",
                  "status": 400,
                  "detail": "Invalid parameter type",
                  "instance": "/api/vehicles/abc",
                  "traceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                  "code": "TYPE_MISMATCH",
                  "errors": {
                    "id": "Expected type: Long"
                  }
                }
                """);

        ApiResponse response = new ApiResponse();
        response.setDescription("Invalid parameter type (e.g., string instead of numeric ID)");
        response.setContent(new io.swagger.v3.oas.models.media.Content()
                .addMediaType("application/problem+json", mediaType));

        return response;
    }

    private ApiResponse createValidationErrorResponse(Schema<?> problemDetailSchema) {
        MediaType mediaType = new MediaType();
        mediaType.setSchema(problemDetailSchema);
        mediaType.setExample("""
                {
                  "type": "https://api.example.com/problems/400",
                  "title": "Bad Request",
                  "status": 400,
                  "detail": "Validation failed",
                  "instance": "/api/vehicles",
                  "traceId": "e7f2b6f8-2a3e-4b0b-9a4c-2d8e9d1c5a11",
                  "code": "VALIDATION_ERROR",
                  "errors": {
                    "model": "must not be blank",
                    "firstRegistrationYear": "First registration year must have 4 digits",
                    "cubicCapacity": "must be greater than 0",
                    "fuel": "must not be null",
                    "mileage": "must be greater than or equal to 0"
                  }
                }
                """);

        ApiResponse response = new ApiResponse();
        response.setDescription("Bad Request - Validation errors");
        response.setContent(new io.swagger.v3.oas.models.media.Content()
                .addMediaType("application/problem+json", mediaType));

        return response;
    }
}
