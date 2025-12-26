package kharchafi.claims_management_back_end.Entity;

import jakarta.persistence.*;
import kharchafi.claims_management_back_end.Enum.ClaimStatus;
import java.time.Instant;

@Entity
@Table(name = "claim_status_history",
        indexes = {
                @Index(name = "idx_hist_claim_id", columnList = "claim_id")
        }
)
public class ClaimStatusHistoryEntity {

    @Id
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private String id;

    @Column(name = "claim_id", length = 36, nullable = false)
    private String claimId;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 32)
    private ClaimStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", length = 32, nullable = false)
    private ClaimStatus newStatus;

    @Column(name = "changed_by", length = 255)
    private String changedBy;

    @Lob
    @Column(name = "change_reason")
    private String changeReason;

    @Column(name = "kafka_message_id", length = 255)
    private String kafkaMessageId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }

    // Getters/Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getClaimId() { return claimId; }
    public void setClaimId(String claimId) { this.claimId = claimId; }

    public ClaimStatus getPreviousStatus() { return previousStatus; }
    public void setPreviousStatus(ClaimStatus previousStatus) { this.previousStatus = previousStatus; }

    public ClaimStatus getNewStatus() { return newStatus; }
    public void setNewStatus(ClaimStatus newStatus) { this.newStatus = newStatus; }

    public String getChangedBy() { return changedBy; }
    public void setChangedBy(String changedBy) { this.changedBy = changedBy; }

    public String getChangeReason() { return changeReason; }
    public void setChangeReason(String changeReason) { this.changeReason = changeReason; }

    public String getKafkaMessageId() { return kafkaMessageId; }
    public void setKafkaMessageId(String kafkaMessageId) { this.kafkaMessageId = kafkaMessageId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
