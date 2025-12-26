package kharchafi.claims_management_back_end.Entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "service_types",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_service_code", columnNames = "code")
        }
)
public class ServiceTypeEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "code", length = 100, nullable = false)
    private String code;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Lob
    @Column(name = "description")
    private String description;

    @Column(name = "kafka_topic", length = 255, nullable = false)
    private String kafkaTopic;

    @Lob
    @Column(name = "service_url")
    private String serviceUrl;

    @Column(name = "icon", length = 100)
    private String icon;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // JSON stored as LONGTEXT
    @Lob
    @Column(name = "extra_fields_schema", nullable = false, columnDefinition = "LONGTEXT")
    private String extraFieldsSchemaJson;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
        if (extraFieldsSchemaJson == null) extraFieldsSchemaJson = "{}";
        if (isActive == null) isActive = true;
    }

    // Getters/Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getKafkaTopic() { return kafkaTopic; }
    public void setKafkaTopic(String kafkaTopic) { this.kafkaTopic = kafkaTopic; }

    public String getServiceUrl() { return serviceUrl; }
    public void setServiceUrl(String serviceUrl) { this.serviceUrl = serviceUrl; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean active) { isActive = active; }

    public String getExtraFieldsSchemaJson() { return extraFieldsSchemaJson; }
    public void setExtraFieldsSchemaJson(String extraFieldsSchemaJson) { this.extraFieldsSchemaJson = extraFieldsSchemaJson; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
