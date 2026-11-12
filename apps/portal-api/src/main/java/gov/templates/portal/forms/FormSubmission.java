package gov.templates.portal.forms;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "form_submission")
public class FormSubmission {

    @Id
    private UUID id;

    @Column(name = "reference_number", nullable = false, unique = true, length = 20)
    private String referenceNumber;

    @Column(name = "form_key", nullable = false, length = 60)
    private String formKey;

    /** Submitted values as JSON. Size is capped in FormService. */
    @Column(nullable = false, length = 20000)
    private String payload;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    protected FormSubmission() {
    }

    FormSubmission(String referenceNumber, String formKey, String payload, Instant submittedAt) {
        this.id = UUID.randomUUID();
        this.referenceNumber = referenceNumber;
        this.formKey = formKey;
        this.payload = payload;
        this.submittedAt = submittedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public String getFormKey() {
        return formKey;
    }

    public String getPayload() {
        return payload;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }
}
