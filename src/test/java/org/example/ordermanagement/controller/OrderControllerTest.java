package org.example.ordermanagement.controller;

import org.example.ordermanagement.db.enums.OrderStatus;
import org.example.ordermanagement.dto.request.OrderRequest;
import org.example.ordermanagement.dto.response.OrderDTO;
import org.example.ordermanagement.service.OrderService;
import org.example.ordermanagement.utils.ReceiveToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderControllerTest {

    @InjectMocks
    private OrderController orderController;

    @Mock
    private ReceiveToken receiveToken;

    @Mock
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private void mockToken(String role) {
        when(receiveToken.tokenData()).thenReturn(Map.of(
                "role", role,
                "id", "1"
        ));
        orderController.receiveToken = receiveToken;
        orderController.initData();
    }

    // GetOrders
    @Test
    void testGetOrdersAsAdmin() {
        mockToken("RoleAdmin");
        Page<OrderDTO> mockPage = new PageImpl<>(Collections.singletonList(new OrderDTO()));
        when(orderService.getOrdersForAdmin(any(), any(), any(), any())).thenReturn(mockPage);

        ResponseEntity<Page<OrderDTO>> response = orderController.getOrders(null, BigDecimal.ONE, BigDecimal.TEN, PageRequest.of(0, 10));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());
        verify(orderService, times(1)).getOrdersForAdmin(any(), any(), any(), any());
    }

    @Test
    void testGetOrdersAsClient() {
        mockToken("RoleClient");
        Page<OrderDTO> mockPage = new PageImpl<>(Collections.singletonList(new OrderDTO()));
        when(orderService.getOrdersForCustomer(eq(1L), any(), any(), any(), any())).thenReturn(mockPage);

        ResponseEntity<Page<OrderDTO>> response = orderController.getOrders(null, BigDecimal.ONE, BigDecimal.TEN, PageRequest.of(0, 10));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());
        verify(orderService, times(1)).getOrdersForCustomer(eq(1L), any(), any(), any(), any());
    }

    @Test
    void testGetOrdersWithUnauthorizedRole() {
        mockToken("UnauthorizedRole");

        ResponseEntity<Page<OrderDTO>> response = orderController.getOrders(null, BigDecimal.ONE, BigDecimal.TEN, PageRequest.of(0, 10));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNull(response.getBody());

        verify(orderService, never()).getOrdersForAdmin(any(), any(), any(), any());
        verify(orderService, never()).getOrdersForCustomer(anyLong(), any(), any(), any(), any());
    }

    @Test
    void testGetOrderById() {
        Long orderId = 1L;
        OrderDTO mockOrder = new OrderDTO();
        when(orderService.getOrderById(orderId)).thenReturn(mockOrder);

        ResponseEntity<OrderDTO> response = orderController.getOrderById(orderId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(orderService, times(1)).getOrderById(orderId);
    }

    // Create Order
    @Test
    void testCreateOrderAsAdmin() {
        mockToken("RoleAdmin");
        OrderRequest mockRequest = new OrderRequest();
        mockRequest.setCustomerId(1L);
        OrderDTO mockOrder = new OrderDTO();
        when(orderService.createOrder(mockRequest, 1L)).thenReturn(mockOrder);

        ResponseEntity<?> response = orderController.createOrder(mockRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(orderService, times(1)).createOrder(mockRequest, 1L);
    }

    @Test
    void testCreateOrderAsClient() {
        mockToken("RoleClient");
        OrderRequest mockRequest = new OrderRequest();
        OrderDTO mockOrder = new OrderDTO();
        when(orderService.createOrder(mockRequest, 1L)).thenReturn(mockOrder);

        ResponseEntity<?> response = orderController.createOrder(mockRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(orderService, times(1)).createOrder(mockRequest, 1L);
    }

    @Test
    void testCreateOrderWithUnauthorizedRole() {
        mockToken("UnauthorizedRole");
        OrderRequest mockRequest = new OrderRequest();

        ResponseEntity<?> response = orderController.createOrder(mockRequest);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(orderService, never()).createOrder(any(), any());
    }

    @Test
    void testCreateOrderMissingCustomerIdForAdmin() {
        mockToken("RoleAdmin");
        OrderRequest mockRequest = new OrderRequest();

        ResponseEntity<?> response = orderController.createOrder(mockRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Customer ID is required for admin", response.getBody());
        verify(orderService, never()).createOrder(any(), any());
    }

    // Update Order
    @Test
    void testUpdateOrderAsAdmin() {
        mockToken("RoleAdmin");
        Long orderId = 1L;
        OrderRequest mockRequest = new OrderRequest();
        OrderDTO mockOrder = new OrderDTO();
        when(orderService.updateOrder(orderId, mockRequest, true)).thenReturn(mockOrder);

        ResponseEntity<OrderDTO> response = orderController.updateOrder(orderId, mockRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(orderService, times(1)).updateOrder(orderId, mockRequest, true);
    }

    @Test
    void testUpdateOrderAsClient() {
        mockToken("RoleClient");
        Long orderId = 1L;
        OrderRequest mockRequest = new OrderRequest();
        mockRequest.setStatus(OrderStatus.Cancelled);
        OrderDTO mockOrder = new OrderDTO();
        when(orderService.updateOrder(orderId, mockRequest, false)).thenReturn(mockOrder);

        ResponseEntity<OrderDTO> response = orderController.updateOrder(orderId, mockRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(orderService, times(1)).updateOrder(orderId, mockRequest, false);
    }

    @Test
    void testUpdateOrderWithUnauthorizedRole() {
        mockToken("UnauthorizedRole");
        Long orderId = 1L;
        OrderRequest mockRequest = new OrderRequest();

        ResponseEntity<OrderDTO> response = orderController.updateOrder(orderId, mockRequest);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNull(response.getBody());

        verify(orderService, never()).updateOrder(anyLong(), any(), anyBoolean());
    }

    // Delete order
    @Test
    void testSoftDeleteOrder() {
        mockToken("RoleAdmin");
        Long orderId = 1L;

        ResponseEntity<Void> response = orderController.softDeleteOrder(orderId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(orderService, times(1)).softDeleteOrder(orderId);
    }

    @Test
    void testSoftDeleteOrderWithUnauthorizedRole() {
        mockToken("AnyOtherRole");
        Long orderId = 1L;

        ResponseEntity<Void> response = orderController.softDeleteOrder(orderId);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNull(response.getBody());

        verify(orderService, never()).softDeleteOrder(orderId);
    }

}
