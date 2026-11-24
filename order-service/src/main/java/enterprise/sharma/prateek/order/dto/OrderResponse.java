package enterprise.sharma.prateek.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import enterprise.sharma.prateek.order.entity.OrderStatus;

public record OrderResponse(
		
		UUID orderId,
        String customerId,
        BigDecimal totalAmount,
        OrderStatus status,
        List<OrderItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
		
		) {}
