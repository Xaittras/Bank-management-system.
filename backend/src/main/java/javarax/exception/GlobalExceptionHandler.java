package javarax.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/** Окремий клас верхнього рівня: вкладений нестатичний клас Spring як advice не підхоплює. */
@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(MissingRequestHeaderException.class)
	public ResponseEntity<String> handleMissingHeader(MissingRequestHeaderException e) {
		return ResponseEntity.badRequest().body("Відсутній обов'язковий заголовок: " + e.getHeaderName());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<String> handleValidation(MethodArgumentNotValidException e) {
		String msg = e.getBindingResult().getFieldErrors().stream()
				.map(f -> f.getField() + ": " + f.getDefaultMessage())
				.reduce((a, b) -> a + "; " + b).orElse("Невалідний запит");
		return ResponseEntity.badRequest().body(msg);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<String> handleUnreadable(HttpMessageNotReadableException e) {
		return ResponseEntity.badRequest().body("Некоректне тіло запиту");
	}

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<String> handleStatus(ResponseStatusException e) {
		return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
	}

	// Без цього AccessDeniedException від @PreAuthorize потрапив би в handleOther і дав 500
	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<String> handleAccessDenied(AccessDeniedException e) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Доступ заборонено");
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<String> handleNotFound(ResourceNotFoundException e) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
	}

	@ExceptionHandler(ConflictException.class)
	public ResponseEntity<String> handleConflict(ConflictException e) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<String> handleIntegrity(DataIntegrityViolationException e) {
		log.warn("Порушення цілісності даних: {}", e.getMostSpecificCause().getMessage());
		return ResponseEntity.status(HttpStatus.CONFLICT).body("Конфлікт даних");
	}

	// Бізнес-помилки: недостатньо коштів, невалідна сума тощо
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException e) {
		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(e.getMessage());
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<String> handleIllegalState(IllegalStateException e) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<String> handleOther(Exception e) {
		log.error("Необроблена помилка", e);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Внутрішня помилка сервера");
	}
}
