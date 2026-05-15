package pl.edu.ur.km131467.trainit.ui.workouts

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import pl.edu.ur.km131467.trainit.R
import pl.edu.ur.km131467.trainit.data.local.SessionManager
import pl.edu.ur.km131467.trainit.data.repository.FeatureRepository

/**
 * Ekran formularza dodawania lub edycji planu treningowego.
 *
 * Obsługuje tryb tworzenia planu własnego, edycji istniejącego planu
 * oraz tworzenia planu dla klienta (trener podaje [EXTRA_TARGET_USER_ID]).
 */
class AddWorkoutActivity : AppCompatActivity() {
    private lateinit var tvScreenTitle: TextView
    private lateinit var etWorkoutName: EditText
    private lateinit var etDescription: EditText
    private lateinit var etDuration: EditText
    private lateinit var acDifficultyLevel: AutoCompleteTextView
    private lateinit var btnSaveWorkout: MaterialButton
    private lateinit var btnManageExercises: MaterialButton

    private lateinit var sessionManager: SessionManager
    private val featureRepository = FeatureRepository()

    private val editWorkoutId: Int?
        get() {
            val id = intent.getIntExtra(EXTRA_WORKOUT_ID, -1)
            return if (id > 0) id else null
        }

    private val targetUserId: Int?
        get() {
            val id = intent.getIntExtra(EXTRA_TARGET_USER_ID, -1)
            return if (id > 0) id else null
        }

    /**
     * Wiąże formularz; w trybie edycji wczytuje szczegóły planu z backendu.
     *
     * @param savedInstanceState zapisany stan instancji aktywności
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_workout)

        sessionManager = SessionManager(this)
        initViews()
        bindDropdowns()
        setupActions()

        val wid = editWorkoutId
        if (wid != null) {
            tvScreenTitle.text = "Edytuj trening"
            btnSaveWorkout.text = "Zapisz zmiany"
            btnManageExercises.visibility = View.VISIBLE
            btnManageExercises.setOnClickListener {
                startActivity(
                    Intent(this, WorkoutExercisesActivity::class.java)
                        .putExtra(EXTRA_WORKOUT_ID, wid),
                )
            }
            btnSaveWorkout.isEnabled = false
            lifecycleScope.launch {
                runCatching { featureRepository.getWorkoutPlanDetail(sessionManager, wid) }
                    .onSuccess { detail ->
                        etWorkoutName.setText(detail.name)
                        etDescription.setText(detail.description.orEmpty())
                        etDuration.setText((detail.estimatedDuration ?: 60).toString())
                        val level = normalizeDifficultyForForm(detail.difficultyLevel)
                        acDifficultyLevel.setText(level, false)
                        btnSaveWorkout.isEnabled = true
                    }
                    .onFailure {
                        btnSaveWorkout.isEnabled = true
                        Toast.makeText(
                            this@AddWorkoutActivity,
                            it.message ?: "Nie udało się wczytać planu",
                            Toast.LENGTH_LONG,
                        ).show()
                        finish()
                    }
            }
        } else if (targetUserId != null) {
            tvScreenTitle.text = "Utwórz plan dla klienta"
        }
    }

    private fun initViews() {
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        tvScreenTitle = findViewById(R.id.tvScreenTitle)
        etWorkoutName = findViewById(R.id.etWorkoutName)
        etDescription = findViewById(R.id.etDescription)
        etDuration = findViewById(R.id.etDuration)
        acDifficultyLevel = findViewById(R.id.acDifficultyLevel)
        btnSaveWorkout = findViewById(R.id.btnSaveWorkout)
        btnManageExercises = findViewById(R.id.btnManageExercises)
    }

    private fun bindDropdowns() {
        val adapter = ArrayAdapter(
            this,
            R.layout.item_dropdown_dark,
            listOf("ŁATWY", "ŚREDNI", "TRUDNY"),
        )
        adapter.setDropDownViewResource(R.layout.item_dropdown_dark)
        acDifficultyLevel.setAdapter(adapter)
        if (editWorkoutId == null) {
            acDifficultyLevel.setText("ŚREDNI", false)
        }
    }

    private fun setupActions() {
        btnSaveWorkout.setOnClickListener {
            val name = etWorkoutName.text?.toString()?.trim().orEmpty()
            val level = acDifficultyLevel.text?.toString()?.trim().orEmpty().ifBlank { "ŚREDNI" }
            val duration = etDuration.text?.toString()?.trim()?.toIntOrNull() ?: 60
            val descriptionRaw = etDescription.text?.toString()?.trim().orEmpty()

            if (name.isBlank()) {
                Toast.makeText(this, "Podaj nazwę treningu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (duration <= 0) {
                Toast.makeText(this, "Czas musi być większy od 0", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSaveWorkout.isEnabled = false
            lifecycleScope.launch {
                val wid = editWorkoutId
                val result = if (wid == null) {
                    runCatching {
                        featureRepository.createWorkoutForUser(
                            sessionManager = sessionManager,
                            name = name,
                            description = descriptionRaw.ifBlank { "Dodane z aplikacji mobilnej" },
                            difficultyLevel = level,
                            estimatedDuration = duration,
                            targetUserId = targetUserId,
                        )
                    }
                } else {
                    runCatching {
                        featureRepository.updateWorkoutForUser(
                            sessionManager = sessionManager,
                            workoutId = wid,
                            name = name,
                            description = descriptionRaw.takeIf { it.isNotBlank() },
                            difficultyLevel = level,
                            estimatedDuration = duration,
                        )
                    }
                }
                result
                    .onSuccess {
                        setResult(RESULT_OK)
                        val msg = if (editWorkoutId == null) "Dodano trening" else "Zapisano zmiany"
                        Toast.makeText(this@AddWorkoutActivity, msg, Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .onFailure {
                        btnSaveWorkout.isEnabled = true
                        Toast.makeText(
                            this@AddWorkoutActivity,
                            it.message ?: "Nie udało się zapisać treningu",
                            Toast.LENGTH_LONG,
                        ).show()
                    }
            }
        }
    }

    private fun normalizeDifficultyForForm(raw: String?): String {
        val u = raw.orEmpty().uppercase()
        return when {
            "ŁATWY" in u || "LATWY" in u || "EASY" in u -> "ŁATWY"
            "TRUDNY" in u || "HARD" in u -> "TRUDNY"
            else -> "ŚREDNI"
        }
    }

    companion object {
        const val EXTRA_WORKOUT_ID = "extra_workout_id"
        const val EXTRA_TARGET_USER_ID = "extra_target_user_id"
    }
}
