package org.example.specification;

import org.example.dto.SearchOrdersFilterDTO;
import org.example.entity.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class OrderSpecificationBuilder
{
    public Specification<Order> buildSpecification(SearchOrdersFilterDTO filter)
    {
        Specification<Order> spec = Specification.where(OrderSpecification.notDeleted());

        if (filter != null)
        {
            if (filter.getUserId() != null)
            {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("userId"), filter.getUserId()));
            }
            if (!CollectionUtils.isEmpty(filter.getStatuses()))
            {
                spec = spec.and(OrderSpecification.statusIn(filter.getStatuses()));
            }
            if (filter.getFromDate() != null || filter.getToDate() != null)
            {
                spec = spec.and(OrderSpecification.createdBetween(filter.getFromDate(), filter.getToDate()));
            }
        }
        return spec;
    }
}