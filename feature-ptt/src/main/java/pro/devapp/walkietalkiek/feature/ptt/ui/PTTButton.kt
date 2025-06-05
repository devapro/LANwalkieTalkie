package pro.devapp.walkietalkiek.feature.ptt.ui

import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import pro.devapp.walkietalkiek.feature.ptt.R

@Composable
fun PTTButton(
    modifier: Modifier = Modifier,
    isOnline: Boolean = true,
    onPress: () -> Unit = {},
    onRelease: () -> Unit = {}
) {
    var pulseSize by remember { mutableStateOf(IntSize(0, 0)) }
    val pulsarRadius = 50f
    val infiniteTransition = rememberInfiniteTransition()

    val isPressed = remember { mutableStateOf(false) }

    val alpha1 by if (isOnline && isPressed.value) {
        infiniteTransition.animateFloat(
            initialValue = 0.9f,
            targetValue = 0.4f,
            animationSpec = InfiniteRepeatableSpec(
                animation = tween(1200),
                initialStartOffset = StartOffset(50),
                repeatMode = RepeatMode.Reverse
            )
        )
    } else {
        remember {
            mutableFloatStateOf(0.3f)
        }
    }
    val alpha2 by if (isOnline && isPressed.value) {
        infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = InfiniteRepeatableSpec(
                animation = tween(800),
                initialStartOffset = StartOffset(100),
                repeatMode = RepeatMode.Reverse
            )
        )
    } else {
        remember {
            mutableFloatStateOf(0.3f)
        }
    }

    val radius by if (isOnline && isPressed.value) {
        infiniteTransition.animateFloat(
            initialValue = (pulseSize.width / 2).toFloat(),
            targetValue = pulseSize.width + pulsarRadius,
            animationSpec = InfiniteRepeatableSpec(
                animation = tween(1500),
                repeatMode = RepeatMode.Reverse
            )
        )
    } else {
        remember {
            mutableFloatStateOf((pulseSize.width / 2).toFloat())
        }
    }

    Box(
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    try {
                        isPressed.value = true
                        onPress()
                        // Start recording here
                        awaitRelease()
                    } finally {
                        isPressed.value = false
                        // Stop recording here
                        onRelease()
                    }
                },
            )
        },
        contentAlignment = Alignment.Center
    ) {
        val isDarkTheme = isSystemInDarkTheme()
        val pressedColor = if (isDarkTheme) Color(0xFFF57C00) else Color(0xFFE65100) // Orange 700 / Orange 800
        val color1 = Color(0xFFE65100)
        val color2 = Color(0xFFFFB300)
        Canvas(
            Modifier
                .fillMaxSize()
                .onGloballyPositioned {
                    if (it.isAttached) {
                        pulseSize = it.size
                    }
                }, onDraw = {
                drawCircle(
                    color = color1,
                    alpha = alpha1
                )
                drawCircle(
                    radius = radius/2.5f,
                    color = color2,
                    alpha = 1f
                )
                drawCircle(
                    radius = radius/3.5f,
                    color = pressedColor,
                    alpha = alpha2
                )
            })
        Box(
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.mic),
                contentDescription = if (isOnline) "Press to talk" else "Offline",
                modifier = Modifier
                    .padding(8.dp)
                    .background(
                        color = if (isOnline) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        shape = CircleShape
                    )
                    .size((pulseSize.width/8f).dp),
                tint = if (isOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )

        }
    }
}

@Preview
@Composable
fun PTTButtonPreview() {
    PTTButton(
        modifier = Modifier.size(100.dp),
        isOnline = true,
        onPress = { /* Handle click */ },
        onRelease = { /* Handle release */ }
    )
}