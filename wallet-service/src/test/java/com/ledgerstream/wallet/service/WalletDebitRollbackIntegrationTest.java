package com.ledgerstream.wallet.service;

import com.ledgerstream.wallet.event.WalletDebitedEvent;
import com.ledgerstream.wallet.outbox.WalletOutboxService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = "wallet.outbox.relay.enabled=false"
)
class WalletDebitRollbackIntegrationTest {

    @Autowired
    private WalletDebitService walletDebitService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private WalletOutboxService walletOutboxService;

    private String walletId;

    @BeforeEach
    void setUp() {
        walletId = "rollback-test-" + UUID.randomUUID();

        jdbcTemplate.update("""
                INSERT INTO wallets (wallet_id, balance)
                VALUES (?, ?)
                """,
                walletId,
                new BigDecimal("1000.00")
        );
    }

    @AfterEach
    void cleanUp() {
        jdbcTemplate.update(
                "DELETE FROM outbox_events WHERE aggregate_id = ?",
                walletId
        );

        jdbcTemplate.update(
                "DELETE FROM wallets WHERE wallet_id = ?",
                walletId
        );
    }

    @Test
    void outboxFailureRollsBackWalletBalanceUpdate() {
        doThrow(new IllegalStateException("Simulated outbox failure"))
                .when(walletOutboxService)
                .saveEvent(any(WalletDebitedEvent.class));

        assertThrows(
                IllegalStateException.class,
                () -> walletDebitService.debit(
                        walletId,
                        new BigDecimal("250.00")
                )
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

        Integer transactionCount = jdbcTemplate.queryForObject("""
        SELECT COUNT(*)
        FROM wallet_transactions
        WHERE wallet_id = ?
        """,
                Integer.class,
                walletId
        );

        assertEquals(0, transactionCount);
        assertEquals(
                0,
                new BigDecimal("1000.00").compareTo(balance)
        );

        assertEquals(0, eventCount);
    }
}