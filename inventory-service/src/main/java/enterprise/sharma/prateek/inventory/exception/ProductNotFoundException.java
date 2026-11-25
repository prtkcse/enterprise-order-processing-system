package enterprise.sharma.prateek.inventory.exception;

public class ProductNotFoundException extends RuntimeException {

	public ProductNotFoundException(String productId) {
		super("Product not found in inventory: " + productId);
	}

}
