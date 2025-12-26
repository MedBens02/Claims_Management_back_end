package kharchafi.claims_management_back_end.Service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class DeadLetterPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${app.kafka.topics.deadLetter}")
    private String deadLetterTopic;

    public DeadLetterPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(String originalTopic, String rawJson, String error) {
        String payload = """
            {"originalTopic":"%s","timestamp":"%s","error":"%s","raw":%s}
            """.formatted(escape(originalTopic), Instant.now().toString(), escape(error), safeJson(rawJson));
        kafkaTemplate.send(deadLetterTopic, payload);
    }

    private String safeJson(String raw) {
        if (raw == null) return "null";
        return raw.trim().startsWith("{") || raw.trim().startsWith("[") ? raw : "\"" + escape(raw) + "\"";
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
