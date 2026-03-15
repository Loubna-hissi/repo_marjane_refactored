package com.nimbleways.springboilerplate.controllers;

import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.implementations.NotificationService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MyControllerIntegrationTests {

    private final MockMvc mockMvc;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @MockBean
    private NotificationService notificationService;

    MyControllerIntegrationTests(
            MockMvc mockMvc,
            OrderRepository orderRepository,
            ProductRepository productRepository) {
        this.mockMvc = mockMvc;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @Test
    void processOrderShouldReturnOk() throws Exception {

        List<Product> products = createProducts();
        productRepository.saveAll(products);

        Order order = createOrder(Set.copyOf(products));
        order = orderRepository.save(order);

        mockMvc.perform(post("/orders/{orderId}/processOrder", order.getId()))
                .andExpect(status().isOk());

        Order result = orderRepository.findById(order.getId()).orElseThrow();

        assertEquals(order.getId(), result.getId());
    }
}