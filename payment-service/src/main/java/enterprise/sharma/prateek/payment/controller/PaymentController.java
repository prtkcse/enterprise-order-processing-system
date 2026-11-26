package enterprise.sharma.prateek.payment.controller;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import enterprise.sharma.prateek.payment.dto.InitiatePaymentRequest;
import enterprise.sharma.prateek.payment.dto.PaymentResponse;
import enterprise.sharma.prateek.payment.service.PaymentService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
	
	private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

	private final PaymentService paymentService;

	public PaymentController(PaymentService paymentService) {
		this.paymentService = paymentService;
	}

	@PostMapping
	public ResponseEntity<PaymentResponse> initiatePayment(@Valid @RequestBody InitiatePaymentRequest request) {
		
		log.info("InitiatePaymentRequest received : "+request);

		PaymentResponse response = paymentService.initiatePayment(request);
		
		log.info("PaymentResponse sent : "+response);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/order/{orderId}")
	public ResponseEntity<PaymentResponse> getPaymentByOrderId(@PathVariable UUID orderId) {
		
		log.info("getPaymentByOrderId received with orderId : "+orderId);

		PaymentResponse response = paymentService.getPaymentByOrderId(orderId);
		
		log.info("PaymentResponse sent : "+response);

		return ResponseEntity.ok(response);
	}

}
