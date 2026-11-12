package gov.templates.portal.audit;

import java.time.Clock;
import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Writes audit events in the same transaction as the change they describe, so a
 * change is never saved without its record. Never put personal data in "detail".
 */
@Service
public class AuditService {

    public static final String PUBLIC_ACTOR = "public";

    private final AuditEventRepository repository;
    private final Clock clock;

    public AuditService(AuditEventRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public void record(String actor, String action, String subjectType, String subjectId, String detail) {
        repository.save(new AuditEvent(Instant.now(clock), actor, action, subjectType, subjectId, detail));
    }
}
