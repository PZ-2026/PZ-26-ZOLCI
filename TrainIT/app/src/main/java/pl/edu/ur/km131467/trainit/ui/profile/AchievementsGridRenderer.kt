package pl.edu.ur.km131467.trainit.ui.profile

import android.graphics.Color
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.util.TypedValue
import pl.edu.ur.km131467.trainit.R
import pl.edu.ur.km131467.trainit.ui.common.dpToPx

/**
 * Buduje siatkę osiągnięć (dwa rzędy) w kontenerach [LinearLayout].
 *
 * @see ProfileHardcodedData.achievements
 */
class AchievementsGridRenderer(
    private val activity: AppCompatActivity,
) {

    /**
     * Dzieli [achievements] na odblokowane i zablokowane oraz dodaje badge'e do rzędów.
     *
     * @param achievementsRow1 kontener pierwszego rzędu (odblokowane).
     * @param achievementsRow2 kontener drugiego rzędu (zablokowane).
     * @param achievements pełna lista modeli osiągnięć.
     */
    fun render(
        achievementsRow1: LinearLayout,
        achievementsRow2: LinearLayout,
        achievements: List<ProfileHardcodedData.Achievement>,
    ) {
        val unlockedAchievements = achievements.filter { it.unlocked }
        val lockedAchievements = achievements.filter { !it.unlocked }

        for (achievement in unlockedAchievements) {
            achievementsRow1.addView(createAchievementBadge(achievement))
        }
        for (achievement in lockedAchievements) {
            achievementsRow2.addView(createAchievementBadge(achievement))
        }
    }

    /**
     * Tworzy pojedynczy badge osiągnięcia (ikona + etykieta, stała wysokość).
     *
     * @param achievement dane wyświetlane na badge'u.
     * @return skonfigurowany [LinearLayout].
     */
    fun createAchievementBadge(achievement: ProfileHardcodedData.Achievement): LinearLayout {
        return LinearLayout(activity).apply {
            layoutParams = LinearLayout.LayoutParams(0, activity.dpToPx(100f), 1f).apply {
                marginEnd = activity.dpToPx(8f)
            }
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundResource(
                if (achievement.unlocked) R.drawable.bg_achievement_unlocked
                else R.drawable.bg_achievement_locked,
            )
            setPadding(
                activity.dpToPx(8f),
                activity.dpToPx(14f),
                activity.dpToPx(8f),
                activity.dpToPx(14f),
            )

            val icon = ImageView(activity).apply {
                layoutParams = LinearLayout.LayoutParams(activity.dpToPx(28f), activity.dpToPx(28f))
                setImageResource(achievement.icon)
                if (!achievement.unlocked) {
                    alpha = 0.4f
                    setColorFilter(
                        ContextCompat.getColor(activity, R.color.achievement_locked_icon),
                        android.graphics.PorterDuff.Mode.SRC_IN,
                    )
                }
            }
            addView(icon)

            val label = TextView(activity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                ).apply { topMargin = activity.dpToPx(6f) }
                text = achievement.label
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
                minLines = 2
                maxLines = 2
                setTextColor(
                    if (achievement.unlocked) Color.WHITE
                    else ContextCompat.getColor(activity, R.color.text_secondary),
                )
                gravity = Gravity.CENTER
            }
            addView(label)
        }
    }
}
