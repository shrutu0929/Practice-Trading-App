package com.tradedesk.risk;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.tradedesk.account.TradingAccount;
import com.tradedesk.order.TradingOrder;
import com.tradedesk.shared.BusinessException;

@Component
public class MaximumOrderValueRule implements RiskRule {
    private final BigDecimal maximumOrderValue;

    public MaximumOrderValueRule(
            @Value("${app.risk.maximum-order-value:1000000}") BigDecimal maximumOrderValue) {
        this.maximumOrderValue = maximumOrderValue;
    }

    @Override
    public void check(TradingAccount account, TradingOrder order, BigDecimal executionPrice) {
        BigDecimal orderValue = executionPrice.multiply(order.getQuantity());
        if (orderValue.compareTo(maximumOrderValue) > 0) {
            throw new BusinessException("Order exceeds the maximum permitted value");
        }
    }
}
