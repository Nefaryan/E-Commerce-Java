package com.nefdev.ecommerce.service;

import com.nefdev.ecommerce.dao.OrderDAO;
import com.nefdev.ecommerce.model.Order;
import com.nefdev.ecommerce.model.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    private OrderDAO orderDAO;

    public OrderService(OrderDAO orderDAO) {
        this.orderDAO = orderDAO;
    }

    /**
     * Gets the list of orders for a given user.
     * @param user The user to search for.
     * @return The list of orders.
     */
    public List<Order> getOrder(User user){
        return orderDAO.findByUser(user);
    }
}
