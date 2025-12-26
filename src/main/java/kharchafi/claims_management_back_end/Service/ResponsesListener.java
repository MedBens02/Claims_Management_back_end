package kharchafi.claims_management_back_end.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import kharchafi.claims_management_back_end.DTO.ServiceResponsePayload;
import kharchafi.claims_management_back_end.Entity.ClaimEntity;
import kharchafi.claims_management_back_end.Entity.ClaimMessageEntity;
import kharchafi.claims_management_back_end.Entity.ClaimStatusHistoryEntity;
import kharchafi.claims_management_back_end.Enum.ClaimStatus;
import kharchafi.claims_management_back_end.Enum.SenderType;
import kharchafi.claims_management_back_end.Repository.ClaimMessageRepository;
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
public class ResponsesListener {

    private final KafkaIdempotenceService idem;
    private final DeadLetterPublisher dlt;
    private final ClaimRepository claimRepo;
    private final ClaimMessageRepository msgRepo;
    private final ClaimStatusHistoryRepository histRepo;
    private final ObjectMapper mapper;

    public ResponsesListener(
            KafkaIdempotenceService idem,
            DeadLetterPublisher dlt,
            ClaimRepository claimRepo,
            ClaimMessageRepository msgRepo,
            ClaimStatusHistoryRepository histRepo,
            ObjectMapper mapper
    ) {
        this.idem = idem;
        this.dlt = dlt;
        this.claimRepo = claimRepo;
        this.msgRepo = msgRepo;
        this.histRepo = histRepo;
        this.mapper = mapper;
    }

    @KafkaListener(topics = "${app.kafka.topics.responses}", groupId = "portal-claims-service")
    @Transactional
    public void onMessage(ConsumerRecord<String, String> record, Acknowledgment ack) {
        String json = record.value();

        try {
            ServiceResponsePayload p = mapper.readValue(json, ServiceResponsePayload.class);

            if (!idem.markIfNew(p.messageId, record.topic(), record.partition(), record.offset())) {
                ack.acknowledge();
                return;
            }

            ClaimEntity claim = claimRepo.findById(p.claimId)
                    .orElseThrow(() -> new IllegalArgumentException("Claim not found: " + p.claimId));

            // Insert message (service)
            ClaimMessageEntity m = new ClaimMessageEntity();
            m.setId(UUID.randomUUID().toString());
            m.setClaimId(claim.getId());
            m.setSenderType(SenderType.SERVICE);
            m.setSenderId(p.response != null && p.response.from != null ? p.response.from.operatorId : null);
            m.setSenderName(p.response != null && p.response.from != null ? p.response.from.operatorName : null);
            m.setMessage(p.response != null ? p.response.message : "");
            m.setAttachmentsJson(mapper.writeValueAsString(p.response != null ? p.response.attachments : java.util.List.of()));
            m.setKafkaMessageId(p.messageId);
            msgRepo.save(m);

            // Update serviceReference
            if (p.serviceReference != null && !p.serviceReference.isBlank()) {
                claim.setServiceReference(p.serviceReference);
                claimRepo.save(claim);
            }

            // requestedFields => pending_info + history
            if (p.response != null && p.response.requestedFields != null && !p.response.requestedFields.isEmpty()) {
                String prev = claim.getStatus();
                claim.setStatus("pending_info");
                claimRepo.save(claim);

                ClaimStatusHistoryEntity h = new ClaimStatusHistoryEntity();
                h.setId(UUID.randomUUID().toString());
                h.setClaimId(claim.getId());
                h.setPreviousStatus(prev);
                h.setNewStatus("pending_info");
                h.setChangedBy("service");
                h.setChangeReason("Service requested additional info");
                h.setKafkaMessageId(p.messageId);
                histRepo.save(h);
            }

            ack.acknowledge();
        } catch (Exception e) {
            dlt.publish(record.topic(), json, e.getMessage());
            ack.acknowledge();
        }
    }
}
