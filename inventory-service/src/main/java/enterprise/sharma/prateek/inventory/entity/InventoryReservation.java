package enterprise.sharma.prateek.inventory.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "INVENTORY_RESERVATIONS")
public class InventoryReservation {

	@Id
	@Column(name = "ID", columnDefinition = "BINARY(16)")
	@JdbcTypeCode(SqlTypes.BINARY)
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "ORDER_ID", nullable = false, unique = true)
	private UUID orderId;

	@Enumerated(EnumType.STRING)
	@Column(name = "STATUS", nullable = false)
	private ReservationStatus status;

	@Column(name = "FAILURE_REASON")
	private String failureReason;

	@Column(name = "CREATED_AT", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "UPDATED_AT", nullable = false)
	private LocalDateTime updatedAt;

	@OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ReservationItem> items = new ArrayList<>();

	protected InventoryReservation() {
	}

	public InventoryReservation(UUID orderId) {
		this.orderId = orderId;
		this.status = ReservationStatus.RESERVED;
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}

	public void addItem(ReservationItem item) {
		items.add(item);
		item.setReservation(this);
	}

	public void markFailed(String reason) {
		this.status = ReservationStatus.FAILED;
		this.failureReason = reason;
		this.updatedAt = LocalDateTime.now();
	}

	public void markReleased() {
		this.status = ReservationStatus.RELEASED;
		this.updatedAt = LocalDateTime.now();
	}

	public UUID getId() {
		return id;
	}

	public UUID getOrderId() {
		return orderId;
	}

	public ReservationStatus getStatus() {
		return status;
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

	public List<ReservationItem> getItems() {
		return items;
	}

}
