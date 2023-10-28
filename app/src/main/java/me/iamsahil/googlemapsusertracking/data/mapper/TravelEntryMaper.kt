package me.iamsahil.googlemapsusertracking.data.mapper

import com.google.android.gms.maps.model.LatLng
import me.iamsahil.googlemapsusertracking.data.dto.TravelEntryDto
import me.iamsahil.googlemapsusertracking.domain.model.TravelEntry
import java.time.LocalDateTime

fun TravelEntryDto.toTravelEntry() = TravelEntry(
    savedAt = LocalDateTime.parse(savedAt),
    trackPath = trackPath.map { LatLng(it.latitude, it.longitude) }
)

fun TravelEntry.toTravelEntryDto() = TravelEntryDto(
    savedAt = savedAt.toString(),
    trackPath = trackPath.map { TravelEntryDto.LatLng(it.latitude,it.longitude) }
)