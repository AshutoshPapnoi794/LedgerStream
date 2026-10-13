package com.ledgerstream.wallet.kafka;

import com.ledgerstream.wallet.event.WalletDebitedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
public class WalletEventProducer {

    private static final String TOPIC = "wallet-events";

    private final KafkaTemplate<String, WalletDebitedEvent> kafkaTemplate;

    public WalletEventProducer(
            KafkaTemplate<String, WalletDebitedEvent> kafkaTemplate
    ) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishWalletDebited(WalletDebitedEvent event) throws ExecutionException, InterruptedException {
        kafkaTemplate.send(
                TOPIC,
                event.walletId(),
                event
        ).get();
    }
}