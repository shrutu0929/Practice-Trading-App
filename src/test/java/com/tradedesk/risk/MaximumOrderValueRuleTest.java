package com.tradedesk.risk;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.tradedesk.account.TradingAccount;
import com.tradedesk.order.OrderSide;
import com.tradedesk.order.OrderType;
import com.tradedesk.order.TradingOrder;
import com.tradedesk.shared.BusinessException;

class MaximumOrderValueRuleTest {
    private final MaximumOrderValueRule rule =
            new MaximumOrderValueRule(new BigDecimal("1000"));

    @Test
    void acceptsOrderAtMaximumValue() {
        TradingAccount account = new TradingAccount("user-1", new BigDecimal("10000"));
        TradingOrder order = order("10");

        assertDoesNotThrow(() -> rule.check(account, order, new BigDecimal("100")));
    }

    @Test
    void rejectsOrderAboveMaximumValue() {
        TradingAccount account = new TradingAccount("user-1", new BigDecimal("10000"));
        TradingOrder order = order("10");

        assertThrows(BusinessException.class,
                () -> rule.check(account, order, new BigDecimal("100.01")));
    }

    private TradingOrder order(String quantity) {
        return new TradingOrder("user-1", "AAPL", OrderSide.BUY, OrderType.MARKET,
                new BigDecimal(quantity), null, null, "test-key");
    }
}
