package pl.edu.ur.km131467.trainit.ui.profile

import android.widget.TextView

/**
 * Ustawia teksty nagłówka profilu i trzech kafelków statystyk.
 */
object ProfileStatsBinder {

    /**
     * Wypełnia pola nagłówka i statystyk wartościami przekazanymi z warstwy danych.
     *
     * @param tvProfileName pole imienia i nazwiska.
     * @param tvMemberSince pole daty członkostwa.
     * @param tvProfileStatWorkouts wartość „Treningów”.
     * @param tvProfileStatHours wartość sumy czasu.
     * @param tvProfileStatStreak wartość serii dni.
     * @param profileName wyświetlane imię i nazwisko.
     * @param memberSinceText opis członkostwa.
     * @param workoutsText liczba treningów.
     * @param hoursText łączny czas treningów.
     * @param streakText seria dni.
     */
    fun bindHeaderAndPills(
        tvProfileName: TextView,
        tvMemberSince: TextView,
        tvProfileStatWorkouts: TextView,
        tvProfileStatHours: TextView,
        tvProfileStatStreak: TextView,
        profileName: String,
        memberSinceText: String,
        workoutsText: String,
        hoursText: String,
        streakText: String,
    ) {
        tvProfileName.text = profileName
        tvMemberSince.text = memberSinceText
        tvProfileStatWorkouts.text = workoutsText
        tvProfileStatHours.text = hoursText
        tvProfileStatStreak.text = streakText
    }
}
