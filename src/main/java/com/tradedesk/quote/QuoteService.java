package com.tradedesk.quote;

import java.math.BigDecimal;
import java.util.Locale;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradedesk.order.TradingOrderService;

@Service
public class QuoteService {
    private final QuoteRepository quotes;
    private final SimpMessagingTemplate messaging;
    private final TradingOrderService orderService;

    public QuoteService(QuoteRepository quotes, SimpMessagingTemplate messaging,
                       TradingOrderService orderService) {
        this.quotes = quotes;
        this.messaging = messaging;
        this.orderService = orderService;
    }

    @Cacheable(cacheNames = "quotes", key = "#symbol.toUpperCase()")
    @Transactional(readOnly = true)
    public Quote get(String symbol) {
        return quotes.findBySymbol(symbol.toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new com.tradedesk.shared.BusinessException("Quote not found"));
    }

    @CachePut(cacheNames = "quotes", key = "#result.symbol")
    @Transactional
    public Quote update(String symbol, BigDecimal price) {
        String normalizedSymbol = symbol.toUpperCase(Locale.ROOT);
        Quote quote = quotes.findBySymbol(normalizedSymbol)
                .map(existing -> {
                    existing.updatePrice(price);
                    return existing;
                })
                .orElseGet(() -> new Quote(normalizedSymbol, price));
        Quote saved = quotes.save(quote);
        orderService.matchOpenOrders(normalizedSymbol, price);
        messaging.convertAndSend("/topic/quotes/" + normalizedSymbol, saved);
        return saved;
    }
}
