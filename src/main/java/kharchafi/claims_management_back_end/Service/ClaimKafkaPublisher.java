package kharchafi.claims_management_back_end.Service;


import com.fasterxml.jackson.databind.ObjectMapper;
import kharchafi.claims_management_back_end.DTO.ClaimPayload;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class ClaimKafkaPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper mapper;

    @Value("${app.kafka.topics.submitted}")
    private String submittedTopic;

    @Value("${app.kafka.serviceTopicPrefix}")
    private String serviceTopicPrefix;

    public ClaimKafkaPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper mapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.mapper = mapper;
    }

    public void publishClaimCreated(ClaimPayload p) {
        sendJson(submittedTopic, p.claimId, p);
        sendJson(serviceTopic(p.claim.serviceType), p.claimId, p);
    }

    public void publishClaimMessage(ClaimPayload p) {
        sendJson(serviceTopic(p.claim.serviceType), p.claimId, p);
    }

    private String serviceTopic(String serviceType) {
        return serviceTopicPrefix + serviceType; // claims.water
    }

    private void sendJson(String topic, String key, Object payload) {
        try {
            kafkaTemplate.send(topic, key, mapper.writeValueAsString(payload));
        } catch (Exception e) {
            throw new RuntimeException("Kafka publish failed topic=" + topic, e);
        }
    }
}
