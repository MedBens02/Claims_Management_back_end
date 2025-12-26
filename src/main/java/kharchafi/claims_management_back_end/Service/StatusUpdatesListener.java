package kharchafi.claims_management_back_end.Service;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import kharchafi.claims_management_back_end.DTO.StatusUpdatePayload;
import kharchafi.claims_management_back_end.Entity.ClaimEntity;
import kharchafi.claims_management_back_end.Entity.ClaimStatusHistoryEntity;
import kharchafi.claims_management_back_end.Enum.ClaimStatus;
import kharchafi.claims_management_back_end.Repository.ClaimRepository;
import kharchafi.claims_management_back_end.Repository.ClaimStatusHistoryRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class StatusUpdatesListener {

    private final KafkaIdempotenceService idem;
    private final DeadLetterPublisher dlt;
    private final ClaimRepository claimRepo;
    private final ClaimStatusHistoryRepository histRepo;
    private final ObjectMapper mapper;

    public StatusUpdatesListener(
            KafkaIdempotenceService idem,
            DeadLetterPublisher dlt,
            ClaimRepository claimRepo,
            ClaimStatusHistoryRepository histRepo,
            ObjectMapper mapper
    ) {
        this.idem = idem;
        this.dlt = dlt;
        this.claimRepo = claimRepo;
        this.histRepo = histRepo;
        this.mapper = mapper;
    }

    @KafkaListener(topics = "${app.kafka.topics.statusUpdates}", groupId = "portal-claims-service")
    @Transactional
    public void onMessage(ConsumerRecord<String, String> record, Acknowledgment ack) {
        String json = record.value();

        try {
            StatusUpdatePayload p = mapper.readValue(json, StatusUpdatePayload.class);

            if (!idem.markIfNew(p.messageId, record.topic(), record.partition(), record.offset())) {
                ack.acknowledge();
                return;
            }

            ClaimEntity claim = claimRepo.findById(p.claimId)
                    .orElseThrow(() -> new IllegalArgumentException("Claim not found: " + p.claimId));

            ClaimStatus prev = claim.getStatus();
            String newStString = (p.status != null ? p.status.newStatus : null);
            if (newStString == null || newStString.isBlank()) throw new IllegalArgumentException("status.new required");

            ClaimStatus newSt = ClaimStatus.fromValue(newStString);
            claim.setStatus(newSt);

            if (p.status != null && p.status.assignedTo != null) {
                claim.setAssignedOperator(p.status.assignedTo.operatorName);
            }
            if (p.serviceReference != null && !p.serviceReference.isBlank()) {
                claim.setServiceReference(p.serviceReference);
            }

            claimRepo.save(claim);

            ClaimStatusHistoryEntity h = new ClaimStatusHistoryEntity();
            h.setId(UUID.randomUUID().toString());
            h.setClaimId(claim.getId());
            h.setPreviousStatus(prev);
            h.setNewStatus(newSt);
            h.setChangedBy(p.status != null && p.status.assignedTo != null ? p.status.assignedTo.operatorId : "service");
            h.setChangeReason(p.status != null ? p.status.reason : null);
            h.setKafkaMessageId(p.messageId);
            histRepo.save(h);

            ack.acknowledge();
        } catch (Exception e) {
            dlt.publish(record.topic(), json, e.getMessage());
            ack.acknowledge();
        }
    }
}
