package pl.edu.ur.km131467.trainit.ui.login

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import pl.edu.ur.km131467.trainit.MainActivity
import pl.edu.ur.km131467.trainit.R

/**
 * Aktywność ekranu logowania i rejestracji aplikacji TrainIT.
 *
 * Ekran składa się z dwóch formularzy przełączanych zakładkami:
 * - **Logowanie** — pola email i hasło, przycisk "Zaloguj się",
 *   odnośnik do przypomnienia hasła oraz link do przełączenia na rejestrację.
 * - **Rejestracja** — pola imię, email i hasło, przycisk "Utwórz konto"
 *   oraz link do przełączenia na logowanie.
 *
 * Logowanie odbywa się testowo na sztywno — jedyne poprawne dane to:
 * - email: `jan@example.com`
 * - hasło: `demo`
 *
 * Po poprawnym zalogowaniu ustawiany jest znacznik `is_logged_in` w
 * [SharedPreferences][android.content.SharedPreferences] i użytkownik
 * jest przekierowywany do [MainActivity].
 *
 * Rejestracja jest obecnie niezaimplementowana (stub).
 *
 * @see MainActivity
 */
class LoginActivity : AppCompatActivity() {

    /** Pole tekstowe z nazwą aplikacji "TrainIT". */
    private lateinit var tvAppName: TextView

    /** Zakładka "Logowanie" w selektorze formularzy. */
    private lateinit var tabLogin: TextView

    /** Zakładka "Rejestracja" w selektorze formularzy. */
    private lateinit var tabRegister: TextView

    /** Kontener formularza logowania. */
    private lateinit var loginFormContainer: LinearLayout

    /** Pole tekstowe adresu email w formularzu logowania. */
    private lateinit var etEmail: TextInputEditText

    /** Pole tekstowe hasła w formularzu logowania. */
    private lateinit var etPassword: TextInputEditText

    /** Odnośnik "Zapomniałeś hasła?" w formularzu logowania. */
    private lateinit var tvForgotPassword: TextView

    /** Przycisk "Zaloguj się". */
    private lateinit var btnLogin: MaterialButton

    /** Link dolny "Nie masz jeszcze konta? Zarejestruj się". */
    private lateinit var tvRegisterLink: TextView

    /** Kontener formularza rejestracji. */
    private lateinit var registerFormContainer: LinearLayout

    /** Pole tekstowe imienia i nazwiska w formularzu rejestracji. */
    private lateinit var etName: TextInputEditText

    /** Pole tekstowe adresu email w formularzu rejestracji. */
    private lateinit var etRegEmail: TextInputEditText

    /** Pole tekstowe hasła w formularzu rejestracji. */
    private lateinit var etRegPassword: TextInputEditText

    /** Przycisk "Utwórz konto". */
    private lateinit var btnRegister: MaterialButton

    /** Link dolny "Masz już konto? Zaloguj się". */
    private lateinit var tvLoginLink: TextView

    /** Flaga określająca, czy aktualnie wyświetlany jest formularz logowania (`true`) czy rejestracji (`false`). */
    private var isLoginMode = true

