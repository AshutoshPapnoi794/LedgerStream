package com.ledgerstream.wallet.service;

import com.ledgerstream.wallet.event.WalletDebitedEvent;
import com.ledgerstream.wallet.outbox.WalletOutboxService;
import com.ledgerstream.wallet.repository.WalletRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
public class WalletDebitService {
    private final WalletRepository walletRepository;
    private final WalletOutboxService walletOutboxService;

    public WalletDebitService(WalletRepository walletRepository, WalletOutboxService walletOutboxService) {
        this.walletRepository = walletRepository;
        this.walletOutboxService = walletOutboxService;
    }

    @Transactional
    public WalletDebitedEvent debit(String walletId, BigDecimal amount){
        if(walletId==null || walletId.isBlank()){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Wallet ID is required"
            );
        }

        if (amount == null || amount.signum() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Amount must be greater than zero"
            );
        }

        if (amount.stripTrailingZeros().scale() > 2) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Amount cannot have more than two decimal places"
            );
        }

        int updated = walletRepository.debitIfSufficient(walletId, amount);

        if(updated==0){
            if (!walletRepository.existsById(walletId)) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Wallet not found"
                );
            }

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Insufficient wallet balance"
            );
        }

        WalletDebitedEvent event = new WalletDebitedEvent(
                UUID.randomUUID(),
                walletId,
                amount,
                Instant.now()
        );
        walletOutboxService.saveEvent(event);

        return event;
    }
}
