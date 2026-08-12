package com.fitness.aiservice.service;

import com.fitness.aiservice.model.Activity;
import com.fitness.aiservice.model.Recommendation;
import com.fitness.aiservice.repository.RecommendationsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityMessageListener {

    private final ActivityAiService aiService;
    private final RecommendationsRepository recommendationsRepository;

    @RabbitListener(queues = "activity.queue")
    public void processListener(Activity activity) {
        try {
            log.info("Received activity for processing: {}", activity.getId());

            Recommendation recommendation =
                    aiService.generateRecommendation(activity);

            Recommendation saved =
                    recommendationsRepository.save(recommendation);

            log.info("✅ Recommendation saved with id: {}", saved.getId());

        } catch (Exception e) {
            log.error("❌ Failed to process activity: {}", activity.getId(), e);
        }
    }
}
