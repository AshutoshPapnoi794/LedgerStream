package com.ledgerstream.wallet.controller;

import com.ledgerstream.wallet.event.WalletDebitedEvent;
import com.ledgerstream.wallet.kafka.WalletEventProducer;
import com.ledgerstream.wallet.outbox.WalletOutboxService;
import com.ledgerstream.wallet.service.WalletDebitService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/wallets")
public class WalletController {

    private final WalletDebitService walletDebitService;

    public WalletController(WalletDebitService walletDebitService) {
        this.walletDebitService = walletDebitService;
    }

    @PostMapping("/{walletId}/debit")
    public ResponseEntity<WalletDebitedEvent> debit(
            @PathVariable String walletId,
            @RequestParam BigDecimal amount
    ) {

        WalletDebitedEvent event = walletDebitService.debit(walletId,amount);

        return ResponseEntity.accepted().body(event);
    }
}