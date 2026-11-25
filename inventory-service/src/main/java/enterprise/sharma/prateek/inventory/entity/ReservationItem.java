package enterprise.sharma.prateek.inventory.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "INVENTORY_RESERVATION_ITEMS")
public class ReservationItem {

	@Id
	@Column(name = "ID", columnDefinition = "BINARY(16)")
	@JdbcTypeCode(SqlTypes.BINARY)
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "RESERVATION_ID", nullable = false)
	private InventoryReservation reservation;

	@Column(name = "PRODUCT_ID", nullable = false)
	private String productId;

	@Column(name = "QUANTITY", nullable = false)
	private Integer quantity;

	@Column(name = "CREATED_AT", nullable = false)
	private LocalDateTime createdAt;

	protected ReservationItem() {
	}

	public ReservationItem(String productId, Integer quantity) {
		this.productId = productId;
		this.quantity = quantity;
		this.createdAt = LocalDateTime.now();
	}

	void setReservation(InventoryReservation reservation) {
		this.reservation = reservation;
	}

	public UUID getId() {
		return id;
	}

	public String getProductId() {
		return productId;
	}

	public Integer getQuantity() {
		return quantity;
	}

}
