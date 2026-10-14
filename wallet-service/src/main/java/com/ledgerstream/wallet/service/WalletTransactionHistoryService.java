package com.ledgerstream.wallet.service;

import com.ledgerstream.wallet.dto.WalletTransactionResponse;
import com.ledgerstream.wallet.repository.WalletRepository;
import com.ledgerstream.wallet.repository.WalletTransactionRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class WalletTransactionHistoryService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;

    public WalletTransactionHistoryService(
            WalletRepository walletRepository,
            WalletTransactionRepository transactionRepository
    ) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<WalletTransactionResponse> getHistory(
            String walletId,
            int limit
    ) {
        if (limit < 1 || limit > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Limit must be between 1 and 100"
            );
        }

        if (!walletRepository.existsById(walletId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Wallet not found"
            );
        }

        return transactionRepository.findRecentByWalletId(
                walletId,
                limit
        );
    }
}