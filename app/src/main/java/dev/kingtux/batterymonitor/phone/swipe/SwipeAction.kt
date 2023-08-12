package dev.kingtux.batterymonitor.phone.swipe

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import kotlin.math.abs

class SwipeAction(
    val onSwipe: () -> Unit,
    val icon: (@Composable () -> Unit)? = null,
    val background: Color,
    val weight: Double = 1.0,
    val text: String? = null,
    val textColor : Color = Color.White,
) {
    init {
        require(weight > 0.0) { "invalid weight $weight; must be greater than zero" }
    }
}
fun SwipeAction(
    onSwipe: () -> Unit,
    icon: Painter,
    text: String? = null,
    background: Color,
    weight: Double = 1.0,
    textColor : Color = Color.White,

    ): SwipeAction {
    return SwipeAction(
        icon = {
            Image(
                modifier = Modifier.padding(16.dp),
                painter = icon,
                contentDescription = text
            )
        },
        text= text,
        background = background,
        weight = weight,
        onSwipe = onSwipe,
        textColor = textColor
    )
}
internal data class SwipeActionMeta(
    val value: SwipeAction,
    val isOnRightSide: Boolean,
)

internal data class ActionFinder(
    private val left: List<SwipeAction>,
    private val right: List<SwipeAction>
) {
    fun getActionSet(offset: Float): SwipeDirection? {
        if (offset == 0f) {
            return null
        }

        val isOnRightSide = offset < 0f
        return if (isOnRightSide) SwipeDirection.Right else SwipeDirection.Left

    }
    fun actionAt(offset: Float, totalWidth: Int): SwipeActionMeta? {
        if (offset == 0f) {
            return null
        }

        val isOnRightSide = offset < 0f
        val actions = if (isOnRightSide) right else left

        val actionAtOffset = actions.actionAt(
            offset = abs(offset).coerceAtMost(totalWidth.toFloat()),
            totalWidth = totalWidth
        )
        return actionAtOffset?.let {
            SwipeActionMeta(
                value = actionAtOffset,
                isOnRightSide = isOnRightSide
            )
        }
    }

    private fun List<SwipeAction>.actionAt(offset: Float, totalWidth: Int): SwipeAction? {
        if (isEmpty()) {
            return null
        }

        val totalWeights = this.sumOf { it.weight }
        var offsetSoFar = 0.0

        @Suppress("ReplaceManualRangeWithIndicesCalls") // Avoid allocating an Iterator for every pixel swiped.
        for (i in 0 until size) {
            val action = this[i]
            val actionWidth = (action.weight / totalWeights) * totalWidth
            val actionEndX = offsetSoFar + actionWidth

            if (offset <= actionEndX) {
                return action
            }
            offsetSoFar += actionEndX
        }

        // Precision error in the above loop maybe?
        error("Couldn't find any swipe action. Width=$totalWidth, offset=$offset, actions=$this")
    }
}