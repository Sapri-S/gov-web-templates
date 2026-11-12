package gov.templates.portal.forms;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FormSubmissionRepository extends JpaRepository<FormSubmission, UUID> {

    boolean existsByReferenceNumber(String referenceNumber);

    Page<FormSubmission> findByFormKey(String formKey, Pageable pageable);
}
