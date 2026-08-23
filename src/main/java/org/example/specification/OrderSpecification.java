package org.example.specification;

import org.example.entity.DeletedStatus;
import org.example.entity.Order;
import org.example.entity.OrderStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;

public class OrderSpecification
{
    public static Specification<Order> notDeleted()
    {
        return (root, query, cb) -> cb.notEqual(root.get("deleted"), DeletedStatus.DELETED);
    }

    public static Specification<Order> statusIn(Collection<OrderStatus> statuses)
    {
        return (root, query, cb) -> root.get("status").in(statuses);
    }

    public static Specification<Order> createdBetween(LocalDate fromDate, LocalDate toDate)
    {
        return (root, query, cb) -> {
            if (fromDate == null && toDate == null) {
                return cb.conjunction();
            }
            LocalDateTime fromDateTime = fromDate != null ? fromDate.atStartOfDay() : null;
            LocalDateTime toDateTime = toDate != null ? toDate.atTime(LocalTime.MAX) : null;
            if (fromDateTime != null && toDateTime != null) {
                return cb.between(root.get("createdAt"), fromDateTime, toDateTime);
            } else if (fromDateTime != null) {
                return cb.greaterThanOrEqualTo(root.get("createdAt"), fromDateTime);
            } else {
                return cb.lessThanOrEqualTo(root.get("createdAt"), toDateTime);
            }
        };
    }
}