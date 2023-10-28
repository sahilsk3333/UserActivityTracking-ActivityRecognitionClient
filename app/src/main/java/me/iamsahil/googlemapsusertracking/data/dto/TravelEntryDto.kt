package me.iamsahil.googlemapsusertracking.data.dto

data class TravelEntryDto(
    val savedAt: String = "",
    val trackPath: List<LatLng> = emptyList()
) {
    data class LatLng(
        val latitude: Double = 0.000,
        val longitude: Double = 0.0000
    )
}

