package org.example.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "order_item", indexes = {
        @Index(name = "order_id_idx", columnList = "order_id")
})
public class OrderItem extends Auditing
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(nullable = false)
    @Min(value = 0, message = "Quantity should be >= 0")
    @NotNull(message = "Quantity should be not null")
    private Integer quantity;

    @Column(nullable = false)
    @Min(value = 0, message = "Price should be >= 0")
    @NotNull(message = "Price should be not null")
    private Integer price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeletedStatus deleted = DeletedStatus.CREATED;
}