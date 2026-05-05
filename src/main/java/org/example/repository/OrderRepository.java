package org.example.repository;

import org.example.entity.Order;
import org.example.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order>
{
    @Query("select o from Order o where o.id = :id and o.deleted <> 'DELETED'")
    Optional<Order> getOrderById(@Param("id") Long id);

    @Query("select o from Order o where o.userId = :userId and o.deleted <> 'DELETED'")
    List<Order> findAllActiveByUserId(@Param("userId") Long userId);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Order o set o.status = :status, o.totalPrice = :totalPrice where o.id = :id and o.deleted <> 'DELETED'")
    int updateOrderById(@Param("id") Long id,
                        @Param("status") OrderStatus status,
                        @Param("totalPrice") Integer totalPrice);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Order o set o.deleted = 'DELETED' where o.id = :id")
    int softDeleteOrderById(@Param("id") Long id);
}