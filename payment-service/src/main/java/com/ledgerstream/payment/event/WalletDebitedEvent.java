package com.ledgerstream.payment.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WalletDebitedEvent(
        UUID eventId,
        String walletId,
        BigDecimal amount,
        Instant occurredAt
) {
}