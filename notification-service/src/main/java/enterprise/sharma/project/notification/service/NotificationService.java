package enterprise.sharma.project.notification.service;

import java.util.List;
import java.util.UUID;

import enterprise.sharma.project.notification.dto.NotificationResponse;

public interface NotificationService {

	List<NotificationResponse> getNotifications(String recipientId);

	List<NotificationResponse> getUnreadNotifications(String recipientId);

	NotificationResponse markAsRead(UUID notificationId, String recipientId);

}
