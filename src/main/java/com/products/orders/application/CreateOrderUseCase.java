package com.products.orders.application;

import com.products.catalog.domain.Product;
import com.products.catalog.domain.ProductRepository;
import com.products.clients.domain.ClientRepository;
import com.products.orders.domain.Order;
import com.products.orders.domain.OrderItem;
import com.products.orders.domain.OrderRepository;
import com.products.orders.domain.factory.OrderFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CreateOrderUseCase {

    private final OrderRepository orderRepo;
    private final ClientRepository clientRepo;     
    private final ProductRepository productRepo;   

    public CreateOrderUseCase(OrderRepository orderRepo,
                              ClientRepository clientRepo,
                              ProductRepository productRepo) {
        this.orderRepo = orderRepo;
        this.clientRepo = clientRepo;
        this.productRepo = productRepo;
    }

    public static record ItemInput(Long productId, Integer quantity) {}

    @Transactional
    public Order handle(Long clientId, List<ItemInput> items) {

        clientRepo.findById(clientId).orElseThrow(() -> new IllegalArgumentException("Client not found"));


        List<OrderItem> builtItems = items.stream().map(i -> {
            Product p = productRepo.findById(i.productId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + i.productId()));
            if (i.quantity() == null || i.quantity() <= 0) {
                throw new IllegalArgumentException("Invalid quantity for product " + i.productId());
            }
            return OrderItem.of(p.getId(), p.getName(), p.getUnitPrice(), i.quantity());
        }).toList();

        Order order = OrderFactory.create(clientId, builtItems);

        return orderRepo.save(order);
    }
}
