package pl.edu.ur.km131467.trainit.ui.feature

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import pl.edu.ur.km131467.trainit.data.local.SessionManager
import pl.edu.ur.km131467.trainit.data.repository.FeatureRepository

/**
 * ViewModel dla ekranów modułowych opartych o [FeatureModule].
 *
 * Odpowiada za pobranie danych przez [FeatureRepository] i mapowanie ich na [FeatureUiState].
 */
class FeatureViewModel(
    application: Application,
) : AndroidViewModel(application) {
    /** Menedżer sesji używany m.in. do odczytu roli użytkownika. */
    private val sessionManager = SessionManager(application)

    /** Repozytorium dostarczające dane z API lub fallbacki lokalne. */
    private val featureRepository = FeatureRepository()

    /** Wewnętrzny, mutowalny stan UI. */
    private val _uiState = MutableStateFlow<FeatureUiState>(FeatureUiState.Idle)

    /** Publiczny strumień stanu UI obserwowany przez widok. */
    val uiState: StateFlow<FeatureUiState> = _uiState.asStateFlow()

    /** Jednorazowe komunikaty UI (np. wynik akcji głównej). */
    private val _messages = MutableSharedFlow<String>()

    /** Publiczny strumień komunikatów UI. */
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    /**
     * Ładuje dane wybranego modułu i publikuje odpowiedni stan UI.
     *
     * @param module moduł widoku, dla którego należy pobrać dane.
     */
    fun load(module: FeatureModule) {
        viewModelScope.launch {
            _uiState.value = FeatureUiState.Loading
            runCatching {
                featureRepository.getItems(module, sessionManager)
            }.onSuccess {
                _uiState.value = if (it.isEmpty()) {
                    FeatureUiState.Empty
                } else {
                    FeatureUiState.Success(it)
                }
            }.onFailure {
                _uiState.value = FeatureUiState.Error(it.message ?: "Nie udało się wczytać danych")
            }
        }
    }

    /**
     * Uruchamia akcję główną modułu, a następnie odświeża dane listy.
     *
     * @param module moduł, którego akcję należy wykonać.
     */
    fun runPrimaryAction(module: FeatureModule) {
        viewModelScope.launch {
            _uiState.value = FeatureUiState.Loading
            runCatching {
                featureRepository.runPrimaryAction(module, sessionManager)
            }.onSuccess { message ->
                _messages.emit(message)
                load(module)
            }.onFailure { throwable ->
                _uiState.value = FeatureUiState.Error(throwable.message ?: "Nie udało się wykonać akcji")
            }
        }
    }
}
