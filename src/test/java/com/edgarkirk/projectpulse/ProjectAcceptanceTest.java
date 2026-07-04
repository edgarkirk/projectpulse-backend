package com.edgarkirk.projectpulse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(classes = ProjectPulseTestApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProjectAcceptanceTest {

    private static final String VALID_PROJECT_NAME = "Atlas Migration";
    private static final String VALID_OWNER_NAME = "Jane Doe";
    private static final String VALID_STATUS = "Active";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void should_createProject_whenRequestIsValid() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Atlas Migration",
                                  "ownerName": "Jane Doe",
                                  "status": "Active"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value(VALID_PROJECT_NAME))
                .andExpect(jsonPath("$.ownerName").value(VALID_OWNER_NAME))
                .andExpect(jsonPath("$.status").value(VALID_STATUS))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    void should_listProjects_whenProjectsExist() throws Exception {
        createProject("Apollo", "Amy Adams", "Blocked");
        createProject("Beacon", "Ben Brooks", "At Risk");
        createProject(VALID_PROJECT_NAME, VALID_OWNER_NAME, VALID_STATUS);

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].name").value(VALID_PROJECT_NAME))
                .andExpect(jsonPath("$[1].name").value("Beacon"))
                .andExpect(jsonPath("$[2].name").value("Apollo"));
    }

    @Test
    void should_getProject_whenIdExists() throws Exception {
        UUID projectId = createProject(VALID_PROJECT_NAME, VALID_OWNER_NAME, VALID_STATUS);

        mockMvc.perform(get("/api/projects/{id}", projectId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(projectId.toString()))
                .andExpect(jsonPath("$.name").value(VALID_PROJECT_NAME))
                .andExpect(jsonPath("$.ownerName").value(VALID_OWNER_NAME))
                .andExpect(jsonPath("$.status").value(VALID_STATUS))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    void should_getDashboardSummary_whenProjectsExist() throws Exception {
        createProject("Alpha", "Ava Adams", "Active");
        createProject("Bravo", "Ben Brown", "Active");
        createProject("Charlie", "Cara Chen", "Active");
        createProject("Delta", "Dan Diaz", "At Risk");
        createProject("Echo", "Elle Evans", "At Risk");
        createProject("Foxtrot", "Finn Foster", "Blocked");

        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalProjects").value(6))
                .andExpect(jsonPath("$.active").value(3))
                .andExpect(jsonPath("$.atRisk").value(2))
                .andExpect(jsonPath("$.blocked").value(1))
                .andExpect(jsonPath("$.onHold").value(0));
    }

    private UUID createProject(String name, String ownerName, String status) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "ownerName": "%s",
                                  "status": "%s"
                                }
                                """.formatted(name, ownerName, status)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(response.get("id")).isNotNull();
        assertThat(response.get("createdAt")).isNotNull();
        return UUID.fromString(response.get("id").asText());
    }
}
