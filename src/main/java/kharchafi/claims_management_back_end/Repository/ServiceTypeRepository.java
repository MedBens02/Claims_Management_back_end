package kharchafi.claims_management_back_end.Repository;


import kharchafi.claims_management_back_end.Entity.ServiceTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ServiceTypeRepository extends JpaRepository<ServiceTypeEntity, String> {

    Optional<ServiceTypeEntity> findByCode(String code);

    boolean existsByCode(String code);
}
