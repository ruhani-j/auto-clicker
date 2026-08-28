package com.autoclicker.ui.overlay

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autoclicker.data.ClickerProfile
import com.autoclicker.ui.theme.AutoClickerTheme

@Composable
fun OverlayPanel(
    profiles: List<ClickerProfile>,
    onToggleProfile: (ClickerProfile, Boolean) -> Unit,
    onStopAll: () -> Unit,
    onDrag: (dx: Float, dy: Float) -> Unit
) {
    var minimized by remember { mutableStateOf(false) }

    AutoClickerTheme {
        if (minimized) {
            FilledTonalIconButton(
                onClick = { minimized = false },
                modifier = Modifier
                    .size(56.dp)
                    .pointerInput(Unit) {
                        detectDragGestures { change, drag ->
                            change.consume()
                            onDrag(drag.x, drag.y)
                        }
                    }
            ) {
                Text("▶", fontSize = 20.sp)
            }
        } else {
            Surface(
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .width(220.dp)
                    .wrapContentHeight()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pointerInput(Unit) {
                                detectDragGestures { change, drag ->
                                    change.consume()
                                    onDrag(drag.x, drag.y)
                                }
                            },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DragHandle, contentDescription = "Drag")
                        Text("AutoClicker", style = MaterialTheme.typography.labelLarge)
                        IconButton(onClick = { minimized = true }, modifier = Modifier.size(24.dp)) {
                            Text("—", fontSize = 14.sp)
                        }
                    }

                    HorizontalDivider()

                    if (profiles.isEmpty()) {
                        Text(
                            "No active clickers.\nOpen the app to configure.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        profiles.forEach { profile ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    profile.name,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Switch(
                                    checked = profile.isEnabled,
                                    onCheckedChange = { onToggleProfile(profile, it) }
                                )
                            }
                        }
                    }

                    HorizontalDivider()

                    Button(
                        onClick = onStopAll,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Stop All")
                    }
                }
            }
        }
    }
}
