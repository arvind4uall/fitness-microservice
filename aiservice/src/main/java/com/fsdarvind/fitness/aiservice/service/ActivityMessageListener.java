package com.fsdarvind.fitness.aiservice.service;

import com.fsdarvind.fitness.aiservice.model.Activity;
import com.fsdarvind.fitness.aiservice.model.Recommendation;
import com.fsdarvind.fitness.aiservice.repository.RecommendationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityMessageListener {
    private final ActivityAIService aiService;
    private final RecommendationRepository recommendationRepository;
    @RabbitListener(queues = "activity.queue")
    public void processActivity(Activity activity){
        log.info("Received activity for processing: {}",activity.getId());
        try {
//            log.info("Generated Recommendation: {}", aiService.generateRecommendation(activity));
            Recommendation recommendation = aiService.generateRecommendation(activity);
            recommendationRepository.save(recommendation);
        } catch (Exception e) {
            // Never rethrow: an escaping exception gets this message redelivered forever.
            log.error("Dropping activity {}: {}", activity.getId(), e.getMessage());
        }
    }
}
