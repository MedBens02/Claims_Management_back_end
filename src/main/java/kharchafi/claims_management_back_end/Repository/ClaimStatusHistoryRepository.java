package kharchafi.claims_management_back_end.Repository;


import kharchafi.claims_management_back_end.Entity.ClaimStatusHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClaimStatusHistoryRepository extends JpaRepository<ClaimStatusHistoryEntity, String> {

    List<ClaimStatusHistoryEntity> findByClaimIdOrderByCreatedAtDesc(String claimId);
}
