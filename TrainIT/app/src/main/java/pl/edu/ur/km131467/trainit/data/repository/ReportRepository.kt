package pl.edu.ur.km131467.trainit.data.repository

import android.content.Context
import androidx.core.content.FileProvider
import pl.edu.ur.km131467.trainit.data.remote.NetworkModule
import java.io.File

/**
 * Repozytorium obsługujące generowanie i pobieranie raportów PDF z serwera.
 */
class ReportRepository(private val context: Context) {

    private val api = NetworkModule.reportApi

    /**
     * Pobiera raport PDF z backendu i zapisuje go w katalogu cache aplikacji.
     *
     * @param token token JWT (bez prefiksu "Bearer")
     * @param userId opcjonalny identyfikator użytkownika (null = zalogowany)
     * @param dateFrom data początkowa yyyy-MM-dd (null = brak ograniczenia)
     * @param dateTo   data końcowa yyyy-MM-dd (null = brak ograniczenia)
     * @param type     typ raportu
     * @return [Result] z [File] wskazującym na pobrany plik PDF lub błąd
     */
    suspend fun downloadReport(
        token: String,
        userId: Int? = null,
        dateFrom: String? = null,
        dateTo: String? = null,
        type: String = "PODSUMOWANIE",
    ): Result<File> = runCatching {
        val response = api.generateReport(
            authorization = "Bearer $token",
            userId = userId,
            dateFrom = dateFrom,
            dateTo = dateTo,
            type = type,
        )

        if (!response.isSuccessful) {
            when (response.code()) {
                204, 404 -> error("Brak danych treningowych w wybranym zakresie dat.")
                else -> error("Błąd serwera: HTTP ${response.code()}")
            }
        }

        val body = response.body() ?: error("Brak danych treningowych w wybranym zakresie dat.")

        val reportsDir = File(context.cacheDir, "reports").also { it.mkdirs() }
        val fileName = "raport_${System.currentTimeMillis()}.pdf"
        val file = File(reportsDir, fileName)

        file.outputStream().use { out ->
            body.byteStream().copyTo(out)
        }

        if (file.length() < 512) {
            file.delete()
            error("Brak danych treningowych w wybranym zakresie dat.")
        }

        file
    }

    /**
     * Zwraca [android.net.Uri] do pliku PDF za pośrednictwem FileProvider,
     * gotowe do przekazania do zewnętrznego przeglądarkę PDF.
     */
    fun getUriForFile(file: File) = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file,
    )
}
