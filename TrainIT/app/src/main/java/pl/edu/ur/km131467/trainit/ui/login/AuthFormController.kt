package pl.edu.ur.km131467.trainit.ui.login

import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import pl.edu.ur.km131467.trainit.R

/**
 * Steruje przełączaniem formularzy logowania i rejestracji na ekranie [LoginActivity].
 *
 * Przechowuje stan [isLoginMode] oraz aktualizuje wygląd zakładek i widoczność kontenerów.
 *
 * @property activity aktywność-host (dostęp do [android.content.res.Resources] i kolorów).
 * @property tabLogin zakładka „Logowanie”.
 * @property tabRegister zakładka „Rejestracja”.
 * @property loginFormContainer kontener pól logowania.
 * @property registerFormContainer kontener pól rejestracji.
 * @property tvRegisterLink dolny link widoczny w trybie logowania.
 * @property tvLoginLink dolny link widoczny w trybie rejestracji.
 */
class AuthFormController(
    private val activity: AppCompatActivity,
    private val tabLogin: TextView,
    private val tabRegister: TextView,
    private val loginFormContainer: LinearLayout,
    private val registerFormContainer: LinearLayout,
    private val tvRegisterLink: TextView,
    private val tvLoginLink: TextView,
) {

    /** `true` gdy widoczny jest formularz logowania, `false` dla rejestracji. */
    var isLoginMode: Boolean = true
        private set

    /**
     * Podpina listenery kliknięć zakładek — przełącza formularz tylko gdy to konieczne.
     */
    fun setupTabSwitching() {
        tabLogin.setOnClickListener {
            if (!isLoginMode) showLoginForm()
        }
        tabRegister.setOnClickListener {
            if (isLoginMode) showRegisterForm()
        }
    }

    /**
     * Pokazuje formularz logowania i styluje zakładki (aktywna: logowanie).
     */
    fun showLoginForm() {
        isLoginMode = true

        tabLogin.setBackgroundResource(R.drawable.bg_tab_active)
        tabLogin.setTextColor(Color.BLACK)
        tabLogin.setTypeface(null, Typeface.BOLD)

        tabRegister.setBackgroundColor(Color.TRANSPARENT)
        tabRegister.setTextColor(activity.getColor(R.color.text_secondary))
        tabRegister.setTypeface(null, Typeface.NORMAL)

        loginFormContainer.visibility = View.VISIBLE
        registerFormContainer.visibility = View.GONE

        tvRegisterLink.visibility = View.VISIBLE
        tvLoginLink.visibility = View.GONE
    }

    /**
     * Pokazuje formularz rejestracji i styluje zakładki (aktywna: rejestracja).
     */
    fun showRegisterForm() {
        isLoginMode = false

        tabRegister.setBackgroundResource(R.drawable.bg_tab_active)
        tabRegister.setTextColor(Color.BLACK)
        tabRegister.setTypeface(null, Typeface.BOLD)

        tabLogin.setBackgroundColor(Color.TRANSPARENT)
        tabLogin.setTextColor(activity.getColor(R.color.text_secondary))
        tabLogin.setTypeface(null, Typeface.NORMAL)

        loginFormContainer.visibility = View.GONE
        registerFormContainer.visibility = View.VISIBLE

        tvRegisterLink.visibility = View.GONE
        tvLoginLink.visibility = View.VISIBLE
    }
}
