package com.fintech.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.Map;
import com.fintech.dto.github.FundCompositionJson;
import java.util.Base64;
import com.fintech.dto.github.GitHubContentResponse;
import com.fintech.dto.github.PortfolioJson;
import jakarta.annotation.PostConstruct;



@Slf4j
@Component
@RequiredArgsConstructor
public class GitHubApiClient {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${github.api.url:https://api.github.com}")
    private String githubApiUrl;

    @Value("${github.api.token}")
    private String githubToken;

    @Value("${github.org}")
    private String githubOrg;

    @Value("${github.fund.repo}")
    private String fundRepo;

    @Value("${github.portfolio.repo}")
    private String portfolioRepo;

    @PostConstruct
    public void validateConfig() {
        if (githubToken == null || githubToken.isEmpty()) {
            throw new IllegalStateException("GitHub API token not configured. Set GITHUB_TOKEN environment variable.");
        }
        log.info("GitHub API client configured for org: {}", githubOrg);
    }

    public FundCompositionJson getFundCompositions() {
        try {
            String artifactsUrl = String.format(
                "%s/repos/%s/%s/actions/artifacts?name=fund-compositions",
                githubApiUrl.trim(), githubOrg.trim(), fundRepo.trim());

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(githubToken);
            
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                artifactsUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                JsonNode.class
            );

            JsonNode artifacts = response.getBody().path("artifacts");
            if (artifacts.size() > 0) {
                String downloadUrl = artifacts.get(0).path("archive_download_url").asText();
                byte[] zipContent = restTemplate.exchange(
                    downloadUrl,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    byte[].class
                ).getBody();
                return processZipContent(zipContent);
            }
            throw new RuntimeException("No fund composition artifacts found");
        } catch (Exception e) {
            log.error("Failed to fetch fund compositions", e);
            throw new RuntimeException("Failed to fetch fund compositions", e);
        }
    }

    private FundCompositionJson processZipContent(byte[] zipContent) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipContent))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                log.info("Found file in zip: {}", entry.getName());
                if (entry.getName().endsWith(".json")) {
                    String jsonContent = new String(zis.readAllBytes());
                    log.info("JSON content: {}", jsonContent);
                    
                    // Create ObjectMapper with configuration
                    ObjectMapper mapper = new ObjectMapper();
                    // Convert the JSON string to a Map first
                    Map<String, FundCompositionJson.Fund> fundsMap = mapper.readValue(jsonContent,
                        mapper.getTypeFactory().constructMapType(
                            Map.class, String.class, FundCompositionJson.Fund.class));
                            
                    // Create and populate FundCompositionJson
                    FundCompositionJson result = new FundCompositionJson();
                    result.setFunds(fundsMap);
                    return result;
                }
            }
        }
        throw new RuntimeException("No JSON file found in artifact");
    }

    public FundCompositionJson getHistoricalFundCompositionData(String date) {
        try {
            String artifactsUrl = String.format(
                "https://api.github.com/repos/%s/%s/actions/artifacts?name=fund-compositions-dated",
                githubOrg, fundRepo);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(githubToken);
            
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                artifactsUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                JsonNode.class
            );

            // Find artifact with matching date
            JsonNode artifacts = response.getBody().path("artifacts");
            for (JsonNode artifact : artifacts) {
                if (artifact.path("name").asText().contains(date)) {
                    String downloadUrl = artifact.path("archive_download_url").asText();
                    byte[] zipContent = restTemplate.exchange(
                        downloadUrl,
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        byte[].class
                    ).getBody();
                    return processZipContent(zipContent);
                }
            }
            throw new RuntimeException("No data found for date: " + date);
        } catch (Exception e) {
            log.error("Error fetching historical data for date: {}", date, e);
            throw new RuntimeException("Failed to fetch historical data", e);
        }
    }

    public PortfolioJson getPortfolioData() {
        try {
            // Update artifact name to match exactly what's in the workflow
            String artifactsUrl = String.format(
                "%s/repos/%s/%s/actions/artifacts",  // Removed specific name filter
                githubApiUrl.trim(), githubOrg.trim(), portfolioRepo.trim());
            log.info("Artifacts URL: {}", artifactsUrl);
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(githubToken);
            
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                artifactsUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                JsonNode.class
            );

            JsonNode artifacts = response.getBody().path("artifacts");
            log.info("Found {} artifacts", artifacts.size());
            
            // Get the most recent artifact
            if (artifacts.size() > 0) {
                JsonNode latestArtifact = artifacts.get(0);  // GitHub returns most recent first
                log.info("Latest artifact name: {}", latestArtifact.path("name").asText());
                
                String downloadUrl = latestArtifact.path("archive_download_url").asText();
                log.info("Downloading from URL: {}", downloadUrl);
                
                byte[] zipContent = restTemplate.exchange(
                    downloadUrl,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    byte[].class
                ).getBody();
                
                return processPortfolioZipContent(zipContent);
            }

            log.warn("No artifacts found, trying direct file access");
            // Fall back to direct file access if no artifacts
            String fileUrl = String.format("%s/repos/%s/%s/contents/artifacts/portfolio.json",
                githubApiUrl.trim(), githubOrg.trim(), portfolioRepo.trim());

            ResponseEntity<GitHubContentResponse> fileResponse = restTemplate.exchange(
                fileUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                GitHubContentResponse.class
            );

            if (fileResponse.getBody() != null && fileResponse.getBody().getContent() != null) {
                String content = new String(Base64.getDecoder().decode(fileResponse.getBody().getContent()));
                return objectMapper.readValue(content, PortfolioJson.class);
            }

            throw new RuntimeException("No portfolio data found");
        } catch (Exception e) {
            log.error("Failed to fetch portfolio: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch portfolio: " + e.getMessage(), e);
        }
    }

    public PortfolioJson getHistoricalPortfolioData(String date) {
        try {
            String artifactsUrl = String.format(
                "https://api.github.com/repos/%s/%s/actions/artifacts?name=portfolio-dated",
                githubOrg, portfolioRepo);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(githubToken);
            
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                artifactsUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                JsonNode.class
            );

            JsonNode artifacts = response.getBody().path("artifacts");
            for (JsonNode artifact : artifacts) {
                if (artifact.path("name").asText().contains(date)) {
                    String downloadUrl = artifact.path("archive_download_url").asText();
                    byte[] zipContent = restTemplate.exchange(
                        downloadUrl,
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        byte[].class
                    ).getBody();
                    return processPortfolioZipContent(zipContent);
                }
            }
            throw new RuntimeException("No portfolio data found for date: " + date);
        } catch (Exception e) {
            log.error("Error fetching historical portfolio for date: {}", date, e);
            throw new RuntimeException("Failed to fetch historical portfolio", e);
        }
    }

    private PortfolioJson processPortfolioZipContent(byte[] zipContent) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipContent))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().endsWith(".json")) {
                    return objectMapper.readValue(zis.readAllBytes(), PortfolioJson.class);
                }
            }
        }
        throw new RuntimeException("No portfolio JSON file found in artifact");
    }
} 