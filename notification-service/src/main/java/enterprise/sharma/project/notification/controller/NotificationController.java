package enterprise.sharma.project.notification.controller;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import enterprise.sharma.project.notification.dto.NotificationResponse;
import enterprise.sharma.project.notification.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

	private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

	private final NotificationService notificationService;

	public NotificationController(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	@GetMapping("/customer/{recipientId}")
	public ResponseEntity<List<NotificationResponse>> getNotifications(@PathVariable String recipientId) {

		log.info("getNotifications request received with the recipientId : " + recipientId);

		return ResponseEntity.ok(notificationService.getNotifications(recipientId));
	}

	@GetMapping("/customer/{recipientId}/unread")
	public ResponseEntity<List<NotificationResponse>> getUnreadNotifications(@PathVariable String recipientId) {

		log.info("getUnreadNotifications request received with the recipientId : " + recipientId);

		return ResponseEntity.ok(notificationService.getUnreadNotifications(recipientId));
	}

	@PatchMapping("/{notificationId}/read")
	public ResponseEntity<NotificationResponse> markAsRead(@PathVariable UUID notificationId,
			@RequestParam String recipientId) {

		log.info("markAsRead request received with the recipientId : " + recipientId + " and notificationId : "
				+ notificationId);

		return ResponseEntity.ok(notificationService.markAsRead(notificationId, recipientId));
	}

}
