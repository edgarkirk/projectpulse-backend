package com.edgarkirk.projectpulse.api;

import com.edgarkirk.projectpulse.api.dto.request.CreateProjectRequest;
import com.edgarkirk.projectpulse.domain.ProjectStatus;
import com.edgarkirk.projectpulse.service.ProjectService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
class RestExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService projectService;

    @Test
    void should_return_message_for_malformed_uuid() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/projects/{id}", "not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("id must be a valid UUID"));
    }

    @Test
    void should_return_message_for_missing_status() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Atlas Migration\",\"ownerName\":\"Jane Doe\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("status is required"));
    }
}
