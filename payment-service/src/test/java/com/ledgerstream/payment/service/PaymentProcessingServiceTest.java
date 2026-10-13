package com.ledgerstream.payment.service;

import com.ledgerstream.payment.event.WalletDebitedEvent;
import com.ledgerstream.payment.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentProcessingServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentProcessingService paymentProcessingService;

    @Test
    void newEventCreatesPayment(){
        WalletDebitedEvent event = new WalletDebitedEvent(
                UUID.randomUUID(),
                "wallet-101",
                new BigDecimal("500.00"),
                Instant.now()
        );

        when(paymentRepository.recordEventIfNew(event.eventId()))
                .thenReturn(1);

        paymentProcessingService.process(event);

        verify(paymentRepository).savePayment(any(UUID.class),any(WalletDebitedEvent.class));
    }

    @Test
    void duplicateEventDoesNotCreateAnotherPayment(){
        WalletDebitedEvent event = new WalletDebitedEvent(
                UUID.randomUUID(),
                "wallet-101",
                new BigDecimal("500.00"),
                Instant.now()
        );

        when(paymentRepository.recordEventIfNew(event.eventId()))
                .thenReturn(0);

        paymentProcessingService.process(event);

        verify(paymentRepository,never()).savePayment(any(UUID.class),any(WalletDebitedEvent.class));
    }
}
