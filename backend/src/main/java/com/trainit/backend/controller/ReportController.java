package com.trainit.backend.controller;

import com.trainit.backend.security.JwtPrincipal;
import com.trainit.backend.service.ReportService;
import com.trainit.backend.util.AppLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Kontroler REST do generowania raportów PDF aktywności treningowej.
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

	private static final Logger log = LoggerFactory.getLogger(ReportController.class);

	private final ReportService reportService;

	/**
	 * Tworzy kontroler z wymaganym serwisem raportów.
	 *
	 * @param reportService serwis warstwy biznesowej raportów
	 */
	public ReportController(ReportService reportService) {
		this.reportService = reportService;
	}

	/**
	 * Generuje raport PDF dla użytkownika w podanym zakresie dat.
	 *
	 * @param userId opcjonalny identyfikator użytkownika (domyślnie zalogowany)
	 * @param dateFrom data początkowa yyyy-MM-dd (opcjonalna)
	 * @param dateTo data końcowa yyyy-MM-dd (opcjonalna)
	 * @param type typ raportu (domyślnie PODSUMOWANIE)
	 * @param authentication kontekst uwierzytelnienia
	 * @return plik PDF jako tablica bajtów
	 * @throws IllegalArgumentException gdy brak poprawnego kontekstu uwierzytelnienia
	 */
	@GetMapping("/generate")
	public ResponseEntity<byte[]> generate(
			@RequestParam(required = false) Integer userId,
			@RequestParam(required = false) String dateFrom,
			@RequestParam(required = false) String dateTo,
			@RequestParam(required = false, defaultValue = "PODSUMOWANIE") String type,
			Authentication authentication
	) {
		Integer effectiveUserId = resolveEffectiveUserId(authentication, userId);
		AppLog.success(log, "GET /api/reports/generate, userId={}, type={}, dateFrom={}, dateTo={}",
				effectiveUserId, type, dateFrom, dateTo);
		byte[] pdf = reportService.generatePdf(effectiveUserId, dateFrom, dateTo, type);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"raport.pdf\"")
				.body(pdf);
	}

	/**
	 * Wyznacza efektywny identyfikator użytkownika dla generowania raportu.
	 *
	 * @param authentication kontekst uwierzytelnienia
	 * @param requestedUserId opcjonalny identyfikator z parametru zapytania
	 * @return identyfikator użytkownika, dla którego generowany jest raport
	 * @throws IllegalArgumentException gdy principal nie jest typu {@link JwtPrincipal}
	 */
	private Integer resolveEffectiveUserId(Authentication authentication, Integer requestedUserId) {
		JwtPrincipal principal = resolvePrincipal(authentication);
		if ("ADMIN".equals(principal.role()) || "TRAINER".equals(principal.role())) {
			return requestedUserId != null ? requestedUserId : principal.userId();
		}
		return principal.userId();
	}

	/**
	 * Rozwiązuje principal JWT z kontekstu uwierzytelnienia.
	 *
	 * @param authentication kontekst uwierzytelnienia
	 * @return obiekt {@link JwtPrincipal}
	 * @throws IllegalArgumentException gdy principal nie jest typu {@link JwtPrincipal}
	 */
	private JwtPrincipal resolvePrincipal(Authentication authentication) {
		Object principal = authentication == null ? null : authentication.getPrincipal();
		if (principal instanceof JwtPrincipal jwtPrincipal) {
			return jwtPrincipal;
		}
		log.warn("Brak poprawnego kontekstu uwierzytelnienia w żądaniu raportu");
		throw new IllegalArgumentException("Brak poprawnego kontekstu uwierzytelnienia");
	}
}
