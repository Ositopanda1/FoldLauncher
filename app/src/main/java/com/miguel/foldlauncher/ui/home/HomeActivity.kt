package com.miguel.foldlauncher.ui.home

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.miguel.foldlauncher.data.AppEntry
import com.miguel.foldlauncher.data.AppRepository
import com.miguel.foldlauncher.ui.wallpaper.WallpaperStudio

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                HomeScreen()
            }
        }
    }
}

@Composable
private fun HomeScreen() {
    val context = LocalContext.current
    val apps = remember { AppRepository.loadLaunchableApps(context) }

    var showDrawer by remember { mutableStateOf(false) }
    var screen by remember { mutableStateOf("home") }

    Surface(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {

            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("FoldLauncher", style = MaterialTheme.typography.titleLarge)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { screen = "wallpaper" }) { Text("Wallpaper") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { showDrawer = !showDrawer }) {
                        Text(if (showDrawer) "Close" else "Apps")
                    }
                }
            }

            if (screen == "wallpaper") {
                WallpaperStudio(onBack = { screen = "home" })
                return@Column
            }

            if (!showDrawer) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Launcher skeleton is running.\nTap Apps to open app drawer.")
                }
            } else {
                AppDrawer(
                    apps = apps,
                    onLaunch = { app ->
                        val launchIntent = Intent().apply {
                            component = app.component
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(launchIntent)
                    }
                )
            }
        }
    }
}

@Composable
private fun AppDrawer(
    apps: List<AppEntry>,
    onLaunch: (AppEntry) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)
    ) {
        items(apps.size) { idx ->
            val app = apps[idx]
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                onClick = { onLaunch(app) }
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AndroidView(
                        factory = { android.widget.ImageView(it).apply { setImageDrawable(app.icon) } },
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(app.label, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
