package enterprise.sharma.project.notification.kafka;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import enterprise.sharma.prateek.common.events.PaymentCompletedEvent;
import enterprise.sharma.prateek.common.events.PaymentFailedEvent;
import enterprise.sharma.project.notification.entity.Notification;
import enterprise.sharma.project.notification.entity.NotificationType;
import enterprise.sharma.project.notification.repository.NotificationRepository;

@Service
public class NotificationEventProcessor {

	private final NotificationRepository notificationRepository;

	public NotificationEventProcessor(NotificationRepository notificationRepository) {
		this.notificationRepository = notificationRepository;
	}

	@Transactional
	public void processPaymentCompleted(PaymentCompletedEvent event) {

		if (notificationRepository.existsByEventId(event.eventId())) {
			return;
		}

		String recipientId = event.orderId().toString();

		Notification notification = new Notification(event.eventId(), event.orderId(), event.paymentId(), recipientId,
				NotificationType.PAYMENT_SUCCESS, "Payment Successful",
				"Your payment of " + event.amount() + " " + event.currency() + " for order " + event.orderId()
						+ " was successful. Transaction reference: " + event.transactionReference());

		notificationRepository.save(notification);

	}

	@Transactional
	public void processPaymentFailed(PaymentFailedEvent event) {

		if (notificationRepository.existsByEventId(event.eventId())) {
			return;
		}

		String recipientId = event.orderId().toString();

		Notification notification = new Notification(event.eventId(), event.orderId(), event.paymentId(), recipientId,
				NotificationType.PAYMENT_FAILED, "Payment Failed",
				"Your payment of " + event.amount() + " " + event.currency() + " for order " + event.orderId()
						+ " could not be completed. Reason: " + event.failureReason());

		notificationRepository.save(notification);

	}

}
