package com.prism.music.ui.player

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.FloatState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import com.prism.music.playback.Spectrum
import com.prism.music.playback.VisualizerTap
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * What the visualizer shows this frame (1.7.4): [bands] (0–1, low to high) and [level] (the beat). Drawing code reads
 * [frame] (or [level]) inside its draw block, so a new frame redraws only the visualizer, never the player around it.
 */
class VisualizerState {
    internal val spectrum = Spectrum()
    var autoStyle: com.prism.music.data.prefs.VisualizerStyle by mutableStateOf(com.prism.music.data.prefs.VisualizerStyle.GEOMETRIC)
    var presetIndex: Int = 0
    val bands: FloatArray get() = spectrum.bands
    internal val frameState = mutableLongStateOf(0L)
    internal val levelState = mutableFloatStateOf(0f)
    val frame: Long get() = frameState.longValue
    val level: FloatState get() = levelState
}

/** The player's visualizer while it runs, for the parts drawn deeper in (the progress bar's Pulse). */
val LocalVisualizer = staticCompositionLocalOf<VisualizerState?> { null }

/** The beat (0–1) for the progress bar's Pulse, or null when no visualizer runs. */
@Composable
fun rememberVisualizerLevel(): FloatState? = LocalVisualizer.current?.level

/**
 * A visualizer that runs while [running] (the player shown, a style picked, Reduce motion off): it listens to the
 * tap and updates once a frame. The frame clock stops with the screen, and leaving composition stops it all.
 */
@Composable
fun rememberVisualizer(running: Boolean): VisualizerState? {
    if (!running) return null
    val state = remember { VisualizerState() }
    DisposableEffect(Unit) {
        VisualizerTap.listen()
        onDispose { VisualizerTap.stop() }
    }
    LaunchedEffect(state) {
        var last = 0L
        while (true) {
            withFrameNanos { now ->
                val dt = if (last == 0L) 16f else ((now - last) / 1_000_000f).coerceIn(1f, 100f)
                last = now
                state.spectrum.update(dt)
                state.levelState.floatValue = state.spectrum.level
                state.frameState.longValue++
            }
        }
    }
    return state
}

/** Bars rising from the bottom of the player, softened so the controls over them stay readable. */
@Composable
fun VisualizerBars(state: VisualizerState, brush: Brush, modifier: Modifier) {
    Canvas(modifier) {
        state.frame // redraw each frame
        val bands = state.bands
        val n = bands.size
        val gap = size.width / n * 0.28f
        val w = size.width / n - gap
        for (i in 0 until n) {
            val h = size.height * (0.04f + 0.96f * bands[i])
            drawRoundRect(
                brush, Offset(i * (w + gap) + gap / 2, size.height - h), Size(w, h), CornerRadius(w / 2),
                // Faint enough that the buttons over them stay easy to read.
                alpha = 0.22f + 0.23f * bands[i],
            )
        }
    }
}

/** A soft wave, mirrored about its middle line, behind the title. */
@Composable
fun VisualizerWave(state: VisualizerState, brush: Brush, modifier: Modifier) {
    val path = remember { Path() }
    Canvas(modifier) {
        state.frame
        val bands = state.bands
        val mid = size.height / 2
        val step = size.width / (bands.size - 1)
        path.reset()
        path.moveTo(0f, mid)
        // Up over the bands, then back under them: a smooth closed shape.
        for (i in bands.indices) {
            val x = i * step
            val y = mid - mid * 0.9f * bands[i]
            if (i == 0) path.lineTo(x, y) else {
                val px = (i - 1) * step
                val py = mid - mid * 0.9f * bands[i - 1]
                path.cubicTo(px + step / 2, py, x - step / 2, y, x, y)
            }
        }
        for (i in bands.indices.reversed()) {
            val x = i * step
            val y = mid + mid * 0.9f * bands[i]
            if (i == bands.lastIndex) path.lineTo(x, y) else {
                val px = (i + 1) * step
                val py = mid + mid * 0.9f * bands[i + 1]
                path.cubicTo(px - step / 2, py, x + step / 2, y, x, y)
            }
        }
        path.close()
        // Soft enough that the title over it reads as clearly as before.
        drawPath(path, brush, alpha = 0.16f)
        drawPath(path, brush, alpha = 0.45f, style = Stroke(1.5f * density))
    }
}

/**
 * Around the cover: for a round cover (Vinyl), bars standing out from its edge all the way round; for a square one, its
 * outline glowing outward with the beat and the bands running round its edge.
 */
