package enterprise.sharma.prateek.order.service;

import java.util.UUID;

import org.springframework.data.domain.Page;

import enterprise.sharma.prateek.order.dto.CancelOrderResponse;
import enterprise.sharma.prateek.order.dto.CreateOrderRequest;
import enterprise.sharma.prateek.order.dto.OrderResponse;

public interface OrderService {

	OrderResponse createOrder(CreateOrderRequest request);

	OrderResponse getOrder(UUID orderId);

	Page<OrderResponse> getOrders(int page, int size);

	Page<OrderResponse> getCustomerOrders(String customerId, int page, int size);

	CancelOrderResponse cancelOrder(UUID orderId);

}
