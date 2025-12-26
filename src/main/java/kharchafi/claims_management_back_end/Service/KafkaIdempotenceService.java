package kharchafi.claims_management_back_end.Service;


import jakarta.transaction.Transactional;
import kharchafi.claims_management_back_end.Entity.ProcessedKafkaMessageEntity;
import kharchafi.claims_management_back_end.Repository.ProcessedKafkaMessageRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class KafkaIdempotenceService {

    private final ProcessedKafkaMessageRepository repo;

    public KafkaIdempotenceService(ProcessedKafkaMessageRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public boolean markIfNew(String messageId, String topic, Integer partition, Long offset) {
        if (messageId == null || messageId.isBlank()) return true; // pas d'id => on traite
        if (repo.existsByMessageId(messageId)) return false;

        ProcessedKafkaMessageEntity e = new ProcessedKafkaMessageEntity();
        e.setId(UUID.randomUUID().toString());
        e.setMessageId(messageId);
        e.setTopic(topic);
        e.setPartitionNo(partition);
        e.setOffsetNo(offset);
        repo.save(e);
        return true;
    }
}
