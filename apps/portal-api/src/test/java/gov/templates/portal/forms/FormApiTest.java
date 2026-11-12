package gov.templates.portal.forms;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

import gov.templates.portal.config.SecurityConfig;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FormApiTest {

    private static final String VALID = """
            {"values":{"name":"Pat Resident","email":"pat@example.com",
                       "topic":"General question","message":"When is bulk trash pickup?"}}
            """;

    @Autowired
    MockMvc mvc;

    @Test
    void definitionIsPublic() throws Exception {
        mvc.perform(get("/api/forms/contact"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key").value("contact"))
                .andExpect(jsonPath("$.fields[0].name").value("name"))
                .andExpect(jsonPath("$.fields[1].type").value("EMAIL"));
    }

    @Test
    void validSubmissionGetsReferenceNumber() throws Exception {
        mvc.perform(post("/api/forms/contact/submissions").contentType(MediaType.APPLICATION_JSON).content(VALID))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.referenceNumber").value(matchesPattern("FM-[2-9A-HJKMNP-TV-Z]{10}")));
    }

    @Test
    void missingRequiredFieldsAreReported() throws Exception {
        mvc.perform(post("/api/forms/contact/submissions").contentType(MediaType.APPLICATION_JSON)
                .content("{\"values\":{\"name\":\"Pat\"}}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.message").exists());
    }

    @Test
    void unknownFieldsAreRejected() throws Exception {
        mvc.perform(post("/api/forms/contact/submissions").contentType(MediaType.APPLICATION_JSON)
                .content("{\"values\":{\"name\":\"Pat\",\"email\":\"pat@example.com\",\"topic\":\"General question\","
                        + "\"message\":\"Hi\",\"ssn\":\"123-45-6789\"}}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.ssn").exists());
    }

    @Test
    void optionOutsideListIsRejected() throws Exception {
        mvc.perform(post("/api/forms/contact/submissions").contentType(MediaType.APPLICATION_JSON)
                .content("{\"values\":{\"name\":\"Pat\",\"email\":\"pat@example.com\",\"topic\":\"Other\","
                        + "\"message\":\"Hi\"}}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.topic").exists());
    }

    @Test
    void unknownFormIsNotFound() throws Exception {
        mvc.perform(get("/api/forms/does-not-exist")).andExpect(status().isNotFound());
    }

    @Test
    void staffCanListSubmissions() throws Exception {
        mvc.perform(post("/api/forms/contact/submissions").contentType(MediaType.APPLICATION_JSON).content(VALID))
                .andExpect(status().isCreated());
        mvc.perform(get("/api/staff/forms/contact/submissions")
                .with(jwt().authorities(new SimpleGrantedAuthority(SecurityConfig.STAFF_AUTHORITY))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].values.email").value("pat@example.com"));
    }

    @Test
    void submissionsAreNotPublic() throws Exception {
        mvc.perform(get("/api/staff/forms/contact/submissions")).andExpect(status().isUnauthorized());
    }
}
