package javarax.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ResourceNotFoundExceptionTest {

	@Test
	void shouldCarryGivenMessage() {
		ResourceNotFoundException ex = new ResourceNotFoundException("Account not found");

		assertEquals("Account not found", ex.getMessage());
	}

	@Test
	void shouldBeARuntimeException() {
		ResourceNotFoundException ex = new ResourceNotFoundException("User not found");

		assertTrue(ex instanceof RuntimeException);
	}
}
