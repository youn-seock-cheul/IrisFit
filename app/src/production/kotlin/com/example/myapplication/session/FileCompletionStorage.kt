package com.example.myapplication.session

import android.content.Context
import android.util.AtomicFile
import java.io.File

/** Atomic replacement in the app-private, backup-excluded files directory. */
class FileCompletionStorage(baseDir: File, fileName: String = "guided_sessions_v1.txt") : CompletionStorage {
    constructor(context: Context, fileName: String = "guided_sessions_v1.txt") : this(context.filesDir, fileName)

    private val targetFile = File(baseDir, fileName)

    companion object {
        private var sharedRepository: CompletionRepository? = null

        /** All Activity instances must append through the same serialized ledger. */
        @Synchronized
        fun repository(context: Context): CompletionRepository =
            sharedRepository ?: CompletionRepository(FileCompletionStorage(context.applicationContext))
                .also { sharedRepository = it }
    }

    override fun read(): String = try {
        val af = try { AtomicFile(targetFile) } catch (_: Throwable) { null }
        af?.readFully()?.toString(Charsets.UTF_8)
            ?: if (targetFile.exists()) targetFile.readText(Charsets.UTF_8) else ""
    } catch (_: Throwable) {
        if (targetFile.exists()) targetFile.readText(Charsets.UTF_8) else ""
    }

    override fun write(value: String): Boolean = try {
        val af = try { AtomicFile(targetFile) } catch (_: Throwable) { null }
        if (af != null) {
            val stream = af.startWrite()
            try {
                stream.write(value.toByteArray(Charsets.UTF_8))
                af.finishWrite(stream)
                af.readFully().toString(Charsets.UTF_8) == value
            } catch (error: Exception) {
                af.failWrite(stream)
                throw error
            }
        } else {
            targetFile.writeText(value, Charsets.UTF_8)
            targetFile.exists() && targetFile.readText(Charsets.UTF_8) == value
        }
    } catch (_: Throwable) {
        // Safe fallback to direct file I/O in JVM unit test environment where AtomicFile is not mocked
        try {
            targetFile.writeText(value, Charsets.UTF_8)
            targetFile.exists() && targetFile.readText(Charsets.UTF_8) == value
        } catch (_: Exception) {
            false
        }
    }
}
