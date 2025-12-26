package kharchafi.claims_management_back_end.Repository;


import kharchafi.claims_management_back_end.Entity.ClaimEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClaimRepository extends JpaRepository<ClaimEntity, String> {

    Optional<ClaimEntity> findByClaimNumber(String claimNumber);

    boolean existsByClaimNumber(String claimNumber);

    List<ClaimEntity> findByUserIdOrderByCreatedAtDesc(String userId);
}
