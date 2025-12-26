package kharchafi.claims_management_back_end.Entity;

import jakarta.persistence.*;
import kharchafi.claims_management_back_end.Enum.SenderType;
import java.time.Instant;

@Entity
@Table(name = "claim_messages",
        indexes = {
                @Index(name = "idx_messages_claim_id", columnList = "claim_id")
        }
)
public class ClaimMessageEntity {

    @Id
    @Column(name = "id", length = 36, columnDefinition = "CHAR(36)")
    private String id;


    @Column(name = "claim_id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private String claimId;


    @Enumerated(EnumType.STRING)
    @Column(name = "sender_type", length = 20, nullable = false)
    private SenderType senderType; // USER|SERVICE|SYSTEM

    @Column(name = "sender_id", length = 255)
    private String senderId;

    @Column(name = "sender_name", length = 255)
    private String senderName;

    @Lob
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    // JSON stored as LONGTEXT
    @Lob
    @Column(name = "attachments", nullable = false, columnDefinition = "LONGTEXT")
    private String attachmentsJson;

    @Column(name = "kafka_message_id", length = 255)
    private String kafkaMessageId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "read_at")
    private Instant readAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
        if (attachmentsJson == null) attachmentsJson = "[]";
    }

    // Getters/Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getClaimId() { return claimId; }
    public void setClaimId(String claimId) { this.claimId = claimId; }

    public SenderType getSenderType() { return senderType; }
    public void setSenderType(SenderType senderType) { this.senderType = senderType; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getAttachmentsJson() { return attachmentsJson; }
    public void setAttachmentsJson(String attachmentsJson) { this.attachmentsJson = attachmentsJson; }

    public String getKafkaMessageId() { return kafkaMessageId; }
    public void setKafkaMessageId(String kafkaMessageId) { this.kafkaMessageId = kafkaMessageId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getReadAt() { return readAt; }
    public void setReadAt(Instant readAt) { this.readAt = readAt; }
}
