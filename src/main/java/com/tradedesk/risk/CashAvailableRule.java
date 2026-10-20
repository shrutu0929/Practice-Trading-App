package com.tradedesk.risk;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.tradedesk.account.TradingAccount;
import com.tradedesk.order.OrderSide;
import com.tradedesk.order.TradingOrder;
import com.tradedesk.shared.BusinessException;

@Component
public class CashAvailableRule implements RiskRule {
    @Override
    public void check(TradingAccount account, TradingOrder order, BigDecimal executionPrice) {
        if (order.getSide() == OrderSide.BUY
                && account.getCashBalance().compareTo(executionPrice.multiply(order.getQuantity())) < 0) {
            throw new BusinessException("Insufficient cash to place this order");
        }
    }
}
