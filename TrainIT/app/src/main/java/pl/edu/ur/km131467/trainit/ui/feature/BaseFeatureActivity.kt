package pl.edu.ur.km131467.trainit.ui.feature

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.launch
import pl.edu.ur.km131467.trainit.R
import pl.edu.ur.km131467.trainit.data.local.SessionManager
import pl.edu.ur.km131467.trainit.ui.common.BottomNavHelper
import pl.edu.ur.km131467.trainit.ui.login.LoginActivity

/**
 * Bazowa aktywność dla ekranów modułowych (Ćwiczenia, Sesje, Raporty itd.).
 *
 * Klasa zapewnia wspólny layout, obsługę stanu [FeatureUiState], renderowanie listy
 * i integrację z dolną nawigacją przez [BottomNavHelper].
 */
abstract class BaseFeatureActivity : AppCompatActivity() {
    /** Konfiguracja modułu odpowiadająca tekstom i źródłu danych ekranu. */
    abstract val module: FeatureModule

    /** Zakładka dolnej nawigacji, która ma być aktywna dla bieżącego ekranu. */
    open val bottomNavItem: Int = R.id.nav_profile

    /** ViewModel dostarczający stany danych do renderowania. */
    private val viewModel: FeatureViewModel by viewModels()

    /** Nagłówek modułu. */
    private lateinit var tvModuleTitle: TextView

    /** Podtytuł modułu. */
    private lateinit var tvModuleSubtitle: TextView

    /** Pasek postępu widoczny podczas ładowania. */
    private lateinit var loadingIndicator: LinearProgressIndicator

    /** Kontener komunikatu błędu. */
    private lateinit var tvError: TextView

    /** Komunikat pustego stanu. */
    private lateinit var tvEmpty: TextView

    /** Przycisk akcji głównej modułu. */
    private lateinit var btnPrimaryAction: MaterialButton

    /** Przycisk odświeżenia danych. */
    private lateinit var btnSecondaryAction: MaterialButton

    /** Kontener listy pozycji modułu. */
    private lateinit var listContainer: LinearLayout

    /** Dolna nawigacja głównych sekcji aplikacji. */
    private lateinit var bottomNavigation: BottomNavigationView

    /**
     * Inicjalizuje wspólny ekran modułowy i uruchamia pierwsze ładowanie danych.
     *
     * Przy braku aktywnej sesji przekierowuje użytkownika do [LoginActivity].
     *
     * @param savedInstanceState zapisany stan instancji aktywności.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!SessionManager(this).isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_feature_list)

        initViews()
        bindStaticTexts()
        BottomNavHelper.setupBottomNav(bottomNavigation, this, bottomNavItem)
        setupActions()
        observeState()
        observeMessages()
        viewModel.load(module)
    }

    /** Wiąże referencje widoków z layoutu bazowego. */
    private fun initViews() {
        tvModuleTitle = findViewById(R.id.tvModuleTitle)
        tvModuleSubtitle = findViewById(R.id.tvModuleSubtitle)
        loadingIndicator = findViewById(R.id.loadingIndicator)
        tvError = findViewById(R.id.tvError)
        tvEmpty = findViewById(R.id.tvEmpty)
        btnPrimaryAction = findViewById(R.id.btnPrimaryAction)
        btnSecondaryAction = findViewById(R.id.btnSecondaryAction)
        listContainer = findViewById(R.id.listContainer)
        bottomNavigation = findViewById(R.id.bottomNavigation)
    }

    /** Ustawia statyczne etykiety nagłówka i przycisków zgodnie z [module]. */
    private fun bindStaticTexts() {
        tvModuleTitle.text = module.title
        tvModuleSubtitle.text = module.subtitle
        btnPrimaryAction.text = module.primaryActionLabel
    }

    /** Konfiguruje obsługę kliknięć akcji głównej i odświeżenia. */
    private fun setupActions() {
        btnPrimaryAction.setOnClickListener {
            viewModel.runPrimaryAction(module)
        }
        btnSecondaryAction.setOnClickListener { viewModel.load(module) }
    }

    /** Subskrybuje zmiany stanu [FeatureViewModel.uiState] dla cyklu życia STARTED. */
    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    render(state)
                }
            }
        }
    }

    /** Subskrybuje komunikaty jednorazowe (toast) emitowane po akcjach użytkownika. */
    private fun observeMessages() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.messages.collect { message ->
                    Toast.makeText(this@BaseFeatureActivity, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Renderuje ekran zgodnie z aktualnym stanem [FeatureUiState].
     *
     * @param state stan dostarczony przez ViewModel.
     */
    private fun render(state: FeatureUiState) {
        when (state) {
            FeatureUiState.Idle -> {
                loadingIndicator.visibility = android.view.View.GONE
                tvError.visibility = android.view.View.GONE
                tvEmpty.visibility = android.view.View.GONE
            }
            FeatureUiState.Loading -> {
                loadingIndicator.visibility = android.view.View.VISIBLE
                tvError.visibility = android.view.View.GONE
                tvEmpty.visibility = android.view.View.GONE
            }
            is FeatureUiState.Error -> {
                loadingIndicator.visibility = android.view.View.GONE
                tvError.visibility = android.view.View.VISIBLE
                tvEmpty.visibility = android.view.View.GONE
                tvError.text = state.message
                listContainer.removeAllViews()
            }
            is FeatureUiState.Success -> {
                loadingIndicator.visibility = android.view.View.GONE
                tvError.visibility = android.view.View.GONE
                tvEmpty.visibility = android.view.View.GONE
                bindItems(state.items)
            }
            FeatureUiState.Empty -> {
                loadingIndicator.visibility = android.view.View.GONE
                tvError.visibility = android.view.View.GONE
                tvEmpty.visibility = android.view.View.VISIBLE
                listContainer.removeAllViews()
            }
        }
    }

    /**
     * Tworzy i dodaje wiersze listy danych modułu.
     *
     * @param items kolekcja pozycji do wyrenderowania.
     */
    private fun bindItems(items: List<FeatureListItem>) {
        listContainer.removeAllViews()
        val inflater = LayoutInflater.from(this)
        items.forEach { item ->
            val row = inflater.inflate(R.layout.item_feature_entry, listContainer, false)
            row.findViewById<TextView>(R.id.tvItemTitle).text = item.title
            row.findViewById<TextView>(R.id.tvItemSubtitle).text = item.subtitle
            listContainer.addView(row)
        }
    }
}
