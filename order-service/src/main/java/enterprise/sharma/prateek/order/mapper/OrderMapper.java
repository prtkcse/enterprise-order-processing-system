package enterprise.sharma.prateek.order.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import enterprise.sharma.prateek.order.dto.OrderItemResponse;
import enterprise.sharma.prateek.order.dto.OrderResponse;
import enterprise.sharma.prateek.order.entity.Order;
import enterprise.sharma.prateek.order.entity.OrderItem;

@Component
public class OrderMapper {

	public OrderResponse toResponse(Order order) {

		List<OrderItemResponse> itemResponses = order.getItems().stream().map(this::toItemResponse).toList();

		return new OrderResponse(order.getId(), order.getCustomerId(), order.getTotalAmount(), order.getStatus(),
				itemResponses, order.getCreatedAt(), order.getUpdatedAt());

	}

	private OrderItemResponse toItemResponse(OrderItem item) {

		return new OrderItemResponse(item.getProductId(), item.getQuantity(), item.getUnitPrice(),
				item.getUnitPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())));

	}

}
