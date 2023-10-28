package me.iamsahil.googlemapsusertracking.presentation.user_activity_recognition

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.iamsahil.googlemapsusertracking.domain.location.LocationClient
import me.iamsahil.googlemapsusertracking.domain.repository.UserTrackRepository
import me.iamsahil.googlemapsusertracking.utils.UserActivity
import me.iamsahil.googlemapsusertracking.utils.UserActivityTransition
import java.lang.IndexOutOfBoundsException
import javax.inject.Inject

@HiltViewModel
class UserActivityRecognitionViewModel @Inject constructor(
    private val locationClient: LocationClient,
    private val userTrackRepository: UserTrackRepository
) : ViewModel() {

    private val _state = MutableStateFlow(UserActivityRecognitionState())
    val state = _state.asStateFlow()

    private var trackingJob: Job? = null

    fun onEvent(event: UserActivityRecognitionEvent) {
        when (event) {
            is UserActivityRecognitionEvent.OnUserActivityChange -> {
                _state.update {
                    it.copy(
                        currentActivity = "${event.userActivity} : ${event.userActivityTransition}"
                    )
                }
                handleOnUserActivityChange(
                    event.userActivity,
                    event.userActivityTransition
                )
            }

            UserActivityRecognitionEvent.OnDeregisterActivityTransitions -> _state.update {
                it.copy(
                    currentActivity = ""
                )
            }

            is UserActivityRecognitionEvent.OnSelectTravelEntry -> _state.update {

                val bounds = try {
                    LatLngBounds.Builder()
                        .include(
                            event.travelEntry.trackPath[0]
                        )
                        .include(
                            event.travelEntry.trackPath.last()
                        )
                        .build()
                } catch (e: IndexOutOfBoundsException) {
                    null
                }

                it.copy(
                    trackingPath = event.travelEntry.trackPath,
                    mapCameraBound = bounds
                )
            }
        }
    }

    init {
        userTrackRepository.getTravelEntries().map { travelEntries ->
            _state.update {
                it.copy(
                    travelEntries = travelEntries
                )
            }
            Log.e("UserActivityRecognitionViewModel", "All travel Entries : $travelEntries")
        }.launchIn(viewModelScope)
    }

    private fun handleOnUserActivityChange(
        userActivity: UserActivity,
        userActivityTransition: UserActivityTransition
    ) {

        when (userActivityTransition) {
            UserActivityTransition.STARTED -> {
                when (userActivity) {
                    UserActivity.IN_VEHICLE, UserActivity.ON_BICYCLE, UserActivity.WALKING, UserActivity.RUNNING -> {
                        // user start moving
                        startTracking()
                    }
                    else -> Unit
                }
            }

            UserActivityTransition.STOOPED -> {
                when (userActivity) {
                    UserActivity.IN_VEHICLE, UserActivity.ON_BICYCLE, UserActivity.WALKING, UserActivity.RUNNING -> {
                        // user stooped moving
                        stopTracking()
                    }
                    else -> Unit
                }
            }

            UserActivityTransition.UNKNOWN -> Unit
        }

    }

    private fun startTracking() {
        _state.update {
            it.copy(
                isTracking = true,
                trackingPath = emptyList()
            )
        }
        trackingJob = viewModelScope.launch {
            locationClient.getLocationUpdates(1000)
                .cancellable()
                .catch { it.printStackTrace() }
                .collectLatest { location ->

                    val bounds = try {
                        LatLngBounds.Builder()
                            .include(
                                state.value.trackingPath[0]
                            )
                            .include(
                                LatLng(
                                    location.latitude,
                                    location.longitude
                                )
                            )
                            .build()
                    } catch (e: IndexOutOfBoundsException) {
                        null
                    }

                    _state.update {
                        it.copy(
                            trackingPath = it.trackingPath + LatLng(
                                location.latitude,
                                location.longitude
                            ),
                            mapCameraBound = bounds
                        )
                    }

                }
        }
    }

    private fun stopTracking() {
        trackingJob?.cancel()
        saveTrackEvent()
        _state.update {
            it.copy(
                isTracking = false,
                trackingPath = emptyList()
            )
        }
    }

    private fun saveTrackEvent() {
        viewModelScope.launch {
            userTrackRepository.saveTravelEntry(
                trackPath = state.value.trackingPath
            )
        }
    }

}