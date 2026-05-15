package com.trainit.backend.pdf.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Model danych wejściowych do wygenerowania raportu PDF aktywności treningowej.
 *
 * <p>Agreguje metadane raportu (użytkownik, typ, zakres dat) oraz listę sesji
 * treningowych ({@link SessionData}). Przekazywany do {@link com.trainit.backend.pdf.service.PdfReportService}.
 *
 * @see SessionData
 * @see com.trainit.backend.pdf.service.PdfReportService
 */
public class ReportData {

	/** Imię i nazwisko lub nazwa użytkownika wyświetlana w nagłówku raportu. */
	private String userName;

	/** Typ raportu (np. tygodniowy, miesięczny). */
	private String reportType;

	/** Data początkowa zakresu raportu w formacie tekstowym. */
	private String dateFrom;

	/** Data końcowa zakresu raportu w formacie tekstowym. */
	private String dateTo;

	/** Lista sesji treningowych uwzględnionych w raporcie. */
	private List<SessionData> sessions = new ArrayList<>();

	/**
	 * Konstruktor bezargumentowy wymagany m.in. do mapowania danych z JSON lub ręcznego budowania obiektu.
	 */
	public ReportData() {
	}

	/**
	 * Tworzy dane raportu z podanymi wartościami pól.
	 *
	 * @param userName nazwa użytkownika w nagłówku
	 * @param reportType typ raportu
	 * @param dateFrom data początkowa zakresu
	 * @param dateTo data końcowa zakresu
	 * @param sessions lista sesji; {@code null} zostanie zastąpiona pustą listą
	 */
	public ReportData(
			String userName,
			String reportType,
			String dateFrom,
			String dateTo,
			List<SessionData> sessions) {
		this.userName = userName;
		this.reportType = reportType;
		this.dateFrom = dateFrom;
		this.dateTo = dateTo;
		if (sessions != null) {
			this.sessions = sessions;
		}
	}

	/**
	 * Zwraca nazwę użytkownika wyświetlaną w raporcie.
	 *
	 * @return nazwa użytkownika
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Ustawia nazwę użytkownika w raporcie.
	 *
	 * @param userName imię i nazwisko lub identyfikator wyświetlany w PDF
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * Zwraca typ raportu.
	 *
	 * @return typ raportu (np. WEEKLY, MONTHLY)
	 */
	public String getReportType() {
		return reportType;
	}

	/**
	 * Ustawia typ raportu.
	 *
	 * @param reportType typ raportu
	 */
	public void setReportType(String reportType) {
		this.reportType = reportType;
	}

	/**
	 * Zwraca datę początkową zakresu raportu.
	 *
	 * @return data początkowa w formacie tekstowym
	 */
	public String getDateFrom() {
		return dateFrom;
	}

	/**
	 * Ustawia datę początkową zakresu.
	 *
	 * @param dateFrom data początkowa
	 */
	public void setDateFrom(String dateFrom) {
		this.dateFrom = dateFrom;
	}

	/**
	 * Zwraca datę końcową zakresu raportu.
	 *
	 * @return data końcowa w formacie tekstowym
	 */
	public String getDateTo() {
		return dateTo;
	}

	/**
	 * Ustawia datę końcową zakresu.
	 *
	 * @param dateTo data końcowa
	 */
	public void setDateTo(String dateTo) {
		this.dateTo = dateTo;
	}

	/**
	 * Zwraca listę sesji treningowych w raporcie.
	 *
	 * @return lista sesji (nigdy {@code null} po ustawieniu przez {@link #setSessions})
	 */
	public List<SessionData> getSessions() {
		return sessions;
	}

	/**
	 * Ustawia listę sesji; {@code null} zostanie zastąpiona pustą listą.
	 *
	 * @param sessions lista sesji treningowych
	 */
	public void setSessions(List<SessionData> sessions) {
		this.sessions = sessions != null ? sessions : new ArrayList<>();
	}
}
