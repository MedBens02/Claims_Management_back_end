package kharchafi.claims_management_back_end.DTO;

import java.util.List;
import java.util.Map;

public class ClaimPayload {
    public String messageId;
    public String messageType; // CLAIM_CREATED | CLAIM_MESSAGE
    public String timestamp;
    public String version;
    public String claimId;
    public String claimNumber;
    public String correlationId;
    public User user;
    public Claim claim;

    // For CLAIM_MESSAGE: message and attachments at root level
    public String message;
    public List<Attachment> attachments;

    public static class User {
        public String id;
        public String email;
        public String name;
        public String phone;
    }

    public static class Claim {
        public String serviceType;
        public String title;
        public String description;
        public String priority; // low|medium|high|urgent
        public Location location;
        public List<Attachment> attachments;
        public Map<String,Object> extraData;
    }

    public static class Location {
        public String address;
        public Double latitude;
        public Double longitude;
    }

    // Moved outside Claim class to be reusable at root level
    public static class Attachment {
        public String url;
        public String fileName;
        public String fileType;
    }
}

