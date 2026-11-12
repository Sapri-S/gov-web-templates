package gov.templates.portal.common;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import gov.templates.portal.common.ApiExceptions.ConflictException;
import gov.templates.portal.common.ApiExceptions.FieldValidationException;
import gov.templates.portal.common.ApiExceptions.NotFoundException;

/**
 * Turns errors into RFC 9457 problem responses. Field errors are returned as
 * {"errors": {"field": "message"}} so any frontend can show them next to the field.
 * Stack traces and internal messages are never returned.
 */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(fieldProblem(errors));
    }

    @ExceptionHandler(FieldValidationException.class)
    ResponseEntity<ProblemDetail> handleFieldValidation(FieldValidationException ex) {
        return ResponseEntity.badRequest().body(fieldProblem(ex.errors()));
    }

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ProblemDetail> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    @ExceptionHandler(ConflictException.class)
    ResponseEntity<ProblemDetail> handleConflict(ConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage()));
    }

    /** Unknown sort fields in ?sort=... are a client mistake, not a server error. */
    @ExceptionHandler(PropertyReferenceException.class)
    ResponseEntity<ProblemDetail> handleBadSort(PropertyReferenceException ex) {
        return ResponseEntity.badRequest()
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "That sort field isn't supported."));
    }

    private static ProblemDetail fieldProblem(Map<String, String> errors) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Some fields need attention.");
        problem.setProperty("errors", errors);
        return problem;
    }
}
