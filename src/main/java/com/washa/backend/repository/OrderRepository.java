package com.washa.backend.repository;

import com.washa.backend.model.Dobi;
import com.washa.backend.model.Order;
import com.washa.backend.model.Rider;
import com.washa.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
    List<Order> findByRider(Rider rider);
    List<Order> findByDobi(Dobi dobi);
    List<Order> findByStatus(Order.OrderStatus status);
}