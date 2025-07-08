// app/src/main/java/com/zipgo/projectcorbis/MainActivity.kt
package com.zipgo.projectcorbis

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zipgo.projectcorbis.ui.theme.Project_CorbisTheme
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

data class JoystickPosition(
    val x: Float = 0f,
    val y: Float = 0f,
    val distance: Float = 0f,
    val angle: Float = 0f
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Force landscape mode
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        enableEdgeToEdge()
        setContent {
            Project_CorbisTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DualJoystick(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun DualJoystick(modifier: Modifier = Modifier) {
    var leftJoystickPosition by remember { mutableStateOf(JoystickPosition()) }
    var rightJoystickPosition by remember { mutableStateOf(JoystickPosition()) }

    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Joystick
        JoystickController(
            modifier = Modifier.size(200.dp),
            false,
            onPositionChanged = { position ->
                leftJoystickPosition = position
            }
        )

        // Right Joystick
        JoystickController(
            modifier = Modifier.size(200.dp),
            onPositionChanged = { position ->
                rightJoystickPosition = position
            }
        )
    }
}

@Composable
fun JoystickController(
    modifier: Modifier = Modifier,
    resetOnRelease: Boolean = true,
    onPositionChanged: (JoystickPosition) -> Unit = {}
) {
    val density = LocalDensity.current
    var centerPosition by remember { mutableStateOf(Offset.Zero) }
    var knobPosition by remember { mutableStateOf(Offset.Zero) }
    var isDragging by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color.Gray.copy(alpha = 0.3f))
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            centerPosition = Offset(size.width / 2f, size.height / 2f)
                            knobPosition = offset
                        },
                        onDragEnd = {
                            isDragging = false
                            if(resetOnRelease) {
                                knobPosition = centerPosition
                                onPositionChanged(JoystickPosition())
                            }
                        },
                        onDrag = { _, dragAmount ->
                            val newPosition = knobPosition + dragAmount
                            val distance = (newPosition - centerPosition).getDistance()
                            val maxDistance = size.width / 2f * 0.8f // 80% of radius

                            knobPosition = if (distance <= maxDistance) {
                                newPosition
                            } else {
                                val angle = atan2(
                                    newPosition.y - centerPosition.y,
                                    newPosition.x - centerPosition.x
                                )
                                Offset(
                                    centerPosition.x + cos(angle) * maxDistance,
                                    centerPosition.y + sin(angle) * maxDistance
                                )
                            }

                            // Calculate normalized position (-1 to 1)
                            val normalizedX = (knobPosition.x - centerPosition.x) / maxDistance
                            val normalizedY = (knobPosition.y - centerPosition.y) / maxDistance
                            val normalizedDistance = min(distance / maxDistance, 1f)
                            val angle = atan2(normalizedY, normalizedX)

                            onPositionChanged(
                                JoystickPosition(
                                    x = normalizedX,
                                    y = normalizedY,
                                    distance = normalizedDistance,
                                    angle = angle
                                )
                            )
                        }
                    )
                }
        ) {
            val center = size.center
            val radius = size.minDimension / 2f
            val knobRadius = radius * 0.2f

            if (centerPosition == Offset.Zero) {
                centerPosition = center
                knobPosition = center
            }

            // Draw outer circle (joystick base)
            drawCircle(
                color = Color.Gray,
                radius = radius,
                center = center,
                alpha = 0.5f
            )

            // Draw inner circle (movement area)
            drawCircle(
                color = Color.Gray,
                radius = radius * 0.8f,
                center = center,
                alpha = 0.2f
            )

            // Draw knob
            drawCircle(
                color = if (isDragging) Color.Blue else Color.DarkGray,
                radius = knobRadius,
                center = knobPosition
            )

            // Draw knob border
            drawCircle(
                color = Color.White,
                radius = knobRadius,
                center = knobPosition,
                alpha = 0.8f,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx())
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun JoystickPreview() {
    Project_CorbisTheme {
        DualJoystick()
    }
}