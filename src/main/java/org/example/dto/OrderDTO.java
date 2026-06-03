package org.example.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class OrderDTO
{
    private Long id;
    private Long userId;
    private String status;
    private Integer totalPrice;
    private String deleted;
    private Date createdAt;
    private Date updatedAt;
    private List<OrderItemDTO> items;
}