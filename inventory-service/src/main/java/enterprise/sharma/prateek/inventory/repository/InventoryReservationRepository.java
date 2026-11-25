package enterprise.sharma.prateek.inventory.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import enterprise.sharma.prateek.inventory.entity.InventoryReservation;
import enterprise.sharma.prateek.inventory.entity.ReservationStatus;

public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, UUID> {

	Optional<InventoryReservation> findByOrderId(UUID orderId);

	boolean existsByOrderId(UUID orderId);

	Optional<InventoryReservation> findByOrderIdAndStatus(UUID orderId, ReservationStatus status);

}
