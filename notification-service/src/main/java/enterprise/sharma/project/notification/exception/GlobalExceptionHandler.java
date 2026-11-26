package enterprise.sharma.project.notification.exception;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import enterprise.sharma.project.notification.dto.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {
	
	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
	private static final Logger errorLogger = LoggerFactory.getLogger("ERROR_LOGGER");

	@ExceptionHandler(NotificationNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleNotFound(NotificationNotFoundException exception) {
		
		log.error("NotificationNotFoundException : "+exception.getMessage());
		errorLogger.error("NotificationNotFoundException : "+exception.getMessage());

		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ErrorResponse(exception.getMessage(), LocalDateTime.now()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGenericException(Exception exception) {
		
		log.error("Exception : "+exception.getMessage());
		errorLogger.error("Exception : "+exception.getMessage());

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ErrorResponse("An unexpected error occurred", LocalDateTime.now()));
	}

}
