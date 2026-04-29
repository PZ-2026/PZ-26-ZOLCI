package pl.edu.ur.km131467.trainit.data.repository

import kotlinx.serialization.SerializationException
import pl.edu.ur.km131467.trainit.data.remote.NetworkModule
import pl.edu.ur.km131467.trainit.data.remote.api.AuthApi
import pl.edu.ur.km131467.trainit.data.remote.dto.ErrorResponseDto
import pl.edu.ur.km131467.trainit.data.remote.dto.ForgotPasswordRequestDto
import pl.edu.ur.km131467.trainit.data.remote.dto.LoginRequestDto
import pl.edu.ur.km131467.trainit.data.remote.dto.LoginResponseDto
import pl.edu.ur.km131467.trainit.data.remote.dto.RegisterRequestDto
import pl.edu.ur.km131467.trainit.data.remote.dto.UserDto
import retrofit2.Response
import java.io.IOException

sealed class AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Error(val message: String) : AuthResult<Nothing>()
    data object NetworkError : AuthResult<Nothing>()
}

class AuthRepository(
    private val authApi: AuthApi = NetworkModule.authApi,
) {

    suspend fun register(request: RegisterRequestDto): AuthResult<UserDto> {
        return try {
            mapResponse(authApi.register(request))
        } catch (e: IOException) {
            AuthResult.NetworkError
        } catch (e: SerializationException) {
            AuthResult.Error("Nieoczekiwany błąd: ${e.message}")
        } catch (e: Exception) {
            AuthResult.Error("Nieoczekiwany błąd: ${e.message}")
        }
    }

    suspend fun login(request: LoginRequestDto): AuthResult<LoginResponseDto> {
        return try {
            mapResponse(authApi.login(request))
        } catch (e: IOException) {
            AuthResult.NetworkError
        } catch (e: SerializationException) {
            AuthResult.Error("Nieoczekiwany błąd: ${e.message}")
        } catch (e: Exception) {
            AuthResult.Error("Nieoczekiwany błąd: ${e.message}")
        }
    }

    suspend fun forgotPassword(request: ForgotPasswordRequestDto): AuthResult<Unit> {
        return try {
            val response = authApi.forgotPassword(request)
            if (response.isSuccessful) {
                AuthResult.Success(Unit)
            } else {
                AuthResult.Error(parseErrorBody(response.code(), response.errorBody()?.string()))
            }
        } catch (e: IOException) {
            AuthResult.NetworkError
        } catch (e: SerializationException) {
            AuthResult.Error("Nieoczekiwany błąd: ${e.message}")
        } catch (e: Exception) {
            AuthResult.Error("Nieoczekiwany błąd: ${e.message}")
        }
    }

    private fun <T> mapResponse(response: Response<T>): AuthResult<T> {
        if (response.isSuccessful) {
            val body = response.body()
                ?: return AuthResult.Error("Nieoczekiwany błąd: pusta odpowiedź")
            return AuthResult.Success(body)
        }
        val message = parseErrorBody(response.code(), response.errorBody()?.string())
        return AuthResult.Error(message)
    }

    private fun parseErrorBody(code: Int, raw: String?): String {
        if (raw.isNullOrBlank()) {
            return "Nieznany błąd (HTTP $code)"
        }
        return try {
            val error = NetworkModule.json.decodeFromString<ErrorResponseDto>(raw)
            when {
                error.errors.isNotEmpty() -> {
                    val first = error.errors.first()
                    val firstPart = "${first.field}: ${first.message}"
                    if (error.errors.size == 1) {
                        firstPart
                    } else {
                        val rest = error.errors.drop(1).joinToString(", ") { "${it.field}: ${it.message}" }
                        "$firstPart ($rest)"
                    }
                }
                error.message.isNotBlank() -> error.message
                else -> "Nieznany błąd (HTTP $code)"
            }
        } catch (_: Exception) {
            "Nieznany błąd (HTTP $code)"
        }
    }
}
