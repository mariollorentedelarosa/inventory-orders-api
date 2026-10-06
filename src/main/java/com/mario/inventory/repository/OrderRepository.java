package com.mario.inventory.repository;

import com.mario.inventory.domain.PurchaseOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<PurchaseOrder, Long> {

    @EntityGraph(attributePaths = "customer")
    Page<PurchaseOrder> findAllBy(Pageable pageable);

    @EntityGraph(attributePaths = "customer")
    Page<PurchaseOrder> findByCustomerId(Long customerId, Pageable pageable);
}
