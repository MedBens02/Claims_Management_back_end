package kharchafi.claims_management_back_end.Repository;


import kharchafi.claims_management_back_end.Entity.ClaimAttachmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClaimAttachmentRepository extends JpaRepository<ClaimAttachmentEntity, String> {

    List<ClaimAttachmentEntity> findByClaimId(String claimId);

    void deleteByClaimId(String claimId);
}
