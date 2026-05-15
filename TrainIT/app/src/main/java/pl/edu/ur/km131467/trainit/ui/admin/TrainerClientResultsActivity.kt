package pl.edu.ur.km131467.trainit.ui.admin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.launch
import pl.edu.ur.km131467.trainit.R
import pl.edu.ur.km131467.trainit.data.local.SessionManager
import pl.edu.ur.km131467.trainit.data.remote.dto.SessionExerciseResultDto
import pl.edu.ur.km131467.trainit.data.repository.FeatureRepository
import pl.edu.ur.km131467.trainit.ui.feature.FeatureListItem
import pl.edu.ur.km131467.trainit.ui.feature.FeatureModule
import pl.edu.ur.km131467.trainit.ui.login.LoginActivity

/**
 * Ekran analizy wyników ćwiczeń konkretnego klienta.
 *
 * Pobiera wszystkie sesje klienta, a następnie dla każdej sesji
 * pobiera szczegółowe wyniki ćwiczeń. Wyświetla je w formie
 * pogrupowanej po sesjach.
 */
class TrainerClientResultsActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private val featureRepository = FeatureRepository()

    private var targetUserId: Int = -1

    private lateinit var tvScreenTitle: TextView
    private lateinit var loadingIndicator: LinearProgressIndicator
    private lateinit var resultsContainer: LinearLayout
    private lateinit var tvEmpty: TextView

    /**
     * Odczytuje identyfikator klienta z intencji i uruchamia pobieranie wyników sesji.
     *
     * @param savedInstanceState zapisany stan instancji aktywności
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(this)
        if (!sessionManager.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        targetUserId = intent.getIntExtra(EXTRA_USER_ID, -1)
        if (targetUserId <= 0) {
            Toast.makeText(this, "Brak identyfikatora klienta", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setContentView(R.layout.activity_trainer_client_results)
        initViews()
        tvScreenTitle.text = "Wyniki klienta #$targetUserId"
        loadClientResults()
    }

    private fun initViews() {
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        tvScreenTitle = findViewById(R.id.tvScreenTitle)
        loadingIndicator = findViewById(R.id.loadingIndicator)
        resultsContainer = findViewById(R.id.resultsContainer)
        tvEmpty = findViewById(R.id.tvEmpty)
    }

    private fun loadClientResults() {
        lifecycleScope.launch {
            loadingIndicator.visibility = LinearProgressIndicator.VISIBLE
            tvEmpty.visibility = TextView.GONE
            resultsContainer.removeAllViews()

            val sessions = runCatching {
                featureRepository.getItems(FeatureModule.SESSIONS, sessionManager, targetUserId)
            }.getOrElse {
                Toast.makeText(
                    this@TrainerClientResultsActivity,
                    it.message ?: "Nie udało się pobrać sesji klienta",
                    Toast.LENGTH_LONG,
                ).show()
                loadingIndicator.visibility = LinearProgressIndicator.GONE
                return@launch
            }

            if (sessions.isEmpty()) {
                loadingIndicator.visibility = LinearProgressIndicator.GONE
                tvEmpty.visibility = TextView.VISIBLE
                return@launch
            }

            val inflater = LayoutInflater.from(this@TrainerClientResultsActivity)
            var anyResults = false

            for (session in sessions) {
                val sessionId = session.id ?: continue
                val results = runCatching {
                    featureRepository.getSessionResults(sessionManager, sessionId)
                }.getOrNull()?.takeIf { it.isNotEmpty() } ?: continue

                anyResults = true
                renderSessionGroup(inflater, session, results)
            }

            loadingIndicator.visibility = LinearProgressIndicator.GONE
            if (!anyResults) {
                tvEmpty.visibility = TextView.VISIBLE
            }
        }
    }

    /**
     * Renderuje nagłówek sesji, wiersze wyników ćwiczeń oraz separator wizualny.
     */
    private fun renderSessionGroup(
        inflater: LayoutInflater,
        session: FeatureListItem,
        results: List<SessionExerciseResultDto>,
    ) {
        val header = inflater.inflate(R.layout.item_feature_entry, resultsContainer, false)
        header.findViewById<TextView>(R.id.tvItemTitle).text = session.title
        header.findViewById<TextView>(R.id.tvItemSubtitle).text = session.subtitle
        header.setBackgroundResource(R.drawable.bg_card_dark_elevated)
        resultsContainer.addView(header)

        for (result in results) {
            val row = inflater.inflate(R.layout.item_feature_entry, resultsContainer, false)
            row.findViewById<TextView>(R.id.tvItemTitle).text = result.exerciseName
            val parts = mutableListOf<String>()
            result.setsDone?.let { parts.add("serie: $it") }
            result.repsDone?.let { parts.add("powtórzenia: $it") }
            result.weightUsed?.let { parts.add("ciężar: ${it} kg") }
            result.duration?.let { parts.add("czas: ${it}s") }
            result.notes?.takeIf { it.isNotBlank() }?.let { parts.add("notatki: $it") }
            row.findViewById<TextView>(R.id.tvItemSubtitle).text =
                if (parts.isEmpty()) "Brak szczegółów" else parts.joinToString(", ")
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            )
            params.marginStart = 16
            row.layoutParams = params
            resultsContainer.addView(row)
        }

        val divider = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1,
            ).apply {
                setMargins(0, 12, 0, 12)
            }
            setBackgroundColor(getColor(R.color.card_dark))
        }
        resultsContainer.addView(divider)
    }

    companion object {
        private const val EXTRA_USER_ID = "trainer_client_user_id"

        fun createIntent(context: Context, userId: Int): Intent {
            return Intent(context, TrainerClientResultsActivity::class.java).apply {
                putExtra(EXTRA_USER_ID, userId)
            }
        }
    }
}
