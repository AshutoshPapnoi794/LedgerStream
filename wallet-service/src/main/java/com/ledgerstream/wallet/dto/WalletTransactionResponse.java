package com.ledgerstream.wallet.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record WalletTransactionResponse(
        UUID transactionId,
        UUID eventId,
        String walletId,
        String transactionType,
        BigDecimal amount,
        BigDecimal balanceAfter,
        OffsetDateTime createdAt
) {
}