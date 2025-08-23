package com.vistamed.mgp.vistamedmvp.ui

class AnnounceThrottler(private val intervalMs: Long) {
    @Volatile private var last = 0L
    fun shouldAnnounce(): Boolean {
        val now = System.currentTimeMillis()
        return if (now - last >= intervalMs) { last = now; true } else false
    }
}
