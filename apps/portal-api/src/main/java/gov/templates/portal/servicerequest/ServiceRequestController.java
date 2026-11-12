package gov.templates.portal.servicerequest;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gov.templates.portal.common.PageResponse;
import gov.templates.portal.servicerequest.ServiceRequestDtos.PublicStatus;
import gov.templates.portal.servicerequest.ServiceRequestDtos.StaffView;
import gov.templates.portal.servicerequest.ServiceRequestDtos.StatusUpdate;
import gov.templates.portal.servicerequest.ServiceRequestDtos.SubmitServiceRequest;

@RestController
@RequestMapping("/api")
public class ServiceRequestController {

    private final ServiceRequestService service;

    public ServiceRequestController(ServiceRequestService service) {
        this.service = service;
    }

    @GetMapping("/service-requests/categories")
    public List<String> categories() {
        return service.categories();
    }

    @PostMapping("/service-requests")
    public ResponseEntity<PublicStatus> submit(@Valid @RequestBody SubmitServiceRequest body) {
        PublicStatus created = service.submit(body);
        return ResponseEntity.created(URI.create("/api/service-requests/" + created.trackingNumber())).body(created);
    }

    @GetMapping("/service-requests/{trackingNumber}")
    public PublicStatus status(@PathVariable String trackingNumber) {
        return service.publicStatus(trackingNumber);
    }

    @GetMapping("/staff/service-requests")
    public PageResponse<StaffView> list(
            @RequestParam(required = false) ServiceRequestStatus status,
            @PageableDefault(size = 25, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return PageResponse.from(service.list(status, pageable), view -> view);
    }

    @PatchMapping("/staff/service-requests/{id}/status")
    public StaffView updateStatus(@PathVariable UUID id, @Valid @RequestBody StatusUpdate body,
            @AuthenticationPrincipal Jwt jwt) {
        return service.updateStatus(id, body, jwt.getSubject());
    }
}
