package pl.edu.ur.km131467.trainit.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pl.edu.ur.km131467.trainit.data.local.SessionManager
import pl.edu.ur.km131467.trainit.data.remote.NetworkModule
import pl.edu.ur.km131467.trainit.data.remote.dto.LoginRequestDto
import pl.edu.ur.km131467.trainit.data.remote.dto.RegisterRequestDto
import pl.edu.ur.km131467.trainit.data.repository.AuthRepository
import pl.edu.ur.km131467.trainit.data.repository.AuthResult

sealed class LoginUiState {
    data object Idle : LoginUiState()
    data object Loading : LoginUiState()
    data object Success : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class LoginViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val authRepository = AuthRepository(NetworkModule.authApi)

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun resetToIdle() {
        _uiState.value = LoginUiState.Idle
    }

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
}
