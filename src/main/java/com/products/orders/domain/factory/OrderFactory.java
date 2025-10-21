package com.products.orders.domain.factory;

import com.products.orders.domain.Order;
import com.products.orders.domain.OrderItem;

import java.util.List;

public class OrderFactory {
    public static Order create(Long clientId, List<OrderItem> items) {
        if (clientId == null) throw new IllegalArgumentException("clientId required");
        if (items == null || items.isEmpty()) throw new IllegalArgumentException("items required");

        Order o = new Order();
        o.setClientId(clientId);
        o.addItems(items);
        return o; 
    }
}
