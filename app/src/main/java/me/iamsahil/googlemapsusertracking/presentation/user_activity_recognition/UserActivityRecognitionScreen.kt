package me.iamsahil.googlemapsusertracking.presentation.user_activity_recognition

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.RoundCap
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.iamsahil.googlemapsusertracking.utils.CUSTOM_INTENT_USER_ACTION
import me.iamsahil.googlemapsusertracking.utils.PermissionBox
import me.iamsahil.googlemapsusertracking.utils.bitmapDescriptor
import me.iamsahil.googlemapsusertracking.R
import me.iamsahil.googlemapsusertracking.domain.model.TravelEntry
import me.iamsahil.googlemapsusertracking.utils.UserActivityTransitionManager

@SuppressLint("MissingPermission")
@Composable
fun UserActivityRecognitionScreen(
    state: UserActivityRecognitionState,
    onEvent: (UserActivityRecognitionEvent) -> Unit
) {
    val activityPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        Manifest.permission.ACTIVITY_RECOGNITION
    } else {
        "com.google.android.gms.permission.ACTIVITY_RECOGNITION"
    }
    val permissions = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        activityPermission
    )

    PermissionBox(permissions = permissions) {
        UserActivityRecognitionContent(state, onEvent)
    }
}

@OptIn(ExperimentalMaterialApi::class)
@SuppressLint("InlinedApi")
@RequiresPermission(
    anyOf = [
        Manifest.permission.ACTIVITY_RECOGNITION,
        "com.google.android.gms.permission.ACTIVITY_RECOGNITION",
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    ],
)
@Composable
fun UserActivityRecognitionContent(
    state: UserActivityRecognitionState,
    onEvent: (UserActivityRecognitionEvent) -> Unit
) {
    val context = LocalContext.current
    val appContext = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()
    val manager = remember {
        UserActivityTransitionManager(context)
    }

    val singapore = LatLng(1.35, 103.87)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(singapore, 10f)
    }

    val bottomSheetState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

    // Calling deregister on dispose
    DisposableEffect(LocalLifecycleOwner.current) {
        onDispose {
            scope.launch(Dispatchers.IO) {
                manager.deregisterActivityTransitions()
            }
        }
    }

    LaunchedEffect(state.mapCameraBound){
        state.mapCameraBound?.let {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(it, 50))
        }
    }


    // Register a local broadcast to receive activity transition updates
    UserActivityBroadcastReceiver(systemAction = CUSTOM_INTENT_USER_ACTION) { userActivity, activityTransition ->
        onEvent(UserActivityRecognitionEvent.OnUserActivityChange(userActivity, activityTransition))
    }

    ModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetContent = {
            TravelEntriesBottomSheet(
                travelEntries = state.travelEntries, onTravelEntryClick = {

                onEvent(UserActivityRecognitionEvent.OnSelectTravelEntry(it))
                scope.launch {
                    bottomSheetState.hide()
                }
            })
        }) {
        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap (
                cameraPositionState = cameraPositionState
            ){

                if (state.isTracking) {

                    if (state.trackingPath.isNotEmpty()) {
                        Marker(
                            state = MarkerState(position = state.trackingPath[0]),
                            icon = context.bitmapDescriptor(R.drawable.start_line)
                        )
                        Marker(
                            state = MarkerState(position = state.trackingPath.last()),
                            icon = context.bitmapDescriptor(R.drawable.user)
                        )

                        Polyline(
                            points = state.trackingPath,
                            color = Color.Blue,
                            width = with(LocalDensity.current) { 4.dp.toPx() },
                            startCap = RoundCap(),
                            endCap = RoundCap()
                        )
                    }

                } else {
                    if (state.trackingPath.isNotEmpty()) {

                        Marker(
                            state = MarkerState(position = state.trackingPath[0]),
                            icon = context.bitmapDescriptor(R.drawable.start_line)
                        )

                        Marker(
                            state = MarkerState(position = state.trackingPath.last()),
                            icon = context.bitmapDescriptor(R.drawable.finish)
                        )

                        Polyline(
                            points = state.trackingPath,
                            color = Color.Blue,
                            width = with(LocalDensity.current) { 4.dp.toPx() },
                            startCap = RoundCap(),
                            endCap = RoundCap()
                        )

                    }
                }

            }

            AnimatedVisibility(visible = state.isTracking) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .background(
                            shape = RoundedCornerShape(100),
                            color = Color.Black.copy(alpha = .5f)
                        )
                        .padding(horizontal = 5.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Tracking")
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color.Green)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .animateContentSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Button(
                    onClick = {
                        scope.launch(Dispatchers.IO) {
                            manager.registerActivityTransitions()
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    appContext,
                                    "Registered for Activity Transitions",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    },
                ) {
                    Text(text = "Register for activity transition updates")
                }

                Button(
                    onClick = {
                        onEvent(UserActivityRecognitionEvent.OnDeregisterActivityTransitions)
                        scope.launch(Dispatchers.IO) {
                            manager.deregisterActivityTransitions()
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    appContext,
                                    "UnRegistered for Activity Transitions",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    },
                ) {
                    Text(text = "Deregister for activity transition updates")
                }


                Button(
                    onClick = {
                        scope.launch{
                            bottomSheetState.show()
                        }
                    },
                ) {
                    Text(text = "Show travel entries")
                }

                if (state.currentActivity.isNotBlank()) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .background(color = Color.Black.copy(alpha = .5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "CurrentActivity is = ${state.currentActivity}",
                            color = Color.White,
                        )
                    }

                }
            }
        }
    }


}

@Composable
fun TravelEntriesBottomSheet(
    travelEntries: List<TravelEntry>,
    onTravelEntryClick: (TravelEntry) -> Unit
) {

    if (travelEntries.isNotEmpty()){

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(travelEntries) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                        .clickable {
                            onTravelEntryClick(it)
                        }) {
                    Text(text = it.savedAt.toString())
                    Spacer(modifier = Modifier.height(3.dp))
                    Divider(Modifier.fillMaxWidth())
                }
            }
        }
    }else{
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(text = "No Entries Available")
        }
    }


}