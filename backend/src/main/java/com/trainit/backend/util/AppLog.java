package com.trainit.backend.util;

import org.slf4j.Logger;

/**
 * Pomocnicze logowanie operacji zakończonych powodzeniem.
 *
 * <p>SLF4J nie ma poziomu {@code SUCCESS} — wpisy trafiają na {@code INFO}
 * z prefiksem {@code [SUCCESS]} w treści komunikatu.
 */
public final class AppLog {

	private AppLog() {
	}

	/**
	 * Loguje udaną operację (np. utworzenie zasobu, poprawne żądanie HTTP).
	 *
	 * @param log logger klasy wywołującej
	 * @param format szablon SLF4J (np. {@code "POST /api/users, id={}"})
	 * @param arguments argumenty szablonu
	 */
	public static void success(Logger log, String format, Object... arguments) {
		log.info("[SUCCESS] " + format, arguments);
	}
}
