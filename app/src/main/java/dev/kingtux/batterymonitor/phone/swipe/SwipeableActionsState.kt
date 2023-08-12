package dev.kingtux.batterymonitor.phone.swipe

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class SwipeDirection {
    Left,
    Right,
}
@Composable
fun rememberSwipeableActionsState(): SwipeableActionsState {
    return remember { SwipeableActionsState() }
}
class SwipeableActionsState {
    val offset: State<Float> get() = offsetState


    internal var offsetState = mutableStateOf(0f)
    var isResettingOnRelease: Boolean by mutableStateOf(false)
        private set

    internal lateinit var targetRightOffset: () -> Float
    internal lateinit var targetLeftOffset: () -> Float

    internal lateinit var canSwipeTowardsRight: () -> Boolean
    internal lateinit var canSwipeTowardsLeft: () -> Boolean

    internal val draggableState = DraggableState { delta ->
        val targetOffset = offsetState.value + delta
        val isAllowed = isResettingOnRelease
                || targetOffset > 0f && canSwipeTowardsRight()
                || targetOffset < 0f && canSwipeTowardsLeft()

        // Add some resistance if needed.
        offsetState.value += if (isAllowed) delta else delta / 10
    }
    suspend fun setToOpen(direction: SwipeDirection){
        val targetOffset = when(direction){
            SwipeDirection.Left -> targetLeftOffset()
            SwipeDirection.Right -> targetRightOffset()
        }
        Log.d("SwipeableActionsState", "setToOpen: $offset $direction")
        draggableState.drag(MutatePriority.PreventUserInput) {
            isResettingOnRelease = true
            try {
                Animatable(offsetState.value).animateTo(targetValue = targetOffset, tween(durationMillis = 4_00)) {
                    dragBy(value - offsetState.value)
                }
            } finally {
                isResettingOnRelease = false
                Log.d("SwipeableActionsState", "After setToOpen: $targetOffset $offset")
            }
        }
    }
     suspend fun resetOffset() {
        draggableState.drag(MutatePriority.PreventUserInput) {
            isResettingOnRelease = true
            try {
                Animatable(offsetState.value).animateTo(targetValue = 0f, tween(durationMillis = 4_00)) {
                    dragBy(value - offsetState.value)
                }
            } finally {
                isResettingOnRelease = false
            }
        }
    }
}



@Stable
internal class SwipeRippleState {
    private var ripple = mutableStateOf<SwipeRipple?>(null)

    suspend fun animate(
        action: SwipeActionMeta,
    ) {
        val drawOnRightSide = action.isOnRightSide
        val innerAction = action.value

        ripple.value = SwipeRipple(
            rightSide = drawOnRightSide,
            color = innerAction.background,
            alpha = 0f,
            progress = 0f
        )

        // Reverse animation feels faster (especially for larger swipe distances) so slow it down further.
        val animationDurationMs = (4_00 * 1f).roundToInt()

        coroutineScope {
            launch {
                Animatable(initialValue = 0f).animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = animationDurationMs),
                    block = {
                        ripple.value = ripple.value!!.copy(progress = value)
                    }
                )
            }
            launch {
                Animatable(initialValue =  0.25f).animateTo(
                    targetValue =  0f,
                    animationSpec = tween(
                        durationMillis = animationDurationMs,
                        delayMillis = animationDurationMs / 2
                    ),
                    block = {
                        ripple.value = ripple.value!!.copy(alpha = value)
                    }
                )
            }
        }
    }

    fun draw(scope: DrawScope) {
        ripple.value?.run {
            scope.clipRect {
                val size = scope.size
                // Start the ripple with a radius equal to the available height so that it covers the entire edge.
                val startRadius =  size.height
                val endRadius = size.width + size.height
                val radius = lerp(startRadius, endRadius, fraction = progress)

                drawCircle(
                    color = color,
                    radius = radius,
                    alpha = alpha,
                    center = this.center.copy(x = if (rightSide) this.size.width + this.size.height else 0f - this.size.height)
                )
            }
        }
    }
}

private data class SwipeRipple(
    val rightSide: Boolean,
    val color: Color,
    val alpha: Float,
    val progress: Float,
)

private fun lerp(start: Float, stop: Float, fraction: Float) =
    (start * (1 - fraction) + stop * fraction)
