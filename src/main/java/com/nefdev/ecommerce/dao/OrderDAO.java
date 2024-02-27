package com.nefdev.ecommerce.dao;

import com.nefdev.ecommerce.model.Order;
import com.nefdev.ecommerce.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderDAO extends JpaRepository<Order,Long> {


    List<Order> findByUser(User user);
}
