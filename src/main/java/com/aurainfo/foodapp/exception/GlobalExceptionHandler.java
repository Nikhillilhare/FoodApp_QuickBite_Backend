package com.aurainfo.foodapp.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

/**
 * ONE class, sitting outside every controller, that catches exceptions
 * thrown anywhere in the service layer and converts them into the standard
 * ApiErrorResponse shape before they ever reach the frontend.
 *
 * @RestControllerAdvice = "this class watches every @RestController in the
 * project" — you never have to write try/catch in individual controllers.
 * A controller method can just call a service and let exceptions bubble up;
 * this class is what turns them into a clean HTTP response.
 *
 * Each @ExceptionHandler method below says: "if this specific exception
 * type is thrown anywhere, run this method to build the response."
 * Spring picks the most specific match automatically.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Used to print the REAL error (with full stack trace) to the server's
    // own console/log file — for you to debug. This is completely separate
    // from what the CLIENT sees, which is always the short, safe `message`.
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Thrown all over the service layer for things like:
     *   "Product not found with id: 5"
     *   "Category name is required"
     *   "Order does not belong to this customer"
     * Mapped to 400 Bad Request — the request itself was invalid
     * (a bad ID, missing required data, or acting on something that isn't
     * theirs to act on).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, request);
    }

    /**
     * Thrown for things like:
     *   "Insufficient stock for product: Paneer Tikka"
     *   "Delivered order cannot be cancelled"
     *   "Order is already cancelled"
     * These aren't about bad input — the request is well-formed, but the
     * ACTION conflicts with the current state of the data. 409 Conflict is
     * the standard HTTP status for exactly this situation.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalState(
            IllegalStateException ex,
            HttpServletRequest request
    ) {
        return buildResponse(ex.getMessage(), HttpStatus.CONFLICT, request);
    }

    /**
     * Thrown automatically by Spring when a @RequestBody DTO annotated with
     * @Valid fails validation (e.g. @NotBlank, @Email, @Size in a future
     * SignupRequest DTO for the Auth module). Spring's default message for
     * this is a huge, deeply nested JSON blob that is NOT frontend-friendly
     * — this handler flattens it down to just the first field error's
     * message, e.g. "email: must be a well-formed email address".
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .orElse("Validation failed");

        return buildResponse(message, HttpStatus.BAD_REQUEST, request);
    }

    /**
     * The safety net. Catches anything NOT already handled above — a null
     * pointer slipping through, a database connectivity blip, a bug nobody
     * anticipated. This is deliberately the LAST resort.
     *
     * Critically: the client NEVER sees the real exception message or stack
     * trace here (that could leak internal details — table names, package
     * structure, etc. — which is a real security concern). The client only
     * ever gets a safe, generic message. The real exception is logged
     * server-side via `log.error(...)` so you can still debug it.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Unhandled exception at [{}]", request.getRequestURI(), ex);

        return buildResponse(
                "Something went wrong. Please try again later.",
                HttpStatus.INTERNAL_SERVER_ERROR,
                request
        );
    }

    // Shared by every handler above — this is the one place that actually
    // assembles an ApiErrorResponse, so all of them stay perfectly consistent.
    private ResponseEntity<ApiErrorResponse> buildResponse(
            String message,
            HttpStatus status,
            HttpServletRequest request
    ) {
        ApiErrorResponse body = ApiErrorResponse.builder()
                .success(false)
                .message(message)
                .status(status.value())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(status).body(body);
    }
}
