package enterprise.sharma.prateek.order.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import enterprise.sharma.prateek.order.entity.Order;
import enterprise.sharma.prateek.order.entity.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, UUID> {

	Page<Order> findByCustomerId(String customerId, Pageable pageable);

	List<Order> findByStatus(OrderStatus status);

}
