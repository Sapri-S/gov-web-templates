package gov.templates.portal.forms;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import gov.templates.portal.forms.FormDefinition.FieldDefinition;

/** Checks submitted values against a form definition. Messages follow plain-language style. */
@Component
public class FormValidator {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern PHONE = Pattern.compile("^[0-9+()\\-. ]{7,30}$");

    public Map<String, String> validate(FormDefinition form, Map<String, Object> values) {
        Map<String, Object> input = values == null ? Map.of() : values;
        Map<String, String> errors = new LinkedHashMap<>();

        Set<String> known = form.fields().stream().map(FieldDefinition::name).collect(Collectors.toSet());
        for (String key : input.keySet()) {
            if (!known.contains(key)) {
                errors.put(key, "This field isn't part of the form.");
            }
        }

        for (FieldDefinition field : form.fields()) {
            Object raw = input.get(field.name());

            if (field.type() == FieldType.CHECKBOX) {
                if (raw != null && !(raw instanceof Boolean)) {
                    errors.put(field.name(), "Check the box or leave it unchecked.");
                } else if (field.required() && !Boolean.TRUE.equals(raw)) {
                    errors.put(field.name(), "Check this box to continue.");
                }
                continue;
            }

            if (raw != null && !(raw instanceof String)) {
                errors.put(field.name(), "Enter text for " + lower(field.label()) + ".");
                continue;
            }
            String value = raw == null ? "" : ((String) raw).trim();
            if (value.isEmpty()) {
                if (field.required()) {
                    errors.put(field.name(), "Enter " + lower(field.label()) + ".");
                }
                continue;
            }
            if (value.length() > field.maxLength()) {
                errors.put(field.name(), "Use " + field.maxLength() + " characters or fewer.");
                continue;
            }
            switch (field.type()) {
                case EMAIL -> {
                    if (!EMAIL.matcher(value).matches()) {
                        errors.put(field.name(), "Enter an email address like name@example.com.");
                    }
                }
                case PHONE -> {
                    if (!PHONE.matcher(value).matches()) {
                        errors.put(field.name(), "Enter a phone number like 301-555-0100.");
                    }
                }
                case SELECT -> {
                    if (!field.options().contains(value)) {
                        errors.put(field.name(), "Choose an option from the list.");
                    }
                }
                default -> {
                    // TEXT and TEXTAREA only need the length check above.
                }
            }
        }
        return errors;
    }

    /** Keeps only known fields, with text trimmed. Call after validate() passes. */
    public Map<String, Object> normalize(FormDefinition form, Map<String, Object> values) {
        Map<String, Object> input = values == null ? Map.of() : values;
        Map<String, Object> clean = new LinkedHashMap<>();
        for (FieldDefinition field : form.fields()) {
            Object raw = input.get(field.name());
            if (raw instanceof String text) {
                String trimmed = text.trim();
                if (!trimmed.isEmpty()) {
                    clean.put(field.name(), trimmed);
                }
            } else if (raw instanceof Boolean flag) {
                clean.put(field.name(), flag);
            }
        }
        return clean;
    }

    private static String lower(String label) {
        return label.toLowerCase(Locale.ROOT);
    }
}
