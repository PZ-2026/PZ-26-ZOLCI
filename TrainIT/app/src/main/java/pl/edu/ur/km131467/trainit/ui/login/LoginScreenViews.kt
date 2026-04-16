package pl.edu.ur.km131467.trainit.ui.login

import android.app.Activity
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import pl.edu.ur.km131467.trainit.R

/**
 * Zestaw referencji do widoków ekranu logowania i rejestracji.
 *
 * @param tvAppName nagłówek z nazwą aplikacji (span ustawiany w aktywności).
 * @param tabLogin zakładka „Logowanie”.
 * @param tabRegister zakładka „Rejestracja”.
 * @param loginFormContainer kontener formularza logowania.
 * @param etEmail pole email (logowanie).
 * @param etPassword pole hasła (logowanie).
 * @param tvForgotPassword link „zapomniałem hasła” (stub).
 * @param btnLogin przycisk logowania.
 * @param tvRegisterLink tekst z linkiem do rejestracji.
 * @param registerFormContainer kontener formularza rejestracji.
 * @param etName pole imię i nazwisko.
 * @param etRegEmail pole email (rejestracja).
 * @param etRegPassword pole hasła (rejestracja).
 * @param btnRegister przycisk rejestracji.
 * @param progressLoading wskaźnik ładowania.
 * @param tvLoginLink tekst z linkiem do logowania.
 */
internal data class LoginScreenViews(
    val tvAppName: TextView,
    val tabLogin: TextView,
    val tabRegister: TextView,
    val loginFormContainer: LinearLayout,
    val etEmail: TextInputEditText,
    val etPassword: TextInputEditText,
    val tvForgotPassword: TextView,
    val btnLogin: MaterialButton,
    val tvRegisterLink: TextView,
    val registerFormContainer: LinearLayout,
    val etName: TextInputEditText,
    val etRegEmail: TextInputEditText,
    val etRegPassword: TextInputEditText,
    val btnRegister: MaterialButton,
    val progressLoading: ProgressBar,
    val tvLoginLink: TextView,
)

/**
 * Wiąże widoki [R.layout.activity_login] po ustawieniu content view.
 *
 * @param activity aktywność z już wywołanym [Activity.setContentView].
 * @return zestaw referencji do pól formularzy i kontrolek.
 */
internal fun bindLoginScreenViews(activity: Activity): LoginScreenViews {
    return LoginScreenViews(
        tvAppName = activity.findViewById(R.id.tvAppName),
        tabLogin = activity.findViewById(R.id.tabLogin),
        tabRegister = activity.findViewById(R.id.tabRegister),
        loginFormContainer = activity.findViewById(R.id.loginFormContainer),
        etEmail = activity.findViewById(R.id.etEmail),
        etPassword = activity.findViewById(R.id.etPassword),
        tvForgotPassword = activity.findViewById(R.id.tvForgotPassword),
        btnLogin = activity.findViewById(R.id.btnLogin),
        tvRegisterLink = activity.findViewById(R.id.tvRegisterLink),
        registerFormContainer = activity.findViewById(R.id.registerFormContainer),
        etName = activity.findViewById(R.id.etName),
        etRegEmail = activity.findViewById(R.id.etRegEmail),
        etRegPassword = activity.findViewById(R.id.etRegPassword),
        btnRegister = activity.findViewById(R.id.btnRegister),
        progressLoading = activity.findViewById(R.id.progressLoading),
        tvLoginLink = activity.findViewById(R.id.tvLoginLink),
    )
}
