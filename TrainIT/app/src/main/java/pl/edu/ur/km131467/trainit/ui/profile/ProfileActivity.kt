package pl.edu.ur.km131467.trainit.ui.profile

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import pl.edu.ur.km131467.trainit.R
import pl.edu.ur.km131467.trainit.data.local.SessionManager
import pl.edu.ur.km131467.trainit.data.remote.dto.ProfileAchievementDto
import pl.edu.ur.km131467.trainit.data.remote.dto.UpdateProfileRequestDto
import pl.edu.ur.km131467.trainit.data.repository.AuthRepository
import pl.edu.ur.km131467.trainit.data.repository.AuthResult
import pl.edu.ur.km131467.trainit.data.repository.FeatureRepository
import pl.edu.ur.km131467.trainit.ui.common.BottomNavHelper
import pl.edu.ur.km131467.trainit.ui.feature.NotificationsActivity
import pl.edu.ur.km131467.trainit.ui.feature.ReportsActivity
import pl.edu.ur.km131467.trainit.ui.feature.SettingsActivity
import pl.edu.ur.km131467.trainit.ui.login.LoginActivity

/**
 * Aktywność ekranu profilu użytkownika.
 *
 * Deleguje budowę sekcji do rendererów ([WeeklyChartRenderer], [ProfileRecordsRenderer],
 * [AchievementsGridRenderer], [ProfileSummaryRenderer]) oraz wypełnianie nagłówka
 * przez [ProfileStatsBinder]. Dane pobierane są z backendu PostgreSQL.
 * Wylogowanie czyści sesję przez [SessionManager].
 *
 * @see LoginActivity
 */
class ProfileActivity : AppCompatActivity() {
    /** Menedżer sesji użytkownika. */
    private lateinit var sessionManager: SessionManager

    /** Repozytorium danych backendowych. */
    private val featureRepository = FeatureRepository()
    private val authRepository = AuthRepository()

    /** Dolna nawigacja (Home / Treningi / Profil). */
    private lateinit var bottomNavigation: BottomNavigationView

    /** Przycisk "Wyloguj się". */
    private lateinit var btnLogout: MaterialButton
    /** Skrót do modułu raportów. */
    private lateinit var btnOpenReports: MaterialButton
    /** Skrót do modułu ustawień. */
    private lateinit var btnOpenSettings: MaterialButton
    /** Skrót do modułu powiadomień. */
    private lateinit var btnOpenNotifications: MaterialButton

    /** Kontener na słupki wykresu aktywności tygodniowej. */
    private lateinit var chartBarsContainer: LinearLayout

    /** Kontener na etykiety dni tygodnia pod wykresem. */
    private lateinit var chartDaysContainer: LinearLayout

    /** Kontener na karty rekordów osobistych. */
    private lateinit var recordsContainer: LinearLayout

    /** Pierwszy rząd osiągnięć (odblokowane). */
    private lateinit var achievementsRow1: LinearLayout

    /** Drugi rząd osiągnięć (zablokowane). */
    private lateinit var achievementsRow2: LinearLayout

    /** Kontener na wiersze sekcji podsumowania. */
    private lateinit var summaryContainer: LinearLayout
    private lateinit var tvProfileName: TextView
    private lateinit var tvMemberSince: TextView

