package kharchafi.claims_management_back_end.Repository;


import kharchafi.claims_management_back_end.Entity.ClaimMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClaimMessageRepository extends JpaRepository<ClaimMessageEntity, String> {

    List<ClaimMessageEntity> findByClaimIdOrderByCreatedAtAsc(String claimId);

    List<ClaimMessageEntity> findByClaimIdOrderByCreatedAtDesc(String claimId);
}

