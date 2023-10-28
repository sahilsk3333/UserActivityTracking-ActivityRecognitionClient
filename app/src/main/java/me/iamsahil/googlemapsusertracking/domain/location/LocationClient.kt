package me.iamsahil.googlemapsusertracking.domain.location

import android.location.Location
import kotlinx.coroutines.flow.Flow

interface LocationClient {
    fun getLocationUpdates(interval: Long): Flow<Location>

    class LocationException(message: String) : Exception(message)

    suspend fun getCurrentLocation(location: (Location) -> Unit)
}