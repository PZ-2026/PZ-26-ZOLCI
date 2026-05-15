package pl.edu.ur.km131467.trainit.data.repository

import kotlinx.serialization.SerializationException
import pl.edu.ur.km131467.trainit.data.remote.NetworkModule
import pl.edu.ur.km131467.trainit.data.remote.api.AuthApi
import pl.edu.ur.km131467.trainit.data.remote.dto.ErrorResponseDto
import pl.edu.ur.km131467.trainit.data.remote.dto.ForgotPasswordRequestDto
import pl.edu.ur.km131467.trainit.data.remote.dto.LoginRequestDto
import pl.edu.ur.km131467.trainit.data.remote.dto.LoginResponseDto
import pl.edu.ur.km131467.trainit.data.remote.dto.RegisterRequestDto
import pl.edu.ur.km131467.trainit.data.remote.dto.UpdateProfileRequestDto
import pl.edu.ur.km131467.trainit.data.remote.dto.UserDto
import retrofit2.Response
import java.io.IOException

/**
 * Wynik operacji uwierzytelniania w warstwie repozytorium.
 *
 * @param T typ danych zwracanych przy sukcesie
 */
sealed class AuthResult<out T> {

    /**
     * Operacja zakończona sukcesem.
     *
     * @param data odpowiedź z API
     */
    data class Success<T>(val data: T) : AuthResult<T>()

    /**
     * Błąd biznesowy lub HTTP z komunikatem dla użytkownika.
     *
     * @param message opis błędu
     */
    data class Error(val message: String) : AuthResult<Nothing>()

    /** Błąd sieci (brak połączenia, timeout). */
    data object NetworkError : AuthResult<Nothing>()
}

/**
 * Repozytorium operacji uwierzytelniania i profilu użytkownika.
 *
 * Mapuje odpowiedzi [AuthApi] na typ [AuthResult] z polskimi komunikatami błędów.
 */
class AuthRepository(
    private val authApi: AuthApi = NetworkModule.authApi,
) {

    /**
     * Rejestruje nowe konto użytkownika.
     *
     * @param request dane rejestracji
     * @return [AuthResult] z [UserDto] lub błędem
     */
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

    /**
     * Loguje użytkownika i zwraca token JWT wraz z danymi profilu.
     *
     * @param request dane logowania
     * @return [AuthResult] z [LoginResponseDto] lub błędem
     */
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

    /**
     * Resetuje hasło użytkownika (WF odzyskiwania hasła).
     *
     * @param request e-mail i nowe hasło
     * @return [AuthResult] z [Unit] przy sukcesie lub błędem
     */
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

    /**
     * Pobiera profil zalogowanego użytkownika.
     *
     * @param token token JWT (bez prefiksu „Bearer”)
     * @return [AuthResult] z [UserDto] lub błędem
     */
    suspend fun getMe(token: String): AuthResult<UserDto> {
        return try {
            mapResponse(authApi.getMe("Bearer $token"))
        } catch (e: IOException) {
            AuthResult.NetworkError
        } catch (e: SerializationException) {
            AuthResult.Error("Nieoczekiwany błąd: ${e.message}")
        } catch (e: Exception) {
            AuthResult.Error("Nieoczekiwany błąd: ${e.message}")
        }
    }

    /**
     * Aktualizuje profil zalogowanego użytkownika.
     *
     * @param token token JWT (bez prefiksu „Bearer”)
     * @param request zmienione pola profilu
     * @return [AuthResult] z [UserDto] lub błędem
     */
    suspend fun updateMe(token: String, request: UpdateProfileRequestDto): AuthResult<UserDto> {
        return try {
            mapResponse(authApi.updateMe("Bearer $token", request))
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
