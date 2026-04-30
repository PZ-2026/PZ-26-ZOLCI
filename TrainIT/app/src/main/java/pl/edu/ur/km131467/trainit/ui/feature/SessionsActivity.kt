package pl.edu.ur.km131467.trainit.ui.feature

import android.content.Intent
import android.os.Bundle
import android.view.View
import pl.edu.ur.km131467.trainit.R

/**
 * Ekran modułu sesji treningowych.
 *
 * Wyświetla listę sesji i działania kontekstowe związane z rozpoczęciem treningu.
 */
class SessionsActivity : BaseFeatureActivity() {
    /** Konfiguracja modułu dla sesji treningowych. */
    override val module: FeatureModule = FeatureModule.SESSIONS

    /** Aktywna zakładka dolnej nawigacji dla tego ekranu. */
    override val bottomNavItem: Int = R.id.nav_workouts

    /** WF-11: historia sesji z wyszukiwaniem po statusie / planie. */
    override val enableListSearch: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findViewById<View>(R.id.btnPrimaryAction)?.visibility = View.GONE
    }

    override fun onFeatureItemClicked(item: FeatureListItem) {
        val sessionId = item.id ?: return
        startActivity(
            Intent(this, SessionResultsActivity::class.java)
                .putExtra(SessionResultsActivity.EXTRA_SESSION_ID, sessionId),
        )
    }
}
