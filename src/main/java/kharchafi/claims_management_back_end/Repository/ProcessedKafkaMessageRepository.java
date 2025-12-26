package kharchafi.claims_management_back_end.Repository;


import kharchafi.claims_management_back_end.Entity.ProcessedKafkaMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedKafkaMessageRepository extends JpaRepository<ProcessedKafkaMessageEntity, String> {

    boolean existsByMessageId(String messageId);
}
