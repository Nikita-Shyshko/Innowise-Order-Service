package org.example.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemResponseDTO
{
    private Long id;
    private String name;
    private Integer price;
    private String description;
}