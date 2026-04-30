package pl.edu.ur.km131467.trainit.ui.feature

/**
 * Definiuje moduły ekranów dodatkowych oraz ich konfigurację prezentacyjną.
 *
 * @property title nazwa wyświetlana w nagłówku ekranu.
 * @property subtitle opis funkcji modułu pod nagłówkiem.
 * @property primaryActionLabel etykieta przycisku akcji głównej.
 */
enum class FeatureModule(
    val title: String,
    val subtitle: String,
    val primaryActionLabel: String,
) {
    EXERCISES("Ćwiczenia", "Baza ćwiczeń i filtrowanie grup mięśniowych", "Dodaj ćwiczenie"),
    SESSIONS("Historia treningów", "Lista sesji: plan, status, czas i data", "Rozpocznij sesję"),
    REPORTS("Raporty", "Generowanie i podgląd raportów aktywności", "Generuj raport"),
    STATISTICS("Statystyki", "Postęp treningowy i podsumowania", "Odśwież statystyki"),
    SETTINGS("Ustawienia", "Profil, jednostki i prywatność", "Zapisz zmiany"),
    NOTIFICATIONS("Powiadomienia", "Przypomnienia treningowe i preferencje", "Włącz przypomnienia"),
    ROLE_PANEL("Panel roli", "Skróty funkcji dla roli użytkownika", "Przejdź do panelu"),
}
