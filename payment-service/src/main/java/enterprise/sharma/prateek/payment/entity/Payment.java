package enterprise.sharma.prateek.payment.entity;

import java.math.BigDecimal;
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

@Entity
@Table(name = "PAYMENTS")
public class Payment {

	@Id
	@JdbcTypeCode(SqlTypes.BINARY)
	@Column(name = "ID", columnDefinition = "BINARY(16)")
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@JdbcTypeCode(SqlTypes.BINARY)
	@Column(name = "ORDER_ID", nullable = false, unique = true, columnDefinition = "BINARY(16)")
	private UUID orderId;

	@Column(name = "IDEMPOTENCY_KEY", nullable = false, unique = true)
	private String idempotencyKey;

	@Column(name = "AMOUNT", precision = 12, scale = 2, nullable = false)
	private BigDecimal amount;

	@Column(name = "CURRENCY", length = 3, nullable = false)
	private String currency;

	@Enumerated(EnumType.STRING)
	@Column(name = "STATUS", length = 30, nullable = false)
	private PaymentStatus status;

	@Column(name = "PAYMENT_METHOD", length = 30)
	private String paymentMethod;

	@Column(name = "TRANSACTION_REFERENCE", length = 100)
	private String transactionReference;

	@Column(name = "FAILURE_REASON", length = 500)
	private String failureReason;

	@Column(name = "CREATED_AT", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "UPDATED_AT", nullable = false)
	private LocalDateTime updatedAt;

	protected Payment() {
	}

	public Payment(UUID orderId, String idempotencyKey, BigDecimal amount, String currency, String paymentMethod) {

		this.orderId = orderId;
		this.idempotencyKey = idempotencyKey;
		this.amount = amount;
		this.currency = currency;
		this.paymentMethod = paymentMethod;
		this.status = PaymentStatus.INITIATED;

		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}

	public void markProcessing() {
		this.status = PaymentStatus.PROCESSING;
		this.updatedAt = LocalDateTime.now();
	}

	public void markSuccess(String transactionReference) {
		this.status = PaymentStatus.SUCCESS;
		this.transactionReference = transactionReference;
		this.failureReason = null;
		this.updatedAt = LocalDateTime.now();
	}

	public void markFailed(String failureReason) {
		this.status = PaymentStatus.FAILED;
		this.failureReason = failureReason;
		this.updatedAt = LocalDateTime.now();
	}

	public void markRefunded() {
		this.status = PaymentStatus.REFUNDED;
		this.updatedAt = LocalDateTime.now();
	}

	public UUID getId() {
		return id;
	}

	public UUID getOrderId() {
		return orderId;
	}

	public String getIdempotencyKey() {
		return idempotencyKey;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public String getCurrency() {
		return currency;
	}

	public PaymentStatus getStatus() {
		return status;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public String getTransactionReference() {
		return transactionReference;
	}

	public String getFailureReason() {
		return failureReason;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

}
