package me.iamsahil.googlemapsusertracking.presentation.user_activity_recognition

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import me.iamsahil.googlemapsusertracking.domain.model.TravelEntry

data class UserActivityRecognitionState(
    val isTracking: Boolean = false,
    val trackingPath: List<LatLng> = emptyList(),
    val currentActivity: String = "Unknown Activity",
    val travelEntries : List<TravelEntry> = emptyList(),
    val mapCameraBound : LatLngBounds? = null
)
