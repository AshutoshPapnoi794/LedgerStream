package com.ledgerstream.wallet.repository;

import com.ledgerstream.wallet.dto.WalletTransactionResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
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

    public List<WalletTransactionResponse> findRecentByWalletId(
            String walletId,
            int limit
    ) {
        return jdbcTemplate.query("""
            SELECT
                transaction_id,
                event_id,
                wallet_id,
                transaction_type,
                amount,
                balance_after,
                created_at
            FROM wallet_transactions
            WHERE wallet_id = ?
            ORDER BY created_at DESC, transaction_id DESC
            LIMIT ?
            """,
                (rs, rowNum) -> new WalletTransactionResponse(
                        rs.getObject("transaction_id", UUID.class),
                        rs.getObject("event_id", UUID.class),
                        rs.getString("wallet_id"),
                        rs.getString("transaction_type"),
                        rs.getBigDecimal("amount"),
                        rs.getBigDecimal("balance_after"),
                        rs.getObject("created_at", OffsetDateTime.class)
                ),
                walletId,
                limit
        );
    }
}
