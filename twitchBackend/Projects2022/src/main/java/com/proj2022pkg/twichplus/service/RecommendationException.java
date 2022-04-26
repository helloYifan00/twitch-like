package com.proj2022pkg.twichplus.service;

// we’ll throw this exception in the recommendation code.
public class RecommendationException extends RuntimeException {
    public RecommendationException(String errorMessage) {
        super(errorMessage);
    }
}