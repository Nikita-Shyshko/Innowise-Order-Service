package org.example.repository;

import org.example.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long>
{
    @Modifying
    @Transactional
    @Query("UPDATE OrderItem oi SET oi.deleted = 'DELETED' WHERE oi.order.id = :orderId")
    void softDeleteByOrderId(@Param("orderId") Long orderId);
}