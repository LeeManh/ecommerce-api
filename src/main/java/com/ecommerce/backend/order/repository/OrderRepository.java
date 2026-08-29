package com.ecommerce.backend.order.repository;

import com.ecommerce.backend.order.entity.Order;
import com.ecommerce.backend.order.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository
    extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
  Page<Order> findByUserId(Long userId, Pageable pageable);

  List<Order> findByStatusAndCreatedAtBefore(OrderStatus status, Instant cutoff);

  @Query(
      "SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o "
          + "WHERE o.status = com.ecommerce.backend.order.entity.OrderStatus.PAID "
          + "AND o.paidAt BETWEEN :from AND :to")
  BigDecimal sumRevenueBetween(@Param("from") Instant from, @Param("to") Instant to);

  @Query(
      "SELECT COUNT(o) FROM Order o "
          + "WHERE o.status = com.ecommerce.backend.order.entity.OrderStatus.PAID "
          + "AND o.paidAt BETWEEN :from AND :to")
  long countPaidOrdersBetween(@Param("from") Instant from, @Param("to") Instant to);

  @Query(
      value =
          "SELECT DATE_TRUNC('day', paid_at) AS day, "
              + "COALESCE(SUM(total_amount), 0) AS revenue, "
              + "COUNT(*) AS order_count "
              + "FROM orders "
              + "WHERE status = 'PAID' AND paid_at BETWEEN :from AND :to "
              + "GROUP BY DATE_TRUNC('day', paid_at) "
              + "ORDER BY day",
      nativeQuery = true)
  List<Object[]> findDailyRevenueBetween(@Param("from") Instant from, @Param("to") Instant to);
}
