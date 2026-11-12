package gov.templates.portal.servicerequest;

import java.util.EnumSet;
import java.util.Set;

/** Lifecycle of a service request, and which moves between states are allowed. */
public enum ServiceRequestStatus {
    SUBMITTED,
    IN_REVIEW,
    IN_PROGRESS,
    RESOLVED,
    CLOSED;

    public Set<ServiceRequestStatus> allowedNext() {
        return switch (this) {
            case SUBMITTED -> EnumSet.of(IN_REVIEW, CLOSED);
            case IN_REVIEW -> EnumSet.of(IN_PROGRESS, RESOLVED, CLOSED);
            case IN_PROGRESS -> EnumSet.of(RESOLVED, CLOSED);
            case RESOLVED -> EnumSet.of(IN_PROGRESS, CLOSED);
            case CLOSED -> EnumSet.noneOf(ServiceRequestStatus.class);
        };
    }

    public boolean canMoveTo(ServiceRequestStatus next) {
        return allowedNext().contains(next);
    }
}
