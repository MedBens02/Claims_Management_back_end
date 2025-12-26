package kharchafi.claims_management_back_end.Service;

import kharchafi.claims_management_back_end.DTO.ClaimPayload;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpKafkaPublisher extends ClaimKafkaPublisher {

    public NoOpKafkaPublisher() {
        super(null, null);
    }

    @Override
    public void publishClaimCreated(ClaimPayload payload) {
        System.out.println("Kafka disabled - skipping publish for claim: " + payload.claimId);
    }

    @Override
    public void publishClaimMessage(ClaimPayload payload) {
        System.out.println("Kafka disabled - skipping message publish for claim: " + payload.claimId);
    }
}
