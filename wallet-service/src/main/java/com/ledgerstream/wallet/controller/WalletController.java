package com.ledgerstream.wallet.controller;

import com.ledgerstream.wallet.dto.WalletTransactionResponse;
import com.ledgerstream.wallet.event.WalletDebitedEvent;
import com.ledgerstream.wallet.kafka.WalletEventProducer;
import com.ledgerstream.wallet.outbox.WalletOutboxService;
import com.ledgerstream.wallet.service.WalletDebitService;
import com.ledgerstream.wallet.service.WalletTransactionHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/wallets")
public class WalletController {

    private final WalletDebitService walletDebitService;
    private final WalletTransactionHistoryService historyService;

    public WalletController(WalletDebitService walletDebitService, WalletTransactionHistoryService historyService) {
        this.walletDebitService = walletDebitService;
        this.historyService = historyService;
    }

    @PostMapping("/{walletId}/debit")
    public ResponseEntity<WalletDebitedEvent> debit(
            @PathVariable String walletId,
            @RequestParam BigDecimal amount
    ) {

        WalletDebitedEvent event = walletDebitService.debit(walletId,amount);

        return ResponseEntity.accepted().body(event);
    }

    @GetMapping("/{walletId}/transactions")
    public ResponseEntity<List<WalletTransactionResponse>> getTransactions(
            @PathVariable String walletId,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ResponseEntity.ok(
                historyService.getHistory(walletId, limit)
        );
    }
}