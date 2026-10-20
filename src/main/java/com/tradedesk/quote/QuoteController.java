package com.tradedesk.quote;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/quotes")
public class QuoteController {
    private final QuoteService quoteService;

    public QuoteController(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @GetMapping("/{symbol}")
    public Quote get(@PathVariable String symbol) {
        return quoteService.get(symbol);
    }

    @PutMapping("/{symbol}")
    @PreAuthorize("hasRole('ADMIN')")
    public Quote update(@PathVariable String symbol, @Valid @RequestBody QuoteRequest request) {
        return quoteService.update(symbol, request.price());
    }
}
