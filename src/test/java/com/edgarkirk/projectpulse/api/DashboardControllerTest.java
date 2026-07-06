package com.edgarkirk.projectpulse.api;

import com.edgarkirk.projectpulse.api.dto.response.DashboardSummary;
import com.edgarkirk.projectpulse.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService projectService;

    @Test
    void should_return_dashboard_summary_when_requested() throws Exception {
        when(projectService.getDashboardSummary()).thenReturn(new DashboardSummary(6L, 3L, 2L, 1L, 0L));

        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProjects").value(6))
                .andExpect(jsonPath("$.active").value(3))
                .andExpect(jsonPath("$.atRisk").value(2))
                .andExpect(jsonPath("$.blocked").value(1))
                .andExpect(jsonPath("$.onHold").value(0));
    }
}
