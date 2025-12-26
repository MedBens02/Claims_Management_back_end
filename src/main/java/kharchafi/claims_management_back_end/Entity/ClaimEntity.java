package kharchafi.claims_management_back_end.Entity;

import jakarta.persistence.*;
import kharchafi.claims_management_back_end.Enum.ClaimStatus;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "claims",
        indexes = {
                @Index(name = "idx_claims_user_id", columnList = "user_id"),
                @Index(name = "idx_claims_status", columnList = "status"),
                @Index(name = "idx_claims_service_type", columnList = "service_type"),
                @Index(name = "idx_claims_created_at", columnList = "created_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_claim_number", columnNames = "claim_number")
        }
)
public class ClaimEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "claim_number", length = 20, nullable = false)
    private String claimNumber;

    @Column(name = "user_id", length = 255, nullable = false)
    private String userId;

    @Column(name = "user_email", length = 255, nullable = false)
    private String userEmail;

    @Column(name = "user_name", length = 255)
    private String userName;

    @Column(name = "user_phone", length = 50)
    private String userPhone;

    @Column(name = "service_type", length = 100, nullable = false)
    private String serviceType;

    @Column(name = "title", length = 500, nullable = false)
    private String title;

    @Lob
    @Column(name = "description", nullable = false)
    private String description;

    @Lob
    @Column(name = "location_address")
    private String locationAddress;

    @Column(name = "location_lat", precision = 10, scale = 8)
    private BigDecimal locationLat;

    @Column(name = "location_lng", precision = 11, scale = 8)
    private BigDecimal locationLng;

    // JSON stored as LONGTEXT
    @Lob
    @Column(name = "extra_data", nullable = false, columnDefinition = "LONGTEXT")
    private String extraDataJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 32, nullable = false)
    private ClaimStatus status;

    @Column(name = "priority", length = 16, nullable = false)
    private String priority;

    @Column(name = "assigned_operator", length = 255)
    private String assignedOperator;

    @Column(name = "service_reference", length = 255)
    private String serviceReference;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "review_requested_at")
    private Instant reviewRequestedAt;

    @Column(name = "review_submitted_at")
    private Instant reviewSubmittedAt;

    @Column(name = "review_rating")
    private Integer reviewRating;

    @Lob
    @Column(name = "review_comment")
    private String reviewComment;

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (extraDataJson == null) extraDataJson = "{}";
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
        if (extraDataJson == null) extraDataJson = "{}";
    }

    // Getters/Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getClaimNumber() { return claimNumber; }
    public void setClaimNumber(String claimNumber) { this.claimNumber = claimNumber; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserPhone() { return userPhone; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }

    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocationAddress() { return locationAddress; }
    public void setLocationAddress(String locationAddress) { this.locationAddress = locationAddress; }

    public BigDecimal getLocationLat() { return locationLat; }
    public void setLocationLat(BigDecimal locationLat) { this.locationLat = locationLat; }

    public BigDecimal getLocationLng() { return locationLng; }
    public void setLocationLng(BigDecimal locationLng) { this.locationLng = locationLng; }

    public String getExtraDataJson() { return extraDataJson; }
    public void setExtraDataJson(String extraDataJson) { this.extraDataJson = extraDataJson; }

    public ClaimStatus getStatus() { return status; }
    public void setStatus(ClaimStatus status) { this.status = status; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getAssignedOperator() { return assignedOperator; }
    public void setAssignedOperator(String assignedOperator) { this.assignedOperator = assignedOperator; }

    public String getServiceReference() { return serviceReference; }
    public void setServiceReference(String serviceReference) { this.serviceReference = serviceReference; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public Instant getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Instant resolvedAt) { this.resolvedAt = resolvedAt; }

    public Instant getClosedAt() { return closedAt; }
    public void setClosedAt(Instant closedAt) { this.closedAt = closedAt; }

    public Instant getReviewRequestedAt() { return reviewRequestedAt; }
    public void setReviewRequestedAt(Instant reviewRequestedAt) { this.reviewRequestedAt = reviewRequestedAt; }

    public Instant getReviewSubmittedAt() { return reviewSubmittedAt; }
    public void setReviewSubmittedAt(Instant reviewSubmittedAt) { this.reviewSubmittedAt = reviewSubmittedAt; }

    public Integer getReviewRating() { return reviewRating; }
    public void setReviewRating(Integer reviewRating) { this.reviewRating = reviewRating; }

    public String getReviewComment() { return reviewComment; }
    public void setReviewComment(String reviewComment) { this.reviewComment = reviewComment; }
}
