package pl.edu.ur.km131467.trainit.ui.feature

/**
 * Pojedynczy element listy prezentowanej na ekranach modułowych.
 *
 * @property title główny tytuł pozycji.
 * @property subtitle opis pomocniczy widoczny pod tytułem.
 */
data class FeatureListItem(
    val id: Int? = null,
    val title: String,
    val subtitle: String,
)

/**
 * Reprezentuje stan ładowania danych dla ekranów opartych o [BaseFeatureActivity].
 */
sealed class FeatureUiState {
    /** Stan początkowy przed pierwszym załadowaniem danych. */
    data object Idle : FeatureUiState()

    /** Trwa pobieranie danych (API lub fallback lokalny). */
    data object Loading : FeatureUiState()

    /** Zwrócono poprawne dane do wyświetlenia. */
    data class Success(val items: List<FeatureListItem>) : FeatureUiState()

    /** Zapytanie zakończyło się powodzeniem, ale brak pozycji do wyświetlenia. */
    data object Empty : FeatureUiState()

    /** Wystąpił błąd i należy pokazać użytkownikowi komunikat. */
    data class Error(val message: String) : FeatureUiState()
}
