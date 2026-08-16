package com.eneik.production.service;

import org.springframework.stereotype.Component;

@Component
public class DefaultGitHubPrClient implements GitHubPrClient {

    @Override
    public String getPullRequestState(Integer prNumber) {
        if (prNumber == null) {
            return "MISSING";
        }
        // In real environment, queries GitHub API.
        // For default simulation/mock fallback:
        return "OPEN";
    }
}
