package com.umasankar.applytrack;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApplicationApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void servesDashboardAndHealthEndpoint() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().isOk());

        mockMvc.perform(get("/index.html"))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("KAIROS")));

        mockMvc.perform(get("/api/v1/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void allowsCrossOriginApiRequests() throws Exception {
        mockMvc.perform(options("/api/v1/applications")
                .header("Origin", "https://example-frontend.com")
                .header("Access-Control-Request-Method", "POST"))
            .andExpect(status().isOk())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                .header().string("Access-Control-Allow-Origin", "https://example-frontend.com"));
    }

    @Test
    void createsFiltersAndAggregatesApplications() throws Exception {
        String request = """
            {
              "companyName": "Acme Labs",
              "jobTitle": "Java Developer",
              "location": "Hyderabad",
              "applicationSource": "LINKEDIN",
              "status": "APPLIED",
              "applicationDate": "2026-09-12",
              "notes": "Submitted"
            }
            """;

        mockMvc.perform(post("/api/v1/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.companyName").value("Acme Labs"));

        mockMvc.perform(get("/api/v1/applications").param("status", "APPLIED"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.content[0].jobTitle").value("Java Developer"));

        mockMvc.perform(get("/api/v1/analytics/summary"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalApplications").value(1))
            .andExpect(jsonPath("$.appliedApplications").value(1));
    }
}
