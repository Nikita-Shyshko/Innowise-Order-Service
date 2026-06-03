package org.example.service;

import org.example.dto.OrderCreateDTO;
import org.example.dto.OrderDTO;
import org.example.dto.SearchOrdersFilterDTO;
import org.example.dto.OrderItemCreateDTO;
import org.example.dto.OrderItemDTO;
import org.example.dto.GetOrderResponse;
import org.example.dto.OrderUpdateDTO;
import org.example.dto.UserDTO;
import org.example.entity.DeletedStatus;
import org.example.entity.Item;
import org.example.entity.Order;
import org.example.entity.OrderItem;
import org.example.mapper.OrderItemMapper;
import org.example.mapper.OrderMapper;
import org.example.repository.ItemRepository;
import org.example.repository.OrderItemRepository;
import org.example.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.specification.OrderSpecificationBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class OrderService
{
    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final UserServiceClient userServiceClient;
    private final OrderSpecificationBuilder specificationBuilder;

    @Transactional
    public GetOrderResponse createOrder(OrderCreateDTO createDTO)
    {
        Long userId = createDTO.getUserId();
        if (userId == null)
        {
            userId = getCurrentUserId();
        }

        Order order = orderMapper.toEntity(createDTO);
        order.setUserId(userId);

        List<OrderItem> orderItems = buildOrderItems(order, createDTO.getItems());
        Integer totalPrice = calculateTotalPrice(orderItems);
        order.setTotalPrice(totalPrice);

        Order savedOrder = orderRepository.save(order);
        orderItems.forEach(item -> item.setOrder(savedOrder));
        orderItemRepository.saveAll(orderItems);
        savedOrder.setItems(orderItems);

        OrderDTO orderDTO = buildOrderDTO(savedOrder);
        UserDTO userDTO = userServiceClient.getUserById(savedOrder.getUserId());

        return new GetOrderResponse(orderDTO, userDTO);
    }

    @Transactional(readOnly = true)
    public GetOrderResponse getOrderById(Long id)
    {
        Order order = orderRepository.getOrderById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        OrderDTO orderDTO = buildOrderDTO(order);
        UserDTO userDTO = userServiceClient.getUserById(order.getUserId());
        return new GetOrderResponse(orderDTO, userDTO);
    }

    @Transactional(readOnly = true)
    public Page<GetOrderResponse> getAllOrders(Pageable pageable, SearchOrdersFilterDTO filter)
    {
        Specification<Order> spec = specificationBuilder.buildSpecification(filter);
        return orderRepository.findAll(spec, pageable)
                .map(order -> {
                    OrderDTO orderDTO = buildOrderDTO(order);
                    UserDTO userDTO = userServiceClient.getUserById(order.getUserId());
                    return new GetOrderResponse(orderDTO, userDTO);
                });
    }

    @Transactional
    public GetOrderResponse updateOrder(Long id, OrderUpdateDTO updateDTO)
    {
        Order order = orderRepository.getOrderById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        orderMapper.updateEntity(updateDTO, order);

        if (updateDTO.getItems() != null)
        {
            orderItemRepository.softDeleteByOrderId(order.getId());

            List<OrderItem> newItems = buildOrderItems(order, updateDTO.getItems());
            Integer newTotal = calculateTotalPrice(newItems);
            order.setTotalPrice(newTotal);
            orderItemRepository.saveAll(newItems);
            order.setItems(newItems);
        }

        OrderDTO orderDTO = buildOrderDTO(order);
        UserDTO userDTO = userServiceClient.getUserById(order.getUserId());
        return new GetOrderResponse(orderDTO, userDTO);
    }

    @Transactional
    public void deleteOrderById(Long id)
    {
        int deleted = orderRepository.softDeleteOrderById(id);
        if (deleted == 0) {
            throw new RuntimeException("Order not found or already deleted: " + id);
        }
        log.info("Order with id {} soft deleted", id);
    }

    private List<OrderItem> buildOrderItems(Order order, List<OrderItemCreateDTO> itemCreateDTOs)
    {
        if (CollectionUtils.isEmpty(itemCreateDTOs))
        {
            return new ArrayList<>();
        }
        return itemCreateDTOs.stream().map(dto -> {
            Item item = itemRepository.findById(dto.getItemId())
                    .orElseThrow(() -> new RuntimeException("Item not found: " + dto.getItemId()));
            OrderItem orderItem = orderItemMapper.toEntity(dto);
            orderItem.setOrder(order);
            orderItem.setItem(item);
            orderItem.setPrice(item.getPrice());
            orderItem.setDeleted(DeletedStatus.CREATED);
            return orderItem;
        }).collect(Collectors.toList());
    }

    private Integer calculateTotalPrice(List<OrderItem> items)
    {
        return items.stream()
                .mapToInt(item -> item.getPrice() * item.getQuantity())
                .sum();
    }

    private OrderDTO buildOrderDTO(Order order)
    {
        OrderDTO dto = orderMapper.toDTO(order);
        if (order.getItems() != null)
        {
            List<OrderItemDTO> itemDTOs = order.getItems().stream()
                    .map(orderItemMapper::toDTOWithItemDetails)
                    .collect(Collectors.toList());
            dto.setItems(itemDTOs);
        }
        if (order.getStatus() != null)
        {
            dto.setStatus(order.getStatus().name());
        }
        if (order.getDeleted() != null)
        {
            dto.setDeleted(order.getDeleted().name());
        }
        return dto;
    }

    private Long getCurrentUserId()
    {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated())
        {
            throw new RuntimeException("User is not authenticated");
        }
        return Long.valueOf(authentication.getName());
    }
}