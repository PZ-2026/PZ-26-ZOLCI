package pl.edu.ur.km131467.trainit.ui.feature

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
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

    /** Włącza lokalne filtrowanie listy po polu wyszukiwania (WF-14/15 — np. ćwiczenia). */
    open val enableListSearch: Boolean = false
    /** Włącza filtr grup mięśniowych (WF-15). */
    open val enableMuscleGroupFilter: Boolean = false

    /** Opcjonalna akcja kliknięcia pozycji listy modułu (np. przejście do szczegółów). */
    open fun onFeatureItemClicked(item: FeatureListItem) = Unit

    /** ViewModel dostarczający stany danych do renderowania. */
    protected val viewModel: FeatureViewModel by viewModels()

    /** Opcjonalny identyfikator użytkownika do filtrowania (admin/trainer). */
    protected open var targetUserId: Int? = null

    /** Nagłówek modułu. */
    private lateinit var tvModuleTitle: TextView

    /** Podtytuł modułu. */
    private lateinit var tvModuleSubtitle: TextView

    /** Timer aktywnej sesji (widoczny dla modułu sesji). */
    private lateinit var tvSessionTimer: TextView

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

    /** Wiersz z polem wyszukiwania (widoczny gdy [enableListSearch]). */
    private var searchRow: View? = null
    private var etListSearch: EditText? = null
    private var groupFilterRow: View? = null
    private var acGroupFilter: com.google.android.material.textfield.MaterialAutoCompleteTextView? = null
    private var selectedGroupFilter: String = GROUP_FILTER_ALL

    /** Ostatnio pobrana pełna lista (do filtrowania). */
    private var allListItems: List<FeatureListItem> = emptyList()

    /** Dolna nawigacja głównych sekcji aplikacji. */
    private lateinit var bottomNavigation: BottomNavigationView

    private val timerHandler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null
    private var sessionTimerStartedAtMillis: Long? = null

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
        viewModel.setTargetUserId(targetUserId)
        viewModel.load(module)
    }

    /** Wiąże referencje widoków z layoutu bazowego. */
    private fun initViews() {
        tvModuleTitle = findViewById(R.id.tvModuleTitle)
        tvModuleSubtitle = findViewById(R.id.tvModuleSubtitle)
        tvSessionTimer = findViewById(R.id.tvSessionTimer)
        loadingIndicator = findViewById(R.id.loadingIndicator)
        tvError = findViewById(R.id.tvError)
        tvEmpty = findViewById(R.id.tvEmpty)
        btnPrimaryAction = findViewById(R.id.btnPrimaryAction)
        btnSecondaryAction = findViewById(R.id.btnSecondaryAction)
        listContainer = findViewById(R.id.listContainer)
        bottomNavigation = findViewById(R.id.bottomNavigation)
        searchRow = findViewById(R.id.searchRow)
        etListSearch = findViewById(R.id.etListSearch)
        groupFilterRow = findViewById(R.id.groupFilterRow)
        acGroupFilter = findViewById(R.id.acGroupFilter)
        if (enableListSearch) {
            searchRow?.visibility = View.VISIBLE
            etListSearch?.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                    override fun afterTextChanged(s: Editable?) {
                        applyListSearchFilter()
                    }
                },
            )
        }
        if (enableMuscleGroupFilter) {
            groupFilterRow?.visibility = View.VISIBLE
            acGroupFilter?.setOnItemClickListener { _, _, _, _ ->
                selectedGroupFilter = acGroupFilter?.text?.toString()?.trim().orEmpty().ifBlank { GROUP_FILTER_ALL }
                applyListSearchFilter()
            }
        }
    }

    /** Ustawia statyczne etykiety nagłówka i przycisków zgodnie z [module]. */
    private fun bindStaticTexts() {
        tvModuleTitle.text = module.title
        tvModuleSubtitle.text = module.subtitle
        btnPrimaryAction.text = module.primaryActionLabel
        if (module == FeatureModule.SESSIONS) {
            sessionTimerStartedAtMillis = SessionManager(this).getActiveSessionStartedAt()
            if (sessionTimerStartedAtMillis != null) {
                tvSessionTimer.visibility = android.view.View.VISIBLE
                startSessionTimer()
            } else {
                tvSessionTimer.visibility = android.view.View.GONE
            }
        } else {
            tvSessionTimer.visibility = android.view.View.GONE
        }
    }

    override fun onStart() {
        super.onStart()
        if (module == FeatureModule.SESSIONS) {
            sessionTimerStartedAtMillis = SessionManager(this).getActiveSessionStartedAt()
            if (sessionTimerStartedAtMillis != null) {
                tvSessionTimer.visibility = android.view.View.VISIBLE
                startSessionTimer()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        stopSessionTimer()
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
                allListItems = emptyList()
                loadingIndicator.visibility = android.view.View.GONE
                tvError.visibility = android.view.View.VISIBLE
                tvEmpty.visibility = android.view.View.GONE
                tvError.text = state.message
                listContainer.removeAllViews()
            }
            is FeatureUiState.Success -> {
                allListItems = state.items
                loadingIndicator.visibility = android.view.View.GONE
                tvError.visibility = android.view.View.GONE
                tvEmpty.visibility = android.view.View.GONE
                if (enableMuscleGroupFilter) {
                    bindMuscleGroupFilter(state.items)
                }
                if (enableListSearch) {
                    applyListSearchFilter()
                } else {
                    bindItems(state.items)
                }
            }
            FeatureUiState.Empty -> {
                allListItems = emptyList()
                loadingIndicator.visibility = android.view.View.GONE
                tvError.visibility = android.view.View.GONE
                tvEmpty.visibility = android.view.View.VISIBLE
                tvEmpty.text = getString(R.string.feature_list_empty_default)
                listContainer.removeAllViews()
            }
        }
    }

    /** Filtruje listę po tytule i podtytule (tylko przy [enableListSearch]). */
    private fun applyListSearchFilter() {
        if (!enableListSearch && !enableMuscleGroupFilter) return
        val q = etListSearch?.text?.toString()?.trim().orEmpty()
        val filteredByText = if (q.isEmpty()) {
            allListItems
        } else {
            allListItems.filter { it.title.contains(q, ignoreCase = true) || it.subtitle.contains(q, ignoreCase = true) }
        }
        val filtered = if (!enableMuscleGroupFilter || selectedGroupFilter == GROUP_FILTER_ALL) {
            filteredByText
        } else {
            filteredByText.filter { parseMuscleGroup(it.subtitle).equals(selectedGroupFilter, ignoreCase = true) }
        }
        if (filtered.isEmpty() && allListItems.isNotEmpty()) {
            listContainer.removeAllViews()
            tvEmpty.visibility = View.VISIBLE
            tvEmpty.text = "Brak wyników wyszukiwania."
        } else {
            tvEmpty.visibility = View.GONE
            tvEmpty.text = getString(R.string.feature_list_empty_default)
            bindItems(filtered)
        }
    }

    private fun bindMuscleGroupFilter(items: List<FeatureListItem>) {
        val groups = items
            .map { parseMuscleGroup(it.subtitle) }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
        val options = listOf(GROUP_FILTER_ALL) + groups
        acGroupFilter?.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, options),
        )
        if (selectedGroupFilter !in options) {
            selectedGroupFilter = GROUP_FILTER_ALL
        }
        acGroupFilter?.setText(selectedGroupFilter, false)
    }

    private fun parseMuscleGroup(subtitle: String): String {
        return subtitle.substringBefore("(").trim().ifBlank { subtitle.trim() }
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
            row.setOnClickListener { onFeatureItemClicked(item) }
            listContainer.addView(row)
        }
    }

    private fun startSessionTimer() {
        val startedAt = sessionTimerStartedAtMillis ?: return
        stopSessionTimer()
        timerRunnable = object : Runnable {
            override fun run() {
                val elapsedSeconds = ((System.currentTimeMillis() - startedAt) / 1000).coerceAtLeast(0)
                val hours = elapsedSeconds / 3600
                val minutes = (elapsedSeconds % 3600) / 60
                val seconds = elapsedSeconds % 60
                tvSessionTimer.text = String.format("Sesja trwa: %02d:%02d:%02d", hours, minutes, seconds)
                timerHandler.postDelayed(this, 1000L)
            }
        }.also { timerHandler.post(it) }
    }

    private fun stopSessionTimer() {
        timerRunnable?.let { timerHandler.removeCallbacks(it) }
        timerRunnable = null
    }

    /**
     * Umożliwia podklasom dynamiczną zmianę tytułu nagłówka modułu.
     */
    protected fun setModuleTitle(title: String) {
        tvModuleTitle.text = title
    }

    /**
     * Umożliwia podklasom dynamiczną zmianę podtytułu nagłówka modułu.
     */
    protected fun setModuleSubtitle(subtitle: String) {
        tvModuleSubtitle.text = subtitle
    }

    /**
     * Umożliwia podklasom pokazanie/ukrycie przycisku akcji głównej.
     */
    protected fun setPrimaryActionVisible(visible: Boolean) {
        btnPrimaryAction.visibility = if (visible) android.view.View.VISIBLE else android.view.View.GONE
    }

    companion object {
        private const val GROUP_FILTER_ALL = "Wszystkie grupy"
    }
}
