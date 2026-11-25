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
import jakarta.persistence.Table;

@Entity
@Table(name = "PRODUCT_INVENTORY")
public class ProductInventory {

	@Id
	@Column(name = "ID", columnDefinition = "BINARY(16)")
	@JdbcTypeCode(SqlTypes.BINARY)
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "PRODUCT_ID", nullable = false, unique = true)
	private String productId;

	@Column(name = "PRODUCT_NAME", nullable = false)
	private String productName;

	@Column(name = "TOTAL_STOCK", nullable = false)
	private Integer totalStock;

	@Column(name = "AVAILABLE_STOCK", nullable = false)
	private Integer availableStock;

	@Column(name = "RESERVED_STOCK", nullable = false)
	private Integer reservedStock;

	@Column(name = "CREATED_AT", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "UPDATED_AT", nullable = false)
	private LocalDateTime updatedAt;

	protected ProductInventory() {
	}

	public ProductInventory(String productId, String productName, Integer initialStock) {		
		this.productId = productId;
		this.productName = productName;
		this.totalStock = initialStock;
		this.availableStock = initialStock;
		this.reservedStock = 0;
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}

	public void reserveStock(int quantity) {

		if (quantity <= 0) {
			throw new IllegalArgumentException("Quantity must be greater than zero");
		}

		if (availableStock < quantity) {
			throw new IllegalStateException("Insufficient stock for product: " + productId);
		}

		availableStock -= quantity;
		reservedStock += quantity;

		updatedAt = LocalDateTime.now();
	}

	public void releaseStock(int quantity) {

		if (quantity <= 0 || reservedStock < quantity) {
			throw new IllegalStateException("Invalid stock release for product: " + productId);
		}

		reservedStock -= quantity;
		availableStock += quantity;

		updatedAt = LocalDateTime.now();
	}

	public void addStock(int quantity) {

		if (quantity <= 0) {
			throw new IllegalArgumentException("Stock quantity must be positive");
		}

		totalStock += quantity;
		availableStock += quantity;

		updatedAt = LocalDateTime.now();
	}

	public void updateStock(int quantity) {

		if (quantity < 0) {
			throw new IllegalArgumentException("Stock cannot be negative");
		}

		if (quantity < reservedStock) {
			throw new IllegalArgumentException("Total stock cannot be less than reserved stock");
		}

		totalStock = quantity;
		availableStock = quantity - reservedStock;

		updatedAt = LocalDateTime.now();
	}

	public UUID getId() {
		return id;
	}

	public String getProductId() {
		return productId;
	}

	public String getProductName() {
		return productName;
	}

	public Integer getTotalStock() {
		return totalStock;
	}

	public Integer getAvailableStock() {
		return availableStock;
	}

	public Integer getReservedStock() {
		return reservedStock;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

}
