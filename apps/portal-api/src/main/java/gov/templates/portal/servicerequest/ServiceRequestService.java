package gov.templates.portal.servicerequest;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.templates.portal.audit.AuditService;
import gov.templates.portal.common.ApiExceptions.ConflictException;
import gov.templates.portal.common.ApiExceptions.FieldValidationException;
import gov.templates.portal.common.ApiExceptions.NotFoundException;
import gov.templates.portal.common.ReferenceNumberGenerator;
import gov.templates.portal.config.PortalProperties;
import gov.templates.portal.servicerequest.ServiceRequestDtos.PublicStatus;
import gov.templates.portal.servicerequest.ServiceRequestDtos.StaffView;
import gov.templates.portal.servicerequest.ServiceRequestDtos.StatusUpdate;
import gov.templates.portal.servicerequest.ServiceRequestDtos.SubmitServiceRequest;

/** All service request rules live here, not in controllers or the frontend. */
@Service
public class ServiceRequestService {

    static final String PREFIX = "SR";
    private static final String SUBJECT = "service_request";

    private final ServiceRequestRepository repository;
    private final AuditService audit;
    private final PortalProperties properties;
    private final ReferenceNumberGenerator numbers;
    private final Clock clock;

    public ServiceRequestService(ServiceRequestRepository repository, AuditService audit, PortalProperties properties,
            ReferenceNumberGenerator numbers, Clock clock) {
        this.repository = repository;
        this.audit = audit;
        this.properties = properties;
        this.numbers = numbers;
        this.clock = clock;
    }

    public List<String> categories() {
        return properties.serviceRequests().categories();
    }

    @Transactional
    public PublicStatus submit(SubmitServiceRequest input) {
        if (input.website() != null && !input.website().isBlank()) {
            throw new FieldValidationException(Map.of("website", "Leave this field empty."));
        }
        String category = input.category().trim();
        if (!categories().contains(category)) {
            throw new FieldValidationException(Map.of("category", "Choose a category from the list."));
        }

        String trackingNumber = uniqueTrackingNumber();
        ServiceRequest request = ServiceRequest.newSubmission(trackingNumber, category, input.description().trim(),
                blankToNull(input.location()), blankToNull(input.contactName()), blankToNull(input.contactEmail()),
                blankToNull(input.contactPhone()), Instant.now(clock));
        repository.save(request);
        audit.record(AuditService.PUBLIC_ACTOR, "SUBMITTED", SUBJECT, trackingNumber, "category=" + category);
        return PublicStatus.of(request);
    }

    @Transactional(readOnly = true)
    public PublicStatus publicStatus(String trackingNumber) {
        String normalized = ReferenceNumberGenerator.normalize(PREFIX, trackingNumber);
        return Optional.ofNullable(normalized)
                .flatMap(repository::findByTrackingNumber)
                .map(PublicStatus::of)
                .orElseThrow(() -> new NotFoundException("We couldn't find a request with that tracking number."));
    }

    @Transactional(readOnly = true)
    public Page<StaffView> list(ServiceRequestStatus status, Pageable pageable) {
        Page<ServiceRequest> page = status == null
                ? repository.findAll(pageable)
                : repository.findByStatus(status, pageable);
        return page.map(StaffView::of);
    }

    @Transactional
    public StaffView updateStatus(UUID id, StatusUpdate update, String actor) {
        ServiceRequest request = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Service request not found."));
        ServiceRequestStatus from = request.getStatus();
        if (!from.canMoveTo(update.status())) {
            throw new ConflictException("A request can't move from " + from + " to " + update.status() + ".");
        }
        request.moveTo(update.status(), Instant.now(clock));
        audit.record(actor, "STATUS_CHANGED", SUBJECT, request.getTrackingNumber(),
                "from=" + from + " to=" + update.status());
        return StaffView.of(request);
    }

    private String uniqueTrackingNumber() {
        for (int attempt = 0; attempt < 5; attempt++) {
            String candidate = numbers.next(PREFIX);
            if (!repository.existsByTrackingNumber(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Could not create a unique tracking number.");
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
