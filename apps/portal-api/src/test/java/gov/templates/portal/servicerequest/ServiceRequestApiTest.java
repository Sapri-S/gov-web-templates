package gov.templates.portal.servicerequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.templates.portal.audit.AuditEventRepository;
import gov.templates.portal.config.SecurityConfig;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ServiceRequestApiTest {

    private static final String VALID = """
            {"category":"pothole","description":"Large pothole in the right lane",
             "location":"Main St and 2nd Ave","contactName":"Pat Resident",
             "contactEmail":"pat@example.com","contactPhone":"301-555-0100"}
            """;

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ServiceRequestRepository requests;

    @Autowired
    AuditEventRepository auditEvents;

    private static RequestPostProcessor staff() {
        return jwt().jwt(j -> j.subject("staff-123"))
                .authorities(new SimpleGrantedAuthority(SecurityConfig.STAFF_AUTHORITY));
    }

    private String submitValid() throws Exception {
        String body = mvc.perform(post("/api/service-requests").contentType(MediaType.APPLICATION_JSON).content(VALID))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(body);
        return json.get("trackingNumber").asText();
    }

    @Test
    void submitReturnsTrackingNumberWithoutContactDetails() throws Exception {
        mvc.perform(post("/api/service-requests").contentType(MediaType.APPLICATION_JSON).content(VALID))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.trackingNumber").value(matchesPattern("SR-[2-9A-HJKMNP-TV-Z]{10}")))
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.contactEmail").doesNotExist());
    }

    @Test
    void submissionIsAudited() throws Exception {
        String tracking = submitValid();
        assertThat(auditEvents.findBySubjectTypeAndSubjectIdOrderByOccurredAtAsc("service_request", tracking))
                .extracting("action").containsExactly("SUBMITTED");
    }

    @Test
    void rejectsInvalidEmailWithFieldMessage() throws Exception {
        mvc.perform(post("/api/service-requests").contentType(MediaType.APPLICATION_JSON)
                .content("{\"category\":\"pothole\",\"description\":\"Hole\",\"contactEmail\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.contactEmail").exists());
    }

    @Test
    void rejectsUnknownCategory() throws Exception {
        mvc.perform(post("/api/service-requests").contentType(MediaType.APPLICATION_JSON)
                .content("{\"category\":\"spaceship\",\"description\":\"Hole\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.category").exists());
    }

    @Test
    void rejectsFilledSpamTrap() throws Exception {
        mvc.perform(post("/api/service-requests").contentType(MediaType.APPLICATION_JSON)
                .content("{\"category\":\"pothole\",\"description\":\"Hole\",\"website\":\"http://spam.example\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void publicLookupShowsStatusButNotContactDetails() throws Exception {
        String tracking = submitValid();
        mvc.perform(get("/api/service-requests/" + tracking.toLowerCase()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trackingNumber").value(tracking))
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.contactEmail").doesNotExist())
                .andExpect(jsonPath("$.description").doesNotExist());
    }

    @Test
    void unknownTrackingNumberIsNotFound() throws Exception {
        mvc.perform(get("/api/service-requests/SR-2222222222")).andExpect(status().isNotFound());
        mvc.perform(get("/api/service-requests/not-a-number")).andExpect(status().isNotFound());
    }

    @Test
    void staffListRequiresSignIn() throws Exception {
        mvc.perform(get("/api/staff/service-requests")).andExpect(status().isUnauthorized());
    }

    @Test
    void staffListRequiresStaffScope() throws Exception {
        mvc.perform(get("/api/staff/service-requests").with(jwt())).andExpect(status().isForbidden());
    }

    @Test
    void staffCanSeeContactDetails() throws Exception {
        submitValid();
        mvc.perform(get("/api/staff/service-requests").with(staff()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].contactEmail").exists())
                .andExpect(jsonPath("$.totalItems").isNumber());
    }

    @Test
    void staffStatusChangeIsAppliedAndAudited() throws Exception {
        String tracking = submitValid();
        var id = requests.findByTrackingNumber(tracking).orElseThrow().getId();

        mvc.perform(patch("/api/staff/service-requests/" + id + "/status").with(staff())
                .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"IN_REVIEW\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_REVIEW"));

        assertThat(auditEvents.findBySubjectTypeAndSubjectIdOrderByOccurredAtAsc("service_request", tracking))
                .extracting("action", "actor")
                .contains(org.assertj.core.groups.Tuple.tuple("STATUS_CHANGED", "staff-123"));
    }

    @Test
    void disallowedStatusChangeIsConflict() throws Exception {
        String tracking = submitValid();
        var id = requests.findByTrackingNumber(tracking).orElseThrow().getId();

        mvc.perform(patch("/api/staff/service-requests/" + id + "/status").with(staff())
                .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"RESOLVED\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void unknownRoutesAreDenied() throws Exception {
        mvc.perform(get("/api/anything-else")).andExpect(status().isUnauthorized());
    }

    @Test
    void securityHeadersAreSet() throws Exception {
        mvc.perform(get("/api/service-requests/categories"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("Content-Security-Policy", "default-src 'none'; frame-ancestors 'none'"))
                .andExpect(header().string("Referrer-Policy", "no-referrer"));
    }
}
