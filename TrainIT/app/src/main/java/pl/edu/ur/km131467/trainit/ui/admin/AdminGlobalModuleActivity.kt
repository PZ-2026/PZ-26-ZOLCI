package pl.edu.ur.km131467.trainit.ui.admin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import pl.edu.ur.km131467.trainit.ui.feature.BaseFeatureActivity
import pl.edu.ur.km131467.trainit.ui.feature.FeatureModule

/**
 * Ekran modułu w widoku globalnym (wszystkie rekordy w systemie).
 *
 * Dla ról ADMIN/TRAINER nie przekazuje [userId], co powoduje zwrócenie
 * wszystkich rekordów przez backend.
 */
class AdminGlobalModuleActivity : BaseFeatureActivity() {

    override val module: FeatureModule by lazy {
        FeatureModule.valueOf(intent.getStringExtra(EXTRA_MODULE)!!)
    }

    override val bottomNavItem: Int = pl.edu.ur.km131467.trainit.R.id.nav_profile

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setPrimaryActionVisible(false)
        setModuleTitle(buildGlobalTitle(module))
        setModuleSubtitle("Widok globalny – wszystkie rekordy w systemie")
    }

    companion object {
        private const val EXTRA_MODULE = "admin_module"

        fun createIntent(context: Context, module: FeatureModule): Intent {
            return Intent(context, AdminGlobalModuleActivity::class.java).apply {
                putExtra(EXTRA_MODULE, module.name)
            }
        }

        private fun buildGlobalTitle(module: FeatureModule): String {
            return when (module) {
                FeatureModule.EXERCISES -> "Wszystkie ćwiczenia"
                FeatureModule.SESSIONS -> "Wszystkie sesje treningowe"
                FeatureModule.REPORTS -> "Wszystkie raporty"
                FeatureModule.STATISTICS -> "Statystyki globalne"
                FeatureModule.SETTINGS -> "Ustawienia domyślne"
                FeatureModule.NOTIFICATIONS -> "Powiadomienia globalne"
                FeatureModule.ROLE_PANEL -> "Wszystkie plany treningowe"
            }
        }
    }
}
