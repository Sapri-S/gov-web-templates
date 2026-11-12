package gov.templates.portal.servicerequest;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Request and response shapes. Public responses never include contact details. */
public final class ServiceRequestDtos {

    private ServiceRequestDtos() {
    }

    public record SubmitServiceRequest(
            @NotBlank(message = "Choose a category.")
            @Size(max = 40, message = "Choose a category from the list.")
            String category,

            @NotBlank(message = "Describe the problem.")
            @Size(max = 4000, message = "Use 4,000 characters or fewer.")
            String description,

            @Size(max = 300, message = "Use 300 characters or fewer.")
            String location,

            @Size(max = 100, message = "Use 100 characters or fewer.")
            String contactName,

            @Email(message = "Enter an email address like name@example.com.")
            @Size(max = 254, message = "Use 254 characters or fewer.")
            String contactEmail,

            @Pattern(regexp = "^[0-9+()\\-. ]{7,30}$", message = "Enter a phone number like 301-555-0100.")
            String contactPhone,

            /** Hidden spam trap. People never see or fill this field. */
            String website) {
    }

    /** What anyone with the tracking number can see. */
    public record PublicStatus(
            String trackingNumber,
            String category,
            ServiceRequestStatus status,
            Instant submittedAt,
            Instant updatedAt) {

        static PublicStatus of(ServiceRequest r) {
            return new PublicStatus(r.getTrackingNumber(), r.getCategory(), r.getStatus(), r.getCreatedAt(), r.getUpdatedAt());
        }
    }

    /** What signed-in staff can see. */
    public record StaffView(
            UUID id,
            String trackingNumber,
            String category,
            String description,
            String location,
            String contactName,
            String contactEmail,
            String contactPhone,
            ServiceRequestStatus status,
            Instant submittedAt,
            Instant updatedAt) {

        static StaffView of(ServiceRequest r) {
            return new StaffView(r.getId(), r.getTrackingNumber(), r.getCategory(), r.getDescription(), r.getLocation(),
                    r.getContactName(), r.getContactEmail(), r.getContactPhone(), r.getStatus(), r.getCreatedAt(),
                    r.getUpdatedAt());
        }
    }

    public record StatusUpdate(
            @NotNull(message = "Choose a status.")
            ServiceRequestStatus status,

            @Size(max = 1000, message = "Use 1,000 characters or fewer.")
            String note) {
    }
}
