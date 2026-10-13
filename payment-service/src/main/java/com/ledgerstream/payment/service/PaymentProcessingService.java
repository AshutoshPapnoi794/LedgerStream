package com.ledgerstream.payment.service;

import com.ledgerstream.payment.event.WalletDebitedEvent;
import com.ledgerstream.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PaymentProcessingService {
    private final PaymentRepository paymentRepository;

    public PaymentProcessingService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public void process(WalletDebitedEvent event){
        int inserted = paymentRepository.recordEventIfNew(event.eventId());

        if(inserted==0){
            System.out.println(
                    "Duplicate event ignored: " + event.eventId()
            );
            return;
        }

        paymentRepository.savePayment(UUID.randomUUID(), event);

        System.out.println(
                "Payment processed: " + event.eventId()
        );
    }
}
