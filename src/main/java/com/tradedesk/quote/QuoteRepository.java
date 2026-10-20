package com.tradedesk.quote;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface QuoteRepository extends JpaRepository<Quote, UUID> {
    Optional<Quote> findBySymbol(String symbol);
}
