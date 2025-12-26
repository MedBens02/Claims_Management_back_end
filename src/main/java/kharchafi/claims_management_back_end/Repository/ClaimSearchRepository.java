package kharchafi.claims_management_back_end.Repository;


import kharchafi.claims_management_back_end.Entity.ClaimEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaimSearchRepository extends JpaRepository<ClaimEntity, String> {

    Page<ClaimEntity> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    Page<ClaimEntity> findByUserIdAndStatusOrderByCreatedAtDesc(String userId, String status, Pageable pageable);

    Page<ClaimEntity> findByUserIdAndServiceTypeOrderByCreatedAtDesc(String userId, String serviceType, Pageable pageable);

    Page<ClaimEntity> findByUserIdAndStatusAndServiceTypeOrderByCreatedAtDesc(
            String userId, String status, String serviceType, Pageable pageable
    );
}
