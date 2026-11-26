package enterprise.sharma.project.notification.mapper;

import org.springframework.stereotype.Component;

import enterprise.sharma.project.notification.dto.NotificationResponse;
import enterprise.sharma.project.notification.entity.Notification;

@Component
public class NotificationMapper {

	public NotificationResponse toResponse(Notification notification) {

		return new NotificationResponse(notification.getId(), notification.getOrderId(), notification.getPaymentId(),
				notification.getRecipientId(), notification.getNotificationType(), notification.getTitle(),
				notification.getMessage(), notification.getStatus(), notification.getCreatedAt(),
				notification.getReadAt());
	}

}
