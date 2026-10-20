package com.tradedesk.order;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradingOrderRepository extends JpaRepository<TradingOrder, UUID> {
    Optional<TradingOrder> findByUserIdAndIdempotencyKey(String userId, String idempotencyKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from TradingOrder o where o.id = :id and o.userId = :userId")
    Optional<TradingOrder> findByIdAndUserIdForUpdate(@Param("id") UUID id,
                                                       @Param("userId") String userId);

    List<TradingOrder> findAllByUserIdOrderByCreatedAtDesc(String userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from TradingOrder o where o.symbol = :symbol and o.status = :status and o.type in :types")
    List<TradingOrder> findAllBySymbolAndStatusAndTypeInForUpdate(
            @Param("symbol") String symbol, @Param("status") OrderStatus status,
            @Param("types") List<OrderType> types);
}
