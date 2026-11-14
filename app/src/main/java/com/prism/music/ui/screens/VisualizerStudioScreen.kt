package com.prism.music.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.prism.music.data.prefs.VisualizerMode
import com.prism.music.data.prefs.VisualizerStyle
import com.prism.music.data.prefs.ScrubberColor
import com.prism.music.ui.LocalNavigator
import com.prism.music.ui.theme.LocalAppSettings
import com.prism.music.ui.theme.LocalContainer

private data class StudioEffect(
    val style: VisualizerStyle,
    val title: String,
    val description: String,
)

private val studioEffects = listOf(
    StudioEffect(VisualizerStyle.BARS, "Bars", "Equalizer bars rise with the beat"),
    StudioEffect(VisualizerStyle.WAVE, "Wave", "A smooth wave follows the music"),
    StudioEffect(VisualizerStyle.RING, "Ring", "A circular effect around the artwork"),
    StudioEffect(VisualizerStyle.GLOW, "Glow", "The background softly lights up"),
    StudioEffect(VisualizerStyle.PARTICLES, "Particles", "Floating dots move with the rhythm"),
    StudioEffect(VisualizerStyle.LIQUID, "Liquid", "Flowing colour waves react to sound"),
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun VisualizerStudioScreen(bottomPadding: Dp) {
    val settings = LocalAppSettings.current
    val prefs = LocalContainer.current.settings
    val nav = LocalNavigator.current
    var showColours by rememberSaveable { mutableStateOf(false) }

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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("How should your music look?", style = MaterialTheme.typography.titleLarge)
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = settings.visualizerMode == VisualizerMode.AI,
                    onClick = { prefs.setVisualizerMode(VisualizerMode.AI) }
                )
                Column(Modifier.weight(1f).clickable { prefs.setVisualizerMode(VisualizerMode.AI) }) {
                    Text("Auto", style = MaterialTheme.typography.titleMedium)
                    Text("AURIX changes the effect with the music", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = settings.visualizerMode == VisualizerMode.MANUAL,
                    onClick = { prefs.setVisualizerMode(VisualizerMode.MANUAL) }
                )
                Column(Modifier.weight(1f).clickable { prefs.setVisualizerMode(VisualizerMode.MANUAL) }) {
                    Text("Choose an effect", style = MaterialTheme.typography.titleMedium)
                    Text("Keep one effect while you listen", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (settings.visualizerMode == VisualizerMode.MANUAL) {
                HorizontalDivider()
                Text("Choose one", style = MaterialTheme.typography.titleMedium)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    studioEffects.forEach { effect ->
                        Row(
                            Modifier.fillMaxWidth()
                                .clickable { prefs.setVisualizer(effect.style) }
                                .padding(vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = settings.visualizer == effect.style,
                                onClick = { prefs.setVisualizer(effect.style) }
                            )
                            Column(Modifier.weight(1f).padding(start = 8.dp)) {
                                Text(effect.title, style = MaterialTheme.typography.bodyLarge)
                                Text(effect.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            } else {
                Text(
                    "No need to choose anything. AURIX will select effects automatically.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider()
            Row(
                Modifier.fillMaxWidth().clickable { showColours = !showColours }.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Colours", style = MaterialTheme.typography.titleMedium)
                    Text(
                        when (settings.visualizerColor) {
                            ScrubberColor.SPECTRUM -> "Your light"
                            ScrubberColor.ACCENT -> "Accent colour"
                            ScrubberColor.WHITE -> "White"
                            ScrubberColor.COVER -> "Match album artwork"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(if (showColours) "Hide" else "Change", color = MaterialTheme.colorScheme.primary)
            }
            if (showColours) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ScrubberColor.entries.forEach { color ->
                        FilterChip(
                            selected = settings.visualizerColor == color,
                            onClick = { prefs.setVisualizerColor(color) },
                            label = {
                                Text(when (color) {
                                    ScrubberColor.SPECTRUM -> "Your light"
                                    ScrubberColor.ACCENT -> "Accent"
                                    ScrubberColor.WHITE -> "White"
                                    ScrubberColor.COVER -> "Album art"
                                })
                            }
                        )
                    }
                }
            }
        }
    }
}
