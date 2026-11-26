package enterprise.sharma.project.notification.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import enterprise.sharma.project.notification.entity.Notification;
import enterprise.sharma.project.notification.entity.NotificationStatus;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

	boolean existsByEventId(UUID eventId);

	List<Notification> findByRecipientIdOrderByCreatedAtDesc(String recipientId);

	List<Notification> findByRecipientIdAndStatusOrderByCreatedAtDesc(String recipientId,
			NotificationStatus status);

	Optional<Notification> findByIdAndRecipientId(UUID id, String recipientId);

}
