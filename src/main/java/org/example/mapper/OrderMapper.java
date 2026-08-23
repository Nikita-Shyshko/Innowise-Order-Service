package org.example.mapper;

import org.example.dto.OrderCreateDTO;
import org.example.dto.OrderDTO;
import org.example.dto.OrderUpdateDTO;
import org.example.entity.DeletedStatus;
import org.example.entity.Order;
import org.example.entity.OrderStatus;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        imports = {OrderStatus.class, DeletedStatus.class})
public interface OrderMapper
{
    OrderDTO toDTO(Order order);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", expression = "java(OrderStatus.ACTIVE)")
    @Mapping(target = "totalPrice", ignore = true)
    @Mapping(target = "deleted", expression = "java(DeletedStatus.CREATED)")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "items", ignore = true)
    Order toEntity(OrderCreateDTO createDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "status", source = "status", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "totalPrice", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "items", ignore = true)
    void updateEntity(OrderUpdateDTO updateDTO, @MappingTarget Order order);
}