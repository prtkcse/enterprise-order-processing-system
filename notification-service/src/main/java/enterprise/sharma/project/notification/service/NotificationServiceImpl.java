package enterprise.sharma.project.notification.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import enterprise.sharma.project.notification.dto.NotificationResponse;
import enterprise.sharma.project.notification.entity.Notification;
import enterprise.sharma.project.notification.entity.NotificationStatus;
import enterprise.sharma.project.notification.exception.NotificationNotFoundException;
import enterprise.sharma.project.notification.mapper.NotificationMapper;
import enterprise.sharma.project.notification.repository.NotificationRepository;

@Service
public class NotificationServiceImpl implements NotificationService {

	private final NotificationRepository notificationRepository;
	private final NotificationMapper notificationMapper;

	public NotificationServiceImpl(NotificationRepository notificationRepository,
			NotificationMapper notificationMapper) {
		this.notificationRepository = notificationRepository;
		this.notificationMapper = notificationMapper;
	}

	@Override
	@Transactional(readOnly = true)
	public List<NotificationResponse> getNotifications(String recipientId) {
		return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId).stream()
				.map(notificationMapper::toResponse).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<NotificationResponse> getUnreadNotifications(String recipientId) {
		return notificationRepository
				.findByRecipientIdAndStatusOrderByCreatedAtDesc(recipientId, NotificationStatus.UNREAD).stream()
				.map(notificationMapper::toResponse).toList();
	}

	@Override
	@Transactional
	public NotificationResponse markAsRead(UUID notificationId, String recipientId) {
		Notification notification = notificationRepository.findByIdAndRecipientId(notificationId, recipientId)
				.orElseThrow(() -> new NotificationNotFoundException(notificationId));

		notification.markAsRead();

		return notificationMapper.toResponse(notification);
	}

}
