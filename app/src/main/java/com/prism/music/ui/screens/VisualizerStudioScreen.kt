package com.prism.music.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.prism.music.data.prefs.VisualizerMode
import com.prism.music.data.prefs.VisualizerStyle
import com.prism.music.data.prefs.ScrubberColor
import com.prism.music.ui.LocalNavigator
import com.prism.music.ui.theme.LocalAppSettings
import com.prism.music.ui.theme.LocalContainer

private data class StudioEffect(val style: VisualizerStyle, val title: String)

private val basicEffects = listOf(
    StudioEffect(VisualizerStyle.BARS, "Bars"),
    StudioEffect(VisualizerStyle.WAVE, "Wave"),
    StudioEffect(VisualizerStyle.RING, "Ring"),
    StudioEffect(VisualizerStyle.GLOW, "Glow"),
)

private val extraEffects = listOf(
    StudioEffect(VisualizerStyle.PARTICLES, "Particles"),
    StudioEffect(VisualizerStyle.LIQUID, "Liquid"),
    StudioEffect(VisualizerStyle.GEOMETRIC, "Geometry"),
    StudioEffect(VisualizerStyle.SPECTRUM_360, "360° Spectrum"),
    StudioEffect(VisualizerStyle.FRACTAL, "Fractal"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisualizerStudioScreen(bottomPadding: Dp) {
    val settings = LocalAppSettings.current
    val prefs = LocalContainer.current.settings
    val nav = LocalNavigator.current
    var showMore by rememberSaveable { mutableStateOf(false) }
    var showColours by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Visualizer") },
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
            Modifier.fillMaxWidth().padding(insets)
                .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = bottomPadding + 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Make music move", style = MaterialTheme.typography.headlineSmall)
            Text("Choose one simple way to enjoy your music.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            ModeCard(
                title = "Auto",
                subtitle = "AURIX changes the visuals with the music",
                selected = settings.visualizerMode == VisualizerMode.AI,
                onClick = { prefs.setVisualizerMode(VisualizerMode.AI) }
            )
            ModeCard(
                title = "Manual",
                subtitle = "You choose one visual and keep it",
                selected = settings.visualizerMode == VisualizerMode.MANUAL,
                onClick = { prefs.setVisualizerMode(VisualizerMode.MANUAL) }
            )

            if (settings.visualizerMode == VisualizerMode.MANUAL) {
                HorizontalDivider()
                Text("Choose a visual", style = MaterialTheme.typography.titleMedium)
                basicEffects.chunked(2).forEach { pair ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        pair.forEach { effect ->
                            EffectCard(
                                effect = effect,
                                selected = settings.visualizer == effect.style,
                                onClick = { prefs.setVisualizer(effect.style) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (pair.size == 1) Box(Modifier.weight(1f))
                    }
                }
                Text(
                    if (showMore) "Fewer effects" else "More effects",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { showMore = !showMore }.padding(vertical = 4.dp)
                )
                if (showMore) {
                    extraEffects.chunked(2).forEach { pair ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            pair.forEach { effect ->
                                EffectCard(
                                    effect = effect,
                                    selected = settings.visualizer == effect.style,
                                    onClick = { prefs.setVisualizer(effect.style) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (pair.size == 1) Box(Modifier.weight(1f))
                        }
                    }
                }
            }

            HorizontalDivider()
            Row(
                Modifier.fillMaxWidth().clickable { showColours = !showColours }.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Colour", style = MaterialTheme.typography.titleMedium)
                    Text(
                        when (settings.visualizerColor) {
                            ScrubberColor.SPECTRUM -> "Spectrum"
                            ScrubberColor.ACCENT -> "Accent"
                            ScrubberColor.WHITE -> "White"
                            ScrubberColor.COVER -> "Album artwork"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(if (showColours) "Hide" else "Change", color = MaterialTheme.colorScheme.primary)
            }
            if (showColours) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        ScrubberColor.SPECTRUM to "Spectrum",
                        ScrubberColor.ACCENT to "Accent",
                        ScrubberColor.WHITE to "White",
                        ScrubberColor.COVER to "Album"
                    ).forEach { (color, label) ->
                        FilterChip(
                            selected = settings.visualizerColor == color,
                            onClick = { prefs.setVisualizerColor(color) },
                            label = { Text(label) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeCard(title: String, subtitle: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Column(Modifier.weight(1f).padding(start = 8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EffectCard(effect: StudioEffect, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(Modifier.fillMaxWidth().height(52.dp), contentAlignment = Alignment.Center) {
            EffectGlyph(effect.style)
        }
        Text(effect.title, style = MaterialTheme.typography.labelLarge, color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
        if (selected) Text("Selected", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun EffectGlyph(style: VisualizerStyle) {
    val color = MaterialTheme.colorScheme.primary
    Canvas(Modifier.fillMaxWidth().height(48.dp)) {
        val mid = size.height / 2f
        when (style) {
            VisualizerStyle.BARS -> {
                val heights = listOf(.25f, .55f, .9f, .62f, .38f, .78f, .45f)
                val barW = size.width / 16f
                heights.forEachIndexed { i, h ->
                    drawRoundRect(color, androidx.compose.ui.geometry.Offset(size.width * .28f + i * barW * 1.35f, mid - size.height * h / 2f), androidx.compose.ui.geometry.Size(barW, size.height * h), androidx.compose.ui.geometry.CornerRadius(barW))
                }
            }
            VisualizerStyle.WAVE -> {
                val path = Path()
                for (i in 0..40) {
                    val x = size.width * i / 40f
                    val y = mid + kotlin.math.sin(i * .42f) * size.height * .28f
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path, color, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
            }
            VisualizerStyle.RING -> {
                drawCircle(color, size.height * .30f, style = Stroke(3.dp.toPx()))
                for (i in 0..23) {
                    val a = i * (2f * Math.PI.toFloat() / 24f)
                    val r1 = size.height * .36f
                    val r2 = r1 + size.height * if (i % 3 == 0) .18f else .08f
                    drawLine(color, androidx.compose.ui.geometry.Offset(size.width / 2f + kotlin.math.cos(a) * r1, mid + kotlin.math.sin(a) * r1), androidx.compose.ui.geometry.Offset(size.width / 2f + kotlin.math.cos(a) * r2, mid + kotlin.math.sin(a) * r2), 2.dp.toPx(), cap = StrokeCap.Round)
                }
            }
            VisualizerStyle.GLOW -> {
                drawCircle(androidx.compose.ui.graphics.Brush.radialGradient(listOf(color.copy(alpha = .75f), color.copy(alpha = .03f), Color.Transparent), radius = size.height * .9f), size.height * .9f, center = androidx.compose.ui.geometry.Offset(size.width / 2f, mid))
            }
            else -> drawCircle(color, size.height * .22f, center = androidx.compose.ui.geometry.Offset(size.width / 2f, mid), style = Stroke(3.dp.toPx()))
        }
    }
}
