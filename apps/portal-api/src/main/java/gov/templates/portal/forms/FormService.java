package gov.templates.portal.forms;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.templates.portal.audit.AuditService;
import gov.templates.portal.common.ApiExceptions.FieldValidationException;
import gov.templates.portal.common.ApiExceptions.NotFoundException;
import gov.templates.portal.common.ReferenceNumberGenerator;
import gov.templates.portal.config.PortalProperties;
import gov.templates.portal.forms.FormDefinition.FieldDefinition;

@Service
public class FormService {

    static final String PREFIX = "FM";
    private static final int MAX_PAYLOAD_CHARS = 20_000;
    private static final String SUBJECT = "form_submission";

    private final PortalProperties properties;
    private final FormValidator validator;
    private final FormSubmissionRepository repository;
    private final AuditService audit;
    private final ReferenceNumberGenerator numbers;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public FormService(PortalProperties properties, FormValidator validator, FormSubmissionRepository repository,
            AuditService audit, ReferenceNumberGenerator numbers, ObjectMapper objectMapper, Clock clock) {
        this.properties = properties;
        this.validator = validator;
        this.repository = repository;
        this.audit = audit;
        this.numbers = numbers;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    public PublicForm definition(String formKey) {
        FormDefinition form = find(formKey);
        return new PublicForm(formKey, form.title(), form.description(), form.fields());
    }

    @Transactional
    public Receipt submit(String formKey, Map<String, Object> values, String honeypot) {
        FormDefinition form = find(formKey);
        if (honeypot != null && !honeypot.isBlank()) {
            throw new FieldValidationException(Map.of("website", "Leave this field empty."));
        }
        Map<String, String> errors = validator.validate(form, values);
        if (!errors.isEmpty()) {
            throw new FieldValidationException(errors);
        }

        String payload = toJson(validator.normalize(form, values));
        if (payload.length() > MAX_PAYLOAD_CHARS) {
            throw new FieldValidationException(Map.of("form", "This submission is too long. Shorten your answers."));
        }

        String reference = uniqueReference();
        Instant now = Instant.now(clock);
        repository.save(new FormSubmission(reference, formKey, payload, now));
        audit.record(AuditService.PUBLIC_ACTOR, "SUBMITTED", SUBJECT, reference, "form=" + formKey);
        return new Receipt(reference, now);
    }

    @Transactional(readOnly = true)
    public Page<StaffSubmission> list(String formKey, Pageable pageable) {
        find(formKey);
        return repository.findByFormKey(formKey, pageable)
                .map(s -> new StaffSubmission(s.getId(), s.getReferenceNumber(), s.getFormKey(), fromJson(s.getPayload()),
                        s.getSubmittedAt()));
    }

    private FormDefinition find(String formKey) {
        FormDefinition form = properties.forms().get(formKey);
        if (form == null) {
            throw new NotFoundException("We couldn't find that form.");
        }
        return form;
    }

    private String uniqueReference() {
        for (int attempt = 0; attempt < 5; attempt++) {
            String candidate = numbers.next(PREFIX);
            if (!repository.existsByReferenceNumber(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Could not create a unique reference number.");
    }

    private String toJson(Map<String, Object> values) {
        try {
            return objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not store form values.", e);
        }
    }

    private Map<String, Object> fromJson(String payload) {
        try {
            return objectMapper.readValue(payload, new TypeReference<Map<String, Object>>() { });
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Stored form values are unreadable.", e);
        }
    }

    public record PublicForm(String key, String title, String description, List<FieldDefinition> fields) {
    }

    public record Receipt(String referenceNumber, Instant submittedAt) {
    }

    public record StaffSubmission(UUID id, String referenceNumber, String formKey, Map<String, Object> values,
            Instant submittedAt) {
    }
}
