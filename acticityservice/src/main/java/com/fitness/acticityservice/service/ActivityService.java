package com.fitness.acticityservice.service;

import com.fitness.acticityservice.dto.ActivityRequest;
import com.fitness.acticityservice.dto.ActivityResponse;
import com.fitness.acticityservice.model.Activity;
import com.fitness.acticityservice.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.fitness.acticityservice.service.UserValidationService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityService {


    private  final ActivityRepository activityRepository;
    @Autowired
    private UserValidationService userValidationService;

    @Value("${rabbitmq.exchange.name}")
    private String exchange;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public ActivityResponse trackActivity(ActivityRequest request){
        boolean isValidUser = userValidationService.validateUser(request.getUserId());
        if(!isValidUser){
            throw  new RuntimeException("invalid user " + request.getUserId());
        }

        Activity activity = Activity.builder()
                .userId(request.getUserId())
                .type(request.getType())
                .duration(request.getDuration())
                .caloriesBurned(request.getCaloriesBurned())
                .startTime(request.getStartTime())
                .additionalMetrics(request.getAdditionalMetrics())
                .build();

        Activity savedActivity = activityRepository.save(activity);

        // publish to rabbitmq
        try{
            rabbitTemplate.convertAndSend(exchange,routingKey, savedActivity);

        }catch ( Exception e){
            log.error("failed to publish activity to rabbitmq", e);
        }
        return mapToReponse(savedActivity);

    }

    private ActivityResponse mapToReponse(Activity activity){
        ActivityResponse response = new ActivityResponse();
        response.setId(activity.getId());
        response.setUserId(activity.getUserId());
        response.setType(activity.getType());
        response.setDuration(activity.getDuration());
        response.setCaloriesBurned(activity.getCaloriesBurned());
        response.setStartTime(activity.getStartTime());
        response.setAdditionalMetrics(activity.getAdditionalMetrics());
        response.setCreatedAt(activity.getCreatedAt());
        response.setUpdatedAt(activity.getUpdatedAt());
        return response;
    }


    public List<ActivityResponse> getUserActivities(String userId) {

        List<Activity> activities= activityRepository.findByUserId(userId);
        return activities.stream()
                .map(this::mapToReponse)
                .collect(Collectors.toList());
    }

    public ActivityResponse getActivityById(String activityId) {
        return activityRepository.findById(activityId)
                .map(this::mapToReponse)
                .orElseThrow(() ->new RuntimeException("Activity not found with this id"+ activityId));
    }
}
