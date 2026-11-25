package enterprise.sharma.prateek.payment.service;

public record PaymentProcessingResult(

		boolean success,

		String transactionReference,

		String failureReason

) {

	public static PaymentProcessingResult success(String reference) {

		return new PaymentProcessingResult(true, reference, null);
	}

	public static PaymentProcessingResult failure(String reason) {

		return new PaymentProcessingResult(false, null, reason);
	}

}
