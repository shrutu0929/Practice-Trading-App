package com.tradedesk.portfolio;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tradedesk.account.TradingAccountRepository;
import com.tradedesk.quote.QuoteRepository;
import com.tradedesk.shared.BusinessException;

@RestController
@RequestMapping("/api/v1/portfolio")
public class PortfolioController {
    private final PositionRepository positions;
    private final TradingAccountRepository accounts;
    private final QuoteRepository quotes;

    public PortfolioController(PositionRepository positions, TradingAccountRepository accounts,
                               QuoteRepository quotes) {
        this.positions = positions;
        this.accounts = accounts;
        this.quotes = quotes;
    }

    @GetMapping
    public Map<String, Object> get(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        var account = accounts.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("Create a trading account first"));
        List<Map<String, Object>> holdings = positions.findAllByUserIdOrderBySymbol(userId).stream()
                .map(position -> {
                    BigDecimal marketPrice = quotes.findBySymbol(position.getSymbol())
                            .map(quote -> quote.getPrice()).orElse(position.getAveragePrice());
                    BigDecimal marketValue = marketPrice.multiply(position.getQuantity());
                    BigDecimal costBasis = position.getAveragePrice().multiply(position.getQuantity());
                    return Map.<String, Object>of(
                            "symbol", position.getSymbol(),
                            "quantity", position.getQuantity(),
                            "averagePrice", position.getAveragePrice(),
                            "marketPrice", marketPrice,
                            "marketValue", marketValue,
                            "unrealizedPnl", marketValue.subtract(costBasis));
                }).toList();
        BigDecimal holdingsValue = holdings.stream()
                .map(item -> (BigDecimal) item.get("marketValue"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return Map.of("userId", userId, "cashBalance", account.getCashBalance(),
                "positions", holdings, "equity", account.getCashBalance().add(holdingsValue));
    }
}
