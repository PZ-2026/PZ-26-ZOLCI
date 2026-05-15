package com.trainit.backend.pdf.service;

import com.trainit.backend.pdf.model.ExerciseResultData;
import com.trainit.backend.pdf.model.ReportData;
import com.trainit.backend.pdf.model.SessionData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testy jednostkowe {@link PdfReportService}.
 */
class PdfReportServiceTest {

	private final PdfReportService pdfReportService = new PdfReportService();

	@Test
	@DisplayName("generateReport zwraca niepusty PDF dla poprawnych danych")
	void should_returnPdfBytes_when_dataValid() {
		ReportData data = sampleReportWithSessions();

		byte[] pdf = pdfReportService.generateReport(data);

		assertThat(pdf).isNotNull();
		assertThat(pdf.length).isGreaterThan(500);
	}

	@Test
	@DisplayName("generateReport działa dla pustej listy sesji")
	void should_returnPdfBytes_when_sessionsEmpty() {
		ReportData data = new ReportData(
				"Jan Kowalski",
				"PODSUMOWANIE",
				"2026-01-01",
				"2026-01-31",
				Collections.emptyList()
		);

		byte[] pdf = pdfReportService.generateReport(data);

		assertThat(pdf).isNotNull();
		assertThat(pdf.length).isGreaterThan(200);
	}

	@Test
	@DisplayName("generateReport rzuca IllegalArgumentException gdy data jest null")
	void should_throwIllegalArgumentException_when_dataNull() {
		assertThatThrownBy(() -> pdfReportService.generateReport(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("ReportData");
	}

	private static ReportData sampleReportWithSessions() {
		SessionData session = new SessionData(
				"FBW",
				"2026-01-15 10:00",
				45,
				List.of(new ExerciseResultData("Przysiad", 4, 8, 80.0, "OK"))
		);
		return new ReportData(
				"Jan Kowalski",
				"POSTĘPY",
				"2026-01-01",
				"2026-01-31",
				List.of(session)
		);
	}
}
