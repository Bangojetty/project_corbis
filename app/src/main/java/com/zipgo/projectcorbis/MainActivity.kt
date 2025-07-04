package com.zipgo.projectcorbis

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.zipgo.projectcorbis.ui.theme.Project_CorbisTheme
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Project_CorbisTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        AnalogJoystick { x, y ->
                            Log.d("Joystick", "Move: x=$x, y=$y")
                            // 🚀 This is where you'll send x/y values to the drone later
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Project_CorbisTheme {
        Greeting("Android")
    }
}

@Composable
fun AnalogJoystick(
    baseRadiusDp: Float = 100f,
    handleRadiusDp: Float = 30f,
    onMove: (x: Float, y: Float) -> Unit
) {
    val density = LocalDensity.current
    val baseRadiusPx = with(density) { baseRadiusDp.dp.toPx() }
    val handleRadiusPx = with(density) { handleRadiusDp.dp.toPx() }

    var handleOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .size((baseRadiusDp * 2).dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        handleOffset = Offset.Zero
                        onMove(0f, 0f)
                    },
                    onDrag = { change, dragAmount ->
                        val newOffset = handleOffset + dragAmount

                        val clampedOffset = if (newOffset.getDistance() > baseRadiusPx - handleRadiusPx) {
                            // Clamp to the edge of the base circle
                            newOffset / newOffset.getDistance() * (baseRadiusPx - handleRadiusPx)
                        } else newOffset

                        handleOffset = clampedOffset

                        // Normalize to -1 to 1
                        onMove(
                            clampedOffset.x / (baseRadiusPx - handleRadiusPx),
                            clampedOffset.y / (baseRadiusPx - handleRadiusPx)
                        )
                    }
                )
            }
            .background(Color.LightGray, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(handleOffset.x.roundToInt(), handleOffset.y.roundToInt())
                }
                .size((handleRadiusDp * 2).dp)
                .background(Color.DarkGray, shape = CircleShape)
        )
    }
}

