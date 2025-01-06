package com.fintech.config;

import com.fintech.service.GitHubSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class StartupListener {
    private final GitHubSyncService gitHubSyncService;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void onApplicationEvent() {
        log.info("Initializing application data...");
        try {
            // 1. First sync portfolio data and wait for completion
            gitHubSyncService.syncPortfolioData();
            log.info("Portfolio data sync completed");

            // 2. Then sync fund compositions
            gitHubSyncService.syncFundCompositions();
            log.info("Fund compositions sync completed");
        } catch (Exception e) {
            log.error("Failed to initialize application data", e);
            // Consider if you want to fail application startup
            throw new RuntimeException("Application initialization failed", e);
        }
    }
} 