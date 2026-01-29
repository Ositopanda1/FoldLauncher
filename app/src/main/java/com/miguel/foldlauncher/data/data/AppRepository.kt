package com.miguel.foldlauncher.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent

object AppRepository {

    fun loadLaunchableApps(context: Context): List<AppEntry> {
        val pm = context.packageManager

        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        return pm.queryIntentActivities(intent, 0)
            .map { ri ->
                AppEntry(
                    label = ri.loadLabel(pm).toString(),
                    icon = ri.loadIcon(pm),
                    component = ComponentName(
                        ri.activityInfo.packageName,
                        ri.activityInfo.name
                    )
                )
            }
            .sortedBy { it.label.lowercase() }
    }
}
