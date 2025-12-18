package org.reas.tracker.firebase

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.runBlocking
import org.reas.tracker.AppDataContainer
import org.reas.tracker.database.Event

class FirestoreCloudSave(private val container: AppDataContainer) {
    private val db = Firebase.firestore
    private val queue = mutableListOf<Event>()
    private var listener: ListenerRegistration? = null
    private var userId: String? = null

    private fun eventsCollection(id: String) =
        db.collection("user")
            .document(id)
            .collection("events")

    fun setId(id: String) {
        userId = id
    }

    fun submitEvent(event: Event) {
        val id = userId
        if (id == null) {
            Log.w(TAG, "event ${event.id} not submitted due to not being logged in")
            queue.add(event)
            return
        }
        eventsCollection(id)
            .document(event.id)
            .set(event.asMap())
            .addOnCompleteListener {
                Log.d(TAG, "event ${event.id} successfully sent to firestore")
            }.addOnFailureListener {
                Log.w(TAG, "event ${event.id} failed to save to firestore, adding to queue")
                synchronized(this) {
                    queue.add(event)
                }
            }
    }

    fun submitBatchEvents() {
        val id = userId
        if (id == null) {
            Log.w(TAG, "batch submit failed due to not being logged in")
            return
        }
        val batch = synchronized(this) {
            queue.toTypedArray().also { queue.clear() }
        }
        if (batch.isEmpty()) return
        Log.i(TAG, "submitting batch of ${batch.size} events")
        batch.forEach { event -> submitEvent(event) }
    }

    fun trackRemoteEvents() {
        val id = userId ?: return
        listener = eventsCollection(id).addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Listen failed.", error)
                return@addSnapshotListener
            }
            if (snapshot == null) {
                Log.w(TAG, "snapshot is null")
                return@addSnapshotListener
            }

            val batch = snapshot
                .documentChanges
                .filter { change ->
                    !change.document.metadata.hasPendingWrites() &&
                    change.type == DocumentChange.Type.ADDED
                }
                .also { Log.i(TAG, "syncing a batch of ${it.size} events")}
                .map { Event.fromMap(it.document.data) }
            runBlocking {
                container.eventProcessor.feedBatch(batch, sync = true)
            }
        }
    }

    fun onSignOut() {
        listener?.remove()
        userId = null
        listener = null
    }

    companion object {
        private const val TAG = "FirestoreCloudSave"
    }
}