package com.fitness.aiservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.aiservice.model.Activity;
import com.fitness.aiservice.model.Recommendation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityAiService {

    private final GroqService groqService;

    public Recommendation generateRecommendation(Activity activity) {
        String prompt = createPromptForActivity(activity);
        String aiResponse = groqService.getChatAnswer(prompt);

        log.info("RESPONSE FROM AI: {}", aiResponse);

        return processAiResponse(activity, aiResponse);
    }

    private Recommendation processAiResponse(Activity activity, String aiResponse) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(aiResponse);

            JsonNode analysisJson;

            // ✅ CASE 1: Wrapped response (candidates -> contents -> text)
            if (rootNode.has("candidates")) {
                JsonNode textNode = rootNode
                        .path("candidates")
                        .path(0)
                        .path("contents")
                        .path("text");

                if (textNode.isMissingNode() || textNode.isNull()) {
                    throw new IllegalStateException("Missing AI text content");
                }

                String jsonContent = textNode.asText()
                        .replaceAll("```json\\n", "")
                        .replaceAll("\\n```", "")
                        .trim();

                analysisJson = mapper.readTree(jsonContent);
            }
            // ✅ CASE 2: Direct JSON (YOUR CURRENT RESPONSE)
            else {
                analysisJson = rootNode;
            }

            // ---- ANALYSIS ----
            JsonNode analysisNode = analysisJson.path("analysis");
            StringBuilder fullAnalysis = new StringBuilder();

            addAnalysisSection(fullAnalysis, analysisNode, "overall", "Overall: ");
            addAnalysisSection(fullAnalysis, analysisNode, "pace", "Pace: ");
            addAnalysisSection(fullAnalysis, analysisNode, "heartRate", "Heart Rate: ");
            addAnalysisSection(fullAnalysis, analysisNode, "caloriesBurned", "Calories: ");

            // ---- LISTS ----
            List<String> improvements = extractImprovements(analysisJson.path("improvements"));
            List<String> suggestions = extractSuggestions(analysisJson.path("suggestions"));
            List<String> safety = extractSafetyGuidelines(analysisJson.path("safety"));

            log.info("AI response parsed successfully");

            return Recommendation.builder()
                    .activityId(activity.getId())
                    .userId(activity.getUserId())
                    .activityType(activity.getType())
                    .recommendations(fullAnalysis.toString())
                    .improvements(improvements)
                    .suggestions(suggestions)
                    .safety(safety)
                    .createdAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Failed to process AI response. Raw response:\n{}", aiResponse, e);
            throw new RuntimeException("AI response parsing failed", e);
        }
    }

    // ---------------- HELPERS ----------------

    private List<String> extractSafetyGuidelines(JsonNode safetyNode) {
        List<String> safety = new ArrayList<>();
        if (safetyNode.isArray()) {
            safetyNode.forEach(point -> safety.add(point.asText()));
        }
        return safety.isEmpty()
                ? Collections.singletonList("No safety guidelines provided")
                : safety;
    }

    private List<String> extractSuggestions(JsonNode suggestionsNode) {
        List<String> suggestions = new ArrayList<>();
        if (suggestionsNode.isArray()) {
            suggestionsNode.forEach(s -> {
                suggestions.add(
                        s.path("workout").asText() + ": " +
                                s.path("description").asText()
                );
            });
        }
        return suggestions.isEmpty()
                ? Collections.singletonList("No specific suggestions provided")
                : suggestions;
    }

    private List<String> extractImprovements(JsonNode improvementsNode) {
        List<String> improvements = new ArrayList<>();
        if (improvementsNode.isArray()) {
            improvementsNode.forEach(i -> {
                improvements.add(
                        i.path("area").asText() + ": " +
                                i.path("recommendation").asText()
                );
            });
        }
        return improvements.isEmpty()
                ? Collections.singletonList("No specific improvements provided")
                : improvements;
    }

    private void addAnalysisSection(
            StringBuilder fullAnalysis,
            JsonNode analysisNode,
            String key,
            String prefix
    ) {
        if (!analysisNode.path(key).isMissingNode()) {
            fullAnalysis.append(prefix)
                    .append(analysisNode.path(key).asText())
                    .append("\n\n");
        }
    }

    // ---------------- PROMPT ----------------

    private String createPromptForActivity(Activity activity) {
        return String.format("""
        Analyze this fitness activity and provide detailed recommendations.
        The response MUST be in STRICT JSON format ONLY.

        {
          "analysis": {
            "overall": "Overall analysis",
            "pace": "Pace analysis",
            "heartRate": "Heart rate analysis",
            "caloriesBurned": "Calories analysis"
          },
          "improvements": [
            { "area": "Area", "recommendation": "Recommendation" }
          ],
          "suggestions": [
            { "workout": "Workout", "description": "Description" }
          ],
          "safety": [ "Safety point" ]
        }

        Activity Type: %s  
        Duration: %d minutes  
        Calories Burned: %d  
        Additional Metrics: %s
        """,
                activity.getType(),
                activity.getDuration(),
                activity.getCaloriesBurned(),
                activity.getAdditionalMetrics()
        );
    }
}
