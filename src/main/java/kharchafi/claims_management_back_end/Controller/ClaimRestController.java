package kharchafi.claims_management_back_end.Controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import kharchafi.claims_management_back_end.DTO.ClaimPayload;
import kharchafi.claims_management_back_end.DTO.ServiceResponsePayload;
import kharchafi.claims_management_back_end.DTO.StatusUpdatePayload;
import kharchafi.claims_management_back_end.Entity.ClaimAttachmentEntity;
import kharchafi.claims_management_back_end.Entity.ClaimEntity;
import kharchafi.claims_management_back_end.Entity.ClaimMessageEntity;
import kharchafi.claims_management_back_end.Repository.ClaimAttachmentRepository;
import kharchafi.claims_management_back_end.Repository.ClaimMessageRepository;
import kharchafi.claims_management_back_end.Repository.ClaimRepository;
import kharchafi.claims_management_back_end.Service.ClaimIngestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/claims")
public class ClaimRestController {

    private final ClaimIngestService ingestService;
    private final ClaimRepository claimRepo;
    private final ClaimMessageRepository messageRepo;
    private final ClaimAttachmentRepository attachmentRepo;
    private final ObjectMapper objectMapper;

    public ClaimRestController(
            ClaimIngestService ingestService,
            ClaimRepository claimRepo,
            ClaimMessageRepository messageRepo,
            ClaimAttachmentRepository attachmentRepo,
            ObjectMapper objectMapper) {
        this.ingestService = ingestService;
        this.claimRepo = claimRepo;
        this.messageRepo = messageRepo;
        this.attachmentRepo = attachmentRepo;
        this.objectMapper = objectMapper;
    }

    // POST /api/claims - Create new claim
    @PostMapping
    public ResponseEntity<?> createClaim(@RequestBody ClaimPayload payload) {
        // Generate IDs
        String claimId = UUID.randomUUID().toString();
        String claimNumber = "CLM-" + System.currentTimeMillis();
        String messageId = UUID.randomUUID().toString();

        // Set IDs in payload
        payload.messageId = messageId;
        payload.messageType = "CLAIM_CREATED";
        payload.timestamp = Instant.now().toString();
        payload.claimId = claimId;
        payload.claimNumber = claimNumber;

        // Use existing ingest service
        ingestService.ingest(payload);

        // Return frontend-expected response
        return ResponseEntity.ok(Map.of(
                "claimId", claimId,
                "claimNumber", claimNumber,
                "message", "Claim submitted successfully"
        ));
    }

    // GET /api/claims - Get all claims for user
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getUserClaims(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        // For now, use a test user ID (skip JWT validation)
        String userId = extractUserIdFromToken(authHeader);

        List<ClaimEntity> claims = claimRepo.findByUserIdOrderByCreatedAtDesc(userId);
        List<Map<String, Object>> dtos = claims.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    // GET /api/claims/{claimId} - Get single claim with messages
    @GetMapping("/{claimId}")
    public ResponseEntity<Map<String, Object>> getClaimById(@PathVariable String claimId) {
        ClaimEntity claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Claim not found"));

        Map<String, Object> dto = convertToDTO(claim);

        // Load messages
        List<ClaimMessageEntity> messages = messageRepo.findByClaimIdOrderByCreatedAtAsc(claimId);
        dto.put("messages", messages.stream()
                .map(this::convertMessageToDTO)
                .collect(Collectors.toList()));

        return ResponseEntity.ok(dto);
    }

    // POST /api/claims/{claimId}/messages - Send message
    @PostMapping("/{claimId}/messages")
    public ResponseEntity<?> sendMessage(
            @PathVariable String claimId,
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        String userId = extractUserIdFromToken(authHeader);

        // Verify claim exists
        ClaimEntity claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Claim not found"));

        // Create ClaimPayload for CLAIM_MESSAGE
        ClaimPayload payload = new ClaimPayload();
        payload.messageId = UUID.randomUUID().toString();
        payload.messageType = "CLAIM_MESSAGE";
        payload.timestamp = Instant.now().toString();
        payload.claimId = claimId;

        payload.user = new ClaimPayload.User();
        payload.user.id = userId;
        payload.user.name = claim.getUserName();
        payload.user.email = claim.getUserEmail();

        payload.claim = new ClaimPayload.Claim();
        payload.claim.serviceType = claim.getServiceType();
        payload.claim.description = (String) request.get("message");

        @SuppressWarnings("unchecked")
        List<Map<String, String>> attachments = (List<Map<String, String>>) request.get("attachments");
        if (attachments != null) {
            payload.claim.attachments = attachments.stream()
                    .map(att -> {
                        ClaimPayload.Attachment a = new ClaimPayload.Attachment();
                        a.url = att.get("url");
                        a.fileName = att.get("fileName");
                        a.fileType = att.get("fileType");
                        return a;
                    })
                    .collect(Collectors.toList());
        }

        // Use existing ingest service
        ingestService.ingest(payload);

        return ResponseEntity.ok(Map.of(
                "messageId", payload.messageId,
                "timestamp", payload.timestamp
        ));
    }

    // POST /api/claims/{claimId}/service-response - Simulate service response (for testing)
    @PostMapping("/{claimId}/service-response")
    public ResponseEntity<?> simulateServiceResponse(
            @PathVariable String claimId,
            @RequestBody Map<String, Object> request) {

        ServiceResponsePayload payload = new ServiceResponsePayload();
        payload.messageId = UUID.randomUUID().toString();
        payload.messageType = "SERVICE_RESPONSE";
        payload.timestamp = Instant.now().toString();
        payload.claimId = claimId;

        payload.response = new ServiceResponsePayload.Response();
        payload.response.from = new ServiceResponsePayload.From();
        payload.response.from.serviceType = (String) request.get("serviceType");
        payload.response.from.operatorId = (String) request.get("operatorId");
        payload.response.from.operatorName = (String) request.get("operatorName");
        payload.response.message = (String) request.get("message");
        payload.serviceReference = (String) request.get("serviceReference");

        ingestService.ingest(payload);

        return ResponseEntity.ok(Map.of(
                "messageId", payload.messageId,
                "timestamp", payload.timestamp
        ));
    }

    // POST /api/claims/{claimId}/status-update - Simulate status update (for testing)
    @PostMapping("/{claimId}/status-update")
    public ResponseEntity<?> simulateStatusUpdate(
            @PathVariable String claimId,
            @RequestBody Map<String, Object> request) {

        StatusUpdatePayload payload = new StatusUpdatePayload();
        payload.messageId = UUID.randomUUID().toString();
        payload.messageType = "STATUS_UPDATE";
        payload.timestamp = Instant.now().toString();
        payload.claimId = claimId;

        payload.status = new StatusUpdatePayload.Status();
        payload.status.previous = (String) request.get("previousStatus");
        payload.status.newStatus = (String) request.get("newStatus");
        payload.status.reason = (String) request.get("reason");

        if (request.containsKey("operatorId")) {
            payload.status.assignedTo = new StatusUpdatePayload.AssignedTo();
            payload.status.assignedTo.operatorId = (String) request.get("operatorId");
            payload.status.assignedTo.operatorName = (String) request.get("operatorName");
        }

        ingestService.ingest(payload);

        return ResponseEntity.ok(Map.of(
                "messageId", payload.messageId,
                "timestamp", payload.timestamp,
                "newStatus", payload.status.newStatus
        ));
    }

    // Helper: Extract user ID from JWT token
    private String extractUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // For testing without auth, return test user ID
            System.out.println("No auth header provided - using test user ID");
            return "test-user-123";
        }

