package org.example.specification;

import jakarta.persistence.criteria.Predicate;
import org.example.entity.Order;
import org.example.entity.OrderStatus;
import org.springframework.data.jpa.domain.Specification;

import java.util.Date;
import java.util.List;

public class OrderSpecification
{
    public static Specification<Order> createdBetween(Date from, Date to)
    {
        return (root, query, cb) ->
        {
            Predicate predicate = cb.conjunction();
            if (from != null)
            {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            }
            if (to != null)
            {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("createdAt"), to));
            }
            return predicate;
        };
    }

    public static Specification<Order> statusIn(List<OrderStatus> statuses)
    {
        return (root, query, cb) ->
        {
            if (statuses == null || statuses.isEmpty())
            {
                return cb.conjunction();
            }
            return root.get("status").in(statuses);
        };
    }

    public static Specification<Order> notDeleted()
    {
        return (root, query, cb) -> cb.notEqual(root.get("deleted"), OrderStatus.DELETED);
    }
}
