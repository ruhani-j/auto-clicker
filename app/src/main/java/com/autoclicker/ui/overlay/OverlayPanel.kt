package com.autoclicker.ui.overlay

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autoclicker.data.ClickType
import com.autoclicker.data.ClickerProfile
import com.autoclicker.ui.theme.AutoClickerTheme

private val dotColors = listOf(
    Color(0xFF4CAF50),
    Color(0xFF2196F3),
    Color(0xFFF44336),
    Color(0xFFFF9800),
    Color(0xFF9C27B0),
    Color(0xFF00BCD4),
    Color(0xFFE91E63),
    Color(0xFFFFEB3B),
)

@Composable
fun ClickerDot(
    profile: ClickerProfile,
    clickTrigger: Int,
    flashEnabled: Boolean,
    onDrag: (dx: Float, dy: Float) -> Unit,
    onDragEnd: () -> Unit,
    onProfileUpdate: (ClickerProfile) -> Unit
) {
    val color = dotColors[(profile.id % dotColors.size).toInt()]
    var editOpen by remember { mutableStateOf(false) }

    val flashAlpha = remember { Animatable(0f) }
    LaunchedEffect(clickTrigger) {
        if (clickTrigger == 0 || !flashEnabled) return@LaunchedEffect
        flashAlpha.snapTo(0.7f)
        flashAlpha.animateTo(0f, animationSpec = tween(durationMillis = 220, easing = LinearEasing))
    }

    AutoClickerTheme {
        Column(horizontalAlignment = Alignment.Start) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.42f))
                    .border(1.dp, Color.White.copy(alpha = 0.55f), CircleShape)
                    .clickable { editOpen = !editOpen }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = { onDragEnd() }
                        ) { change, dragAmount ->
                            change.consume()
                            onDrag(dragAmount.x, dragAmount.y)
                        }
                    }
            ) {
                // Flash overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = flashAlpha.value))
                )
            }

            if (editOpen) {
                Spacer(Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shadowElevation = 8.dp,
                    modifier = Modifier.width(200.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            profile.name,
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        HorizontalDivider()

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            FilterChip(
                                selected = profile.clickType == ClickType.SINGLE_TAP,
                                onClick = { onProfileUpdate(profile.copy(clickType = ClickType.SINGLE_TAP)) },
                                label = { Text("Tap", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = profile.clickType == ClickType.PRESS_AND_HOLD,
                                onClick = { onProfileUpdate(profile.copy(clickType = ClickType.PRESS_AND_HOLD)) },
                                label = { Text("Hold", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        StepperRow(
                            label = "Interval",
                            value = "${profile.intervalMs}ms",
                            onDecrease = {
                                onProfileUpdate(profile.copy(intervalMs = (profile.intervalMs - 100).coerceAtLeast(50L)))
                            },
                            onIncrease = {
                                onProfileUpdate(profile.copy(intervalMs = profile.intervalMs + 100))
                            }
                        )

                        if (profile.clickType == ClickType.PRESS_AND_HOLD) {
                            StepperRow(
                                label = "Hold",
                                value = "${profile.holdDurationMs}ms",
                                onDecrease = {
                                    onProfileUpdate(profile.copy(holdDurationMs = (profile.holdDurationMs - 100).coerceAtLeast(50L)))
                                },
                                onIncrease = {
                                    onProfileUpdate(profile.copy(holdDurationMs = profile.holdDurationMs + 100))
                                }
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("∞", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(end = 4.dp))
                            Switch(
                                checked = profile.isInfinite,
                                onCheckedChange = { onProfileUpdate(profile.copy(isInfinite = it)) }
                            )
                            if (!profile.isInfinite) {
                                Spacer(Modifier.weight(1f))
                                StepperRow(
                                    label = "",
                                    value = "${profile.clickCount}×",
                                    onDecrease = {
                                        onProfileUpdate(profile.copy(clickCount = (profile.clickCount - 1).coerceAtLeast(1)))
                                    },
                                    onIncrease = {
                                        onProfileUpdate(profile.copy(clickCount = profile.clickCount + 1))
                                    }
                                )
                            }
                        }

                        HorizontalDivider()

                        TextButton(
                            onClick = { editOpen = false },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(28.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Done", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepperRow(
    label: String,
    value: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (label.isNotEmpty()) {
            Text(label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        }
        TextButton(
            onClick = onDecrease,
            modifier = Modifier.size(28.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text("−", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(54.dp)
        )
        TextButton(
            onClick = onIncrease,
            modifier = Modifier.size(28.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text("+", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun OverlayControls(
    isPaused: Boolean,
    isHidden: Boolean,
    onTogglePause: () -> Unit,
    onToggleHide: () -> Unit,
    onDrag: (dx: Float, dy: Float) -> Unit
) {
    AutoClickerTheme {
        if (isHidden) {
            SmallFloatingActionButton(
                onClick = onToggleHide,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.pointerInput(Unit) {
                    detectDragGestures { change, drag ->
                        change.consume()
                        onDrag(drag.x, drag.y)
                    }
                }
            ) {
                Icon(Icons.Default.Visibility, contentDescription = "Show dots")
            }
        } else {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shadowElevation = 6.dp,
                modifier = Modifier
                    .wrapContentSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, drag ->
                            change.consume()
                            onDrag(drag.x, drag.y)
                        }
                    }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onTogglePause, modifier = Modifier.size(44.dp)) {
                        Icon(
                            imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (isPaused) "Resume" else "Pause",
                            tint = if (isPaused) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onToggleHide, modifier = Modifier.size(44.dp)) {
                        Icon(
                            imageVector = Icons.Default.VisibilityOff,
                            contentDescription = "Hide",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
