package com.fintech.service;

import com.fintech.exception.DataProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataIntegrationService {
    private final GitHubSyncService gitHubSyncService;

    @Scheduled(cron = "${app.data.refresh.cron}")
    public void scheduledDataRefresh() {
        try {
            log.info("Starting daily data sync");
            gitHubSyncService.syncDailyData();
            log.info("Completed daily data sync");
        } catch (Exception e) {
            log.error("Failed to sync daily data", e);
            throw new DataProcessingException("Failed to sync daily data", e);
        }
    }
} 