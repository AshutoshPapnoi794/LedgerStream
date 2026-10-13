package com.ledgerstream.wallet.outbox;

import com.ledgerstream.wallet.event.WalletDebitedEvent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class OutboxRepository {
    private final JdbcTemplate jdbcTemplate;

    public OutboxRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(WalletDebitedEvent event, String payload){
        String query = """
                INSERT INTO outbox_events (event_id,aggregate_id, event_type, payload)
                VALUES (?,?,?, CAST(? AS JSONB))
                """;
        jdbcTemplate.update(
                query,
                event.eventId(),
                event.walletId(),
                "WalletDebitedEvent",
                payload
        );
    }

    public List<PendingOutboxEvent> findPending(int limit){
        String query = """
                SELECT event_id, aggregate_id, payload
                FROM outbox_events
                WHERE published_at IS NULL
                ORDER BY created_at ASC, event_id ASC
                LIMIT ?
                """;
        return jdbcTemplate.query(
                query,
                (rs,rowNum)-> new PendingOutboxEvent(
                        rs.getObject("event_id", UUID.class),
                        rs.getString("aggregate_id"),
                        rs.getString("payload")
                ),
                limit
        );
    }

    public void markPublished(UUID eventId){
        String query = """
                UPDATE outbox_events
                SET published_at = CURRENT_TIMESTAMP
                WHERE event_id=?
                AND published_at IS NULL
                """;
        jdbcTemplate.update(
                query,
                eventId
        );
    }
}
