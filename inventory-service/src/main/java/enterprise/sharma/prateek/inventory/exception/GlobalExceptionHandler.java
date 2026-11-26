package enterprise.sharma.prateek.inventory.exception;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import enterprise.sharma.prateek.inventory.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {
	
	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
	private static final Logger errorLogger = LoggerFactory.getLogger("ERROR_LOGGER");

	@ExceptionHandler(ProductNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleNotFound(ProductNotFoundException ex, HttpServletRequest request) {
		
		log.error("ProductNotFoundException : "+ex.getMessage());
		errorLogger.error("ProductNotFoundException : "+ex.getMessage());
		
		return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, Map.of());
	}

	@ExceptionHandler(InsufficientStockException.class)
	public ResponseEntity<ErrorResponse> handleInsufficientStock(InsufficientStockException ex,
			HttpServletRequest request) {
		
		log.error("InsufficientStockException : "+ex.getMessage());
		errorLogger.error("InsufficientStockException : "+ex.getMessage());
		
		return build(HttpStatus.CONFLICT, ex.getMessage(), request, Map.of());
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex,
			HttpServletRequest request) {
		
		log.error("IllegalArgumentException : "+ex.getMessage());
		errorLogger.error("IllegalArgumentException : "+ex.getMessage());
		
		return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request, Map.of());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
			HttpServletRequest request) {		

		Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream().collect(Collectors
				.toMap(error -> error.getField(), error -> error.getDefaultMessage(), (first, second) -> first));
		
		log.error("MethodArgumentNotValidException : "+errors);
		errorLogger.error("MethodArgumentNotValidException : "+errors);

		return build(HttpStatus.BAD_REQUEST, "Request validation failed", request, errors);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErrorResponse> handleConflict(DataIntegrityViolationException ex,
			HttpServletRequest request) {
		
		log.error("DataIntegrityViolationException : "+ex.getMessage());
		errorLogger.error("DataIntegrityViolationException : "+ex.getMessage());
		
		return build(HttpStatus.CONFLICT, "A conflicting inventory operation already exists", request, Map.of());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
		
		log.error("Exception : "+ex.getMessage());
		errorLogger.error("Exception : "+ex.getMessage());

		return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request, Map.of());
	}

	private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest request,
			Map<String, String> errors) {

		ErrorResponse response = new ErrorResponse(LocalDateTime.now(), status.value(), status.getReasonPhrase(),
				message, request.getRequestURI(), errors);

		return ResponseEntity.status(status).body(response);
	}

}
