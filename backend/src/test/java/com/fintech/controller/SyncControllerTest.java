package com.fintech.controller;

import com.fintech.service.GitHubSyncService;
import com.fintech.service.MarketDataService;
import com.fintech.service.DataIntegrationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.Import;
import com.fintech.config.TestSecurityConfig;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SyncController.class)
@Import({TestSecurityConfig.class})
public class SyncControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GitHubSyncService gitHubSyncService;

    @MockBean
    private MarketDataService marketDataService;

    @MockBean
    private DataIntegrationService dataIntegrationService;

    @Test
    @WithMockUser
    public void syncAll_ShouldTriggerBothSyncs() throws Exception {
        doNothing().when(gitHubSyncService).syncPortfolioData();
        doNothing().when(gitHubSyncService).syncFundCompositions();

        mockMvc.perform(post("/api/sync"))
            .andExpect(status().isOk());

        verify(gitHubSyncService).syncPortfolioData();
        verify(gitHubSyncService).syncFundCompositions();
    }
} 