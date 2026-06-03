package org.example.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemDTO
{
    private Long id;
    private Long itemId;
    private String itemName;
    private Integer quantity;
    private Integer price;
}