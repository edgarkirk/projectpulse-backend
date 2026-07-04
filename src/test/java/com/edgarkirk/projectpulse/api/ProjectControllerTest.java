package com.edgarkirk.projectpulse.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest
@ActiveProfiles("test")
class ProjectControllerTest {

    private static final String VALID_PROJECT_BODY = """
            {
              "name": "Atlas Migration",
              "ownerName": "Jane Doe",
              "status": "Active"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void should_returnConflict_whenProjectNameIsDuplicate() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PROJECT_BODY))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Project name 'Atlas Migration' is already taken."))
                .andExpect(jsonPath("$.error").doesNotExist())
                .andExpect(jsonPath("$.status").doesNotExist());
    }

    @Test
    void should_returnBadRequest_whenNameIsBlank() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "ownerName": "Jane Doe",
                                  "status": "Active"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Project name is required."))
                .andExpect(jsonPath("$.error").doesNotExist())
                .andExpect(jsonPath("$.status").doesNotExist());
    }

    @Test
    void should_returnBadRequest_whenNameExceedsMaximumLength() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "ownerName": "Jane Doe",
                                  "status": "Active"
                                }
                                """.formatted("A".repeat(101))))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Project name must not exceed 100 characters."))
                .andExpect(jsonPath("$.error").doesNotExist())
                .andExpect(jsonPath("$.status").doesNotExist());
    }

    @Test
    void should_returnBadRequest_whenOwnerNameIsBlank() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Atlas Migration",
                                  "ownerName": "",
                                  "status": "Active"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Owner name is required."))
                .andExpect(jsonPath("$.error").doesNotExist())
                .andExpect(jsonPath("$.status").doesNotExist());
    }

    @Test
    void should_returnBadRequest_whenOwnerNameExceedsMaximumLength() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Atlas Migration",
                                  "ownerName": "%s",
                                  "status": "Active"
                                }
                                """.formatted("B".repeat(101))))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Owner name must not exceed 100 characters."))
                .andExpect(jsonPath("$.error").doesNotExist())
                .andExpect(jsonPath("$.status").doesNotExist());
    }

    @Test
    void should_returnBadRequest_whenStatusIsInvalid() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Atlas Migration",
                                  "ownerName": "Jane Doe",
                                  "status": "Done"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Project status must be one of Active, At Risk, Blocked, or On Hold."))
                .andExpect(jsonPath("$.error").doesNotExist())
                .andExpect(jsonPath("$.status").doesNotExist());
    }

    @Test
    void should_returnNotFound_whenProjectDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/projects/{id}", "550e8400-e29b-41d4-a716-446655440000"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Project with id 550e8400-e29b-41d4-a716-446655440000 was not found."))
                .andExpect(jsonPath("$.error").doesNotExist())
                .andExpect(jsonPath("$.status").doesNotExist());
    }
}
