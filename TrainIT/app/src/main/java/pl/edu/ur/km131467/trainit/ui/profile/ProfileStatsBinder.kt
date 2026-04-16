package pl.edu.ur.km131467.trainit.ui.profile

import android.widget.TextView

/**
 * Ustawia teksty nagłówka profilu i trzech kafelków statystyk (pill) ze stubów.
 *
 * @see ProfileHardcodedData
 */
object ProfileStatsBinder {

    /**
     * Wypełnia pola nagłówka i statystyk wartościami z [data].
     *
     * @param tvProfileName pole imienia i nazwiska.
     * @param tvMemberSince pole daty członkostwa.
     * @param tvProfileStatWorkouts wartość „Treningów”.
     * @param tvProfileStatHours wartość sumy czasu.
     * @param tvProfileStatStreak wartość serii dni.
     * @param data obiekt ze stubami tekstów profilu.
     */
    fun bindHeaderAndPills(
        tvProfileName: TextView,
        tvMemberSince: TextView,
        tvProfileStatWorkouts: TextView,
        tvProfileStatHours: TextView,
        tvProfileStatStreak: TextView,
        data: ProfileHardcodedData = ProfileHardcodedData,
    ) {
        tvProfileName.text = data.profileDisplayName
        tvMemberSince.text = data.memberSinceText
        tvProfileStatWorkouts.text = data.statWorkouts
        tvProfileStatHours.text = data.statHours
        tvProfileStatStreak.text = data.statStreak
    }
}
