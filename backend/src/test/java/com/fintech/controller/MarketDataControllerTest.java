package com.fintech.controller;

import com.fintech.service.MarketDataService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.Import;
import com.fintech.config.TestSecurityConfig;

import java.time.LocalDate;
import java.util.Set;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MarketDataController.class)
@Import({TestSecurityConfig.class})
public class MarketDataControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MarketDataService marketDataService;

    @Test
    @WithMockUser
    void syncMarketData_ShouldReturnOk() throws Exception {
        when(marketDataService.getActiveSymbols()).thenReturn(Set.of("AAPL", "GOOGL"));

        mockMvc.perform(post("/api/market-data/sync")
                .param("startDate", LocalDate.now().toString())
                .param("endDate", LocalDate.now().toString()))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getStatus_ShouldReturnOk() throws Exception {
        when(marketDataService.findDistinctSymbols()).thenReturn(Set.of("AAPL", "GOOGL"));

        mockMvc.perform(get("/api/market-data/status"))
            .andExpect(status().isOk());
    }
} 