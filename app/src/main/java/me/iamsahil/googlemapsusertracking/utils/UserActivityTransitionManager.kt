package me.iamsahil.googlemapsusertracking.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionRequest
import com.google.android.gms.location.DetectedActivity
import kotlinx.coroutines.tasks.await

class UserActivityTransitionManager(context: Context) {

    companion object {
        fun getActivityType(int: Int): UserActivity {
            return when (int) {
                0 -> UserActivity.IN_VEHICLE
                1 -> UserActivity.ON_BICYCLE
                2 -> UserActivity.ON_FOOT
                3 -> UserActivity.STILL
                4 -> UserActivity.UNKNOWN
                5 -> UserActivity.TILTING
                7 -> UserActivity.WALKING
                8 -> UserActivity.RUNNING
                else -> UserActivity.UNKNOWN
            }
        }

        fun getTransitionType(int: Int): UserActivityTransition {
            return when (int) {
                0 -> UserActivityTransition.STARTED
                1 -> UserActivityTransition.STOOPED
                else -> UserActivityTransition.UNKNOWN
            }
        }
    }

    // list of activity transitions to be monitored
    private val activityTransitions: List<ActivityTransition> by lazy {
        listOf(
            getUserActivity(
                DetectedActivity.IN_VEHICLE, ActivityTransition.ACTIVITY_TRANSITION_ENTER
            ),
            getUserActivity(
                DetectedActivity.IN_VEHICLE, ActivityTransition.ACTIVITY_TRANSITION_EXIT
            ),
            getUserActivity(
                DetectedActivity.ON_BICYCLE, ActivityTransition.ACTIVITY_TRANSITION_ENTER
            ),
            getUserActivity(
                DetectedActivity.ON_BICYCLE, ActivityTransition.ACTIVITY_TRANSITION_EXIT
            ),
            getUserActivity(
                DetectedActivity.WALKING, ActivityTransition.ACTIVITY_TRANSITION_ENTER
            ),
            getUserActivity(
                DetectedActivity.WALKING, ActivityTransition.ACTIVITY_TRANSITION_EXIT
            ),
            getUserActivity(
                DetectedActivity.RUNNING, ActivityTransition.ACTIVITY_TRANSITION_ENTER
            ),
            getUserActivity(
                DetectedActivity.RUNNING, ActivityTransition.ACTIVITY_TRANSITION_EXIT
            ),
        )
    }

    private val activityClient = ActivityRecognition.getClient(context)

    private val pendingIntent by lazy {
        PendingIntent.getBroadcast(
            context,
            CUSTOM_REQUEST_CODE_USER_ACTION,
            Intent(CUSTOM_INTENT_USER_ACTION),
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                PendingIntent.FLAG_CANCEL_CURRENT
            } else {
                PendingIntent.FLAG_MUTABLE
            }
        )
    }

    private fun getUserActivity(detectedActivity: Int, transitionType: Int): ActivityTransition {
        return ActivityTransition.Builder().setActivityType(detectedActivity)
            .setActivityTransition(transitionType).build()

    }

    @SuppressLint("InlinedApi")
    @RequiresPermission(
        anyOf = [
            Manifest.permission.ACTIVITY_RECOGNITION,
            "com.google.android.gms.permission.ACTIVITY_RECOGNITION"
        ]
    )
    suspend fun registerActivityTransitions() = kotlin.runCatching {
        activityClient.requestActivityTransitionUpdates(
            ActivityTransitionRequest(
                activityTransitions
            ), pendingIntent
        ).await()
    }

    @SuppressLint("InlinedApi")
    @RequiresPermission(
        anyOf = [
            Manifest.permission.ACTIVITY_RECOGNITION,
            "com.google.android.gms.permission.ACTIVITY_RECOGNITION"
        ]
    )
    suspend fun deregisterActivityTransitions() = kotlin.runCatching {
        activityClient.removeActivityUpdates(pendingIntent).await()
    }
}

enum class UserActivity {
    IN_VEHICLE,
    ON_BICYCLE,
    ON_FOOT,
    STILL,
    UNKNOWN,
    TILTING,
    WALKING,
    RUNNING
}

enum class UserActivityTransition { STARTED, STOOPED, UNKNOWN }
