package me.iamsahil.googlemapsusertracking.domain.repository

import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow
import me.iamsahil.googlemapsusertracking.domain.model.TravelEntry
import me.iamsahil.googlemapsusertracking.utils.SimpleResource

interface UserTrackRepository {

    suspend fun saveTravelEntry(
        trackPath: List<LatLng>,
    ): SimpleResource

    fun getTravelEntries(): Flow<List<TravelEntry>>

}