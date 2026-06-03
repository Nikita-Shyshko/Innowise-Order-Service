package org.example.service;

import org.example.dto.*;
import org.example.entity.Item;
import org.example.entity.Order;
import org.example.entity.OrderItem;
import org.example.entity.OrderStatus;
import org.example.mapper.OrderItemMapper;
import org.example.mapper.OrderMapper;
import org.example.repository.ItemRepository;
import org.example.repository.OrderItemRepository;
import org.example.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest
{
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private OrderItemMapper orderItemMapper;
    @Mock
    private UserServiceClient userServiceClient;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private OrderService orderService;

    private Order order;
    private OrderDTO orderDTO;
    private UserDTO userDTO;
    private OrderCreateDTO createDTO;
    private OrderUpdateDTO updateDTO;
    private Item item;

    @BeforeEach
    void setUp()
    {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("1");

        order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setTotalPrice(100);
        order.setStatus(OrderStatus.ACTIVE);

        orderDTO = new OrderDTO();
        orderDTO.setId(1L);
        orderDTO.setUserId(1L);
        orderDTO.setTotalPrice(100);

        userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setName("Test User");

        item = new Item();
        item.setId(10L);
        item.setName("Test Item");
        item.setPrice(50);

        OrderItemCreateDTO itemDto1 = new OrderItemCreateDTO();
        itemDto1.setItemId(10L);
        itemDto1.setQuantity(2);

        OrderItemCreateDTO itemDto2 = new OrderItemCreateDTO();
        itemDto2.setItemId(10L);
        itemDto2.setQuantity(3);

        createDTO = new OrderCreateDTO();
        createDTO.setUserId(1L);
        createDTO.setItems(List.of(itemDto1));

        updateDTO = new OrderUpdateDTO();
        updateDTO.setItems(List.of(itemDto2));
    }

    @Test
    void createOrder_shouldCreateOrderWithUserFromDto()
    {
        when(orderMapper.toEntity(any(OrderCreateDTO.class))).thenReturn(order);
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(orderItemMapper.toEntity(any(OrderItemCreateDTO.class))).thenReturn(new OrderItem());
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderItemRepository.saveAll(anyList())).thenReturn(List.of());
        when(orderMapper.toDTO(any(Order.class))).thenReturn(orderDTO);
        when(userServiceClient.getUserById(1L)).thenReturn(userDTO);

        GetOrderResponse response = orderService.createOrder(createDTO);

        assertThat(response).isNotNull();
        assertThat(response.getOrder()).isEqualTo(orderDTO);
        assertThat(response.getUser()).isEqualTo(userDTO);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void createOrder_shouldUseSecurityUserIdWhenDtoHasNull()
    {
        createDTO.setUserId(null);
        when(orderMapper.toEntity(any(OrderCreateDTO.class))).thenReturn(order);
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(orderItemMapper.toEntity(any(OrderItemCreateDTO.class))).thenReturn(new OrderItem());
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderItemRepository.saveAll(anyList())).thenReturn(List.of());
        when(orderMapper.toDTO(any(Order.class))).thenReturn(orderDTO);
        when(userServiceClient.getUserById(1L)).thenReturn(userDTO);

        GetOrderResponse response = orderService.createOrder(createDTO);

        assertThat(response.getOrder().getUserId()).isEqualTo(1L);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void getOrderById_shouldReturnOrderWithUser()
    {
        when(orderRepository.getOrderById(1L)).thenReturn(Optional.of(order));
        when(orderMapper.toDTO(any(Order.class))).thenReturn(orderDTO);
        when(userServiceClient.getUserById(1L)).thenReturn(userDTO);

        GetOrderResponse response = orderService.getOrderById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getOrder()).isEqualTo(orderDTO);
        assertThat(response.getUser()).isEqualTo(userDTO);
    }

    @Test
    void getOrderById_shouldThrowWhenNotFound()
    {
        when(orderRepository.getOrderById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Order not found with id: 1");
    }

    @Test
    void getAllOrders_shouldReturnPageWithUsers()
    {
        Page<Order> orderPage = new PageImpl<>(List.of(order));
        when(orderRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(orderPage);
        when(orderMapper.toDTO(any(Order.class))).thenReturn(orderDTO);
        when(userServiceClient.getUserById(1L)).thenReturn(userDTO);

        Page<GetOrderResponse> result = orderService.getAllOrders(PageRequest.of(0, 10), null);

        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).getOrder()).isEqualTo(orderDTO);
        assertThat(result.getContent().get(0).getUser()).isEqualTo(userDTO);
    }

    @Test
    void updateOrder_shouldSoftDeleteOldItemsAndAddNew()
    {
        Order existingOrder = new Order();
        existingOrder.setId(1L);
        existingOrder.setUserId(1L);
        existingOrder.setTotalPrice(100);

        when(orderRepository.getOrderById(1L)).thenReturn(Optional.of(existingOrder));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(orderItemMapper.toEntity(any(OrderItemCreateDTO.class))).thenReturn(new OrderItem());
        doNothing().when(orderItemRepository).softDeleteByOrderId(1L);
        when(orderItemRepository.saveAll(anyList())).thenReturn(List.of());
        when(orderRepository.save(any(Order.class))).thenReturn(existingOrder);
        when(orderMapper.toDTO(any(Order.class))).thenReturn(orderDTO);
        when(userServiceClient.getUserById(1L)).thenReturn(userDTO);

        GetOrderResponse response = orderService.updateOrder(1L, updateDTO);

        assertThat(response).isNotNull();
        verify(orderItemRepository).softDeleteByOrderId(1L);
        verify(orderItemRepository).saveAll(anyList());
        verify(orderRepository).save(existingOrder);
    }

    @Test
    void deleteOrderById_shouldSoftDelete()
    {
        when(orderRepository.softDeleteOrderById(1L)).thenReturn(1);

        orderService.deleteOrderById(1L);

        verify(orderRepository).softDeleteOrderById(1L);
    }

    @Test
    void deleteOrderById_shouldThrowIfNotFound()
    {
        when(orderRepository.softDeleteOrderById(1L)).thenReturn(0);

        assertThatThrownBy(() -> orderService.deleteOrderById(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Order not found or already deleted: 1");
    }
}