package gov.templates.portal.config;

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import gov.templates.portal.forms.FormDefinition;

/**
 * Client-specific settings. Each client project overrides these in its own
 * application-{client}.yml instead of changing code.
 */
@ConfigurationProperties(prefix = "portal")
public record PortalProperties(
        List<String> allowedOrigins,
        ServiceRequests serviceRequests,
        Map<String, FormDefinition> forms) {

    public PortalProperties {
        allowedOrigins = allowedOrigins == null ? List.of() : List.copyOf(allowedOrigins);
        serviceRequests = serviceRequests == null ? new ServiceRequests(List.of()) : serviceRequests;
        forms = forms == null ? Map.of() : Map.copyOf(forms);
    }

    public record ServiceRequests(List<String> categories) {
        public ServiceRequests {
            categories = categories == null ? List.of() : List.copyOf(categories);
        }
    }
}
