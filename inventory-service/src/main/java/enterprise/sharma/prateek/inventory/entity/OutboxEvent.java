package enterprise.sharma.prateek.inventory.entity;

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
	@Column(name = "PAYLOAD", nullable = false)
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

	@Column(name = "CLAIMED_AT")
	private LocalDateTime claimedAt;

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

}
