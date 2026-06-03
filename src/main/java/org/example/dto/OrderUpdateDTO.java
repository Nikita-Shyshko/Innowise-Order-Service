package org.example.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class OrderUpdateDTO
{
    private String status;

    private List<OrderItemCreateDTO> items;
}