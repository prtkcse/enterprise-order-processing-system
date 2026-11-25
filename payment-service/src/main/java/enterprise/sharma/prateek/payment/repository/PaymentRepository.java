package enterprise.sharma.prateek.payment.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import enterprise.sharma.prateek.payment.entity.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID>{
	
	Optional<Payment> findByOrderId(UUID orderId);

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    boolean existsByOrderId(UUID orderId);

    boolean existsByIdempotencyKey(String idempotencyKey);

}
