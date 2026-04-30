package pl.edu.ur.km131467.trainit.ui.feature

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import kotlinx.coroutines.launch
import pl.edu.ur.km131467.trainit.R
import pl.edu.ur.km131467.trainit.data.local.SessionManager
import pl.edu.ur.km131467.trainit.data.remote.dto.AddSessionExerciseResultRequestDto
import pl.edu.ur.km131467.trainit.data.remote.dto.SessionExerciseResultDto
import pl.edu.ur.km131467.trainit.data.repository.FeatureRepository

class SessionResultsActivity : AppCompatActivity() {
    private lateinit var sessionManager: SessionManager
    private val featureRepository = FeatureRepository()
    private var sessionId: Int = -1

    private lateinit var resultsContainer: LinearLayout
    private lateinit var tvEmptyResults: TextView
    private lateinit var fabAddResult: FloatingActionButton
    private lateinit var tvTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(this)
        sessionId = intent.getIntExtra(EXTRA_SESSION_ID, -1)
        if (sessionId <= 0) {
            Toast.makeText(this, "Brak identyfikatora sesji", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        setContentView(R.layout.activity_session_results)
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        tvTitle = findViewById(R.id.tvTitle)
        tvTitle.text = "Wyniki sesji #$sessionId"
        resultsContainer = findViewById(R.id.resultsContainer)
        tvEmptyResults = findViewById(R.id.tvEmptyResults)
        fabAddResult = findViewById(R.id.fabAddResult)
        fabAddResult.setOnClickListener { showAddResultDialog() }
    }

    override fun onResume() {
        super.onResume()
        loadResults()
    }

    private fun loadResults() {
        lifecycleScope.launch {
            runCatching { featureRepository.getSessionResults(sessionManager, sessionId) }
                .onSuccess { renderResults(it) }
                .onFailure {
                    renderResults(emptyList())
                    Toast.makeText(
                        this@SessionResultsActivity,
                        it.message ?: "Nie udało się pobrać wyników sesji",
                        Toast.LENGTH_LONG,
                    ).show()
                }
        }
    }

    private fun renderResults(items: List<SessionExerciseResultDto>) {
        resultsContainer.removeAllViews()
        tvEmptyResults.visibility = if (items.isEmpty()) TextView.VISIBLE else TextView.GONE
        val totalDuration = items.sumOf { it.duration ?: 0 }
        tvTitle.text = "Wyniki sesji #$sessionId (${items.size} ćw., ${totalDuration}s)"
        val inflater = LayoutInflater.from(this)
        items.forEach { result ->
            val row = inflater.inflate(R.layout.item_session_result, resultsContainer, false)
            row.findViewById<TextView>(R.id.tvResultExercise).text = result.exerciseName
            row.findViewById<TextView>(R.id.tvResultMeta).text = buildMeta(result)
            val notes = row.findViewById<TextView>(R.id.tvResultNotes)
            if (result.notes.isNullOrBlank()) {
                notes.visibility = TextView.GONE
            } else {
                notes.visibility = TextView.VISIBLE
                notes.text = "Notatka: ${result.notes}"
            }
            row.findViewById<MaterialButton>(R.id.btnEditResult).setOnClickListener {
                showAddResultDialog(existing = result)
            }
            row.findViewById<MaterialButton>(R.id.btnDeleteResult).setOnClickListener {
                confirmDeleteResult(result)
            }
            resultsContainer.addView(row)
        }
    }

    private fun buildMeta(result: SessionExerciseResultDto): String {
        val parts = mutableListOf<String>()
        result.setsDone?.let { parts.add("serie: $it") }
        result.repsDone?.let { parts.add("powt.: $it") }
        result.weightUsed?.let { parts.add("kg: $it") }
        result.duration?.let { parts.add("czas: ${it}s") }
        return if (parts.isEmpty()) "Brak szczegółów" else parts.joinToString(", ")
    }

    private fun showAddResultDialog(existing: SessionExerciseResultDto? = null) {
        lifecycleScope.launch {
            val exercises = runCatching { featureRepository.getItems(FeatureModule.EXERCISES, sessionManager) }
                .getOrElse {
                    Toast.makeText(
                        this@SessionResultsActivity,
                        it.message ?: "Nie udało się pobrać ćwiczeń",
                        Toast.LENGTH_LONG,
                    ).show()
                    return@launch
                }
                .mapNotNull { item -> item.id?.let { it to item.title } }

            if (exercises.isEmpty()) {
                Toast.makeText(this@SessionResultsActivity, "Brak ćwiczeń do wyboru", Toast.LENGTH_LONG).show()
                return@launch
            }

            val dialogView = layoutInflater.inflate(R.layout.dialog_add_session_result, null, false)
            val acExercise = dialogView.findViewById<MaterialAutoCompleteTextView>(R.id.acExerciseName)
            val etSetsDone = dialogView.findViewById<EditText>(R.id.etSetsDone)
            val etRepsDone = dialogView.findViewById<EditText>(R.id.etRepsDone)
            val etWeightUsed = dialogView.findViewById<EditText>(R.id.etWeightUsed)
            val etDuration = dialogView.findViewById<EditText>(R.id.etDuration)
            val etNotes = dialogView.findViewById<EditText>(R.id.etNotes)

            var selectedExerciseId: Int? = existing?.exerciseId
            val adapter = ArrayAdapter(this@SessionResultsActivity, R.layout.item_dropdown_dark, exercises.map { it.second })
            adapter.setDropDownViewResource(R.layout.item_dropdown_dark)
            acExercise.setAdapter(adapter)
            acExercise.setOnItemClickListener { _, _, position, _ ->
                selectedExerciseId = exercises[position].first
            }
            if (existing != null) {
                val existingName = exercises.firstOrNull { it.first == existing.exerciseId }?.second ?: existing.exerciseName
                acExercise.setText(existingName, false)
                etSetsDone.setText(existing.setsDone?.toString().orEmpty())
                etRepsDone.setText(existing.repsDone?.toString().orEmpty())
                etWeightUsed.setText(existing.weightUsed?.toString().orEmpty())
                etDuration.setText(existing.duration?.toString().orEmpty())
                etNotes.setText(existing.notes.orEmpty())
            }

            val dialog = MaterialAlertDialogBuilder(this@SessionResultsActivity, R.style.ThemeOverlay_TrainIT_MaterialAlertDialog)
                .setTitle(if (existing == null) "Dodaj wynik ćwiczenia" else "Edytuj wynik ćwiczenia")
                .setView(dialogView)
                .setNegativeButton("Anuluj", null)
                .setPositiveButton(if (existing == null) "Dodaj" else "Zapisz", null)
                .create()

            dialog.setOnShowListener {
                val btn = dialog.getButton(DialogInterface.BUTTON_POSITIVE) ?: return@setOnShowListener
                btn.setOnClickListener {
                    var exerciseId = selectedExerciseId
                    if (exerciseId == null) {
                        val typed = acExercise.text?.toString()?.trim().orEmpty()
                        exerciseId = exercises.firstOrNull { it.second.equals(typed, ignoreCase = true) }?.first
                    }
                    if (exerciseId == null) {
                        Toast.makeText(this@SessionResultsActivity, "Wybierz ćwiczenie", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    val setsDone = etSetsDone.text?.toString()?.trim()?.toIntOrNull()
                    val repsDone = etRepsDone.text?.toString()?.trim()?.toIntOrNull()
                    val weightUsed = etWeightUsed.text?.toString()?.trim()?.toDoubleOrNull()
                    val duration = etDuration.text?.toString()?.trim()?.toIntOrNull()
                    if ((setsDone ?: 0) < 0 || (repsDone ?: 0) < 0 || (weightUsed ?: 0.0) < 0.0 || (duration ?: 0) < 0) {
                        Toast.makeText(this@SessionResultsActivity, "Wartości nie mogą być ujemne", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    lifecycleScope.launch {
                        runCatching {
                            val payload = AddSessionExerciseResultRequestDto(
                                exerciseId = exerciseId,
                                setsDone = setsDone,
                                repsDone = repsDone,
                                weightUsed = weightUsed,
                                duration = duration,
                                notes = etNotes.text?.toString()?.trim().takeIf { !it.isNullOrBlank() },
                            )
                            if (existing == null) {
                                featureRepository.addSessionResult(
                                    sessionManager = sessionManager,
                                    sessionId = sessionId,
                                    request = payload,
                                )
                            } else {
                                featureRepository.updateSessionResult(
                                    sessionManager = sessionManager,
                                    sessionId = sessionId,
                                    resultId = existing.id,
                                    request = payload,
                                )
                            }
                        }.onSuccess {
                            dialog.dismiss()
                            loadResults()
                        }.onFailure {
                            Toast.makeText(
                                this@SessionResultsActivity,
                                it.message ?: "Nie udało się zapisać wyniku",
                                Toast.LENGTH_LONG,
                            ).show()
                        }
                    }
                }
            }
            dialog.show()
        }
    }

    private fun confirmDeleteResult(result: SessionExerciseResultDto) {
        MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_TrainIT_MaterialAlertDialog)
            .setTitle("Usuń wynik")
            .setMessage("Usunąć wynik ćwiczenia \"${result.exerciseName}\"?")
            .setNegativeButton("Anuluj", null)
            .setPositiveButton("Usuń") { _, _ ->
                lifecycleScope.launch {
                    runCatching {
                        featureRepository.deleteSessionResult(sessionManager, sessionId, result.id)
                    }.onSuccess {
                        loadResults()
                    }.onFailure {
                        Toast.makeText(
                            this@SessionResultsActivity,
                            it.message ?: "Nie udało się usunąć wyniku",
                            Toast.LENGTH_LONG,
                        ).show()
                    }
                }
            }
            .show()
    }

    companion object {
        const val EXTRA_SESSION_ID = "extra_session_id"
    }
}