    /**
     * Metoda cyklu życia wywoływana przy tworzeniu aktywności.
     *
     * Inicjalizuje layout, wiąże widoki, konfiguruje stylowanie nazwy
     * aplikacji, przełączanie zakładek, listenery przycisków oraz dolne
     * linki nawigacyjne. Domyślnie wyświetla formularz logowania.
     *
     * @param savedInstanceState zapisany stan instancji (nieużywany).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initViews()
        setupAppNameSpan()
        setupTabSwitching()
        setupClickListeners()
        setupBottomLinks()

        showLoginForm()
    }

    /**
     * Inicjalizuje referencje do wszystkich widoków layoutu [R.layout.activity_login].
     *
     * Wiąże elementy wspólne (nazwa aplikacji, zakładki), elementy formularza
     * logowania oraz elementy formularza rejestracji.
     */
    private fun initViews() {
        tvAppName = findViewById(R.id.tvAppName)
        tabLogin = findViewById(R.id.tabLogin)
        tabRegister = findViewById(R.id.tabRegister)

        loginFormContainer = findViewById(R.id.loginFormContainer)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegisterLink = findViewById(R.id.tvRegisterLink)

        registerFormContainer = findViewById(R.id.registerFormContainer)
        etName = findViewById(R.id.etName)
        etRegEmail = findViewById(R.id.etRegEmail)
        etRegPassword = findViewById(R.id.etRegPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvLoginLink = findViewById(R.id.tvLoginLink)
    }

    /**
     * Konfiguruje nazwę aplikacji "TrainIT" z wyróżnionym fragmentem "IT".
     *
     * Nakłada [ForegroundColorSpan] w kolorze żółtym (#FFD600) na dwa
     * ostatnie znaki tekstu ("IT"), tworząc charakterystyczny branding aplikacji.
     */
    private fun setupAppNameSpan() {
        val text = "TrainIT"
        val spannable = SpannableString(text)

        val yellowColor = Color.parseColor("#FFD600")
        spannable.setSpan(
            ForegroundColorSpan(yellowColor),
            5, 7,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        tvAppName.text = spannable
    }

    /**
     * Konfiguruje listenery kliknięć na zakładkach "Logowanie" i "Rejestracja".
     *
     * Kliknięcie zakładki powoduje przełączenie formularza tylko wtedy,
     * gdy wybrany formularz nie jest jeszcze aktywny, co zapobiega
     * zbędnemu odświeżaniu widoku.
     */
    private fun setupTabSwitching() {
        tabLogin.setOnClickListener {
            if (!isLoginMode) showLoginForm()
        }

        tabRegister.setOnClickListener {
            if (isLoginMode) showRegisterForm()
        }
    }

    /**
     * Przełącza widok na formularz logowania.
     *
     * Aktualizuje stan wewnętrzny ([isLoginMode] = `true`), zmienia wygląd
     * zakładek (aktywna: żółte tło, czarny tekst, pogrubienie; nieaktywna:
     * przezroczyste tło, szary tekst), przełącza widoczność kontenerów
     * formularzy oraz dolnych linków nawigacyjnych.
     */
    private fun showLoginForm() {
        isLoginMode = true

        tabLogin.setBackgroundResource(R.drawable.bg_tab_active)
        tabLogin.setTextColor(Color.BLACK)
        tabLogin.setTypeface(null, Typeface.BOLD)

        tabRegister.setBackgroundColor(Color.TRANSPARENT)
        tabRegister.setTextColor(getColor(R.color.text_secondary))
        tabRegister.setTypeface(null, Typeface.NORMAL)

        loginFormContainer.visibility = View.VISIBLE
        registerFormContainer.visibility = View.GONE

        tvRegisterLink.visibility = View.VISIBLE
        tvLoginLink.visibility = View.GONE
    }

    /**
     * Przełącza widok na formularz rejestracji.
     *
     * Aktualizuje stan wewnętrzny ([isLoginMode] = `false`), zmienia wygląd
     * zakładek (analogicznie do [showLoginForm], ale z zamienionymi rolami)
     * oraz przełącza widoczność kontenerów formularzy i dolnych linków.
     */
    private fun showRegisterForm() {
        isLoginMode = false

        tabRegister.setBackgroundResource(R.drawable.bg_tab_active)
        tabRegister.setTextColor(Color.BLACK)
        tabRegister.setTypeface(null, Typeface.BOLD)

        tabLogin.setBackgroundColor(Color.TRANSPARENT)
        tabLogin.setTextColor(getColor(R.color.text_secondary))
        tabLogin.setTypeface(null, Typeface.NORMAL)

        loginFormContainer.visibility = View.GONE
        registerFormContainer.visibility = View.VISIBLE

        tvRegisterLink.visibility = View.GONE
        tvLoginLink.visibility = View.VISIBLE
    }

    /**
     * Konfiguruje listenery kliknięć przycisków formularzy.
     *
     * Przypisuje:
     * - przycisk logowania → [handleLogin],
     * - link "Zapomniałeś hasła?" → [handleForgotPassword],
     * - przycisk rejestracji → [handleRegister].
     */
    private fun setupClickListeners() {
        btnLogin.setOnClickListener { handleLogin() }
        tvForgotPassword.setOnClickListener { handleForgotPassword() }
        btnRegister.setOnClickListener { handleRegister() }
    }

    /**
     * Konfiguruje dolne linki z klikalnym, żółtym fragmentem tekstu.
     *
     * Tworzy dwa linki nawigacyjne:
     * - "Nie masz jeszcze konta? **Zarejestruj się**" → przełącza na [showRegisterForm],
     * - "Masz już konto? **Zaloguj się**" → przełącza na [showLoginForm].
     *
     * Każdy link korzysta z [setupClickableSpan] do stylowania fragmentu tekstu.
     */
    private fun setupBottomLinks() {
        setupClickableSpan(
            textView = tvRegisterLink,
            fullText = "Nie masz jeszcze konta? Zarejestruj się",
            clickablePart = "Zarejestruj się",
            onClick = { showRegisterForm() }
        )

        setupClickableSpan(
            textView = tvLoginLink,
            fullText = "Masz już konto? Zaloguj się",
            clickablePart = "Zaloguj się",
            onClick = { showLoginForm() }
        )
    }

    /**
     * Nakłada na fragment tekstu styl klikalnego, żółtego, pogrubionego spanu.
     *
     * Tworzy [SpannableString] i nakłada trzy spany na wskazany fragment:
     * - [ForegroundColorSpan] — kolor żółty (#FFD600),
     * - [StyleSpan] — pogrubienie,
     * - [ClickableSpan] — obsługa kliknięcia (bez podkreślenia).
     *
     * Dodatkowo ustawia [LinkMovementMethod] na widoku tekstowym, aby
     * kliknięcia spanów były prawidłowo przechwytywane, oraz wyłącza
     * kolor podświetlenia.
     *
     * @param textView widok tekstowy, w którym zostanie ustawiony span.
     * @param fullText pełny tekst do wyświetlenia.
     * @param clickablePart fragment tekstu, który ma być klikalny.
     * @param onClick akcja wywoływana po kliknięciu fragmentu.
     */
    private fun setupClickableSpan(
        textView: TextView,
        fullText: String,
        clickablePart: String,
        onClick: () -> Unit
    ) {
        val spannable = SpannableString(fullText)
        val startIndex = fullText.indexOf(clickablePart)
        val endIndex = startIndex + clickablePart.length
        val yellowColor = Color.parseColor("#FFD600")

        spannable.setSpan(
            ForegroundColorSpan(yellowColor),
            startIndex, endIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            startIndex, endIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannable.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    onClick()
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = false
                    ds.color = yellowColor
                }
            },
            startIndex, endIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        textView.text = spannable
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.highlightColor = Color.TRANSPARENT
    }

    /**
     * Obsługuje logowanie użytkownika.
     *
     * Waliduje pola email i hasło (nie mogą być puste), a następnie porównuje
     * wprowadzone dane z testowymi danymi logowania (`jan@example.com` / `demo`).
     *
     * W przypadku poprawnych danych:
     * - zapisuje flagę `is_logged_in = true` w [SharedPreferences][android.content.SharedPreferences],
     * - uruchamia [MainActivity],
     * - kończy bieżącą aktywność.
     *
     * W przypadku niepoprawnych danych wyświetla komunikat [Toast]
     * o nieprawidłowym loginie lub haśle.
     */
    private fun handleLogin() {
        val email = etEmail.text?.toString().orEmpty()
        val password = etPassword.text?.toString().orEmpty()

        if (email.isBlank()) {
            etEmail.error = "Podaj adres email"
            return
        }
        if (password.isBlank()) {
            etPassword.error = "Podaj hasło"
            return
        }

        if (email == "jan@example.com" && password == "demo") {
            getSharedPreferences("trainit_prefs", MODE_PRIVATE)
                .edit()
                .putBoolean("is_logged_in", true)
                .apply()

            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            Toast.makeText(this, "Nieprawidłowy login lub hasło", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Obsługuje rejestrację nowego użytkownika.
     *
     * Waliduje pola: imię i nazwisko, email oraz hasło (żadne nie może
     * być puste). Logika rejestracji nie jest jeszcze zaimplementowana —
     * metoda wyświetla jedynie tymczasowy komunikat [Toast].
     */
    private fun handleRegister() {
        val name = etName.text?.toString().orEmpty()
        val email = etRegEmail.text?.toString().orEmpty()
        val password = etRegPassword.text?.toString().orEmpty()

        if (name.isBlank()) {
            etName.error = "Podaj imię i nazwisko"
            return
        }
        if (email.isBlank()) {
            etRegEmail.error = "Podaj adres email"
            return
        }
        if (password.isBlank()) {
            etRegPassword.error = "Podaj hasło"
            return
        }

        // TODO: Zaimplementować właściwą logikę rejestracji
        Toast.makeText(this, "Rejestracja... (stub)", Toast.LENGTH_SHORT).show()
    }

    /**
     * Obsługuje kliknięcie "Zapomniałeś hasła?".
     *
     * Funkcjonalność nie jest jeszcze zaimplementowana — metoda wyświetla
     * jedynie tymczasowy komunikat [Toast].
     */
    private fun handleForgotPassword() {
        // TODO: Nawigacja do ekranu przypomnienia hasła
        Toast.makeText(this, "Zapomniałeś hasła? (stub)", Toast.LENGTH_SHORT).show()
    }
}
