package pl.edu.ur.km131467.trainit.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pl.edu.ur.km131467.trainit.data.local.SessionManager
import pl.edu.ur.km131467.trainit.data.remote.NetworkModule
import pl.edu.ur.km131467.trainit.data.remote.dto.ForgotPasswordRequestDto
import pl.edu.ur.km131467.trainit.data.remote.dto.LoginRequestDto
import pl.edu.ur.km131467.trainit.data.remote.dto.RegisterRequestDto
import pl.edu.ur.km131467.trainit.data.repository.AuthRepository
import pl.edu.ur.km131467.trainit.data.repository.AuthResult

/**
 * Stan ekranu logowania / rejestracji.
 */
sealed class LoginUiState {

    /** Stan bezczynności — brak trwającej operacji sieciowej. */
    data object Idle : LoginUiState()

    /** Stan ładowania — trwa wywołanie API uwierzytelniania. */
    data object Loading : LoginUiState()

    /** Stan sukcesu — sesja użytkownika została zapisana lokalnie. */
    data object Success : LoginUiState()

    /**
     * Stan błędu z komunikatem dla użytkownika.
     *
     * @param message treść błędu do wyświetlenia na ekranie.
     */
    data class Error(val message: String) : LoginUiState()
}

/**
 * ViewModel ekranu logowania, rejestracji i resetu hasła.
 *
 * Koordynuje wywołania [AuthRepository] oraz zapis sesji w [SessionManager].
 */
class LoginViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val authRepository = AuthRepository(NetworkModule.authApi)

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    /** Obserwowalny stan UI ekranu uwierzytelniania. */
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 1)
    /** Jednorazowe komunikaty (np. po resecie hasła) emitowane do UI. */
    val messages: SharedFlow<String> = _messages

    /** Przywraca stan [LoginUiState.Idle] po obsłużeniu sukcesu lub błędu. */
    fun resetToIdle() {
        _uiState.value = LoginUiState.Idle
    }

    /**
     * Loguje użytkownika i zapisuje token oraz profil w [SessionManager].
     *
     * @param email adres e-mail
     * @param password hasło
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            when (val result = authRepository.login(LoginRequestDto(email.trim(), password))) {
                is AuthResult.Success -> {
                    val data = result.data
                    sessionManager.saveSession(
                        userId = data.id,
                        token = data.token,
                        email = data.email,
                        firstName = data.firstName,
                        lastName = data.lastName,
                        role = data.role,
                    )
                    _uiState.value = LoginUiState.Success
                }
                is AuthResult.Error -> _uiState.value = LoginUiState.Error(result.message)
                AuthResult.NetworkError -> _uiState.value =
                    LoginUiState.Error("Brak połączenia z serwerem")
            }
        }
    }

    /**
     * Rejestruje konto, a następnie automatycznie loguje użytkownika.
     *
     * @param fullName imię i nazwisko (rozdzielane spacją)
     * @param email adres e-mail
     * @param password hasło
     */
    fun register(fullName: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            val trimmedName = fullName.trim()
            val nameParts = trimmedName.split(" ", limit = 2)
            val firstName = nameParts.getOrElse(0) { "" }
            val lastName = nameParts.getOrNull(1)?.trim().orEmpty()
            val registerResult = authRepository.register(
                RegisterRequestDto(
                    email = email.trim(),
                    password = password,
                    firstName = firstName,
                    lastName = lastName,
                ),
            )
            when (registerResult) {
                is AuthResult.Error -> _uiState.value = LoginUiState.Error(registerResult.message)
                AuthResult.NetworkError -> _uiState.value =
                    LoginUiState.Error("Brak połączenia z serwerem")
                is AuthResult.Success -> {
                    when (val loginResult = authRepository.login(LoginRequestDto(email.trim(), password))) {
                        is AuthResult.Success -> {
                            val data = loginResult.data
                            sessionManager.saveSession(
                                userId = data.id,
                                token = data.token,
                                email = data.email,
                                firstName = data.firstName,
                                lastName = data.lastName,
                                role = data.role,
                            )
                            _uiState.value = LoginUiState.Success
                        }
                        is AuthResult.Error -> _uiState.value = LoginUiState.Error(loginResult.message)
                        AuthResult.NetworkError -> _uiState.value =
                            LoginUiState.Error("Brak połączenia z serwerem")
                    }
                }
            }
        }
    }

    /**
     * Resetuje hasło użytkownika i emituje komunikat przez [messages].
     *
     * @param email adres e-mail konta
     * @param newPassword nowe hasło
     */
    fun forgotPassword(email: String, newPassword: String) {
        viewModelScope.launch {
            when (
                val result = authRepository.forgotPassword(
                    ForgotPasswordRequestDto(email.trim(), newPassword),
                )
            ) {
                is AuthResult.Success -> _messages.emit("Hasło zostało zresetowane. Zaloguj się nowym hasłem.")
                is AuthResult.Error -> _messages.emit(result.message)
                AuthResult.NetworkError -> _messages.emit("Brak połączenia z serwerem")
            }
        }
    }
}
