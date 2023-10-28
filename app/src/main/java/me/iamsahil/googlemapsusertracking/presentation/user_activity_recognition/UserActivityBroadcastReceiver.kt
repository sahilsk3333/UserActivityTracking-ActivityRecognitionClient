package me.iamsahil.googlemapsusertracking.presentation.user_activity_recognition

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.ActivityTransitionResult
import me.iamsahil.googlemapsusertracking.utils.UserActivity
import me.iamsahil.googlemapsusertracking.utils.UserActivityTransition
import me.iamsahil.googlemapsusertracking.utils.UserActivityTransitionManager.Companion.getActivityType
import me.iamsahil.googlemapsusertracking.utils.UserActivityTransitionManager.Companion.getTransitionType

@Composable
fun UserActivityBroadcastReceiver(
    systemAction: String,
    systemEvent: (userActivity: UserActivity, activityTransition: UserActivityTransition) -> Unit,
) {
    val context = LocalContext.current
    val currentSystemOnEvent by rememberUpdatedState(systemEvent)

    DisposableEffect(context, systemAction) {
        val intentFilter = IntentFilter(systemAction)
        val broadcast = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val result = intent?.let { ActivityTransitionResult.extractResult(it) } ?: return
                var userActivity: UserActivity? = null
                var activityTransition: UserActivityTransition? = null
                for (event in result.transitionEvents) {
                    userActivity = getActivityType(event.activityType)
                    activityTransition = getTransitionType(event.transitionType)
                }
                Log.d(
                    "UserActivityReceiver",
                    "onReceive: ${userActivity?.name} , ${activityTransition?.name}"
                )
                if (userActivity != null && activityTransition != null) {
                    currentSystemOnEvent(userActivity, activityTransition)
                }

            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(broadcast, intentFilter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(broadcast, intentFilter)
        }

        onDispose {
            context.unregisterReceiver(broadcast)
        }
    }
}