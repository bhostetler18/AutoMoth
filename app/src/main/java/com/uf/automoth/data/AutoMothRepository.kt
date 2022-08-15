package com.uf.automoth.data

import android.content.Context
import android.location.Location
import android.util.Log
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.OffsetDateTime

object AutoMothRepository {

    private lateinit var imageDatabase: ImageDatabase
    private lateinit var coroutineScope: CoroutineScope
    lateinit var storageLocation: File

    operator fun invoke(context: Context, coroutineScope: CoroutineScope) {
        if (this::imageDatabase.isInitialized) {
            return
        }
        imageDatabase = Room.databaseBuilder(
            context,
            ImageDatabase::class.java,
            "image-db"
        ).fallbackToDestructiveMigration() // TODO: remove when ready to release
            .build()
        storageLocation = context.getExternalFilesDir(null)!! // TODO: handle ejection?
        this.coroutineScope = coroutineScope
        Log.d("[INFO]", "External file path is ${storageLocation.path}")
    }

    val allSessionsFlow by lazy {
        imageDatabase.sessionDAO().getAllSessions()
    }

    // TODO: indicate filesystem errors in below functions either by exception or return

    suspend fun create(session: Session): Long? {
        val sessionDir = File(storageLocation, session.directory)
        val created = withContext(Dispatchers.IO) {
            return@withContext sessionDir.mkdir()
        }
        if (created) {
            session.sessionID = imageDatabase.sessionDAO().insert(session)
            return session.sessionID
        }
        return null
    }

    fun delete(session: Session) = coroutineScope.launch {
        val sessionDir = File(storageLocation, session.directory)
        val deleted = withContext(Dispatchers.IO) {
            return@withContext sessionDir.deleteRecursively()
        }
        if (deleted) {
            imageDatabase.sessionDAO().delete(session)
        }
    }

    fun insert(image: Image) = coroutineScope.launch {
        image.imageID = imageDatabase.imageDAO().insert(image)
    }

    fun delete(image: Image) = coroutineScope.launch {
        val session = imageDatabase.sessionDAO().getSession(image.parentSessionID) ?: return@launch
        val sessionDir = File(storageLocation, session.directory)
        val imagePath = File(sessionDir, image.filename)

        val deleted = withContext(Dispatchers.IO) {
            return@withContext imagePath.delete()
        }

        if (deleted) {
            imageDatabase.imageDAO().delete(image)
        }
    }

    suspend fun getSession(id: Long): Session? = imageDatabase.sessionDAO().getSession(id)
    suspend fun getImage(id: Long): Image? = imageDatabase.imageDAO().getImage(id)

    fun getImagesInSession(id: Long): Flow<List<Image>> {
        return imageDatabase.sessionDAO().getImagesInSession(id)
    }

    fun getImagesInSessionBlocking(id: Long): List<Image> {
        return imageDatabase.sessionDAO().getImagesInSessionBlocking(id)
    }

    fun getNumImagesInSession(id: Long): Flow<Int> {
        return imageDatabase.sessionDAO().getNumImagesInSession(id)
    }

    fun updateSessionLocation(id: Long, location: Location) = coroutineScope.launch {
        imageDatabase.sessionDAO().updateSessionLocation(id, location.latitude, location.longitude)
    }

    fun updateSessionCompletion(id: Long, time: OffsetDateTime) = coroutineScope.launch {
        imageDatabase.sessionDAO().updateSessionCompletion(id, time)
    }

    fun renameSession(id: Long, name: String) = coroutineScope.launch {
        imageDatabase.sessionDAO().renameSession(id, name)
    }

    fun resolve(session: Session): File {
        return File(storageLocation, session.directory)
    }

    fun resolve(image: Image, session: Session): File {
        return File(resolve(session), image.filename)
    }
}
