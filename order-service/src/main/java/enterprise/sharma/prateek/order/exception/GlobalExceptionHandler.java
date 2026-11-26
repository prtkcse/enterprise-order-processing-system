package enterprise.sharma.prateek.order.exception;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import enterprise.sharma.prateek.order.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {
	
	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
	private static final Logger errorLogger = LoggerFactory.getLogger("ERROR_LOGGER");

	@ExceptionHandler(OrderNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleOrderNotFound(OrderNotFoundException ex, HttpServletRequest request) {
		log.error("OrderNotFoundException : "+ex.getMessage()+"\n"+request.getRequestURI());
		errorLogger.error("OrderNotFoundException : "+ex.getMessage()+"\n"+request.getRequestURI());
		
		return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI(), Map.of());

	}

	@ExceptionHandler(InvalidOrderException.class)
	public ResponseEntity<ErrorResponse> handleInvalidOrder(InvalidOrderException ex, HttpServletRequest request) {
		log.error("InvalidOrderException : "+ex.getMessage()+"\n"+request.getRequestURI());
		errorLogger.error("InvalidOrderException : "+ex.getMessage()+"\n"+request.getRequestURI());
		
		return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI(), Map.of());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
			HttpServletRequest request) {

		Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream().collect(Collectors
				.toMap(error -> error.getField(), error -> error.getDefaultMessage(), (first, second) -> first));
		
		log.error("MethodArgumentNotValidException : "+errors+"\n"+request.getRequestURI());
		errorLogger.error("MethodArgumentNotValidException : "+errors+"\n"+request.getRequestURI());

		return buildResponse(HttpStatus.BAD_REQUEST, "Request validation failed", request.getRequestURI(), errors);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
		
		log.error("Exception : "+ex.getMessage()+"\n"+request.getRequestURI());
		errorLogger.error("Exception : "+ex.getMessage()+"\n"+request.getRequestURI());

		return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request.getRequestURI(),
				Map.of());
	}

	private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message, String path,
			Map<String, String> errors) {

		ErrorResponse response = new ErrorResponse(LocalDateTime.now(), status.value(), status.getReasonPhrase(),
				message, path, errors);

		return ResponseEntity.status(status).body(response);
	}

}
