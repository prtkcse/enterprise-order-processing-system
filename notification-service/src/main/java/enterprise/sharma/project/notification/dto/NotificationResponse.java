package enterprise.sharma.project.notification.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import enterprise.sharma.project.notification.entity.NotificationStatus;
import enterprise.sharma.project.notification.entity.NotificationType;

public record NotificationResponse(

		UUID notificationId, 
		UUID orderId, 
		UUID paymentId, 
		String recipientId, 
		NotificationType notificationType,
		String title, 
		String message, 
		NotificationStatus status, 
		LocalDateTime createdAt, 
		LocalDateTime readAt

) {

}
