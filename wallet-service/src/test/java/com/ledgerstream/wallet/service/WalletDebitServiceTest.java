package com.ledgerstream.wallet.service;

import com.ledgerstream.wallet.event.WalletDebitedEvent;
import com.ledgerstream.wallet.outbox.WalletOutboxService;
import com.ledgerstream.wallet.repository.WalletRepository;
import com.ledgerstream.wallet.repository.WalletTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WalletDebitServiceTest {

    @Mock
    private WalletRepository walletRepository;
    @Mock
    private WalletOutboxService walletOutboxService;
    @Mock
    private WalletTransactionRepository walletTransactionRepository;
    @InjectMocks
    private WalletDebitService walletDebitService;

    @Test
    void successfulDebitCreatesOutboxEvent(){
        when(walletRepository.debitIfSufficient("wallet-101",new BigDecimal("500")))
                .thenReturn(1);
        when(walletRepository.findBalance("wallet-101"))
                .thenReturn(new BigDecimal("9500.00"));

        WalletDebitedEvent event = walletDebitService.debit("wallet-101",new BigDecimal("500"));

        assertEquals("wallet-101",event.walletId());
        assertEquals(0, new BigDecimal("500").compareTo(event.amount()));
        verify(walletTransactionRepository).saveDebit(
                eq("wallet-101"),
                eq(event.eventId()),
                eq(new BigDecimal("500")),
                eq(new BigDecimal("9500.00"))
        );
        verify(walletOutboxService).saveEvent(event);
    }

    @Test
    void insufficientBalanceDoesNotCreateEvent(){
        when(walletRepository.debitIfSufficient("wallet-101",new BigDecimal("20000")))
                .thenReturn(0);

        when(walletRepository.existsById("wallet-101"))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                ()->walletDebitService.debit("wallet-101",new BigDecimal("20000"))
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(walletOutboxService, never()).saveEvent(any(WalletDebitedEvent.class));
    }

    @Test
    void missingWalletReturnsNotFound(){
        when(walletRepository.debitIfSufficient("wallet-404",new BigDecimal("500")))
                .thenReturn(0);

        when(walletRepository.existsById("wallet-404")).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                ()->walletDebitService.debit("wallet-404",new BigDecimal("500"))
        );

        assertEquals(HttpStatus.NOT_FOUND,exception.getStatusCode());
        verify(walletOutboxService,never()).saveEvent(any(WalletDebitedEvent.class));
    }

    @Test
    void zeroAmountIsRejected(){
        assertThrows(
                ResponseStatusException.class,
                () -> walletDebitService.debit(
                        "wallet-101",
                        BigDecimal.ZERO
                )
        );
        verifyNoInteractions(walletRepository,walletOutboxService,walletTransactionRepository);
    }


}
