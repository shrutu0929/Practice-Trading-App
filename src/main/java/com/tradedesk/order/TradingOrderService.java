package com.tradedesk.order;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradedesk.account.TradingAccount;
import com.tradedesk.account.TradingAccountRepository;
import com.tradedesk.account.LedgerEntry;
import com.tradedesk.account.LedgerEntryRepository;
import com.tradedesk.portfolio.Position;
import com.tradedesk.portfolio.PositionRepository;
import com.tradedesk.quote.Quote;
import com.tradedesk.quote.QuoteRepository;
import com.tradedesk.risk.RiskRule;
import com.tradedesk.shared.BusinessException;

@Service
public class TradingOrderService {
    private final TradingOrderRepository orders;
    private final TradingAccountRepository accounts;
    private final LedgerEntryRepository ledger;
    private final PositionRepository positions;
    private final QuoteRepository quotes;
    private final List<RiskRule> riskRules;

    public TradingOrderService(TradingOrderRepository orders, TradingAccountRepository accounts,
                               LedgerEntryRepository ledger, PositionRepository positions, QuoteRepository quotes,
                               List<RiskRule> riskRules) {
        this.orders = orders;
        this.accounts = accounts;
        this.ledger = ledger;
        this.positions = positions;
        this.quotes = quotes;
        this.riskRules = riskRules;
    }

    @Transactional
    public TradingOrder submit(String userId, String idempotencyKey, OrderRequest request) {
        if (idempotencyKey == null || idempotencyKey.isBlank() || idempotencyKey.length() > 100) {
            throw new BusinessException("Idempotency-Key must contain 1 to 100 characters");
        }
        var existing = orders.findByUserIdAndIdempotencyKey(userId, idempotencyKey);
        if (existing.isPresent()) {
            return existing.get();
        }

        TradingAccount account = accounts.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new BusinessException("Create a trading account first"));
        existing = orders.findByUserIdAndIdempotencyKey(userId, idempotencyKey);
        if (existing.isPresent()) {
            return existing.get();
        }
        String symbol = request.symbol().toUpperCase(java.util.Locale.ROOT);
        OrderRequest normalized = new OrderRequest(symbol, request.side(), request.type(),
                request.quantity(), request.limitPrice(), request.stopPrice());
        validatePriceFields(normalized);

        TradingOrder order = new TradingOrder(userId, symbol, request.side(), request.type(),
                request.quantity(), request.limitPrice(), request.stopPrice(), idempotencyKey);
        Quote quote = quotes.findBySymbol(symbol)
                .orElseThrow(() -> new BusinessException("No quote is available for " + symbol));
        BigDecimal executionPrice = quote.getPrice();
        checkRisk(account, order, executionPrice);
        orders.save(order);
        if (order.getType() == OrderType.MARKET) {
            execute(account, order, executionPrice);
        }
        return order;
    }

    @Transactional(readOnly = true)
    public List<TradingOrder> list(String userId) {
        return orders.findAllByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public TradingOrder cancel(String userId, UUID orderId) {
        TradingOrder order = orders.findByIdAndUserIdForUpdate(orderId, userId)
                .orElseThrow(() -> new BusinessException("Order not found"));
        if (order.getStatus() != OrderStatus.OPEN) {
            throw new BusinessException("Only open orders can be cancelled");
        }
        order.cancel();
        return order;
    }

    @Transactional
    public void matchOpenOrders(String symbol, BigDecimal marketPrice) {
        List<TradingOrder> candidates = orders.findAllBySymbolAndStatusAndTypeInForUpdate(
                symbol, OrderStatus.OPEN, List.of(OrderType.LIMIT, OrderType.STOP_LOSS));
        for (TradingOrder order : candidates) {
            if (!order.matches(marketPrice)) {
                continue;
            }
            TradingAccount account = accounts.findByUserIdForUpdate(order.getUserId())
                    .orElseThrow(() -> new BusinessException("Trading account no longer exists"));
            try {
                checkRisk(account, order, marketPrice);
                execute(account, order, marketPrice);
            } catch (BusinessException exception) {
                order.cancel();
            }
        }
    }

    private void checkRisk(TradingAccount account, TradingOrder order, BigDecimal price) {
        for (RiskRule riskRule : riskRules) {
            riskRule.check(account, order, price);
        }
    }

    private void execute(TradingAccount account, TradingOrder order, BigDecimal price) {
        BigDecimal value = price.multiply(order.getQuantity());
        if (order.getSide() == OrderSide.BUY) {
            account.debit(value);
            Position position = positions.findByUserIdAndSymbol(order.getUserId(), order.getSymbol())
                    .orElseGet(() -> positions.save(new Position(order.getUserId(), order.getSymbol(),
                            BigDecimal.ZERO, BigDecimal.ZERO)));
            position.buy(order.getQuantity(), price);
        } else {
            Position position = positions.findByUserIdAndSymbol(order.getUserId(), order.getSymbol())
                    .orElseThrow(() -> new BusinessException("No position available to sell"));
            position.sell(order.getQuantity());
            account.credit(value);
        }
        order.fill(price);
        BigDecimal ledgerAmount = order.getSide() == OrderSide.BUY ? value.negate() : value;
        ledger.save(new LedgerEntry(order.getUserId(), order.getId(), order.getSide().name(),
                ledgerAmount, account.getCashBalance()));
    }

    private void validatePriceFields(OrderRequest request) {
        boolean valid = switch (request.type()) {
            case MARKET -> request.limitPrice() == null && request.stopPrice() == null;
            case LIMIT -> request.limitPrice() != null && request.stopPrice() == null;
            case STOP_LOSS -> request.stopPrice() != null && request.limitPrice() == null;
        };
        if (!valid) {
            throw new BusinessException("Provide only the price field required by the selected order type");
        }
    }
}
