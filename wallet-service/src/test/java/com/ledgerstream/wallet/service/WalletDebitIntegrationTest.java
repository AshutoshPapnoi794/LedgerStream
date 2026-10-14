package com.ledgerstream.wallet.service;

import com.ledgerstream.wallet.event.WalletDebitedEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = "wallet.outbox.relay.enabled=false"
)
public class WalletDebitIntegrationTest {
    @Autowired
    private WalletDebitService walletDebitService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String walletId;

    @BeforeEach
    void setUp(){
        walletId = "test-wallet-"+ UUID.randomUUID();

        jdbcTemplate.update("""
                INSERT INTO wallets (wallet_id, balance)
                VALUES (?, ?)
                """,
                walletId,
                new BigDecimal("1000.00")
        );
    }

    @AfterEach
    void cleanUp(){
        jdbcTemplate.update(
                "DELETE FROM outbox_events WHERE aggregate_id = ?",
                walletId
        );

        jdbcTemplate.update(
                "DELETE FROM wallet_transactions WHERE wallet_id = ?",
                walletId
        );

        jdbcTemplate.update("""
                DELETE FROM wallets WHERE wallet_id = ?
                """,
                walletId
        );
    }

    @Test
    void successfulDebitUpdatesBalanceAndCreatesOutboxEvent(){
        WalletDebitedEvent event = walletDebitService.debit(
                walletId,
                new BigDecimal("250.00")
        );

        BigDecimal balance = jdbcTemplate.queryForObject(
                "SELECT balance FROM wallets WHERE wallet_id = ?",
                BigDecimal.class,
                walletId
        );

        Integer eventCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM outbox_events
                WHERE event_id = ?
                """,
                Integer.class,
                event.eventId()
        );

        Integer transactionCount = jdbcTemplate.queryForObject("""
        SELECT COUNT(*)
        FROM wallet_transactions
        WHERE event_id = ?
        """,
                Integer.class,
                event.eventId()
        );

        assertEquals(1, transactionCount);
        assertEquals(0, new BigDecimal("750.00").compareTo(balance));
        assertEquals(1,eventCount);
    }

    @Test
    void insufficientBalanceDoesNotUpdateWalletOrCreateEvent(){

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                ()->walletDebitService.debit(
                        walletId,
                        new BigDecimal("1500.00")
                )
        );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );

        BigDecimal balance = jdbcTemplate.queryForObject(
                "SELECT balance FROM wallets WHERE wallet_id = ?",
                BigDecimal.class,
                walletId
        );

        Integer eventCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM outbox_events
                WHERE aggregate_id = ?
                """,
                Integer.class,
                walletId
        );

        assertEquals(
                0,
                new BigDecimal("1000.00").compareTo(balance)
        );

        assertEquals(0, eventCount);

    }
}
