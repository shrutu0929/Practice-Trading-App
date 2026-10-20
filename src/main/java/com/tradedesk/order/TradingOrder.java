package com.tradedesk.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "trading_orders", uniqueConstraints =
        @UniqueConstraint(name = "uk_order_idempotency", columnNames = {"user_id", "idempotency_key"}))
public class TradingOrder {
    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false, length = 160)
    private String userId;

    @Column(nullable = false, length = 12)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private OrderSide side;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private OrderType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private OrderStatus status;

    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal quantity;

    @Column(name = "limit_price", precision = 19, scale = 4)
    private BigDecimal limitPrice;

    @Column(name = "stop_price", precision = 19, scale = 4)
    private BigDecimal stopPrice;

    @Column(name = "filled_price", precision = 19, scale = 4)
    private BigDecimal filledPrice;

    @Column(name = "idempotency_key", nullable = false, length = 100)
    private String idempotencyKey;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected TradingOrder() {
    }

    public TradingOrder(String userId, String symbol, OrderSide side, OrderType type,
                        BigDecimal quantity, BigDecimal limitPrice, BigDecimal stopPrice,
                        String idempotencyKey) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.symbol = symbol;
        this.side = side;
        this.type = type;
        this.quantity = quantity;
        this.limitPrice = limitPrice;
        this.stopPrice = stopPrice;
        this.idempotencyKey = idempotencyKey;
        this.status = OrderStatus.OPEN;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getUserId() { return userId; }
    public String getSymbol() { return symbol; }
    public OrderSide getSide() { return side; }
    public OrderType getType() { return type; }
    public OrderStatus getStatus() { return status; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getLimitPrice() { return limitPrice; }
    public BigDecimal getStopPrice() { return stopPrice; }
    public BigDecimal getFilledPrice() { return filledPrice; }
    public Instant getCreatedAt() { return createdAt; }

    public boolean matches(BigDecimal marketPrice) {
        if (type == OrderType.LIMIT) {
            return side == OrderSide.BUY
                    ? marketPrice.compareTo(limitPrice) <= 0
                    : marketPrice.compareTo(limitPrice) >= 0;
        }
        if (type == OrderType.STOP_LOSS) {
            return side == OrderSide.BUY
                    ? marketPrice.compareTo(stopPrice) >= 0
                    : marketPrice.compareTo(stopPrice) <= 0;
        }
        return true;
    }

    public void fill(BigDecimal price) {
        status = OrderStatus.FILLED;
        filledPrice = price;
    }

    public void cancel() {
        status = OrderStatus.CANCELLED;
    }
}
