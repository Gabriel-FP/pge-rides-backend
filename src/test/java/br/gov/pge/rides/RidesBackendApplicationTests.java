package br.gov.pge.rides;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class RidesBackendApplicationTests {

	@Test
	void mainClassShouldExist() {
		assertDoesNotThrow(() -> Class.forName("br.gov.pge.rides.RidesBackendApplication"));
	}
}
