package pl.edu.ur.km131467.trainit.ui.common

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import pl.edu.ur.km131467.trainit.MainActivity
import pl.edu.ur.km131467.trainit.R
import pl.edu.ur.km131467.trainit.ui.profile.ProfileActivity
import pl.edu.ur.km131467.trainit.ui.workouts.WorkoutsActivity

/**
 * Wspólna konfiguracja dolnej nawigacji ([BottomNavigationView]) dla ekranów głównych aplikacji.
 *
 * Zapewnia spójne przełączanie między [MainActivity], [WorkoutsActivity] i [ProfileActivity]
 * z zamykaniem bieżącej aktywności ([AppCompatActivity.finish]), aby nie narastał stos.
 *
 * @see MainActivity
 * @see WorkoutsActivity
 * @see ProfileActivity
 */
object BottomNavHelper {

    /**
     * Ustawia zaznaczoną pozycję menu i listener nawigacji.
     *
     * Wybór pozycji równej [currentItemId] nie uruchamia nowej aktywności (pozostaje bieżący ekran).
     * Inne pozycje uruchamiają odpowiednią aktywność i kończą [activity].
     *
     * @param nav widok dolnej nawigacji z menu [R.menu.bottom_nav_menu].
     * @param activity aktywność-host (źródło [Intent] i miejsce wywołania [finish][AppCompatActivity.finish]).
     * @param currentItemId identyfikator zakładki odpowiadającej bieżącemu ekranowi
     * ([R.id.nav_home], [R.id.nav_workouts] lub [R.id.nav_profile]).
     */
    fun setupBottomNav(
        nav: BottomNavigationView,
        activity: AppCompatActivity,
        currentItemId: Int,
    ) {
        nav.selectedItemId = currentItemId
        nav.setOnItemSelectedListener { item ->
            if (item.itemId == currentItemId) {
                return@setOnItemSelectedListener true
            }
            when (item.itemId) {
                R.id.nav_home -> {
                    activity.startActivity(Intent(activity, MainActivity::class.java))
                    activity.finish()
                    true
                }
                R.id.nav_workouts -> {
                    activity.startActivity(Intent(activity, WorkoutsActivity::class.java))
                    activity.finish()
                    true
                }
                R.id.nav_profile -> {
                    activity.startActivity(Intent(activity, ProfileActivity::class.java))
                    activity.finish()
                    true
                }
                else -> false
            }
        }
    }
}
