package enterprise.sharma.prateek.order.entity;

import java.math.BigDecimal;
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
@Table(name = "ORDER_ITEMS")
public class OrderItem {

	@Id
	@Column(name = "ID", columnDefinition = "BINARY(16)")
	@JdbcTypeCode(SqlTypes.BINARY)
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "PRODUCT_ID", nullable = false)
	private String productId;

	@Column(name = "QUANTITY", nullable = false)
	private Integer quantity;

	@Column(name = "UNIT_PRICE", nullable = false)
	private BigDecimal unitPrice;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ORDER_ID", nullable = false)
	private Order order;

	protected OrderItem() {
	}

	public OrderItem(String productId, Integer quantity, BigDecimal unitPrice) {		
		this.productId = productId;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
	}

	public void setOrder(Order order) {
		this.order = order;
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

	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	public Order getOrder() {
		return order;
	}

}
