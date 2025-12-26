package kharchafi.claims_management_back_end.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import kharchafi.claims_management_back_end.DTO.ClaimPayload;
import kharchafi.claims_management_back_end.DTO.ServiceResponsePayload;
import kharchafi.claims_management_back_end.DTO.StatusUpdatePayload;
import kharchafi.claims_management_back_end.Entity.ClaimAttachmentEntity;
import kharchafi.claims_management_back_end.Entity.ClaimEntity;
import kharchafi.claims_management_back_end.Entity.ClaimMessageEntity;
import kharchafi.claims_management_back_end.Entity.ClaimStatusHistoryEntity;
import kharchafi.claims_management_back_end.Entity.ProcessedKafkaMessageEntity;
import kharchafi.claims_management_back_end.Enum.ClaimStatus;
import kharchafi.claims_management_back_end.Enum.SenderType;
import kharchafi.claims_management_back_end.Repository.ClaimAttachmentRepository;
import kharchafi.claims_management_back_end.Repository.ClaimMessageRepository;
import kharchafi.claims_management_back_end.Repository.ClaimRepository;
import kharchafi.claims_management_back_end.Repository.ClaimStatusHistoryRepository;
import kharchafi.claims_management_back_end.Repository.ProcessedKafkaMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ClaimIngestService {

    private final ClaimRepository claimRepo;
    private final ClaimMessageRepository msgRepo;
    private final ClaimAttachmentRepository attRepo;
    private final ClaimStatusHistoryRepository histRepo;
    private final ProcessedKafkaMessageRepository processedRepo;
    private final ClaimKafkaPublisher publisher;
    private final ObjectMapper mapper;

    public ClaimIngestService(
            ClaimRepository claimRepo,
            ClaimMessageRepository msgRepo,
            ClaimAttachmentRepository attRepo,
            ClaimStatusHistoryRepository histRepo,
            ProcessedKafkaMessageRepository processedRepo,
            ClaimKafkaPublisher publisher,
            ObjectMapper mapper
    ) {
        this.claimRepo = claimRepo;
        this.msgRepo = msgRepo;
        this.attRepo = attRepo;
        this.histRepo = histRepo;
        this.processedRepo = processedRepo;
        this.publisher = publisher;
        this.mapper = mapper;
    }

    @Transactional
    public void ingest(Object payload) {
        if (payload == null) {
            throw new IllegalArgumentException("payload is required");
        }

        // Route based on payload type
        if (payload instanceof ClaimPayload) {
            ClaimPayload p = (ClaimPayload) payload;
            if (p.messageType == null) {
                throw new IllegalArgumentException("messageType is required");
            }

            if ("CLAIM_CREATED".equals(p.messageType)) {
                // 1) DB FIRST
                handleClaimCreated(p);
                // 2) Kafka AFTER COMMIT
                afterCommit(() -> publisher.publishClaimCreated(p));
                return;
            }

            if ("CLAIM_MESSAGE".equals(p.messageType)) {
                // 1) DB FIRST
                handleClaimMessage(p);
                // 2) Kafka AFTER COMMIT
                afterCommit(() -> publisher.publishClaimMessage(p));
                return;
            }

            throw new IllegalArgumentException("Unsupported messageType=" + p.messageType);

        } else if (payload instanceof ServiceResponsePayload) {
            handleServiceResponse((ServiceResponsePayload) payload);
            return;

        } else if (payload instanceof StatusUpdatePayload) {
            handleStatusUpdate((StatusUpdatePayload) payload);
            return;

        } else {
            throw new IllegalArgumentException("Unknown payload type: " + payload.getClass().getName());
        }
    }

    private void handleClaimCreated(ClaimPayload p) {
        if (p.claimId == null || p.claimId.isBlank()) throw new IllegalArgumentException("claimId required");
        if (p.claimNumber == null || p.claimNumber.isBlank()) throw new IllegalArgumentException("claimNumber required");
        if (p.user == null) throw new IllegalArgumentException("user required");
        if (p.claim == null) throw new IllegalArgumentException("claim required");

        // ✅ IMPORTANT : éviter NPE + topic invalide
        if (p.claim.serviceType == null || p.claim.serviceType.isBlank()) {
            throw new IllegalArgumentException("claim.serviceType is required (water/electricity/roads/waste/transport/...)");
        }

        ClaimEntity c = new ClaimEntity();
        c.setId(p.claimId);
        c.setClaimNumber(p.claimNumber);

        c.setUserId(p.user.id);
        c.setUserEmail(p.user.email);
        c.setUserName(p.user.name);
        c.setUserPhone(p.user.phone);

        c.setServiceType(p.claim.serviceType);
        c.setTitle(p.claim.title);
        c.setDescription(p.claim.description);

        if (p.claim.location != null) {
            c.setLocationAddress(p.claim.location.address);

            if (p.claim.location.latitude != null) {
                c.setLocationLat(BigDecimal.valueOf(p.claim.location.latitude));
            }
            if (p.claim.location.longitude != null) {
                c.setLocationLng(BigDecimal.valueOf(p.claim.location.longitude));
            }
        }

        c.setExtraDataJson(toJsonObject(p.claim.extraData));
        c.setStatus(ClaimStatus.SUBMITTED);
        c.setPriority(p.claim.priority == null ? "medium" : p.claim.priority);

        // ✅ Insert claim
        claimRepo.save(c);

        // ✅ Insert attachments
        if (p.claim.attachments != null) {
            for (ClaimPayload.Attachment a : p.claim.attachments) {
                ClaimAttachmentEntity att = new ClaimAttachmentEntity();
                att.setId(UUID.randomUUID().toString());
                att.setClaimId(c.getId());
                att.setFileUrl(a.url);
                att.setFileName(a.fileName);
                att.setFileType(a.fileType);
                att.setUploadedBy(p.user.id);
                attRepo.save(att);
            }
        }

        // ✅ Insert history
        ClaimStatusHistoryEntity h = new ClaimStatusHistoryEntity();
        h.setId(UUID.randomUUID().toString());
        h.setClaimId(c.getId());
        h.setPreviousStatus(null);
        h.setNewStatus(ClaimStatus.SUBMITTED);
        h.setChangedBy(p.user.id);
        h.setChangeReason("Claim created");
        h.setKafkaMessageId(p.messageId);
        histRepo.save(h);
    }

    private void handleClaimMessage(ClaimPayload p) {
        if (p.claimId == null || p.claimId.isBlank()) {
            throw new IllegalArgumentException("claimId required for CLAIM_MESSAGE");
        }

        // ✅ IMPORTANT : si tu publies vers serviceTopic(p.claim.serviceType)
        if (p.claim == null || p.claim.serviceType == null || p.claim.serviceType.isBlank()) {
            throw new IllegalArgumentException("claim.serviceType required for CLAIM_MESSAGE");
        }

        ClaimEntity c = claimRepo.findById(p.claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found: " + p.claimId));

        ClaimMessageEntity m = new ClaimMessageEntity();
        m.setId(UUID.randomUUID().toString());
        m.setClaimId(c.getId());
        m.setSenderType(SenderType.USER);
        m.setSenderId(p.user != null ? p.user.id : null);
        m.setSenderName(p.user != null ? p.user.name : null);

        String msg = (p.claim != null && p.claim.description != null) ? p.claim.description : "";
        m.setMessage(msg);

        List<?> attachments = (p.claim != null ? p.claim.attachments : null);
        m.setAttachmentsJson(toJsonArray(attachments));

        m.setKafkaMessageId(p.messageId);
        msgRepo.save(m);
    }

    private void handleServiceResponse(ServiceResponsePayload p) {
        // Validate required fields
        if (p.claimId == null || p.claimId.isBlank()) {
            throw new IllegalArgumentException("claimId required for SERVICE_RESPONSE");
        }
        if (p.response == null) {
            throw new IllegalArgumentException("response required for SERVICE_RESPONSE");
        }

        // Check for duplicate processing
        if (p.messageId != null && processedRepo.existsByMessageId(p.messageId)) {
            System.out.println("Duplicate SERVICE_RESPONSE messageId=" + p.messageId);
            return;
        }

        // Find existing claim
        ClaimEntity claim = claimRepo.findById(p.claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found: " + p.claimId));

        // Create message from service operator
        ClaimMessageEntity msg = new ClaimMessageEntity();
        msg.setId(UUID.randomUUID().toString());
        msg.setClaimId(p.claimId);
        msg.setSenderType(SenderType.SERVICE);
        msg.setSenderId(p.response.from != null ? p.response.from.operatorId : "system");
        msg.setSenderName(p.response.from != null ? p.response.from.operatorName : "Service");
        msg.setMessage(p.response.message != null ? p.response.message : "");
        msg.setKafkaMessageId(p.messageId);

        // Store attachments as JSON
        if (p.response.attachments != null && !p.response.attachments.isEmpty()) {
            msg.setAttachmentsJson(toJsonArray(p.response.attachments));
        }

        msgRepo.save(msg);

        // Update service reference if provided
        if (p.serviceReference != null && !p.serviceReference.isBlank()) {
            claim.setServiceReference(p.serviceReference);
            claimRepo.save(claim);
        }

        // Mark as processed
        if (p.messageId != null) {
            ProcessedKafkaMessageEntity processed = new ProcessedKafkaMessageEntity();
            processed.setId(UUID.randomUUID().toString());
            processed.setMessageId(p.messageId);
            processed.setTopic("claims.responses");
            processed.setProcessedAt(Instant.now());
            processedRepo.save(processed);
        }

        System.out.println("SERVICE_RESPONSE processed: " + p.messageId);
    }

    private void handleStatusUpdate(StatusUpdatePayload p) {
        // Validate required fields
        if (p.claimId == null || p.claimId.isBlank()) {
            throw new IllegalArgumentException("claimId required for STATUS_UPDATE");
        }
        if (p.status == null) {
            throw new IllegalArgumentException("status required for STATUS_UPDATE");
        }
        if (p.status.newStatus == null || p.status.newStatus.isBlank()) {
            throw new IllegalArgumentException("status.new required for STATUS_UPDATE");
        }

        // Check for duplicate processing
        if (p.messageId != null && processedRepo.existsByMessageId(p.messageId)) {
            System.out.println("Duplicate STATUS_UPDATE messageId=" + p.messageId);
            return;
        }

        // Find existing claim
        ClaimEntity claim = claimRepo.findById(p.claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found: " + p.claimId));

        ClaimStatus previousStatus = claim.getStatus();
        ClaimStatus newStatus = ClaimStatus.fromValue(p.status.newStatus);

        // Update claim status
        claim.setStatus(newStatus);

        // Update assigned operator if provided
        if (p.status.assignedTo != null && p.status.assignedTo.operatorName != null) {
            claim.setAssignedOperator(p.status.assignedTo.operatorName);
        }

        // Update service reference if provided
        if (p.serviceReference != null && !p.serviceReference.isBlank()) {
            claim.setServiceReference(p.serviceReference);
        }

        // Handle resolution data when status becomes "resolved"
        if (ClaimStatus.RESOLVED.equals(newStatus)) {
            claim.setResolvedAt(Instant.now());
            // Note: Resolution details from p.resolution could be stored in extraDataJson if needed
        }

        claimRepo.save(claim);

        // Create status history entry
        ClaimStatusHistoryEntity hist = new ClaimStatusHistoryEntity();
        hist.setId(UUID.randomUUID().toString());
        hist.setClaimId(p.claimId);
        hist.setPreviousStatus(previousStatus);
        hist.setNewStatus(newStatus);
        hist.setChangedBy(p.status.assignedTo != null ? p.status.assignedTo.operatorId : "system");
        hist.setChangeReason(p.status.reason);
        hist.setKafkaMessageId(p.messageId);
        histRepo.save(hist);

        // Mark as processed
        if (p.messageId != null) {
            ProcessedKafkaMessageEntity processed = new ProcessedKafkaMessageEntity();
            processed.setId(UUID.randomUUID().toString());
            processed.setMessageId(p.messageId);
            processed.setTopic("claims.status-updates");
            processed.setProcessedAt(Instant.now());
            processedRepo.save(processed);
        }

        System.out.println("STATUS_UPDATE processed: " + newStatus + " for claim " + p.claimId);
    }

    private void afterCommit(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            // si jamais on n'est pas dans une transaction, on exécute direct
            action.run();
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    action.run();
                } catch (Exception e) {
                    // ✅ Ici, on ne rollback plus rien : la BD est déjà commit.
                    // Tu peux logger l’erreur ou envoyer vers un dead-letter si tu veux.
                    System.err.println("Kafka publish failed AFTER COMMIT: " + e.getMessage());
                }
            }
        });
    }

    private String toJsonObject(Object obj) {
        try {
            if (obj == null) return "{}";
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String toJsonArray(Object obj) {
        try {
            if (obj == null) return "[]";
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
