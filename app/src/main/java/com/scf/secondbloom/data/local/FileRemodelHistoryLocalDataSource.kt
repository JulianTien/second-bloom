package com.scf.secondbloom.data.local

import android.content.Context
import android.util.AtomicFile
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

internal class FileRemodelHistoryLocalDataSource(
    context: Context,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }
) : RemodelHistoryLocalDataSource {

    private val atomicFile = AtomicFile(File(context.filesDir, HistoryFileName))
    private val ioMutex = Mutex()

    override suspend fun readSnapshot(): RemodelHistorySnapshot = withContext(Dispatchers.IO) {
        ioMutex.withLock {
            val file = atomicFile.baseFile
            if (!file.exists()) {
                return@withLock RemodelHistorySnapshot()
            }

            runCatching {
                val rawBytes = atomicFile.readFully()
                val content = rawBytes.decodeToString()
                json.decodeFromString(RemodelHistorySnapshot.serializer(), content)
            }.getOrElse {
                RemodelHistorySnapshot()
            }
        }
    }

    override suspend fun writeSnapshot(snapshot: RemodelHistorySnapshot) = withContext(Dispatchers.IO) {
        ioMutex.withLock {
            val payload = json.encodeToString(RemodelHistorySnapshot.serializer(), snapshot)
            val output = atomicFile.startWrite()
            try {
                output.write(payload.encodeToByteArray())
                output.flush()
                atomicFile.finishWrite(output)
            } catch (error: Throwable) {
                atomicFile.failWrite(output)
                throw error
            }
        }
    }

    private companion object {
        const val HistoryFileName = "remodel_history.json"
    }
}
