package gov.templates.portal.audit;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Append-only record of who did what, kept as evidence for audit logging controls. */
@Entity
@Table(name = "audit_event")
public class AuditEvent {

    @Id
    private UUID id;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(nullable = false, length = 200)
    private String actor;

    @Column(nullable = false, length = 60)
    private String action;

    @Column(name = "subject_type", nullable = false, length = 60)
    private String subjectType;

    @Column(name = "subject_id", nullable = false, length = 60)
    private String subjectId;

    @Column(length = 2000)
    private String detail;

    protected AuditEvent() {
    }

    AuditEvent(Instant occurredAt, String actor, String action, String subjectType, String subjectId, String detail) {
        this.id = UUID.randomUUID();
        this.occurredAt = occurredAt;
        this.actor = actor;
        this.action = action;
        this.subjectType = subjectType;
        this.subjectId = subjectId;
        this.detail = detail;
    }

    public UUID getId() {
        return id;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public String getActor() {
        return actor;
    }

    public String getAction() {
        return action;
    }

    public String getSubjectType() {
        return subjectType;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public String getDetail() {
        return detail;
    }
}
