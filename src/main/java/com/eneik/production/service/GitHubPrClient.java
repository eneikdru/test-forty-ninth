package com.eneik.production.service;

public interface GitHubPrClient {
    String getPullRequestState(Integer prNumber);
}
