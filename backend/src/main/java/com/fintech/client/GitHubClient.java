package com.fintech.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class GitHubClient {
    private final RestTemplate restTemplate;

    @Value("${github.org}")
    private String githubOrg;

    @Value("${github.fund.repo}")
    private String fundRepo;

    public String getFundCompositionData() {
        return fetchFromGitHub("fund_compositions.json");
    }

    public String getHistoricalFundCompositionData(String date) {
        return fetchFromGitHub("fund_compositions_" + date + ".json");
    }

    private String fetchFromGitHub(String filename) {
        try {
            String url = String.format(
                "https://raw.githubusercontent.com/%s/%s/main/artifacts/%s",
                githubOrg, fundRepo, filename);
            log.info("Fetching from URL: {}", url);
            return restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            log.error("Error fetching data from GitHub: {}", filename, e);
            throw new RuntimeException("Failed to fetch GitHub data: " + filename, e);
        }
    }
} 