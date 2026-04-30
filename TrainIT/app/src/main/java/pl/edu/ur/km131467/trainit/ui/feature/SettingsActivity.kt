package pl.edu.ur.km131467.trainit.ui.feature

import android.content.DialogInterface
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import pl.edu.ur.km131467.trainit.R
import pl.edu.ur.km131467.trainit.data.local.SessionManager
import pl.edu.ur.km131467.trainit.data.repository.FeatureRepository

/**
 * Ekran ustawień użytkownika.
 *
 * Zawiera sekcje związane z profilem, jednostkami i prywatnością.
 */
class SettingsActivity : BaseFeatureActivity() {
    private lateinit var sessionManager: SessionManager
    private val featureRepository = FeatureRepository()

    /** Konfiguracja modułu ustawień. */
    override val module: FeatureModule = FeatureModule.SETTINGS

    /** Aktywna zakładka dolnej nawigacji dla tego ekranu. */
    override val bottomNavItem: Int = R.id.nav_profile

    override fun onStart() {
        super.onStart()
        sessionManager = SessionManager(this)
    }

    override fun onFeatureItemClicked(item: FeatureListItem) {
        val settingId = item.id ?: return
        if (isBooleanLikeSetting(item)) {
            val toggledValue = toggleBooleanLikeValue(item.subtitle)
            lifecycleScope.launch {
                runCatching {
                    featureRepository.updateSettingForUser(sessionManager, settingId, toggledValue)
                }.onSuccess {
                    recreate()
                }.onFailure {
                    Toast.makeText(
                        this@SettingsActivity,
                        it.message ?: "Nie udało się zapisać ustawienia",
                        Toast.LENGTH_LONG,
                    ).show()
                }
            }
            return
        }

        val input = EditText(this).apply {
            setText(item.subtitle)
            setSelection(text?.length ?: 0)
            setTextColor(resources.getColor(R.color.white, theme))
            setHintTextColor(resources.getColor(R.color.text_secondary, theme))
        }

        val dialog = MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_TrainIT_MaterialAlertDialog)
            .setTitle("Zmień: ${item.title}")
            .setView(input)
            .setNegativeButton("Anuluj", null)
            .setPositiveButton("Zapisz", null)
            .create()

        dialog.setOnShowListener {
            val btn = dialog.getButton(DialogInterface.BUTTON_POSITIVE) ?: return@setOnShowListener
            btn.setOnClickListener {
                val newValue = input.text?.toString()?.trim().orEmpty()
                if (newValue.isBlank()) {
                    Toast.makeText(this, "Wartość nie może być pusta", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                lifecycleScope.launch {
                    runCatching {
                        featureRepository.updateSettingForUser(sessionManager, settingId, newValue)
                    }.onSuccess {
                        dialog.dismiss()
                        recreate()
                    }.onFailure {
                        Toast.makeText(
                            this@SettingsActivity,
                            it.message ?: "Nie udało się zapisać ustawienia",
                            Toast.LENGTH_LONG,
                        ).show()
                    }
                }
            }
        }
        dialog.show()
    }

    private fun isBooleanLikeSetting(item: FeatureListItem): Boolean {
        val title = item.title.lowercase()
        val subtitle = item.subtitle.trim().lowercase()
        val hasToggleTitle = title.contains("tryb prywatny") || title.contains("przypomnienia")
        val hasToggleValue = subtitle in setOf("włączone", "wyłączone", "wlaczone", "wylaczone", "on", "off", "true", "false")
        return hasToggleTitle || hasToggleValue
    }

    private fun toggleBooleanLikeValue(currentValue: String): String {
        return when (currentValue.trim().lowercase()) {
            "włączone", "wlaczone", "on", "true" -> "wyłączone"
            else -> "włączone"
        }
    }
}
