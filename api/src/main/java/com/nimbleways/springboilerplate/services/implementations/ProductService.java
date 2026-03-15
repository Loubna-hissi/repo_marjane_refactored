package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    public void processProduct(Product product) {

        if (product.isNormal()) {
            processNormal(product);
            return;
        }

        if (product.isSeasonal()) {
            processSeasonal(product);
            return;
        }

        if (product.isExpirable()) {
            processExpirable(product);
        }
    }

    private void processNormal(Product product) {

        if (product.getAvailable() > 0) {
            decreaseStock(product);
        } else {
            notifyDelay(product.getLeadTime(), product);
        }
    }

    private void processSeasonal(Product product) {

        LocalDate now = LocalDate.now();

        boolean inSeason =
                now.isAfter(product.getSeasonStartDate()) &&
                        now.isBefore(product.getSeasonEndDate());

        if (inSeason && product.getAvailable() > 0) {
            decreaseStock(product);
        } else {
            handleSeasonalProduct(product);
        }
    }

    private void processExpirable(Product product) {

        if (product.getAvailable() > 0 && product.getExpiryDate().isAfter(LocalDate.now())) {
            decreaseStock(product);
        } else {
            handleExpiredProduct(product);
        }
    }

    private void decreaseStock(Product product) {
        product.setAvailable(product.getAvailable() - 1);
        productRepository.save(product);
    }

    public void notifyDelay(int leadTime, Product product) {

        product.setLeadTime(leadTime);
        productRepository.save(product);

        notificationService.sendDelayNotification(leadTime, product.getName());
    }

    public void handleSeasonalProduct(Product product) {

        if (LocalDate.now().plusDays(product.getLeadTime()).isAfter(product.getSeasonEndDate())) {

            notificationService.sendOutOfStockNotification(product.getName());
            product.setAvailable(0);
            productRepository.save(product);

        } else if (product.getSeasonStartDate().isAfter(LocalDate.now())) {

            notificationService.sendOutOfStockNotification(product.getName());
            productRepository.save(product);

        } else {

            notifyDelay(product.getLeadTime(), product);
        }
    }

    public void handleExpiredProduct(Product product) {

        if (product.getAvailable() > 0 && product.getExpiryDate().isAfter(LocalDate.now())) {

            decreaseStock(product);

        } else {

            notificationService.sendExpirationNotification(product.getName(), product.getExpiryDate());
            product.setAvailable(0);
            productRepository.save(product);
        }
    }
}