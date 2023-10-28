package me.iamsahil.googlemapsusertracking.data.firebase

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.snapshots
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import me.iamsahil.googlemapsusertracking.data.dto.TravelEntryDto
import javax.inject.Inject

class FirebaseRealtimeDataSource @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase
) {

    private val ref = firebaseDatabase.getReference("travelEntries")

    suspend fun saveTravelEntry(travelEntry: TravelEntryDto) {
        ref.push().setValue(travelEntry).await()
    }

    fun getTravelEntries(): Flow<List<TravelEntryDto>> {
        return callbackFlow {
            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val dataList = dataSnapshot.children.mapNotNull { it.getValue(TravelEntryDto::class.java) }
                    trySendBlocking(dataList)
                }

                override fun onCancelled(databaseError: DatabaseError) {

                    close(databaseError.toException())
                }
            }
            ref.addValueEventListener(valueEventListener)
            awaitClose {
                ref.removeEventListener(valueEventListener)
            }

        }

    }

}