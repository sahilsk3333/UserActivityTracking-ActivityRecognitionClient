package me.iamsahil.googlemapsusertracking.domain.model

import com.google.android.gms.maps.model.LatLng
import java.time.LocalDateTime

data class TravelEntry(
    val trackPath: List<LatLng>,
    val savedAt: LocalDateTime
)
