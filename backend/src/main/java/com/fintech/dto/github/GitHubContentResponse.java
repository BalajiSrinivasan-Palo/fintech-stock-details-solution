package com.fintech.dto.github;

import lombok.Data;

@Data
public class GitHubContentResponse {
    private String name;
    private String path;
    private String sha;
    private String content;
    private String encoding;
} 