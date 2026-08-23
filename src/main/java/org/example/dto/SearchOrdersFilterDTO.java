package org.example.dto;

import lombok.Getter;
import lombok.Setter;
import org.example.entity.OrderStatus;

import jakarta.validation.constraints.AssertTrue;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class SearchOrdersFilterDTO
{
    private Long userId;
    private List<OrderStatus> statuses;
    private LocalDate fromDate;
    private LocalDate toDate;

    @AssertTrue(message = "fromDate must not be later than toDate")
    public boolean isDatesValid()
    {
        if (fromDate == null || toDate == null)
        {
            return true;
        }
        return !fromDate.isAfter(toDate);
    }
}