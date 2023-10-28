package me.iamsahil.googlemapsusertracking.presentation


import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import me.iamsahil.googlemapsusertracking.domain.location.LocationClient
import me.iamsahil.googlemapsusertracking.presentation.theme.GoogleMapsUserTrackingTheme
import me.iamsahil.googlemapsusertracking.presentation.user_activity_recognition.UserActivityRecognitionScreen
import me.iamsahil.googlemapsusertracking.presentation.user_activity_recognition.UserActivityRecognitionViewModel
import me.iamsahil.googlemapsusertracking.utils.PermissionBox
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var locationClient: LocationClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val vm by viewModels<UserActivityRecognitionViewModel>()

        setContent {
            GoogleMapsUserTrackingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val state by vm.state.collectAsState()
                    UserActivityRecognitionScreen(
                        state = state,
                        onEvent = vm::onEvent
                    )
                }
            }

        }
    }
}





