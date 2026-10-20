package com.tradedesk.portfolio;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

@Entity
@Table(name = "positions", uniqueConstraints =
        @UniqueConstraint(name = "uk_position_user_symbol", columnNames = {"user_id", "symbol"}))
public class Position {
    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false, length = 160)
    private String userId;

    @Column(nullable = false, length = 12)
    private String symbol;

    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal quantity;

    @Column(name = "average_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal averagePrice;

    @Version
    private long version;

    protected Position() {
    }

    public Position(String userId, String symbol, BigDecimal quantity, BigDecimal averagePrice) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.symbol = symbol;
        this.quantity = quantity;
        this.averagePrice = averagePrice;
    }

    public String getUserId() { return userId; }
    public String getSymbol() { return symbol; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getAveragePrice() { return averagePrice; }

    public void buy(BigDecimal bought, BigDecimal price) {
        BigDecimal totalCost = quantity.multiply(averagePrice).add(bought.multiply(price));
        quantity = quantity.add(bought);
        averagePrice = totalCost.divide(quantity, 4, java.math.RoundingMode.HALF_UP);
    }

    public void sell(BigDecimal sold) {
        quantity = quantity.subtract(sold);
        if (quantity.signum() == 0) {
            averagePrice = BigDecimal.ZERO;
        }
    }
}
