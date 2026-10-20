package com.tradedesk.risk;

import java.math.BigDecimal;

import com.tradedesk.account.TradingAccount;
import com.tradedesk.order.TradingOrder;

public interface RiskRule {
    void check(TradingAccount account, TradingOrder order, BigDecimal executionPrice);
}
