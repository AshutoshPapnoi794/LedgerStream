package com.ledgerstream.wallet.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public class WalletRepository {
    private final JdbcTemplate jdbcTemplate;

    public WalletRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int debitIfSufficient(String walletId, BigDecimal amount){
        String query = """
                UPDATE wallets
                SET balance = balance-?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE wallet_id = ?
                    AND balance >= ?
                """;

        return jdbcTemplate.update(query,amount,walletId,amount);
    }

    public boolean existsById(String walletId){
        String query = """
                SELECT COUNT(*)
                FROM wallets
                WHERE wallet_id = ?
                """;

        Integer count = jdbcTemplate.queryForObject(query,Integer.class,walletId);

        return count != null && count > 0;
    }

    public BigDecimal findBalance(String walletId) {
        return jdbcTemplate.queryForObject("""
            SELECT balance
            FROM wallets
            WHERE wallet_id = ?
            """,
                BigDecimal.class,
                walletId
        );
    }
}
