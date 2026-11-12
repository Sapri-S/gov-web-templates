package gov.templates.portal.forms;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import gov.templates.portal.forms.FormDefinition.FieldDefinition;

class FormValidatorTest {

    private final FormValidator validator = new FormValidator();

    private final FormDefinition form = new FormDefinition("Test", null, List.of(
            new FieldDefinition("name", "Name", null, FieldType.TEXT, true, 10, null),
            new FieldDefinition("email", "Email", null, FieldType.EMAIL, false, null, null),
            new FieldDefinition("phone", "Phone", null, FieldType.PHONE, false, null, null),
            new FieldDefinition("size", "Size", null, FieldType.SELECT, false, null, List.of("Small", "Large")),
            new FieldDefinition("agree", "Agreement", null, FieldType.CHECKBOX, true, null, null)));

    @Test
    void acceptsValidValues() {
        Map<String, String> errors = validator.validate(form,
                Map.of("name", "Pat", "email", "pat@example.com", "phone", "(301) 555-0100", "size", "Small", "agree", true));
        assertThat(errors).isEmpty();
    }

    @Test
    void reportsEachProblemOnItsField() {
        Map<String, String> errors = validator.validate(form,
                Map.of("name", "Patricia Resident", "email", "nope", "phone", "abc", "size", "Huge", "agree", false));
        assertThat(errors).containsOnlyKeys("name", "email", "phone", "size", "agree");
    }

    @Test
    void requiredTextCannotBeBlank() {
        assertThat(validator.validate(form, Map.of("name", "   ", "agree", true))).containsKey("name");
    }

    @Test
    void rejectsWrongValueTypes() {
        assertThat(validator.validate(form, Map.of("name", 42, "agree", "yes"))).containsKeys("name", "agree");
    }

    @Test
    void normalizeTrimsAndDropsUnknownAndEmpty() {
        Map<String, Object> clean = validator.normalize(form, Map.of("name", "  Pat ", "email", "", "extra", "x", "agree", true));
        assertThat(clean).containsExactly(Map.entry("name", "Pat"), Map.entry("agree", true));
    }

    @Test
    void definitionFillsDefaults() {
        FieldDefinition field = new FieldDefinition("notes", null, null, null, false, null, null);
        assertThat(field.label()).isEqualTo("notes");
        assertThat(field.type()).isEqualTo(FieldType.TEXT);
        assertThat(field.maxLength()).isEqualTo(FieldType.TEXT.defaultMaxLength());
    }
}
