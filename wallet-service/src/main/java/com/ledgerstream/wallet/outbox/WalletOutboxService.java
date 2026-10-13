package com.ledgerstream.wallet.outbox;

import com.ledgerstream.wallet.event.WalletDebitedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Service
public class WalletOutboxService {
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public WalletOutboxService(OutboxRepository outboxRepository, ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void saveEvent(WalletDebitedEvent event){
        String payload;

        try{
            payload = objectMapper.writeValueAsString(event);
        } catch (Exception ex){
            throw new IllegalStateException(
                    "Could not serialize wallet event", ex
            );
        }

        outboxRepository.save(event,payload);
    }
}