    /**
     * Wiąże layout, wypełnia dane profilu i konfiguruje nawigację oraz wylogowanie.
     *
     * @param savedInstanceState zapisany stan instancji (nieużywany).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(this)
        if (!sessionManager.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        setContentView(R.layout.activity_profile)

        initViews()
        ProfileStatsBinder.bindHeaderAndPills(
            tvProfileName = findViewById(R.id.tvProfileName),
            tvMemberSince = findViewById(R.id.tvMemberSince),
            tvProfileStatWorkouts = findViewById(R.id.tvProfileStatWorkouts),
            tvProfileStatHours = findViewById(R.id.tvProfileStatHours),
            tvProfileStatStreak = findViewById(R.id.tvProfileStatStreak),
            profileName = listOfNotNull(sessionManager.getFirstName(), sessionManager.getRole()).joinToString(" • "),
            memberSinceText = "Ładowanie...",
            workoutsText = "0",
            hoursText = "0h",
            streakText = "0",
        )

        BottomNavHelper.setupBottomNav(bottomNavigation, this, R.id.nav_profile)
        setupFeatureShortcuts()
        setupLogout()
        loadProfileData()
    }

    /** Inicjalizuje wszystkie kontrolki ekranu profilu. */
    private fun initViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation)
        btnLogout = findViewById(R.id.btnLogout)
        btnOpenReports = findViewById(R.id.btnOpenReports)
        btnOpenSettings = findViewById(R.id.btnOpenSettings)
        btnOpenNotifications = findViewById(R.id.btnOpenNotifications)
        chartBarsContainer = findViewById(R.id.chartBarsContainer)
        chartDaysContainer = findViewById(R.id.chartDaysContainer)
        recordsContainer = findViewById(R.id.recordsContainer)
        achievementsRow1 = findViewById(R.id.achievementsRow1)
        achievementsRow2 = findViewById(R.id.achievementsRow2)
        summaryContainer = findViewById(R.id.summaryContainer)
        tvProfileName = findViewById(R.id.tvProfileName)
        tvMemberSince = findViewById(R.id.tvMemberSince)
        tvProfileName.setOnClickListener { showEditProfileDialog() }
        tvMemberSince.setOnClickListener { showEditProfileDialog() }
    }

    /** Konfiguruje skróty nawigacyjne do dodatkowych modułów. */
    private fun setupFeatureShortcuts() {
        btnOpenReports.setOnClickListener { startActivity(Intent(this, ReportsActivity::class.java)) }
        btnOpenSettings.setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }
        btnOpenNotifications.setOnClickListener { startActivity(Intent(this, NotificationsActivity::class.java)) }
    }

    /**
     * Czyści sesję ([SessionManager.clearSession]) i otwiera [LoginActivity] z czyszczeniem stosu.
     */
    private fun setupLogout() {
        btnLogout.setOnClickListener {
            SessionManager(this).clearSession()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    /** Pobiera dane profilu użytkownika z backendu i odświeża sekcję statystyk oraz podsumowania. */
    private fun loadProfileData() {
        lifecycleScope.launch {
            val overview = runCatching { featureRepository.getProfileOverview(sessionManager) }.getOrNull() ?: return@launch

            ProfileStatsBinder.bindHeaderAndPills(
                tvProfileName = findViewById(R.id.tvProfileName),
                tvMemberSince = findViewById(R.id.tvMemberSince),
                tvProfileStatWorkouts = findViewById(R.id.tvProfileStatWorkouts),
                tvProfileStatHours = findViewById(R.id.tvProfileStatHours),
                tvProfileStatStreak = findViewById(R.id.tvProfileStatStreak),
                profileName = overview.profileName,
                memberSinceText = overview.memberSinceText,
                workoutsText = overview.workoutsText,
                hoursText = overview.totalHoursText,
                streakText = overview.streakText,
            )

            chartBarsContainer.removeAllViews()
            chartDaysContainer.removeAllViews()
            WeeklyChartRenderer(this@ProfileActivity).render(
                chartBarsContainer = chartBarsContainer,
                chartDaysContainer = chartDaysContainer,
                weeklyData = overview.weeklyHours,
                dayLabels = ProfileHardcodedData.dayLabels,
            )

            recordsContainer.removeAllViews()
            ProfileRecordsRenderer(this@ProfileActivity).render(
                recordsContainer,
                overview.personalRecords.map {
                    ProfileHardcodedData.PersonalRecord(it.exercise, it.weight, it.date, it.reps)
                },
            )

            achievementsRow1.removeAllViews()
            achievementsRow2.removeAllViews()
            AchievementsGridRenderer(this@ProfileActivity).render(
                achievementsRow1,
                achievementsRow2,
                overview.achievements.map { it.toUiAchievement() },
            )

            summaryContainer.removeAllViews()
            ProfileSummaryRenderer(this@ProfileActivity).render(
                summaryContainer,
                overview.summaryItems.map {
                    ProfileHardcodedData.SummaryItem(
                        icon = when {
                            it.title.contains("raport", ignoreCase = true) -> R.drawable.ic_dumbbell
                            it.title.contains("przypomnienia", ignoreCase = true) -> R.drawable.ic_clock
                            else -> R.drawable.ic_fire
                        },
                        label = it.title,
                        value = it.subtitle,
                    )
                },
            )
        }
    }

    private fun ProfileAchievementDto.toUiAchievement(): ProfileHardcodedData.Achievement {
        val iconRes = when (key.lowercase()) {
            "fire" -> R.drawable.ic_fire
            "muscle" -> R.drawable.ic_muscle
            "trophy" -> R.drawable.ic_trophy
            "target" -> R.drawable.ic_target
            "star" -> R.drawable.ic_star
            else -> R.drawable.ic_medal
        }
        return ProfileHardcodedData.Achievement(iconRes, label, unlocked)
    }

    private fun showEditProfileDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        val etFirstName = dialogView.findViewById<TextInputEditText>(R.id.etEditProfileFirstName)
        val etLastName = dialogView.findViewById<TextInputEditText>(R.id.etEditProfileLastName)
        val etEmail = dialogView.findViewById<TextInputEditText>(R.id.etEditProfileEmail)
        val etPassword = dialogView.findViewById<TextInputEditText>(R.id.etEditProfilePassword)
        etFirstName.setText(sessionManager.getFirstName().orEmpty())
        etLastName.setText(sessionManager.getLastName().orEmpty())
        etEmail.setText(sessionManager.getEmail().orEmpty())

        val dialog = MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_TrainIT_MaterialAlertDialog)
            .setTitle("Edytuj profil")
            .setView(dialogView)
            .setNegativeButton("Anuluj", null)
            .setPositiveButton("Zapisz", null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE)?.setOnClickListener {
                val firstName = etFirstName.text?.toString()?.trim().orEmpty()
                val lastName = etLastName.text?.toString()?.trim().orEmpty()
                val email = etEmail.text?.toString()?.trim().orEmpty()
                val password = etPassword.text?.toString()?.trim().orEmpty()
                if (firstName.isBlank() || lastName.isBlank() || email.isBlank()) {
                    Toast.makeText(this, "Uzupełnij imię, nazwisko i email", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (password.isNotBlank() && password.length < 8) {
                    Toast.makeText(this, "Nowe hasło musi mieć minimum 8 znaków", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val token = sessionManager.getToken()
                if (token.isNullOrBlank()) {
                    Toast.makeText(this, "Brak aktywnej sesji", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                lifecycleScope.launch {
                    when (
                        val result = authRepository.updateMe(
                            token,
                            UpdateProfileRequestDto(
                                firstName = firstName,
                                lastName = lastName,
                                email = email,
                                newPassword = password.ifBlank { null },
                            ),
                        )
                    ) {
                        is AuthResult.Success -> {
                            sessionManager.updateProfile(
                                firstName = result.data.firstName,
                                lastName = result.data.lastName,
                                email = result.data.email,
                            )
                            dialog.dismiss()
                            loadProfileData()
                            Toast.makeText(this@ProfileActivity, "Zapisano zmiany profilu", Toast.LENGTH_SHORT).show()
                        }
                        is AuthResult.Error -> Toast.makeText(this@ProfileActivity, result.message, Toast.LENGTH_LONG).show()
                        AuthResult.NetworkError -> Toast.makeText(this@ProfileActivity, "Brak połączenia z serwerem", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
        dialog.show()
    }
}
