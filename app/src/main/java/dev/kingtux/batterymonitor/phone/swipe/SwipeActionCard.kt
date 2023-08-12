package dev.kingtux.batterymonitor.phone.swipe

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun SwipeableActionsBox(
    modifier: Modifier = Modifier,
    leftActions: List<SwipeAction> = emptyList(),
    rightActions: List<SwipeAction> = emptyList(),
    state: SwipeableActionsState = rememberSwipeableActionsState(),
    backgroundUntilSwipeThreshold: Color = Color.DarkGray,
    leftBackground: Color = Color.Blue,
    rightBackground: Color = Color.Red,
    content: @Composable BoxScope.() -> Unit,
) = BoxWithConstraints(modifier) {
    val ripple = remember {
        SwipeRippleState()
    }
    val rightSwipeSize = (rightActions.size * 64).toFloat()
    val leftSwipeSize = (leftActions.size * 64).toFloat()
    val actions = remember(leftActions, rightActions) {
        ActionFinder(left = leftActions, right = rightActions)
    }
    LaunchedEffect(state, actions) {
        state.run {
            canSwipeTowardsRight = { leftActions.isNotEmpty() }
            canSwipeTowardsLeft = { rightActions.isNotEmpty() }
            targetRightOffset = {
                rightSwipeSize
            }
            targetLeftOffset = {
                leftSwipeSize
            }
        }
    }
    val rightSwipeThresholdPx = LocalDensity.current.run { rightSwipeSize.dp.toPx() }
    val leftSwipeThresholdPx = LocalDensity.current.run { leftSwipeSize.dp.toPx() }

    val offset = state.offset.value
    val offsetValue = remember(offset) {
        mutableStateOf(IntOffset(x = offset.roundToInt() * 10, y = 0))
    }
    val thresholdCrossedRight = abs(offset) > rightSwipeThresholdPx
    val thresholdCrossedLeft = abs(offset) > leftSwipeThresholdPx


    val swipeOpened: SwipeDirection? = remember(offset, actions) {
        actions.getActionSet(offset)
    }

    val backgroundColor: Color by animateColorAsState(
        when {
            swipeOpened != null -> {
                when (swipeOpened) {
                    SwipeDirection.Left -> leftBackground
                    SwipeDirection.Right -> rightBackground
                }
            }

            !thresholdCrossedRight -> backgroundUntilSwipeThreshold
            !thresholdCrossedRight -> backgroundUntilSwipeThreshold
            else -> backgroundUntilSwipeThreshold
        }, label = ""
    )

    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .absoluteOffset {
                offsetValue.value
            }
            .draggable(
                orientation = Orientation.Horizontal,
                enabled = !state.isResettingOnRelease,
                onDragStopped = {
                    Log.d("SwipeableActionsBox", "onDragStopped: $offset $offsetValue")
                    scope.launch {
                        if (!thresholdCrossedRight || !thresholdCrossedLeft) {
                            state.resetOffset()
                            Log.d("SwipeableActionsBox", "after: $offset $offsetValue")
                        } else {
                            swipeOpened?.let {
                                state.setToOpen(it)
                                Log.d("SwipeableActionsBox", "after: $offset $offsetValue")

                            }
                        }
                    }
                    scope.launch {
                    }
                },
                state = state.draggableState,
            ),
        content = content
    )
    if (swipeOpened != null) {
        when (swipeOpened) {
            SwipeDirection.Left -> {
                ActionsBox(
                    action = leftActions,
                    backgroundColor = backgroundColor,
                    reset = {
                        scope.launch {
                            state.resetOffset()
                        }
                    },
                    swipeDirection = swipeOpened,
                    modifier = Modifier
                        .matchParentSize(),
                    size = leftSwipeSize,

                    )
            }

            SwipeDirection.Right -> {
                ActionsBox(
                    action = rightActions,
                    backgroundColor = backgroundColor,
                    reset = {
                        scope.launch {
                            state.resetOffset()
                        }
                    },
                    swipeDirection = swipeOpened,
                    modifier = Modifier
                        .matchParentSize()
                        .offset((-64).dp, 0.dp),

                    size = rightSwipeSize,
                )
            }
        }
    }


}

@Composable
private fun ActionBox(
    action: SwipeAction,
    width: Float,
    reset: () -> Unit
) {
    Column(
        modifier = Modifier
            .background(color = action.background)
            .padding(16.dp)
            .width(width.dp)
            .clickable {
                action.onSwipe()
                reset()
            }
    ) {
        action.icon?.let {
            it()
        }
        action.text?.let {
            Text(
                text = it,
                color = action.textColor,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun ActionsBox(
    action: List<SwipeAction>,
    backgroundColor: Color,
    reset: () -> Unit,
    swipeDirection: SwipeDirection,
    modifier: Modifier,
    size: Float
) {
    val widthPerItem = size / action.size

    Log.d("ActionsBox", "ActionsBox: $size $widthPerItem $swipeDirection $action")
    Row(
        modifier = modifier
            .requiredWidth(size.dp)
            .background(color = backgroundColor)

        // Force to be on the left of the screen

        ,
        horizontalArrangement = if (swipeDirection == SwipeDirection.Left) {
            Arrangement.Start
        } else {
            Arrangement.End
        },
        verticalAlignment = Alignment.CenterVertically
    ) {
    }
}

private fun Modifier.drawOverContent(onDraw: DrawScope.() -> Unit): Modifier {
    return drawWithContent {
        drawContent()
        onDraw(this)
    }
}
