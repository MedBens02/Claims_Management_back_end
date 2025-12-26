package kharchafi.claims_management_back_end.Entity;


import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "claim_attachments",
        indexes = {
                @Index(name = "fk_att_claim", columnList = "claim_id")
        }
)
public class ClaimAttachmentEntity {

    @Id
    @Column(name = "id", length = 36, columnDefinition = "char(36)")
    private String id;


    @Column(name = "claim_id", length = 36, columnDefinition = "char(36)", nullable = false)
    private String claimId;


    @Lob
    @Column(name = "file_url", nullable = false, columnDefinition = "TEXT")
    private String fileUrl;


    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "file_type", length = 100)
    private String fileType;

    @Column(name = "file_size")
    private Integer fileSize;

    @Column(name = "uploaded_by", length = 255, nullable = false)
    private String uploadedBy;

    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt;

    @PrePersist
    public void prePersist() {
        if (uploadedAt == null) uploadedAt = Instant.now();
    }

    // Getters/Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getClaimId() { return claimId; }
    public void setClaimId(String claimId) { this.claimId = claimId; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public Integer getFileSize() { return fileSize; }
    public void setFileSize(Integer fileSize) { this.fileSize = fileSize; }

    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }

    public Instant getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(Instant uploadedAt) { this.uploadedAt = uploadedAt; }
}

