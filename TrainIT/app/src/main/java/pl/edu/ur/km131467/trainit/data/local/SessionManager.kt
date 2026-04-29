package pl.edu.ur.km131467.trainit.data.local

import android.content.Context
import android.content.SharedPreferences

/**
 * Prosta warstwa dostępu do danych sesji przechowywanych w [SharedPreferences].
 *
 * Odpowiada za zapis i odczyt danych uwierzytelnionego użytkownika.
 */
class SessionManager(context: Context) {

    /** Pamięć współdzielona przechowująca stan sesji użytkownika. */
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Zapisuje kompletny stan sesji po udanym logowaniu/rejestracji.
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

    /**
     * Zwraca rolę użytkownika.
     *
     * Domyślnie zwraca `"USER"`, jeśli rola nie została jeszcze zapisana.
     */
    fun getRole(): String = prefs.getString(KEY_ROLE, "USER").orEmpty()

    companion object {
        private const val PREFS_NAME = "trainit_prefs"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_TOKEN = "token"
        private const val KEY_EMAIL = "email"
        private const val KEY_FIRST_NAME = "first_name"
        private const val KEY_LAST_NAME = "last_name"
        private const val KEY_ROLE = "role"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }
}
