package me.iamsahil.googlemapsusertracking.presentation.user_activity_recognition

import me.iamsahil.googlemapsusertracking.domain.model.TravelEntry
import me.iamsahil.googlemapsusertracking.utils.UserActivity
import me.iamsahil.googlemapsusertracking.utils.UserActivityTransition

sealed interface UserActivityRecognitionEvent {
    data class OnUserActivityChange(
        val userActivity: UserActivity,
        val userActivityTransition: UserActivityTransition
    ) : UserActivityRecognitionEvent

    object OnDeregisterActivityTransitions : UserActivityRecognitionEvent

    data class OnSelectTravelEntry(val travelEntry: TravelEntry) : UserActivityRecognitionEvent
}