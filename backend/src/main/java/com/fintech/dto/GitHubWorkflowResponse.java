package com.fintech.dto;

import lombok.Data;
import java.util.List;

@Data
public class GitHubWorkflowResponse {
    private List<WorkflowRun> workflowRuns;

    @Data
    public static class WorkflowRun {
        private String status;
        private String conclusion;
        private String artifactsUrl;
    }
} 