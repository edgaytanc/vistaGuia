package com.vistamed.mgp.vistamedmvp.ui

/** Controla cooldown por clave (por ejemplo, etiqueta/objetivo). */
class AnnounceThrottlerPerKey(private val intervalMs: Long) {
    private val last = HashMap<String, Long>()

    @Synchronized
    fun shouldAnnounce(key: String): Boolean {
        val now = System.currentTimeMillis()
        val prev = last[key] ?: 0L
        return if (now - prev >= intervalMs) {
            last[key] = now
            true
        } else false
    }
}