        try {
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            DecodedJWT jwt = JWT.decode(token);
            String userId = jwt.getSubject(); // Get "sub" claim (Clerk user ID)

            System.out.println("Extracted user ID from JWT: " + userId);
            return userId;
        } catch (Exception e) {
            System.err.println("Failed to decode JWT token: " + e.getMessage());
            // Fallback to test user ID if token is invalid
            return "test-user-123";
        }
    }

    // Helper: Convert ClaimEntity to DTO map
    private Map<String, Object> convertToDTO(ClaimEntity entity) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", entity.getId());
        dto.put("claimNumber", entity.getClaimNumber());
        dto.put("userId", entity.getUserId());
        dto.put("serviceType", entity.getServiceType());
        dto.put("serviceName", getServiceName(entity.getServiceType()));
        dto.put("title", entity.getTitle());
        dto.put("description", entity.getDescription());
        dto.put("location", entity.getLocationAddress());
        dto.put("latitude", entity.getLocationLat());
        dto.put("longitude", entity.getLocationLng());
        dto.put("priority", entity.getPriority());
        dto.put("status", entity.getStatus().getValue());
        dto.put("createdAt", entity.getCreatedAt().toString());
        dto.put("updatedAt", entity.getUpdatedAt().toString());

        // Parse extraData JSON
        try {
            if (entity.getExtraDataJson() != null && !entity.getExtraDataJson().isEmpty()) {
                dto.put("extraData", objectMapper.readValue(entity.getExtraDataJson(), Map.class));
            } else {
                dto.put("extraData", Map.of());
            }
        } catch (Exception e) {
            dto.put("extraData", Map.of());
        }

        // Load attachments (images)
        List<ClaimAttachmentEntity> attachments = attachmentRepo.findByClaimId(entity.getId());
        dto.put("images", attachments.stream()
                .map(ClaimAttachmentEntity::getFileUrl)
                .collect(Collectors.toList()));

        dto.put("messages", List.of()); // Will be populated in detail view

        return dto;
    }

    // Helper: Convert message entity to DTO
    private Map<String, Object> convertMessageToDTO(ClaimMessageEntity entity) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", entity.getId());
        dto.put("claimId", entity.getClaimId());
        dto.put("senderId", entity.getSenderId());
        dto.put("senderName", entity.getSenderName());
        dto.put("senderType", entity.getSenderType().getValue());
        dto.put("content", entity.getMessage());
        dto.put("timestamp", entity.getCreatedAt().toString());

        // Parse attachments JSON
        try {
            if (entity.getAttachmentsJson() != null && !entity.getAttachmentsJson().isEmpty()) {
                dto.put("attachments", objectMapper.readValue(entity.getAttachmentsJson(), List.class));
            } else {
                dto.put("attachments", List.of());
            }
        } catch (Exception e) {
            dto.put("attachments", List.of());
        }

        return dto;
    }

    // Helper: Map service type to display name
    private String getServiceName(String serviceType) {
        Map<String, String> names = Map.of(
                "water", "Water Management",
                "electricity", "Electricity",
                "waste", "Waste Management",
                "transport", "Transportation",
                "roads", "Road Maintenance",
                "health", "Health Services",
                "education", "Education"
        );
        return names.getOrDefault(serviceType, serviceType);
    }
}
