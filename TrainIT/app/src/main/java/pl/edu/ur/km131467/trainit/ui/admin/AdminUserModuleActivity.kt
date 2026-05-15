package pl.edu.ur.km131467.trainit.ui.admin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import pl.edu.ur.km131467.trainit.ui.feature.BaseFeatureActivity
import pl.edu.ur.km131467.trainit.ui.feature.FeatureModule

/**
 * Ekran modułu filtrowany do konkretnego użytkownika.
 *
 * Wymusza przekazanie [targetUserId], co powoduje że backend
 * zwróci dane tylko dla wybranego użytkownika, niezależnie od roli.
 */
class AdminUserModuleActivity : BaseFeatureActivity() {

    override val isAdminView: Boolean = true

    override val module: FeatureModule by lazy {
        FeatureModule.valueOf(intent.getStringExtra(EXTRA_MODULE)!!)
    }

    override val bottomNavItem: Int = pl.edu.ur.km131467.trainit.R.id.nav_profile

    /**
     * Ustawia [targetUserId] z intencji, a następnie inicjalizuje widok modułu dla wybranego użytkownika.
     *
     * @param savedInstanceState zapisany stan instancji aktywności
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        val userId = intent.getIntExtra(EXTRA_USER_ID, -1)
        targetUserId = userId.takeIf { it > 0 }
        super.onCreate(savedInstanceState)
        setPrimaryActionVisible(false)
        setModuleTitle(buildUserTitle(module, userId))
        setModuleSubtitle("Dane użytkownika #$userId")
    }

    companion object {
        private const val EXTRA_MODULE = "admin_module"
        private const val EXTRA_USER_ID = "admin_user_id"

        fun createIntent(context: Context, module: FeatureModule, userId: Int): Intent {
            return Intent(context, AdminUserModuleActivity::class.java).apply {
                putExtra(EXTRA_MODULE, module.name)
                putExtra(EXTRA_USER_ID, userId)
            }
        }

        private fun buildUserTitle(module: FeatureModule, userId: Int): String {
            val prefix = when (module) {
                FeatureModule.EXERCISES -> "Ćwiczenia"
                FeatureModule.SESSIONS -> "Sesje treningowe"
                FeatureModule.REPORTS -> "Raporty"
                FeatureModule.STATISTICS -> "Statystyki"
                FeatureModule.SETTINGS -> "Ustawienia"
                FeatureModule.NOTIFICATIONS -> "Powiadomienia"
                FeatureModule.ROLE_PANEL -> "Plany treningowe"
            }
            return "$prefix – użytkownik $userId"
        }
    }
}
