package com.trainit.backend.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

/**
 * Serwis generujący token JWT dla uwierzytelnionych użytkowników.
 *
 * <p>Token zawiera `userId`, `email`, `role`, unikalny identyfikator `jti`
 * oraz czas wygaśnięcia `exp`.
 */
@Service
public class JwtService {

	private static final String HMAC_ALGORITHM = "HmacSHA256";
	private static final String HEADER = base64Url("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");

	private final String secret;
	private final long expirationSeconds;

	/**
	 * Buduje serwis na podstawie właściwości środowiska.
	 *
	 * @param secret klucz podpisu JWT
	 * @param expirationSeconds czas życia tokena w sekundach
	 */
	public JwtService(
			@Value("${app.security.jwt.secret:change-this-dev-secret-key}") String secret,
			@Value("${app.security.jwt.expiration-seconds:86400}") long expirationSeconds
	) {
		this.secret = secret;
		this.expirationSeconds = expirationSeconds;
	}

	/**
	 * Generuje podpisany token JWT.
	 *
	 * @param userId identyfikator użytkownika
	 * @param email email użytkownika
	 * @param role rola użytkownika
	 * @return token JWT
	 */
	public String generateToken(Integer userId, String email, String role) {
		long exp = Instant.now().getEpochSecond() + expirationSeconds;
		String jti = UUID.randomUUID().toString();
		String payload = base64Url(
				"{\"userId\":" + userId
						+ ",\"email\":\"" + escape(email) + "\""
						+ ",\"role\":\"" + escape(role) + "\""
						+ ",\"jti\":\"" + jti + "\""
						+ ",\"exp\":" + exp + "}"
		);
		String signature = sign(HEADER + "." + payload);
		return HEADER + "." + payload + "." + signature;
	}

	/**
	 * Parsuje i weryfikuje token JWT.
	 *
	 * @param token token z nagłówka Authorization
	 * @return principal użytkownika zapisany w tokenie
	 */
	public JwtPrincipal parseToken(String token) {
		String[] parts = token.split("\\.");
		if (parts.length != 3) {
			throw new IllegalArgumentException("Nieprawidłowy format tokena");
		}
		String unsigned = parts[0] + "." + parts[1];
		if (!sign(unsigned).equals(parts[2])) {
			throw new IllegalArgumentException("Nieprawidłowy podpis tokena");
		}
		String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
		Integer userId = extractInt(payload, "userId");
		String email = extractString(payload, "email");
		String role = extractString(payload, "role");
		Long exp = extractLong(payload, "exp");
		if (userId == null || email == null || role == null || exp == null) {
			throw new IllegalArgumentException("Niekompletny payload tokena");
		}
		if (exp < Instant.now().getEpochSecond()) {
			throw new IllegalArgumentException("Token wygasł");
		}
		return new JwtPrincipal(userId, email, role);
	}

	private String sign(String data) {
		try {
			Mac mac = Mac.getInstance(HMAC_ALGORITHM);
			mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
			byte[] signature = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
			return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
		} catch (Exception ex) {
			throw new IllegalStateException("Nie udało się podpisać tokena", ex);
		}
	}

	private static String base64Url(String value) {
		return Base64.getUrlEncoder().withoutPadding()
				.encodeToString(value.getBytes(StandardCharsets.UTF_8));
	}

	private static String escape(String value) {
		return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
	}

	private static Integer extractInt(String json, String key) {
		Long value = extractLong(json, key);
		return value == null ? null : value.intValue();
	}

	private static Long extractLong(String json, String key) {
		String marker = "\"" + key + "\":";
		int start = json.indexOf(marker);
		if (start < 0) {
			return null;
		}
		int from = start + marker.length();
		int to = from;
		while (to < json.length() && Character.isDigit(json.charAt(to))) {
			to++;
		}
		if (to == from) {
			return null;
		}
		return Long.parseLong(json.substring(from, to));
	}

	private static String extractString(String json, String key) {
		String marker = "\"" + key + "\":\"";
		int start = json.indexOf(marker);
		if (start < 0) {
			return null;
		}
		int from = start + marker.length();
		int to = json.indexOf("\"", from);
		if (to < 0) {
			return null;
		}
		return json.substring(from, to);
	}
}
