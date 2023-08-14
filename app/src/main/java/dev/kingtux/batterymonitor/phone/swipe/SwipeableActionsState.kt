package dev.kingtux.batterymonitor.phone.swipe

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@Composable
fun rememberSwipeableActionsState(): SwipeableActionsState {
    return remember { SwipeableActionsState() }
}
class SwipeableActionsState {
    val offset: State<Float> get() = offsetState


    internal var offsetState = mutableFloatStateOf(0f)
    var isResettingOnRelease: Boolean by mutableStateOf(false)
        private set

    internal lateinit var targetLeftOffset: () -> Float

    internal lateinit var canSwipeTowardsRight: () -> Boolean

    internal val draggableState = DraggableState { delta ->
        val targetOffset = offsetState.floatValue + delta
        val isAllowed = isResettingOnRelease
                || targetOffset > 0f && canSwipeTowardsRight()
        if (targetOffset > targetLeftOffset()){
            offsetState.floatValue = targetLeftOffset()
            return@DraggableState
        }
        offsetState.floatValue += if (isAllowed) delta else delta / 10
    }
    suspend fun setToOpen(){
        draggableState.drag(MutatePriority.PreventUserInput) {
            isResettingOnRelease = true
            try {
                Animatable(offsetState.floatValue).animateTo(targetValue = targetLeftOffset(), tween(durationMillis = 20_00)) {
                    dragBy(value - offsetState.floatValue)
                }
            } finally {
                isResettingOnRelease = false
            }
        }
    }
     suspend fun resetOffset() {
        draggableState.drag(MutatePriority.PreventUserInput) {
            isResettingOnRelease = true
            try {
                Animatable(offsetState.floatValue).animateTo(targetValue = 0f, tween(durationMillis = 4_00)) {
                    dragBy(value - offsetState.floatValue)
                }
            } finally {
                isResettingOnRelease = false
            }
        }
    }
}