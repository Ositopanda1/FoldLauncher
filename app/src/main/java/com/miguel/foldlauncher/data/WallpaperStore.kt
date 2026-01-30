package com.miguel.foldlauncher.data

import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("wallpaper_store")

object WallpaperStore {

    private val WALLPAPER_URIS =
        stringSetPreferencesKey("wallpaper_uris")

    fun observe(context: Context): Flow<List<Uri>> =
        context.dataStore.data.map { prefs ->
            prefs[WALLPAPER_URIS]
                ?.map { Uri.parse(it) }
                ?: emptyList()
        }

    suspend fun save(context: Context, uris: List<Uri>) {
        context.dataStore.edit { prefs ->
            prefs[WALLPAPER_URIS] = uris.map { it.toString() }.toSet()
        }
    }
}
