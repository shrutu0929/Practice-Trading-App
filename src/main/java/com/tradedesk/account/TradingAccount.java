package com.tradedesk.account;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "trading_accounts")
public class TradingAccount {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 160)
    private String userId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal cashBalance;

    @Version
    private long version;

    protected TradingAccount() {
    }

    public TradingAccount(String userId, BigDecimal initialBalance) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.cashBalance = initialBalance;
    }

    public UUID getId() { return id; }
    public String getUserId() { return userId; }
    public BigDecimal getCashBalance() { return cashBalance; }

    public void debit(BigDecimal amount) {
        cashBalance = cashBalance.subtract(amount);
    }

    public void credit(BigDecimal amount) {
        cashBalance = cashBalance.add(amount);
    }
}
