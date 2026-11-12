package gov.templates.portal.common;

import java.util.Map;

/** Exceptions that map to specific HTTP responses in ApiExceptionHandler. */
public final class ApiExceptions {

    private ApiExceptions() {
    }

    /** 404: the thing doesn't exist, or the caller isn't allowed to know it does. */
    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) {
            super(message);
        }
    }

    /** 400 with a message for each field that needs attention. */
    public static class FieldValidationException extends RuntimeException {
        private final Map<String, String> errors;

        public FieldValidationException(Map<String, String> errors) {
            super("Some fields need attention.");
            this.errors = Map.copyOf(errors);
        }

        public Map<String, String> errors() {
            return errors;
        }
    }

    /** 409: the requested change isn't allowed from the current state. */
    public static class ConflictException extends RuntimeException {
        public ConflictException(String message) {
            super(message);
        }
    }
}
