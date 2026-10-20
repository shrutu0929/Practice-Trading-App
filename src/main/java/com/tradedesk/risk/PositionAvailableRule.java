package com.tradedesk.risk;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.tradedesk.account.TradingAccount;
import com.tradedesk.order.OrderSide;
import com.tradedesk.order.TradingOrder;
import com.tradedesk.portfolio.Position;
import com.tradedesk.portfolio.PositionRepository;
import com.tradedesk.shared.BusinessException;

@Component
public class PositionAvailableRule implements RiskRule {
    private final PositionRepository positions;

    public PositionAvailableRule(PositionRepository positions) {
        this.positions = positions;
    }

    @Override
    public void check(TradingAccount account, TradingOrder order, BigDecimal executionPrice) {
        if (order.getSide() == OrderSide.SELL) {
            Position position = positions.findByUserIdAndSymbol(account.getUserId(), order.getSymbol())
                    .orElseThrow(() -> new BusinessException("No position available to sell"));
            if (position.getQuantity().compareTo(order.getQuantity()) < 0) {
                throw new BusinessException("Sell quantity exceeds the available position");
            }
        }
    }
}
