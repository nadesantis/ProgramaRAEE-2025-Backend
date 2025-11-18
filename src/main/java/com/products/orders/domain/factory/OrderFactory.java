/*package com.products.orders.domain.factory;

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
*/


package com.products.orders.domain.factory;

import com.products.orders.domain.Order;
import com.products.orders.domain.OrderItem;
import com.products.orders.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class OrderFactory {

    public static Order create(Long clientId, List<OrderItem> items) {

        Order order = new Order();
        order.setClientId(clientId);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(Instant.now());

        // limpiamos la lista, por las dudas
        order.getItems().clear();

        if (items != null) {
            for (OrderItem it : items) {
                if (it == null) continue;

                // vínculo bidireccional
                it.setOrder(order);

                // solo recalculamos el total de línea, NO tocamos productName
                it.recalcLineTotal();

                order.getItems().add(it);
            }
        }

        // total de la orden
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem it : order.getItems()) {
            if (it.getLineTotal() != null) {
                total = total.add(it.getLineTotal());
            }
        }
        order.setTotalAmount(total);

        return order;
    }
}
