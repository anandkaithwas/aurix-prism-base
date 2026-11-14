package com.prism.music.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.prism.music.data.prefs.VisualizerMode
import com.prism.music.data.prefs.VisualizerStyle
import com.prism.music.data.prefs.ScrubberColor
import com.prism.music.ui.LocalNavigator
import com.prism.music.ui.theme.LocalAppSettings
import com.prism.music.ui.theme.LocalContainer

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun VisualizerStudioScreen(bottomPadding: Dp) {
    val settings = LocalAppSettings.current
    val prefs = LocalContainer.current.settings
    val nav = LocalNavigator.current
    var advanced by rememberSaveable { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Visualizer Studio") },
                navigationIcon = {
                    IconButton(onClick = { nav.back() }) {
                        Icon(com.prism.music.ui.theme.PrismIcons.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { insets ->
        Column(
            Modifier.fillMaxWidth().padding(insets).padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text("Make your music come alive", style = MaterialTheme.typography.titleMedium)
            Text("Choose how AURIX should react to your music.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("MODE", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = settings.visualizerMode == VisualizerMode.AI,
                        onClick = { prefs.setVisualizerMode(VisualizerMode.AI) },
                        label = { Text("Auto") }
                    )
                    FilterChip(
                        selected = settings.visualizerMode == VisualizerMode.MANUAL,
                        onClick = { prefs.setVisualizerMode(VisualizerMode.MANUAL) },
                        label = { Text("Manual") }
                    )
                }
            }
            if (settings.visualizerMode == VisualizerMode.MANUAL) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("VISUAL EFFECT", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        VisualizerStyle.entries.filter { it != VisualizerStyle.OFF }.forEach { style ->
                            FilterChip(
                                selected = settings.visualizer == style,
                                onClick = { prefs.setVisualizer(style) },
                                label = { Text(style.label) }
                            )
                        }
                    }
                }
            } else {
                Text("AURIX will select and transition effects automatically as the music changes.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("COLOUR", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ScrubberColor.entries.forEach { color ->
                        FilterChip(
                            selected = settings.visualizerColor == color,
                            onClick = { prefs.setVisualizerColor(color) },
                            label = { Text(color.label) }
                        )
                    }
                }
            }
            HorizontalDivider()
            FilterChip(
                selected = advanced,
                onClick = { advanced = !advanced },
                label = { Text(if (advanced) "Hide advanced settings" else "Advanced settings") }
            )
            if (advanced) {
                Text("Advanced tuning will be added here. Your existing visualizer modes and colour preferences remain available in this preview build.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Tip: choose Auto for hands-free effect changes, or Manual to lock one effect.", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
