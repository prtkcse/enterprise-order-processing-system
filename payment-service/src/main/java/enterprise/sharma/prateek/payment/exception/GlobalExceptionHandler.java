package enterprise.sharma.prateek.payment.exception;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import enterprise.sharma.prateek.payment.dto.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {
	
	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
	private static final Logger errorLogger = LoggerFactory.getLogger("ERROR_LOGGER");

	@ExceptionHandler(PaymentNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleNotFound(PaymentNotFoundException ex) {
		
		log.error("PaymentNotFoundException : "+ex.getMessage());
		errorLogger.error("PaymentNotFoundException : "+ex.getMessage());

		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ErrorResponse(ex.getMessage(), LocalDateTime.now()));
	}

	@ExceptionHandler(DuplicatePaymentException.class)
	public ResponseEntity<ErrorResponse> handleDuplicate(DuplicatePaymentException ex) {
		
		log.error("DuplicatePaymentException : "+ex.getMessage());
		errorLogger.error("DuplicatePaymentException : "+ex.getMessage());

		return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(ex.getMessage(), LocalDateTime.now()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {

		String message = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage()).findFirst()
				.orElse("Validation failed");
		
		log.error("MethodArgumentNotValidException : "+message);
		errorLogger.error("MethodArgumentNotValidException : "+message);

		return ResponseEntity.badRequest().body(new ErrorResponse(message, LocalDateTime.now()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
		
		log.error("Exception : "+ex.getMessage());
		errorLogger.error("Exception : "+ex.getMessage());

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ErrorResponse("Internal payment service error", LocalDateTime.now()));
	}

}
