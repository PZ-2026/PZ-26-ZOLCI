package pl.edu.ur.km131467.trainit.ui.login

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import pl.edu.ur.km131467.trainit.MainActivity
import pl.edu.ur.km131467.trainit.R
import pl.edu.ur.km131467.trainit.ui.common.applyAppNameSpan
import pl.edu.ur.km131467.trainit.ui.common.setupClickableSpan

/**
 * Aktywność ekranu logowania i rejestracji aplikacji TrainIT.
 *
 * Formularze logowania i rejestracji są przełączane zakładkami; logika sieciowa
 * znajduje się w [LoginViewModel] (Retrofit + [pl.edu.ur.km131467.trainit.data.local.SessionManager]).
 * Przełączanie widoków formularzy deleguje do [AuthFormController]; spany tekstu — do helperów
 * w pakiecie [pl.edu.ur.km131467.trainit.ui.common].
 *
 * @see LoginViewModel
 * @see AuthFormController
 * @see MainActivity
 */
class LoginActivity : AppCompatActivity() {

    private val viewModel: LoginViewModel by viewModels()
    private lateinit var authFormController: AuthFormController
    private lateinit var loginViews: LoginScreenViews
    private val accentYellow: Int = Color.parseColor("#FFD600")

    /**
     * Inicjalizuje layout, kontroler formularza, obserwację stanu ViewModelu oraz linki dolne.
     *
     * @param savedInstanceState zapisany stan instancji (nieużywany).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginViews = bindLoginScreenViews(this)
        authFormController = AuthFormController(
            activity = this,
            tabLogin = loginViews.tabLogin,
            tabRegister = loginViews.tabRegister,
            loginFormContainer = loginViews.loginFormContainer,
            registerFormContainer = loginViews.registerFormContainer,
            tvRegisterLink = loginViews.tvRegisterLink,
            tvLoginLink = loginViews.tvLoginLink,
        )

        applyAppNameSpan(loginViews.tvAppName, "TrainIT", accentYellow, 5, 7)
        authFormController.setupTabSwitching()
        loginViews.btnLogin.setOnClickListener { handleLogin() }
        loginViews.tvForgotPassword.setOnClickListener { handleForgotPassword() }
        loginViews.btnRegister.setOnClickListener { handleRegister() }
        setupBottomLinks()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> handleUiState(state) }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.messages.collect { message ->
                    Toast.makeText(this@LoginActivity, message, Toast.LENGTH_LONG).show()
                }
            }
        }

        authFormController.showLoginForm()
    }

    private fun setupBottomLinks() {
        setupClickableSpan(
            textView = loginViews.tvRegisterLink,
            fullText = "Nie masz jeszcze konta? Zarejestruj się",
            clickablePart = "Zarejestruj się",
            accentColor = accentYellow,
            onClick = { authFormController.showRegisterForm() },
        )
        setupClickableSpan(
            textView = loginViews.tvLoginLink,
            fullText = "Masz już konto? Zaloguj się",
            clickablePart = "Zaloguj się",
            accentColor = accentYellow,
            onClick = { authFormController.showLoginForm() },
        )
    }

    private fun handleLogin() {
        val email = loginViews.etEmail.text?.toString().orEmpty()
        val password = loginViews.etPassword.text?.toString().orEmpty()

        if (email.isBlank()) {
            loginViews.etEmail.error = "Podaj adres email"
            return
        }
        if (password.isBlank()) {
            loginViews.etPassword.error = "Podaj hasło"
            return
        }

        viewModel.login(email, password)
    }

    private fun handleRegister() {
        val name = loginViews.etName.text?.toString().orEmpty()
        val email = loginViews.etRegEmail.text?.toString().orEmpty()
        val password = loginViews.etRegPassword.text?.toString().orEmpty()

        if (name.isBlank()) {
            loginViews.etName.error = "Podaj imię i nazwisko"
            return
        }
        if (email.isBlank()) {
            loginViews.etRegEmail.error = "Podaj adres email"
            return
        }
        if (password.isBlank()) {
            loginViews.etRegPassword.error = "Podaj hasło"
            return
        }

        viewModel.register(name, email, password)
    }

    private fun handleUiState(state: LoginUiState) {
        when (state) {
            LoginUiState.Idle -> {
                loginViews.btnLogin.isEnabled = true
                loginViews.btnRegister.isEnabled = true
                loginViews.progressLoading.visibility = View.GONE
            }
            LoginUiState.Loading -> {
                loginViews.btnLogin.isEnabled = false
                loginViews.btnRegister.isEnabled = false
                loginViews.progressLoading.visibility = View.VISIBLE
            }
            LoginUiState.Success -> {
                loginViews.btnLogin.isEnabled = true
                loginViews.btnRegister.isEnabled = true
                loginViews.progressLoading.visibility = View.GONE
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            is LoginUiState.Error -> {
                loginViews.btnLogin.isEnabled = true
                loginViews.btnRegister.isEnabled = true
                loginViews.progressLoading.visibility = View.GONE
                Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetToIdle()
            }
        }
    }

    private fun handleForgotPassword() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_reset_password, null)
        val emailInput = dialogView.findViewById<TextInputEditText>(R.id.etResetEmail)
        val passwordInput = dialogView.findViewById<TextInputEditText>(R.id.etResetPassword)
        emailInput.setText(loginViews.etEmail.text?.toString().orEmpty())

        val dialog = MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_TrainIT_MaterialAlertDialog)
            .setTitle("Reset hasła")
            .setView(dialogView)
            .setPositiveButton("Zapisz", null)
            .setNegativeButton("Anuluj", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(android.content.DialogInterface.BUTTON_NEGATIVE)?.apply {
                isAllCaps = false
                setTextColor(resources.getColor(R.color.text_secondary, theme))
            }
            dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE)?.apply {
                isAllCaps = false
                setTextColor(resources.getColor(R.color.accent_yellow, theme))
            }?.setOnClickListener {
                val email = emailInput.text?.toString().orEmpty().trim()
                val newPassword = passwordInput.text?.toString().orEmpty()
                when {
                    email.isBlank() -> Toast.makeText(this, "Podaj adres email", Toast.LENGTH_SHORT).show()
                    newPassword.length < 8 -> Toast.makeText(
                        this,
                        "Nowe hasło musi mieć minimum 8 znaków",
                        Toast.LENGTH_SHORT,
                    ).show()
                    else -> {
                        viewModel.forgotPassword(email, newPassword)
                        dialog.dismiss()
                    }
                }
            }
        }
        dialog.show()
    }
}
