package enterprise.sharma.prateek.order.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "OUTBOX_EVENTS")
public class OutboxEvent {

	@Id
	@Column(name = "ID", columnDefinition = "BINARY(16)")
	@JdbcTypeCode(SqlTypes.BINARY)
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "AGGREGATE_ID", nullable = false, columnDefinition = "BINARY(16)")
	@JdbcTypeCode(SqlTypes.BINARY)
	private UUID aggregateId;

	@Column(name = "EVENT_TYPE", nullable = false)
	private String eventType;

	@Column(name = "TOPIC", nullable = false)
	private String topic;

	@Column(name = "EVENT_KEY", nullable = false)
	private String eventKey;

	@Lob
	@Column(name = "PAYLOAD", nullable = false, columnDefinition = "json")
	private String payload;

	@Column(name = "STATUS", nullable = false)
	private String status;

	@Column(name = "CREATED_AT", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "PUBLISHED_AT")
	private LocalDateTime publishedAt;

	@Column(name = "RETRY_COUNT", nullable = false)
	private Integer retryCount;

	@Column(name = "NEXT_RETRY_AT")
	private LocalDateTime nextRetryAt;

	@Column(name = "ERROR_MESSAGE")
	private String errorMessage;

	protected OutboxEvent() {
	}

	public OutboxEvent(UUID aggregateId, String eventType, String topic, String eventKey, String payload) {		
		this.aggregateId = aggregateId;
		this.eventType = eventType;
		this.topic = topic;
		this.eventKey = eventKey;
		this.payload = payload;
		this.status = "PENDING";
		this.createdAt = LocalDateTime.now();
		this.retryCount = 0;
	}

	public void markProcessing() {
		this.status = "PROCESSING";
	}

	public void markPublished() {
		this.status = "PUBLISHED";
		this.publishedAt = LocalDateTime.now();
		this.errorMessage = null;
	}

	public void markFailed(String error) {
		this.retryCount++;
		this.errorMessage = error;

		if (retryCount >= 10) {
			this.status = "FAILED";
			this.nextRetryAt = null;
		} else {
			this.status = "PENDING";

			// Exponential backoff, capped at 5 minutes
			long delaySeconds = Math.min(300, (long) Math.pow(2, retryCount));

			this.nextRetryAt = LocalDateTime.now().plusSeconds(delaySeconds);
		}
	}

	public UUID getId() {
		return id;
	}

	public UUID getAggregateId() {
		return aggregateId;
	}

	public String getEventType() {
		return eventType;
	}

	public String getTopic() {
		return topic;
	}

	public String getEventKey() {
		return eventKey;
	}

	public String getPayload() {
		return payload;
	}

	public String getStatus() {
		return status;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getPublishedAt() {
		return publishedAt;
	}

	public Integer getRetryCount() {
		return retryCount;
	}

	public LocalDateTime getNextRetryAt() {
		return nextRetryAt;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

}
