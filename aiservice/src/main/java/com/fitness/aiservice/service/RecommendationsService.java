package com.fitness.aiservice.service;

import com.fitness.aiservice.model.Recommendation;
import com.fitness.aiservice.repository.RecommendationsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationsService {

    private final RecommendationsRepository recommendationsRepository;

    public List<Recommendation> getUserRecommendations(String userId) {
        return recommendationsRepository.findByUserId(userId);
    }

    public Recommendation getActivityRecommendations(String activityId) {
        return recommendationsRepository.findByActivityId(activityId)
                .orElseThrow(() -> new RuntimeException("Invalid activity id"));
    }
}
