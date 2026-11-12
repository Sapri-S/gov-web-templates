package gov.templates.portal.forms;

import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gov.templates.portal.common.PageResponse;
import gov.templates.portal.forms.FormService.PublicForm;
import gov.templates.portal.forms.FormService.Receipt;
import gov.templates.portal.forms.FormService.StaffSubmission;

@RestController
@RequestMapping("/api")
public class FormController {

    private final FormService service;

    public FormController(FormService service) {
        this.service = service;
    }

    @GetMapping("/forms/{formKey}")
    public PublicForm definition(@PathVariable String formKey) {
        return service.definition(formKey);
    }

    @PostMapping("/forms/{formKey}/submissions")
    public ResponseEntity<Receipt> submit(@PathVariable String formKey, @RequestBody SubmissionRequest body) {
        Receipt receipt = service.submit(formKey, body.values(), body.website());
        return ResponseEntity.status(HttpStatus.CREATED).body(receipt);
    }

    @GetMapping("/staff/forms/{formKey}/submissions")
    public PageResponse<StaffSubmission> list(@PathVariable String formKey,
            @PageableDefault(size = 25, sort = "submittedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return PageResponse.from(service.list(formKey, pageable), submission -> submission);
    }

    /** "website" is a hidden spam trap that people never fill in. */
    public record SubmissionRequest(Map<String, Object> values, String website) {
    }
}
