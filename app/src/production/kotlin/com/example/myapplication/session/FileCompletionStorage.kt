package com.example.myapplication.session

import android.content.Context
import android.util.AtomicFile
import java.io.File
import java.io.FileNotFoundException

/** Production atomic ledger storage in app-private, backup-excluded files directory. */
class FileCompletionStorage(context: Context, fileName: String = "guided_sessions_v1.txt") : CompletionStorage {
    private val atomicFile = AtomicFile(File(context.filesDir, fileName))

    companion object {
        private var sharedRepository: CompletionRepository? = null

        /** All Activity instances must append through the same serialized ledger. */
        @Synchronized
        fun repository(context: Context): CompletionRepository =
            sharedRepository ?: CompletionRepository(FileCompletionStorage(context.applicationContext))
                .also { sharedRepository = it }
    }

    override fun read(): String = try {
        atomicFile.readFully().toString(Charsets.UTF_8)
    } catch (error: FileNotFoundException) {
        if (atomicFile.baseFile.exists()) throw error
        ""
    }

    override fun write(value: String): Boolean {
        val stream = atomicFile.startWrite()
        return try {
            stream.write(value.toByteArray(Charsets.UTF_8))
            atomicFile.finishWrite(stream)
            atomicFile.readFully().toString(Charsets.UTF_8) == value
        } catch (error: Exception) {
            atomicFile.failWrite(stream)
            throw error
        }
    }
}
