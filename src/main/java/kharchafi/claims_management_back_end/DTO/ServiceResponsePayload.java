package kharchafi.claims_management_back_end.DTO;


import java.util.List;

public class ServiceResponsePayload {
    public String messageId;
    public String messageType;   // SERVICE_RESPONSE | INFO_REQUEST
    public String timestamp;
    public String version;

    public String claimId;
    public String claimNumber;
    public String correlationId;

    public Response response;

    public String serviceReference;

    public static class Response {
        public From from;
        public String message;
        public List<Attachment> attachments;
        public List<RequestedField> requestedFields;
    }

    public static class From {
        public String serviceType;
        public String operatorId;
        public String operatorName;
    }

    public static class Attachment {
        public String url;
        public String fileName;
        public String fileType;
    }

    public static class RequestedField {
        public String fieldName;
        public String fieldLabel;
        public String fieldType; // text|select|file|date
        public boolean required;
        public List<String> options;
    }
}
