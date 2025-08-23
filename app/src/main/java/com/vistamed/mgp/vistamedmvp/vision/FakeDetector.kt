package com.vistamed.mgp.vistamedmvp.vision

class FakeDetector : Detector {
    override fun detectRGB888(width: Int, height: Int, pixels: IntArray): List<Detection> {
        // Esqueleto vacío: sin modelo aún.
        return emptyList()
    }
}
