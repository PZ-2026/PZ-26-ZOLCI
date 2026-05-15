package com.trainit.backend.controller;

import com.trainit.backend.exception.GlobalExceptionHandler;
import com.trainit.backend.security.JwtPrincipal;
import com.trainit.backend.service.ReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testy warstwy MVC kontrolera {@link ReportController}.
 */
@WebMvcTest(ReportController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class ReportControllerTest {

	private static final byte[] SAMPLE_PDF = new byte[]{0x25, 0x50, 0x44, 0x46};

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ReportController reportController;

	@MockitoBean
	private ReportService reportService;

	private static Authentication userAuthentication() {
		return jwtAuthentication(new JwtPrincipal(10, "user@test.com", "USER"), "ROLE_USER");
	}

	private static Authentication trainerAuthentication() {
		return jwtAuthentication(new JwtPrincipal(20, "trainer@test.com", "TRAINER"), "ROLE_TRAINER");
	}

	private static Authentication jwtAuthentication(JwtPrincipal principal, String roleAuthority) {
		return new UsernamePasswordAuthenticationToken(
				principal,
				null,
				List.of(new SimpleGrantedAuthority(roleAuthority))
		);
	}

	@Test
	@DisplayName("GET /api/reports/generate jako USER → 200 PDF")
	void should_return200Pdf_when_userAuthenticated() {
		when(reportService.generatePdf(eq(10), isNull(), isNull(), eq("PODSUMOWANIE")))
				.thenReturn(SAMPLE_PDF);

		ResponseEntity<byte[]> response = reportController.generate(
				null, null, null, "PODSUMOWANIE", userAuthentication());

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE))
				.isEqualTo(MediaType.APPLICATION_PDF_VALUE);
		assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
				.isEqualTo("attachment; filename=\"raport.pdf\"");
		assertThat(response.getBody()).isEqualTo(SAMPLE_PDF);
	}

	@Test
	@DisplayName("GET /api/reports/generate jako TRAINER z userId → 200 PDF")
	void should_return200Pdf_when_trainerWithUserId() {
		when(reportService.generatePdf(eq(42), isNull(), isNull(), eq("PODSUMOWANIE")))
				.thenReturn(SAMPLE_PDF);

		ResponseEntity<byte[]> response = reportController.generate(
				42, null, null, "PODSUMOWANIE", trainerAuthentication());

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE))
				.isEqualTo(MediaType.APPLICATION_PDF_VALUE);
		assertThat(response.getBody()).isEqualTo(SAMPLE_PDF);
	}

	@Test
	@DisplayName("GET /api/reports/generate bez principal → 400")
	void should_return400_when_noAuthPrincipal() throws Exception {
		mockMvc.perform(get("/api/reports/generate"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Brak poprawnego kontekstu uwierzytelnienia"));
	}
}
