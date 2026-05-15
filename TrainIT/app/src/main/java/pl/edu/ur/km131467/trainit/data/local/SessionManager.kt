package pl.edu.ur.km131467.trainit.data.local

import android.content.Context
import android.content.SharedPreferences
import java.time.LocalDate

/**
 * Zarządzanie sesją użytkownika w [SharedPreferences] (token JWT, profil, preferencje).
 *
 * Odpowiada za zapis i odczyt danych uwierzytelnionego użytkownika oraz lokalnych
 * markerów aktywnej sesji treningowej i powiadomień.
 */
class SessionManager(context: Context) {
    private val appContext: Context = context.applicationContext

    /** Pamięć współdzielona przechowująca stan sesji użytkownika. */
    private val prefs: SharedPreferences =
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Zapisuje kompletny stan sesji po udanym logowaniu/rejestracji.
     *
     * @param userId identyfikator użytkownika
     * @param token token JWT
     * @param email adres e-mail
     * @param firstName imię
     * @param lastName nazwisko
     * @param role rola w systemie (np. USER, TRAINER, ADMIN)
     */
    fun saveSession(
        userId: Int,
        token: String,
        email: String,
        firstName: String,
        lastName: String,
        role: String,
    ) {
        prefs.edit().apply {
            putInt(KEY_USER_ID, userId)
            putString(KEY_TOKEN, token)
            putString(KEY_EMAIL, email)
            putString(KEY_FIRST_NAME, firstName)
            putString(KEY_LAST_NAME, lastName)
            putString(KEY_ROLE, role)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    /** Czyści wszystkie dane sesji użytkownika. */
    fun clearSession() {
        prefs.edit().clear().apply()
    }

    /** Sprawdza, czy użytkownik jest zalogowany na podstawie obecności tokenu. */
    fun isLoggedIn(): Boolean = getToken() != null

    /** Zwraca zapisany token dostępu lub `null` gdy brak sesji. */
    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    /** Zwraca identyfikator użytkownika lub `null` gdy nie został zapisany. */
    fun getUserId(): Int? =
        if (prefs.contains(KEY_USER_ID)) prefs.getInt(KEY_USER_ID, -1) else null

    /** Zwraca imię użytkownika zapisane podczas logowania. */
    fun getFirstName(): String? = prefs.getString(KEY_FIRST_NAME, null)

    /** Zwraca nazwisko użytkownika zapisane podczas logowania. */
    fun getLastName(): String? = prefs.getString(KEY_LAST_NAME, null)

    /** Zwraca adres e-mail użytkownika zapisany podczas logowania. */
    fun getEmail(): String? = prefs.getString(KEY_EMAIL, null)

    /**
     * Zwraca rolę użytkownika.
     *
     * Domyślnie zwraca `"USER"`, jeśli rola nie została jeszcze zapisana.
     */
    fun getRole(): String = prefs.getString(KEY_ROLE, "USER").orEmpty()

    /** Zapisuje timestamp rozpoczęcia aktywnej sesji treningowej (ms). */
    fun setActiveSessionStartedAt(startedAtMillis: Long) {
        prefs.edit().putLong(KEY_ACTIVE_SESSION_STARTED_AT, startedAtMillis).apply()
    }

    /** Zwraca timestamp rozpoczęcia aktywnej sesji treningowej lub `null`, gdy brak. */
    fun getActiveSessionStartedAt(): Long? {
        val stored = prefs.getLong(KEY_ACTIVE_SESSION_STARTED_AT, -1L)
        return if (stored > 0L) stored else null
    }

    /** Czyści lokalny marker aktywnej sesji treningowej. */
    fun clearActiveSession() {
        prefs.edit().remove(KEY_ACTIVE_SESSION_STARTED_AT).apply()
    }

    /**
     * Aktualizuje dane profilu w lokalnej sesji (po edycji profilu w aplikacji).
     *
     * @param firstName imię
     * @param lastName nazwisko
     * @param email adres e-mail
     */
    fun updateProfile(firstName: String, lastName: String, email: String) {
        prefs.edit().apply {
            putString(KEY_FIRST_NAME, firstName)
            putString(KEY_LAST_NAME, lastName)
            putString(KEY_EMAIL, email)
            apply()
        }
    }

    /**
     * Sprawdza, czy dla danej zaplanowanej sesji nie wysłano jeszcze powiadomienia.
     *
     * @param sessionId identyfikator sesji treningowej
     * @return `true` gdy powiadomienie powinno zostać wyświetlone
     */
    fun shouldNotifyPlannedSession(sessionId: Int): Boolean {
        return prefs.getInt(KEY_LAST_NOTIFIED_PLANNED_SESSION_ID, -1) != sessionId
    }

    /**
     * Zapisuje identyfikator sesji, dla której wysłano już powiadomienie o planie.
     *
     * @param sessionId identyfikator sesji treningowej
     */
    fun markPlannedSessionNotified(sessionId: Int) {
        prefs.edit().putInt(KEY_LAST_NOTIFIED_PLANNED_SESSION_ID, sessionId).apply()
    }

    /**
     * Sprawdza, czy w bieżącym tygodniu nie pokazano jeszcze powiadomienia o osiągnięciu celu.
     *
     * @return `true` gdy powiadomienie o celu powinno zostać wyświetlone
     */
    fun shouldShowGoalReachedThisWeek(): Boolean {
        val marker = currentWeekMarker()
        return prefs.getString(KEY_LAST_GOAL_REACHED_WEEK, null) != marker
    }

    /** Oznacza bieżący tydzień jako taki, w którym pokazano powiadomienie o osiągnięciu celu. */
    fun markGoalReachedThisWeek() {
        prefs.edit().putString(KEY_LAST_GOAL_REACHED_WEEK, currentWeekMarker()).apply()
    }

    /** Zwraca znacznik tygodnia w formacie `rok-Wnumer` (ISO week-based year). */
    private fun currentWeekMarker(): String {
        val now = LocalDate.now()
        val week = now.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR)
        return "${now.year}-W$week"
    }

    /** Zwraca kontekst aplikacji (m.in. do lokalnych notyfikacji). */
    fun getAppContext(): Context = appContext

    companion object {
        private const val PREFS_NAME = "trainit_prefs"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_TOKEN = "token"
        private const val KEY_EMAIL = "email"
        private const val KEY_FIRST_NAME = "first_name"
        private const val KEY_LAST_NAME = "last_name"
        private const val KEY_ROLE = "role"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_ACTIVE_SESSION_STARTED_AT = "active_session_started_at"
        private const val KEY_LAST_NOTIFIED_PLANNED_SESSION_ID = "last_notified_planned_session_id"
        private const val KEY_LAST_GOAL_REACHED_WEEK = "last_goal_reached_week"
    }
}
