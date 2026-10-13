package com.ledgerstream.payment.repository;

import com.ledgerstream.payment.event.WalletDebitedEvent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class PaymentRepository {
    private final JdbcTemplate jdbcTemplate;


    public PaymentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int recordEventIfNew(UUID eventId){
        String query = """
                INSERT INTO processed_events (event_id)
                VALUES (?)
                ON CONFLICT (event_id) DO NOTHING
                """;
        return jdbcTemplate.update(query,eventId);
    }

    public void savePayment(UUID paymentId, WalletDebitedEvent event){
        String query = """
                INSERT INTO payments (payment_id,event_id,wallet_id,amount)
                VALUES (?,?,?,?)
                """;
        jdbcTemplate.update(query,paymentId,event.eventId(),event.walletId(),event.amount());
    }
}
