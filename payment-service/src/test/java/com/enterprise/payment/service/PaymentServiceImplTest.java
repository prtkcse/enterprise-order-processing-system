package com.enterprise.payment.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import enterprise.sharma.prateek.payment.dto.InitiatePaymentRequest;
import enterprise.sharma.prateek.payment.dto.PaymentResponse;
import enterprise.sharma.prateek.payment.entity.Payment;
import enterprise.sharma.prateek.payment.mapper.PaymentMapper;
import enterprise.sharma.prateek.payment.repository.PaymentRepository;
import enterprise.sharma.prateek.payment.service.PaymentServiceImpl;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceImplTest {

	@Mock
	private PaymentRepository paymentRepository;

	@Mock
	private PaymentMapper paymentMapper;

	@InjectMocks
	private PaymentServiceImpl paymentService;

	private UUID orderId;

	@BeforeEach
	void setUp() {
		orderId = UUID.randomUUID();
	}

	@Test
	void shouldReturnExistingPaymentForDuplicateIdempotencyKey() {
		Payment payment = new Payment(orderId, "ORDER-1-PAYMENT", BigDecimal.valueOf(1000), "INR", "UPI");

		var expectedResponse = mock(PaymentResponse.class);

		when(paymentRepository.findByIdempotencyKey("ORDER-1-PAYMENT")).thenReturn(Optional.of(payment));

		when(paymentMapper.toResponse(payment)).thenReturn(expectedResponse);

		InitiatePaymentRequest request = new InitiatePaymentRequest(orderId, BigDecimal.valueOf(1000), "INR", "UPI",
				"ORDER-1-PAYMENT");

		var result = paymentService.initiatePayment(request);

		assertSame(expectedResponse, result);

		verify(paymentRepository, never()).save(any());
	}

	@Test
	void shouldCreateNewPayment() {

		InitiatePaymentRequest request = new InitiatePaymentRequest(orderId, BigDecimal.valueOf(2500), "INR", "UPI",
				"ORDER-2-PAYMENT");

		Payment payment = new Payment(orderId, "ORDER-2-PAYMENT", BigDecimal.valueOf(2500), "INR", "UPI");

		var expectedResponse = mock(PaymentResponse.class);

		when(paymentRepository.findByIdempotencyKey("ORDER-2-PAYMENT")).thenReturn(Optional.empty());

		when(paymentRepository.existsByOrderId(orderId)).thenReturn(false);

		when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

		when(paymentMapper.toResponse(any(Payment.class))).thenReturn(expectedResponse);

		var result = paymentService.initiatePayment(request);

		assertSame(expectedResponse, result);

		verify(paymentRepository, times(1)).save(any(Payment.class));
	}

}
