package javarax.exception;

/** Конфлікт стану ресурсу (наприклад, email уже зайнятий) -> HTTP 409. */
public class ConflictException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public ConflictException(String message) {
		super(message);
	}
}
