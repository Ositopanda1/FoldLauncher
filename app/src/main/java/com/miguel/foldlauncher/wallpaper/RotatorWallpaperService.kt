package com.miguel.foldlauncher.wallpaper

import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import kotlinx.coroutines.*
import java.io.InputStream

/**
 * MVP Live Wallpaper engine:
 * - Draws the most recently chosen image (later we’ll rotate a stored playlist)
 * - For now, it just draws a bundled placeholder if no image is available.
 */
class RotatorWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = RotatorEngine()

    inner class RotatorEngine : Engine() {
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        private var job: Job? = null

        override fun onVisibilityChanged(visible: Boolean) {
            if (visible) start() else stop()
        }

        override fun onDestroy() {
            stop()
            scope.cancel()
            super.onDestroy()
        }

        private fun start() {
            stop()
            job = scope.launch {
                while (isActive) {
                    drawFrame(surfaceHolder)
                    delay(15_000) // every 15 seconds (we’ll make this configurable)
                }
            }
        }

        private fun stop() {
            job?.cancel()
            job = null
        }

        private fun drawFrame(holder: SurfaceHolder) {
            val canvas: Canvas = holder.lockCanvas() ?: return
            try {
                // Placeholder: draw a solid fill-like bitmap if available.
                // (Next step: load from user-selected URIs + rotate)
                val bmp = runCatching {
                    // We don't have app resources set up for a drawable in this phone-only workflow.
                    // So we just draw nothing until we wire in the selected media list.
                    null
                }.getOrNull()

                if (bmp != null) {
                    val dest = Rect(0, 0, canvas.width, canvas.height)
                    canvas.drawBitmap(bmp, null, dest, null)
                } else {
                    // Clear frame (black). Later we draw selected images/videos.
                    canvas.drawRGB(0, 0, 0)
                }
            } finally {
                holder.unlockCanvasAndPost(canvas)
            }
        }
    }
}
