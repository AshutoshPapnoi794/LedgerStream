package com.ledgerstream.wallet.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.UUID;

@Repository
public class WalletTransactionRepository {

    private final JdbcTemplate jdbcTemplate;

    public WalletTransactionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveDebit(
            String walletId,
            UUID eventId,
            BigDecimal amount,
            BigDecimal balanceAfter
    ){
        jdbcTemplate.update("""
                INSERT INTO wallet_transactions (
                    transaction_id,
                    wallet_id,
                    event_id,
                    transaction_type,
                    amount,
                    balance_after
                )
                VALUES (?, ?, ?, 'DEBIT', ?, ?)
                """,
                UUID.randomUUID(),
                walletId,
                eventId,
                amount,
                balanceAfter
        );
    }
}
