package gov.templates.portal.forms;

import java.util.List;

/**
 * A form defined in configuration (application.yml), not code. Adding a client
 * form means adding YAML; the frontend renders whatever this describes.
 */
public record FormDefinition(String title, String description, List<FieldDefinition> fields) {

    public FormDefinition {
        title = title == null ? "" : title;
        description = description == null ? "" : description;
        fields = fields == null ? List.of() : List.copyOf(fields);
    }

    public record FieldDefinition(
            String name,
            String label,
            String hint,
            FieldType type,
            boolean required,
            Integer maxLength,
            List<String> options) {

        public FieldDefinition {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Every form field needs a name.");
            }
            label = label == null || label.isBlank() ? name : label;
            hint = hint == null ? "" : hint;
            type = type == null ? FieldType.TEXT : type;
            maxLength = maxLength == null ? type.defaultMaxLength() : maxLength;
            options = options == null ? List.of() : List.copyOf(options);
        }
    }
}
