package gov.templates.portal.servicerequest;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "service_request")
public class ServiceRequest {

    @Id
    private UUID id;

    @Column(name = "tracking_number", nullable = false, unique = true, length = 20)
    private String trackingNumber;

    @Column(nullable = false, length = 40)
    private String category;

    @Column(nullable = false, length = 4000)
    private String description;

    @Column(length = 300)
    private String location;

    @Column(name = "contact_name", length = 100)
    private String contactName;

    @Column(name = "contact_email", length = 254)
    private String contactEmail;

    @Column(name = "contact_phone", length = 30)
    private String contactPhone;

    // Stored as plain VARCHAR on every database, so schema validation matches the migration.
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private ServiceRequestStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    protected ServiceRequest() {
    }

    static ServiceRequest newSubmission(String trackingNumber, String category, String description, String location,
            String contactName, String contactEmail, String contactPhone, Instant now) {
        ServiceRequest request = new ServiceRequest();
        request.id = UUID.randomUUID();
        request.trackingNumber = trackingNumber;
        request.category = category;
        request.description = description;
        request.location = location;
        request.contactName = contactName;
        request.contactEmail = contactEmail;
        request.contactPhone = contactPhone;
        request.status = ServiceRequestStatus.SUBMITTED;
        request.createdAt = now;
        request.updatedAt = now;
        return request;
    }

    void moveTo(ServiceRequestStatus next, Instant now) {
        this.status = next;
        this.updatedAt = now;
    }

    public UUID getId() {
        return id;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public String getContactName() {
        return contactName;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public ServiceRequestStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
