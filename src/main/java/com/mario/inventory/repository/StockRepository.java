package com.mario.inventory.repository;

import com.mario.inventory.domain.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {

    /** Almacén con más unidades que pueda servir la cantidad pedida. */
    Optional<Stock> findFirstByProductIdAndQuantityGreaterThanEqualOrderByQuantityDesc(Long productId, int quantity);

    Optional<Stock> findByProductIdAndWarehouseId(Long productId, Long warehouseId);

    @Query("select s from Stock s join fetch s.warehouse where s.product.id = :productId order by s.warehouse.code")
    List<Stock> findByProductIdWithWarehouse(@Param("productId") Long productId);

    @Query("select s from Stock s join fetch s.product join fetch s.warehouse where s.id in :ids")
    List<Stock> findWithDetailsByIdIn(@Param("ids") Collection<Long> ids);

    @Query("select coalesce(sum(s.quantity), 0) from Stock s where s.product.id = :productId")
    long totalQuantity(@Param("productId") Long productId);
}
