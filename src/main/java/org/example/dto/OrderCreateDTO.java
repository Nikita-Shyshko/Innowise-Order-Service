package org.example.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class OrderCreateDTO
{
    @NotNull(message = "User id must not be null")
    @Positive(message = "User id must be positive")
    private Long userId;

    @NotNull(message = "Order items must not be null")
    private List<OrderItemCreateDTO> items;
}