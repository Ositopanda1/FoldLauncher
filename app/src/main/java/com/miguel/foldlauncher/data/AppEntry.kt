package com.miguel.foldlauncher.data

import android.content.ComponentName
import android.graphics.drawable.Drawable

data class AppEntry(
    val label: String,
    val icon: Drawable,
    val component: ComponentName
)
