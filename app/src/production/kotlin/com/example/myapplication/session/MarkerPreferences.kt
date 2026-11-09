package com.example.myapplication.session

data class MarkerPreference(val speedStep: Int, val rangeStep: Int) {
    init { require(speedStep in 0..4 && rangeStep in 0..4) }
}

/** Two values written together; only explicit user saves are persisted. */
class MarkerPreferences(private val storage: CompletionStorage) {
    companion object { private val lock = Any() }

    fun read(): MarkerPreference? = synchronized(lock) {
        val value = storage.read()
        if (value.isEmpty()) return@synchronized null
        val parts = value.split(',')
        require(parts.size == 3 && parts[0] == "v1") { "Invalid marker settings" }
        MarkerPreference(parts[1].toInt(), parts[2].toInt())
    }

    fun save(value: MarkerPreference) = synchronized(lock) {
        check(storage.write("v1,${value.speedStep},${value.rangeStep}")) { "Settings save failed" }
    }

    fun clear() = synchronized(lock) {
        check(storage.write("")) { "Settings deletion failed" }
    }
}
