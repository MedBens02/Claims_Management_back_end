package kharchafi.claims_management_back_end.Controller;

import kharchafi.claims_management_back_end.DTO.ClaimPayload;
import kharchafi.claims_management_back_end.Service.ClaimIngestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;


@RestController
@RequestMapping("/api/claims")
public class ClaimIngestController {

    private final ClaimIngestService ingestService;

    public ClaimIngestController(ClaimIngestService ingestService) {
        this.ingestService = ingestService;
    }

    // Ton besoin exact : recevoir le JSON standard
    @PostMapping("/ingest")
    public ResponseEntity<?> ingest(@RequestBody ClaimPayload payload) {
        ingestService.ingest(payload);
        return ResponseEntity.ok(Map.of("status","OK"));
    }
}
