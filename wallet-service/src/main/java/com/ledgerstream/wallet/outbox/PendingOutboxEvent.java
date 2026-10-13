package com.ledgerstream.wallet.outbox;

import java.util.UUID;

public record PendingOutboxEvent(
        UUID eventId,
        String aggregateId,
        String payload
) {
}