package kharchafi.claims_management_back_end.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class StatusUpdatePayload {
    public String messageId;
    public String messageType; // STATUS_UPDATE
    public String timestamp;
    public String version;

    public String claimId;
    public String claimNumber;
    public String correlationId;

    public Status status;
    public Resolution resolution;
    public String serviceReference;

    public static class Status {
        public String previous;

        @JsonProperty("newStatus") // OU supprime l'annotation carrément
        public String newStatus;

        public String reason;
        public AssignedTo assignedTo;
    }

    public static class AssignedTo {
        public String operatorId;
        public String operatorName;
    }

    public static class Resolution {
        public String summary;
        public List<String> actionsTaken;
        public String closingMessage;
    }
}