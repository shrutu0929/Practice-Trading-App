package com.tradedesk.quote;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "quotes")
public class Quote {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 12)
    private String symbol;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal price;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Quote() {
    }

    public Quote(String symbol, BigDecimal price) {
        this.id = UUID.randomUUID();
        this.symbol = symbol;
        this.price = price;
        this.updatedAt = Instant.now();
    }

    public String getSymbol() { return symbol; }
    public BigDecimal getPrice() { return price; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void updatePrice(BigDecimal price) {
        this.price = price;
        this.updatedAt = Instant.now();
    }
}