@Composable
fun VisualizerRing(
    state: VisualizerState, brush: Brush, round: Boolean, cornerPx: Float, modifier: Modifier,
    /** The cover's height to its width (a tape is wider than tall), centred in the box. */
    aspect: Float = 1f,
) {
    Canvas(modifier) {
        state.frame
        val bands = state.bands
        val c = center
        if (round) {
            val r0 = size.minDimension / 2 * 1.02f
            val reach = size.minDimension * 0.10f
            val count = bands.size * 2
            for (i in 0 until count) {
                // Mirrored, so low notes sit at the top and bottom and the ring is symmetric.
                val v = bands[if (i < bands.size) i else count - 1 - i]
                val a = (i.toFloat() / count) * 2 * PI - PI / 2
                val dx = cos(a).toFloat(); val dy = sin(a).toFloat()
                val r1 = r0 + reach * (0.08f + 0.92f * v)
                drawLine(brush, Offset(c.x + dx * r0, c.y + dy * r0), Offset(c.x + dx * r1, c.y + dy * r1), 3f * density, alpha = 0.75f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            }
        } else {
            val beat = state.level.floatValue
            val h = size.width * aspect
            val top = (size.height - h) / 2
            val corner = if (aspect != 1f) size.width * 0.045f else cornerPx
            for (k in 3 downTo 1) {
                val grow = (4f + 10f * beat) * k * density
                drawRoundRect(
                    brush, Offset(-grow, top - grow), Size(size.width + 2 * grow, h + 2 * grow), CornerRadius(corner + grow),
                    alpha = (0.10f + 0.22f * beat) / k, style = Stroke((2f + 3f * beat) * density),
                )
            }
        }
    }
}

/** The whole background breathing: a glow from behind the cover (about a third down) that swells with the beat. */
@Composable
fun VisualizerGlow(state: VisualizerState, color: Color, modifier: Modifier) {
    Canvas(modifier) {
        val beat = state.level.floatValue
        val at = Offset(size.width / 2, size.height * 0.36f)
        val r = size.minDimension * (0.55f + 0.25f * beat)
        drawCircle(
            Brush.radialGradient(listOf(color.copy(alpha = 0.10f + 0.30f * beat), Color.Transparent), at, r),
            radius = r, center = at,
        )
    }
}

/**
 * AURIX Visualizer Studio's additional GPU-light effects. All motion is driven by the existing playback spectrum;
 * no microphone permission or separate audio capture is used.
 */
@Composable
fun VisualizerAdvanced(state: VisualizerState, style: com.prism.music.data.prefs.VisualizerStyle, brush: Brush, modifier: Modifier) {
    Canvas(modifier) {
        state.frame
        val bands = state.bands
        val beat = state.level.floatValue.coerceIn(0f, 1f)
        val variant = state.presetIndex
        val cx = size.width / 2f
        val cy = size.height / 2f
        val minR = size.minDimension * 0.12f
        val maxR = size.minDimension * 0.43f
        when (style) {
            com.prism.music.data.prefs.VisualizerStyle.RING -> {
                val count = bands.size * 2
                val base = size.minDimension * 0.19f
                val reach = size.minDimension * 0.24f
                for (i in 0 until count) {
                    val band = bands[if (i < bands.size) i else count - 1 - i].coerceIn(0f, 1f)
                    val angle = -PI.toFloat() / 2f + i * (2f * PI.toFloat() / count)
                    val inner = base
                    val outer = inner + reach * (0.12f + band * 0.88f)
                    drawLine(
                        brush,
                        Offset(cx + kotlin.math.cos(angle) * inner, cy + kotlin.math.sin(angle) * inner),
                        Offset(cx + kotlin.math.cos(angle) * outer, cy + kotlin.math.sin(angle) * outer),
                        strokeWidth = (2.2f + band * 2.8f) * density,
                        alpha = 0.72f + band * 0.28f,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                    )
                }
                drawCircle(brush, base * (0.55f + beat * 0.18f), Offset(cx, cy), style = Stroke(2.5f * density), alpha = 0.8f)
            }
            com.prism.music.data.prefs.VisualizerStyle.PULSE -> {
                val pulseRadius = size.minDimension * (0.12f + beat * 0.12f)
                for (layer in 0..3) {
                    val radius = pulseRadius + layer * size.minDimension * 0.075f
                    drawCircle(
                        brush, radius, Offset(cx, cy),
                        style = Stroke((2.5f + beat * 2f) * density),
                        alpha = (0.62f - layer * 0.12f).coerceIn(0.12f, 0.7f),
                    )
                }
                val barY = size.height * 0.82f
                val barW = size.width / bands.size * 0.55f
                bands.forEachIndexed { i, value ->
                    val x = i * size.width / bands.size
                    val h = size.height * 0.12f * value
                    drawLine(brush, Offset(x + barW / 2f, barY - h), Offset(x + barW / 2f, barY + h), (2f + value * 2f) * density, alpha = 0.72f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                }
            }
            com.prism.music.data.prefs.VisualizerStyle.GEOMETRIC -> {
                val sides = 3 + (variant % 6)
                for (layer in 0 until 3 + (variant % 5)) {
                    val radius = minR + layer * size.minDimension * 0.055f + beat * 12f
                    val rotation = state.frame * (0.003f + layer * 0.0007f) * if (layer % 2 == 0) 1f else -1f
                    val path = Path()
                    for (i in 0..sides) {
                        val angle = rotation + i * (2f * PI.toFloat() / sides)
                        val r = radius + (bands[(i * 4 + layer) % bands.size].coerceIn(0f, 1f) * 18f)
                        val p = Offset(cx + kotlin.math.cos(angle) * r, cy + kotlin.math.sin(angle) * r)
                        if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
                    }
                    drawPath(path, brush, style = Stroke(width = (1.4f + layer * 0.45f) * density), alpha = 0.75f - layer * 0.09f)
                }
            }
            com.prism.music.data.prefs.VisualizerStyle.PARTICLES -> {
                for (i in 0 until 32 + (variant % 97)) {
                    val seed = i * (2.1f + (variant % 31) * 0.013f)
                    val band = bands[i % bands.size].coerceIn(0f, 1f)
                    val phase = state.frame * (0.012f + (i % 5) * 0.002f) + seed
                    val radius = minR + ((i * 37) % 100) / 100f * (maxR - minR) + band * 28f
                    val x = cx + kotlin.math.cos(phase) * radius
                    val y = cy + kotlin.math.sin(phase * 1.13f) * radius
                    drawCircle(brush, (1.2f + band * 3.2f) * density, Offset(x, y), alpha = 0.35f + band * 0.6f)
                }
            }
            com.prism.music.data.prefs.VisualizerStyle.LIQUID -> {
                for (layer in 0..3) {
                    val path = Path()
                    val base = size.height * (0.42f + layer * 0.055f)
                    path.moveTo(0f, size.height)
                    path.lineTo(0f, base)
                    val steps = 12 + (variant % 37)
                    for (i in 0..steps) {
                        val x = size.width * i / steps
                        val band = bands[(i * 2 + layer * 7) % bands.size].coerceIn(0f, 1f)
                        val wave = kotlin.math.sin(i * 0.48f + state.frame * 0.035f + layer) * (8f + band * 32f)
                        val y = base + wave - band * 18f
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    path.lineTo(size.width, size.height)
                    path.close()
                    drawPath(path, brush, alpha = 0.13f + layer * 0.07f)
                }
            }
            com.prism.music.data.prefs.VisualizerStyle.SPECTRUM_360 -> {
                val count = bands.size
                val rotation = (variant % 360) * PI.toFloat() / 180f
                for (i in 0 until count) {
                    val angle = -PI.toFloat() / 2f + rotation + i * (2f * PI.toFloat() / count)
                    val value = bands[i].coerceIn(0f, 1f)
                    val inner = minR
                    val outer = inner + 22f + value * size.minDimension * 0.23f
                    drawLine(
                        brush,
                        Offset(cx + kotlin.math.cos(angle) * inner, cy + kotlin.math.sin(angle) * inner),
                        Offset(cx + kotlin.math.cos(angle) * outer, cy + kotlin.math.sin(angle) * outer),
                        strokeWidth = (2f + value * 3f) * density,
                        alpha = 0.45f + value * 0.55f,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                    )
                }
                drawCircle(brush, minR * (0.72f + beat * 0.18f), Offset(cx, cy), style = Stroke(2f * density), alpha = 0.75f)
            }
            com.prism.music.data.prefs.VisualizerStyle.FRACTAL -> {
                for (layer in 0..(4 + variant % 8)) {
                    val radius = minR + layer * size.minDimension * 0.047f
                    val segments = 48
                    val path = Path()
                    for (i in 0..segments) {
                        val angle = i * (2f * PI.toFloat() / segments)
                        val band = bands[(i + layer * 3) % bands.size].coerceIn(0f, 1f)
                        val modulation = 1f + 0.16f * kotlin.math.sin(angle * 6f + state.frame * 0.018f + layer) + band * 0.28f
                        val r = radius * modulation * (1f + beat * 0.06f)
                        val x = cx + kotlin.math.cos(angle) * r
                        val y = cy + kotlin.math.sin(angle) * r
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    path.close()
                    drawPath(path, brush, style = Stroke((1f + layer * 0.25f) * density), alpha = 0.62f - layer * 0.045f)
                }
            }
            else -> Unit
        }
    }
}
