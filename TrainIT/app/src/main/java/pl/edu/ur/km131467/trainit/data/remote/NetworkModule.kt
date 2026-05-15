package pl.edu.ur.km131467.trainit.data.remote

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import pl.edu.ur.km131467.trainit.BuildConfig
import pl.edu.ur.km131467.trainit.data.remote.api.AdminApi
import pl.edu.ur.km131467.trainit.data.remote.api.AuthApi
import pl.edu.ur.km131467.trainit.data.remote.api.FeatureApi
import pl.edu.ur.km131467.trainit.data.remote.api.ReportApi
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/**
 * Fabryka klientów Retrofit i instancji API modułów TrainIT.
 */
object NetworkModule {

    val json: Json by lazy {
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }

    private val okHttpClient: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
        if (BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(logging)
        }
        builder.build()
    }

    private val retrofit: Retrofit by lazy {
        val contentType = "application/json".toMediaType()
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    val authApi: AuthApi by lazy { retrofit.create(AuthApi::class.java) }
    val featureApi: FeatureApi by lazy { retrofit.create(FeatureApi::class.java) }
    val adminApi: AdminApi by lazy { retrofit.create(AdminApi::class.java) }
    val reportApi: ReportApi by lazy { retrofit.create(ReportApi::class.java) }
}
