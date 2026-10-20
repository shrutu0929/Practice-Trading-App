package com.tradedesk.account;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ledger")
public class LedgerController {
    private final LedgerEntryRepository ledger;

    public LedgerController(LedgerEntryRepository ledger) {
        this.ledger = ledger;
    }

    @GetMapping
    public List<LedgerEntry> list(@AuthenticationPrincipal Jwt jwt) {
        return ledger.findAllByUserIdOrderByCreatedAtDesc(jwt.getSubject());
    }
}
