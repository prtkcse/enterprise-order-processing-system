package enterprise.sharma.project.notification.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "NOTIFICATIONS", uniqueConstraints = {
		@UniqueConstraint(name = "UK_NOTIFICATION_EVENT", columnNames = "EVENT_ID") })
public class Notification {

	@Id
	@JdbcTypeCode(SqlTypes.BINARY)
	@Column(name = "ID", columnDefinition = "BINARY(16)")
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@JdbcTypeCode(SqlTypes.BINARY)
	@Column(name = "EVENT_ID", nullable = false, columnDefinition = "BINARY(16)")
	private UUID eventId;

	@JdbcTypeCode(SqlTypes.BINARY)
	@Column(name = "ORDER_ID", nullable = false, columnDefinition = "BINARY(16)")
	private UUID orderId;

	@JdbcTypeCode(SqlTypes.BINARY)
	@Column(name = "PAYMENT_ID", nullable = false, columnDefinition = "BINARY(16)")
	private UUID paymentId;

	@Column(name = "RECIPIENT_ID", nullable = false, length = 100)
	private String recipientId;

	@Enumerated(EnumType.STRING)
	@Column(name = "NOTIFICATION_TYPE", nullable = false, length = 30)
	private NotificationType notificationType;

	@Column(name = "TITLE", nullable = false, length = 200)
	private String title;

	@Column(name = "MESSAGE", nullable = false, length = 1000)
	private String message;

	@Enumerated(EnumType.STRING)
	@Column(name = "STATUS", nullable = false, length = 20)
	private NotificationStatus status;

	@Column(name = "CREATED_AT", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "READ_AT")
	private LocalDateTime readAt;

	protected Notification() {
	}

	public Notification(UUID eventId, UUID orderId, UUID paymentId, String recipientId,
			NotificationType notificationType, String title, String message) {

		this.eventId = eventId;
		this.orderId = orderId;
		this.paymentId = paymentId;
		this.recipientId = recipientId;
		this.notificationType = notificationType;
		this.title = title;
		this.message = message;
		this.status = NotificationStatus.UNREAD;
		this.createdAt = LocalDateTime.now();
	}

	public void markAsRead() {
		if (this.status == NotificationStatus.UNREAD) {
			this.status = NotificationStatus.READ;
			this.readAt = LocalDateTime.now();
		}
	}

	public UUID getId() {
		return id;
	}

	public UUID getEventId() {
		return eventId;
	}

	public UUID getOrderId() {
		return orderId;
	}

	public UUID getPaymentId() {
		return paymentId;
	}

	public String getRecipientId() {
		return recipientId;
	}

	public NotificationType getNotificationType() {
		return notificationType;
	}

	public String getTitle() {
		return title;
	}

	public String getMessage() {
		return message;
	}

	public NotificationStatus getStatus() {
		return status;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getReadAt() {
		return readAt;
	}

}
