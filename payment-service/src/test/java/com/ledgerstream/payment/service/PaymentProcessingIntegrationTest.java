package com.ledgerstream.payment.service;

import com.ledgerstream.payment.event.WalletDebitedEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = "spring.kafka.listener.auto-startup=false"
)
public class PaymentProcessingIntegrationTest {
    @Autowired
    private PaymentProcessingService paymentProcessingService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UUID eventId;

    @BeforeEach
    void setUp(){
        eventId=UUID.randomUUID();
    }

    @AfterEach
    void cleanUp(){
        jdbcTemplate.update("Delete FROM payments WHERE event_id=?",eventId);
        jdbcTemplate.update(
                "DELETE FROM processed_events WHERE event_id = ?",
                eventId
        );
    }

    @Test
    void processingSameEventTwiceCreatesOnlyOnePayment(){
        WalletDebitedEvent event = new WalletDebitedEvent(
                eventId,
                "wallet-101",
                new BigDecimal("500.00"),
                Instant.now()
        );

        paymentProcessingService.process(event);
        paymentProcessingService.process(event);

        Integer processedCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM processed_events WHERE event_id = ?",
                Integer.class,
                eventId
        );

        Integer paymentCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM payments WHERE event_id = ?",
                Integer.class,
                eventId
        );

        assertEquals(1,processedCount);
        assertEquals(1,paymentCount);
    }

    @Test
    void failedPaymentInsertRollsBackProcessedEvent(){
        WalletDebitedEvent event = new WalletDebitedEvent(
                eventId,
                "wallet-101",
                null,
                Instant.now()
        );

        assertThrows(
                DataAccessException.class,
                ()->paymentProcessingService.process(event)
        );

        Integer processedCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM processed_events WHERE event_id = ?",
                Integer.class,
                eventId
        );

        assertEquals(0,processedCount);
    }
}
