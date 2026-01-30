package com.miguel.foldlauncher.wallpaper

import android.app.WallpaperManager
import android.graphics.BitmapFactory
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import com.miguel.foldlauncher.data.WallpaperStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RotatorWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = RotatorEngine()

    private inner class RotatorEngine : Engine() {

        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        private var rotateJob: Job? = null

        // Rotation interval (milliseconds)
        private val intervalMs: Long = 30_000L

        private var index = 0

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            if (visible) startRotating() else stopRotating()
        }

        override fun onDestroy() {
            stopRotating()
            scope.cancel()
            super.onDestroy()
        }

        private fun startRotating() {
            if (rotateJob?.isActive == true) return

            rotateJob = scope.launch {
                // Apply immediately once
                applyNextWallpaper()

                // Then rotate
                while (isActive) {
                    delay(intervalMs)
                    applyNextWallpaper()
                }
            }
        }

        private fun stopRotating() {
            rotateJob?.cancel()
            rotateJob = null
        }

        private suspend fun applyNextWallpaper() {
            val ctx = applicationContext

            // Get the saved URIs from DataStore-backed flow
            val uris = try {
                withContext(Dispatchers.IO) {
                    WallpaperStore.observe(ctx).first()
                }
            } catch (_: Throwable) {
                emptyList()
            }

            if (uris.isEmpty()) return

            // Keep index in range even if list changes
            if (index >= uris.size) index = 0
            val uri = uris[index]
            index = (index + 1) % uris.size

            try {
                withContext(Dispatchers.IO) {
                    val wm = WallpaperManager.getInstance(ctx)

                    ctx.contentResolver.openInputStream(uri)?.use { input ->
                        val bitmap = BitmapFactory.decodeStream(input) ?: return@withContext
                        wm.setBitmap(bitmap)
                    }
                }
            } catch (_: Throwable) {
                // If a URI becomes invalid or permission is missing, skip quietly.
            }
        }
    }
}
