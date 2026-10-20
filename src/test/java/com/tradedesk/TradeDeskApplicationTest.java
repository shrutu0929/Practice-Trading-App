package com.tradedesk;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import com.tradedesk.account.LedgerEntryRepository;
import com.tradedesk.account.TradingAccount;
import com.tradedesk.account.TradingAccountRepository;
import com.tradedesk.order.OrderRequest;
import com.tradedesk.order.OrderSide;
import com.tradedesk.order.OrderStatus;
import com.tradedesk.order.OrderType;
import com.tradedesk.order.TradingOrder;
import com.tradedesk.order.TradingOrderService;
import com.tradedesk.portfolio.PositionRepository;
import com.tradedesk.quote.Quote;
import com.tradedesk.quote.QuoteRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
class TradeDeskApplicationTest {
    @Autowired
    private TradingAccountRepository accounts;
    @Autowired
    private QuoteRepository quotes;
    @Autowired
    private PositionRepository positions;
    @Autowired
    private LedgerEntryRepository ledger;
    @Autowired
    private TradingOrderService orderService;

    @Test
    void applicationContextLoads() {
    }

    @Test
    @Transactional
    void marketOrderUpdatesCashPositionAndLedgerOnlyOnce() {
        String userId = "integration-user";
        accounts.save(new TradingAccount(userId, new BigDecimal("1000.00")));
        quotes.save(new Quote("ACME", new BigDecimal("10.00")));
        OrderRequest request = new OrderRequest("ACME", OrderSide.BUY, OrderType.MARKET,
                new BigDecimal("10"), null, null);

        TradingOrder first = orderService.submit(userId, "integration-key", request);
        TradingOrder replay = orderService.submit(userId, "integration-key", request);

        assertEquals(first.getId(), replay.getId());
        assertEquals(OrderStatus.FILLED, first.getStatus());
        assertEquals(new BigDecimal("900.00"), accounts.findByUserId(userId).orElseThrow().getCashBalance());
        assertEquals(new BigDecimal("10"), positions.findByUserIdAndSymbol(userId, "ACME")
                .orElseThrow().getQuantity());
        assertEquals(1, ledger.findAllByUserIdOrderByCreatedAtDesc(userId).size());
    }
}
