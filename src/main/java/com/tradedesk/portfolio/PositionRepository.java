package com.tradedesk.portfolio;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PositionRepository extends JpaRepository<Position, UUID> {
    List<Position> findAllByUserIdOrderBySymbol(String userId);
    Optional<Position> findByUserIdAndSymbol(String userId, String symbol);
}
