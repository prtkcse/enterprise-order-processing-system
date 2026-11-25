package enterprise.sharma.prateek.inventory.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import enterprise.sharma.prateek.inventory.entity.ProductInventory;
import jakarta.persistence.LockModeType;

public interface ProductInventoryRepository extends JpaRepository<ProductInventory, UUID> {

	Optional<ProductInventory> findByProductId(String productId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			SELECT p
			FROM ProductInventory p
			WHERE p.productId IN :productIds
			ORDER BY p.productId
			""")
	List<ProductInventory> findAllForUpdate(@Param("productIds") List<String> productIds);

}
