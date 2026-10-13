package com.ledgerstream.wallet.outbox;

import com.ledgerstream.wallet.event.WalletDebitedEvent;
import com.ledgerstream.wallet.kafka.WalletEventProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class WalletOutboxRelay {
    private final Logger log = LoggerFactory.getLogger(WalletOutboxRelay.class);

    private final OutboxRepository outboxRepository;
    private final WalletEventProducer walletEventProducer;
    private final ObjectMapper objectMapper;

    public WalletOutboxRelay(OutboxRepository outboxRepository, WalletEventProducer walletEventProducer, ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.walletEventProducer = walletEventProducer;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 1000)
    public void publishPendingEvents(){
        List<PendingOutboxEvent> events = outboxRepository.findPending(100);

        for (PendingOutboxEvent pending: events){
            try{
                WalletDebitedEvent event = objectMapper.readValue(pending.payload(), WalletDebitedEvent.class);

                walletEventProducer.publishWalletDebited(event);

                outboxRepository.markPublished(event.eventId());

                log.info(
                        "Published outbox event {}",
                        pending.eventId()
                );

            } catch (ExecutionException | InterruptedException ex) {
                Thread.currentThread().interrupt();
                log.warn("Outbox relay was interrupted", ex);
                return;
            } catch (Exception ex) {
                log.error(
                        "Could not publish event {}. It remains pending.",
                        pending.eventId(),
                        ex
                );

                // Stop here. Retry this event on the next poll.
                break;
            }
        }
    }
}
