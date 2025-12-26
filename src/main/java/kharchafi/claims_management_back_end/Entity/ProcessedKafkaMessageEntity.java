package kharchafi.claims_management_back_end.Entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "processed_kafka_messages",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_processed_msg", columnNames = "message_id")
        }
)
public class ProcessedKafkaMessageEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "message_id", length = 255, nullable = false)
    private String messageId;

    @Column(name = "topic", length = 255, nullable = false)
    private String topic;

    @Column(name = "partition_no")
    private Integer partitionNo;

    @Column(name = "offset_no")
    private Long offsetNo;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    @PrePersist
    public void prePersist() {
        if (processedAt == null) processedAt = Instant.now();
    }

    // Getters/Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public Integer getPartitionNo() { return partitionNo; }
    public void setPartitionNo(Integer partitionNo) { this.partitionNo = partitionNo; }

    public Long getOffsetNo() { return offsetNo; }
    public void setOffsetNo(Long offsetNo) { this.offsetNo = offsetNo; }

    public Instant getProcessedAt() { return processedAt; }
    public void setProcessedAt(Instant processedAt) { this.processedAt = processedAt; }
}
