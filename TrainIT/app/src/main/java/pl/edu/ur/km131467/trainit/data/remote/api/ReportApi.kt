package pl.edu.ur.km131467.trainit.data.remote.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import retrofit2.http.Streaming

/**
 * API REST do generowania raportów PDF aktywności treningowej.
 */
interface ReportApi {

    /**
     * Generuje raport PDF aktywności treningowej.
     *
     * @param authorization token JWT w formacie "Bearer <token>"
     * @param userId opcjonalny identyfikator użytkownika (null = zalogowany)
     * @param dateFrom data początkowa yyyy-MM-dd (null = bez ograniczenia)
     * @param dateTo   data końcowa yyyy-MM-dd (null = bez ograniczenia)
     * @param type     typ raportu (domyślnie "PODSUMOWANIE")
     */
    @Streaming
    @GET("api/reports/generate")
    suspend fun generateReport(
        @Header("Authorization") authorization: String,
        @Query("userId") userId: Int? = null,
        @Query("dateFrom") dateFrom: String? = null,
        @Query("dateTo") dateTo: String? = null,
        @Query("type") type: String = "PODSUMOWANIE",
    ): Response<ResponseBody>
}
