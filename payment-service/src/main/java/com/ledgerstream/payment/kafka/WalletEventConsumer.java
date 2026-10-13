package com.ledgerstream.payment.kafka;

import com.ledgerstream.payment.event.WalletDebitedEvent;
import com.ledgerstream.payment.service.PaymentProcessingService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class WalletEventConsumer {
    private final PaymentProcessingService paymentProcessingService;

    public WalletEventConsumer(PaymentProcessingService paymentProcessingService) {
        this.paymentProcessingService = paymentProcessingService;
    }

    @KafkaListener(topics = "wallet-events")
    public void consume(WalletDebitedEvent event) {
        paymentProcessingService.process(event);
    }
}