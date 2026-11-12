package gov.templates.portal.servicerequest;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, UUID> {

    Optional<ServiceRequest> findByTrackingNumber(String trackingNumber);

    boolean existsByTrackingNumber(String trackingNumber);

    Page<ServiceRequest> findByStatus(ServiceRequestStatus status, Pageable pageable);
}
