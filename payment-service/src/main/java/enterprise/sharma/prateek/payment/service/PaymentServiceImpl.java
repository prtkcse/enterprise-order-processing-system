package enterprise.sharma.prateek.payment.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import enterprise.sharma.prateek.payment.dto.InitiatePaymentRequest;
import enterprise.sharma.prateek.payment.dto.PaymentResponse;
import enterprise.sharma.prateek.payment.entity.Payment;
import enterprise.sharma.prateek.payment.exception.DuplicatePaymentException;
import enterprise.sharma.prateek.payment.exception.PaymentNotFoundException;
import enterprise.sharma.prateek.payment.mapper.PaymentMapper;
import enterprise.sharma.prateek.payment.repository.PaymentRepository;

@Service
public class PaymentServiceImpl implements PaymentService {

	private final PaymentRepository paymentRepository;
	private final PaymentMapper paymentMapper;

	public PaymentServiceImpl(PaymentRepository paymentRepository, PaymentMapper paymentMapper) {

		this.paymentRepository = paymentRepository;
		this.paymentMapper = paymentMapper;
	}

	@Override
	@Transactional
	public PaymentResponse initiatePayment(InitiatePaymentRequest request) {

		/*
		 * Idempotency check
		 */
		var existingPayment = paymentRepository.findByIdempotencyKey(request.idempotencyKey());
		if (existingPayment.isPresent()) {
			return paymentMapper.toResponse(existingPayment.get());
		}

		/*
		 * One payment per order in this implementation.
		 */
		if (paymentRepository.existsByOrderId(request.orderId())) {

			throw new DuplicatePaymentException("A payment already exists for order " + request.orderId());
		}

		Payment payment = new Payment(request.orderId(), request.idempotencyKey(), request.amount(),
				request.currency().toUpperCase(), request.paymentMethod().toUpperCase());

		Payment savedPayment = paymentRepository.save(payment);

		return paymentMapper.toResponse(savedPayment);
	}

	@Override
	@Transactional(readOnly = true)
	public PaymentResponse getPaymentByOrderId(UUID orderId) {
		Payment payment = paymentRepository.findByOrderId(orderId)
				.orElseThrow(() -> new PaymentNotFoundException(orderId));

		return paymentMapper.toResponse(payment);
	}

}
