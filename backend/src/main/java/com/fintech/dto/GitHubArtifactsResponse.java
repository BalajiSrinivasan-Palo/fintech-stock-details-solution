package com.fintech.dto;

import lombok.Data;
import java.util.List;

@Data
public class GitHubArtifactsResponse {
    private List<Artifact> artifacts;

    @Data
    public static class Artifact {
        private String archiveDownloadUrl;
    }
} 