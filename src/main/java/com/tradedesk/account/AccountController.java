package com.tradedesk.account;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tradedesk.shared.BusinessException;

@RestController
@RequestMapping("/api/v1/account")
public class AccountController {
    private static final BigDecimal INITIAL_BALANCE = new BigDecimal("100000.00");
    private final TradingAccountRepository accounts;
    private final LedgerEntryRepository ledger;

    public AccountController(TradingAccountRepository accounts, LedgerEntryRepository ledger) {
        this.accounts = accounts;
        this.ledger = ledger;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public Map<String, Object> create(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        if (accounts.findByUserId(userId).isPresent()) {
            throw new BusinessException("Trading account already exists");
        }
        TradingAccount account = accounts.save(new TradingAccount(userId, INITIAL_BALANCE));
        ledger.save(new LedgerEntry(userId, null, "INITIAL_FUNDING",
                INITIAL_BALANCE, INITIAL_BALANCE));
        return Map.of("userId", account.getUserId(), "cashBalance", account.getCashBalance());
    }

    @GetMapping
    public Map<String, Object> get(@AuthenticationPrincipal Jwt jwt) {
        TradingAccount account = accounts.findByUserId(jwt.getSubject())
                .orElseThrow(() -> new BusinessException("Create a trading account first"));
        return Map.of("userId", account.getUserId(), "cashBalance", account.getCashBalance());
    }
}
