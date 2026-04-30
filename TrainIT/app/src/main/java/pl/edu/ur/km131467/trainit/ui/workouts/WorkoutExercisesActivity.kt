package pl.edu.ur.km131467.trainit.ui.workouts

import android.content.DialogInterface
import android.content.Intent
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
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import pl.edu.ur.km131467.trainit.R
import pl.edu.ur.km131467.trainit.data.local.SessionManager
import pl.edu.ur.km131467.trainit.data.remote.dto.AddWorkoutExerciseLineRequestDto
import pl.edu.ur.km131467.trainit.data.remote.dto.WorkoutExerciseLineDto
import pl.edu.ur.km131467.trainit.data.repository.FeatureRepository
import pl.edu.ur.km131467.trainit.ui.feature.FeatureListItem
import pl.edu.ur.km131467.trainit.ui.feature.FeatureModule
import pl.edu.ur.km131467.trainit.ui.login.LoginActivity

/** Zarządzanie ćwiczeniami w planie (WF-8): lista, dodawanie, usuwanie pozycji. */
class WorkoutExercisesActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private val featureRepository = FeatureRepository()

    private lateinit var linesContainer: LinearLayout
    private lateinit var tvEmptyLines: TextView
    private lateinit var fabAddExercise: FloatingActionButton

    private var workoutId: Int = -1
    private var lines: List<WorkoutExerciseLineDto> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(this)
        if (!sessionManager.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        workoutId = intent.getIntExtra(AddWorkoutActivity.EXTRA_WORKOUT_ID, -1)
        if (workoutId <= 0) {
            Toast.makeText(this, "Brak planu treningowego", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setContentView(R.layout.activity_workout_exercises)
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        linesContainer = findViewById(R.id.linesContainer)
        tvEmptyLines = findViewById(R.id.tvEmptyLines)
        fabAddExercise = findViewById(R.id.fabAddExercise)
        fabAddExercise.setOnClickListener { showAddExerciseDialog() }
    }

    override fun onResume() {
        super.onResume()
        loadLines()
    }

    private fun loadLines() {
        lifecycleScope.launch {
            runCatching { featureRepository.getWorkoutExerciseLines(sessionManager, workoutId) }
                .onSuccess {
                    lines = it
                    renderLines()
                }
                .onFailure {
                    lines = emptyList()
                    renderLines()
                    Toast.makeText(
                        this@WorkoutExercisesActivity,
                        it.message ?: "Nie udało się wczytać ćwiczeń",
                        Toast.LENGTH_LONG,
                    ).show()
                }
        }
    }

    private fun renderLines() {
        linesContainer.removeAllViews()
        tvEmptyLines.visibility = if (lines.isEmpty()) TextView.VISIBLE else TextView.GONE
        val inflater = LayoutInflater.from(this)
        for (line in lines) {
            val row = inflater.inflate(R.layout.item_workout_exercise_line, linesContainer, false)
            row.findViewById<TextView>(R.id.tvExerciseName).text = line.exerciseName
            row.findViewById<TextView>(R.id.tvExerciseParams).text = formatLineParams(line)
            row.findViewById<MaterialButton>(R.id.btnRemoveLine).setOnClickListener {
                confirmRemoveLine(line)
            }
            linesContainer.addView(row)
        }
    }

    private fun formatLineParams(line: WorkoutExerciseLineDto): String {
        val parts = mutableListOf<String>()
        line.sets?.let { parts.add("serie: $it") }
        line.reps?.let { parts.add("powtórzenia: $it") }
        line.weight?.let { parts.add("ciężar: ${it} kg") }
        line.duration?.let { parts.add("czas: ${it} s") }
        return if (parts.isEmpty()) "Brak parametrów" else parts.joinToString(", ")
    }

    private fun confirmRemoveLine(line: WorkoutExerciseLineDto) {
        MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_TrainIT_MaterialAlertDialog)
            .setTitle("Usuń z planu")
            .setMessage("Usunąć \"${line.exerciseName}\" z tego planu?")
            .setNegativeButton("Anuluj", null)
            .setPositiveButton("Usuń") { _, _ ->
                lifecycleScope.launch {
                    runCatching {
                        featureRepository.deleteWorkoutExerciseLine(sessionManager, workoutId, line.id)
                    }
                        .onSuccess { loadLines() }
                        .onFailure {
                            Toast.makeText(
                                this@WorkoutExercisesActivity,
                                it.message ?: "Nie udało się usunąć",
                                Toast.LENGTH_LONG,
                            ).show()
                        }
                }
            }
            .show()
    }

    private fun showAddExerciseDialog() {
        lifecycleScope.launch {
            val exercisePairs: List<Pair<Int, String>> = runCatching {
                featureRepository.getItems(FeatureModule.EXERCISES, sessionManager)
                    .mapNotNull { item: FeatureListItem ->
                        val id = item.id ?: return@mapNotNull null
                        id to item.title
                    }
            }.getOrElse {
                Toast.makeText(
                    this@WorkoutExercisesActivity,
                    it.message ?: "Nie udało się pobrać listy ćwiczeń",
                    Toast.LENGTH_LONG,
                ).show()
                return@launch
            }
            if (exercisePairs.isEmpty()) {
                Toast.makeText(
                    this@WorkoutExercisesActivity,
                    "Brak dostępnych ćwiczeń do dodania",
                    Toast.LENGTH_LONG,
                ).show()
                return@launch
            }

            val dialogView = layoutInflater.inflate(R.layout.dialog_add_plan_exercise, null, false)
            val acExercise = dialogView.findViewById<MaterialAutoCompleteTextView>(R.id.acExerciseName)
            val etSets = dialogView.findViewById<EditText>(R.id.etSets)
            val etReps = dialogView.findViewById<EditText>(R.id.etReps)
            val etWeight = dialogView.findViewById<EditText>(R.id.etWeight)
            val etDuration = dialogView.findViewById<EditText>(R.id.etDuration)

            var selectedExerciseId: Int? = null
            val titles = exercisePairs.map { it.second }
            val adapter = ArrayAdapter(
                this@WorkoutExercisesActivity,
                R.layout.item_dropdown_dark,
                titles,
            )
            adapter.setDropDownViewResource(R.layout.item_dropdown_dark)
            acExercise.setAdapter(adapter)
            acExercise.setOnItemClickListener { _, _, position, _ ->
                selectedExerciseId = exercisePairs[position].first
            }

            val dialog = MaterialAlertDialogBuilder(this@WorkoutExercisesActivity, R.style.ThemeOverlay_TrainIT_MaterialAlertDialog)
                .setTitle("Dodaj ćwiczenie")
                .setView(dialogView)
                .setNegativeButton("Anuluj", null)
                .setPositiveButton("Dodaj", null)
                .create()

            dialog.setOnShowListener {
                val addBtn = dialog.getButton(DialogInterface.BUTTON_POSITIVE) ?: return@setOnShowListener
                addBtn.setOnClickListener {
                    var exId = selectedExerciseId
                    if (exId == null) {
                        val typed = acExercise.text?.toString()?.trim().orEmpty()
                        exId = exercisePairs.firstOrNull { it.second.equals(typed, ignoreCase = true) }?.first
                    }
                    if (exId == null) {
                        Toast.makeText(
                            this@WorkoutExercisesActivity,
                            "Wybierz ćwiczenie z listy lub wpisz dokładną nazwę",
                            Toast.LENGTH_SHORT,
                        ).show()
                        return@setOnClickListener
                    }
                    val sets = etSets.text?.toString()?.trim()?.toIntOrNull()
                    val reps = etReps.text?.toString()?.trim()?.toIntOrNull()
                    val weight = etWeight.text?.toString()?.trim()?.toDoubleOrNull()
                    val duration = etDuration.text?.toString()?.trim()?.toIntOrNull()
                    lifecycleScope.launch {
                        runCatching {
                            featureRepository.addWorkoutExerciseLine(
                                sessionManager,
                                workoutId,
                                AddWorkoutExerciseLineRequestDto(
                                    exerciseId = exId,
                                    sets = sets,
                                    reps = reps,
                                    weight = weight,
                                    duration = duration,
                                ),
                            )
                        }
                            .onSuccess {
                                dialog.dismiss()
                                loadLines()
                            }
                            .onFailure {
                                Toast.makeText(
                                    this@WorkoutExercisesActivity,
                                    it.message ?: "Nie udało się dodać ćwiczenia",
                                    Toast.LENGTH_LONG,
                                ).show()
                            }
                    }
                }
            }
            dialog.show()
        }
    }
}
