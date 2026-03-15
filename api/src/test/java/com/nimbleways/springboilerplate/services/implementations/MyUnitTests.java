package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.enums.ProductType;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class MyUnitTests {

    @Mock
    private NotificationService notificationService;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void testNotifyDelay() {
        Product product = new Product(null, 15, 0, ProductType.NORMAL, "RJ45 Cable", null, null, null);

        productService.notifyDelay(product.getLeadTime(), product);

        assertEquals(0, product.getAvailable());
        assertEquals(15, product.getLeadTime());
        verify(productRepository).save(product);
        verify(notificationService).sendDelayNotification(15, "RJ45 Cable");
    }

    @Test
    void testProcessNormal_withAvailableStock() {
        Product product = new Product(null, 5, 10, ProductType.NORMAL, "USB Cable", null, null, null);

        productService.processProduct(product);

        assertEquals(9, product.getAvailable());
        verify(productRepository).save(product);
        verifyNoInteractions(notificationService);
    }

    @Test
    void testProcessNormal_noStock() {
        Product product = new Product(null, 5, 0, ProductType.NORMAL, "USB Dongle", null, null, null);

        productService.processProduct(product);

        assertEquals(0, product.getAvailable());
        verify(productRepository).save(product);
        verify(notificationService).sendDelayNotification(5, "USB Dongle");
    }

    @Test
    void testProcessExpirable_valid() {
        Product product = new Product(null, 5, 10, ProductType.EXPIRABLE, "Milk",
                LocalDate.now().plusDays(2), null, null);

        productService.processProduct(product);

        assertEquals(9, product.getAvailable());
        verify(productRepository).save(product);
        verifyNoInteractions(notificationService);
    }

    @Test
    void testProcessExpirable_expired() {
        Product product = new Product(null, 5, 10, ProductType.EXPIRABLE, "Milk",
                LocalDate.now().minusDays(1), null, null);

        productService.processProduct(product);

        assertEquals(0, product.getAvailable());
        verify(productRepository, atLeastOnce()).save(product);
        verify(notificationService).sendExpirationNotification("Milk", product.getExpiryDate());
    }

    @Test
    void testProcessSeasonal_inSeason() {
        Product product = new Product(null, 5, 10, ProductType.SEASONAL, "Watermelon",
                null, LocalDate.now().minusDays(1), LocalDate.now().plusDays(5));

        productService.processProduct(product);

        assertEquals(9, product.getAvailable());
        verify(productRepository).save(product);
        verifyNoInteractions(notificationService);
    }

}