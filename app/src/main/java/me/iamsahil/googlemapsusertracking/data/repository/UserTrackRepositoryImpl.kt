package me.iamsahil.googlemapsusertracking.data.repository

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.iamsahil.googlemapsusertracking.data.firebase.FirebaseRealtimeDataSource
import me.iamsahil.googlemapsusertracking.data.mapper.toTravelEntry
import me.iamsahil.googlemapsusertracking.data.mapper.toTravelEntryDto
import me.iamsahil.googlemapsusertracking.domain.model.TravelEntry
import me.iamsahil.googlemapsusertracking.domain.repository.UserTrackRepository
import me.iamsahil.googlemapsusertracking.utils.Resource
import me.iamsahil.googlemapsusertracking.utils.SimpleResource
import java.time.LocalDateTime

class UserTrackRepositoryImpl(
    private val firebaseRealtimeDataSource: FirebaseRealtimeDataSource
) : UserTrackRepository {


    override suspend fun saveTravelEntry(trackPath: List<LatLng>): SimpleResource {
        return try {
            firebaseRealtimeDataSource.saveTravelEntry(
                TravelEntry(
                    trackPath,
                    LocalDateTime.now()
                ).toTravelEntryDto()
            )
            Log.e("UserTrackRepositoryImpl", "Track path added $trackPath")
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message)
        }
    }

    override fun getTravelEntries(): Flow<List<TravelEntry>> =
        firebaseRealtimeDataSource.getTravelEntries().map { it.map { it.toTravelEntry() } }

}