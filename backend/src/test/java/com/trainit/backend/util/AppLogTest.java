package com.trainit.backend.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Testy jednostkowe {@link AppLog}.
 */
class AppLogTest {

	@Test
	@DisplayName("success wywołuje logger.info z prefiksem SUCCESS")
	void should_logWithSuccessPrefix_when_successCalled() {
		var log = LoggerFactory.getLogger(AppLogTest.class);

		assertThatCode(() -> AppLog.success(log, "test operacji, id={}", 1))
				.doesNotThrowAnyException();
	}

	@Test
	@DisplayName("success działa bez argumentów formatu")
	void should_logWithoutArguments_when_formatHasNoPlaceholders() {
		var log = LoggerFactory.getLogger(AppLogTest.class);

		assertThatCode(() -> AppLog.success(log, "operacja zakończona"))
				.doesNotThrowAnyException();
	}
}
