package dev.kingtux.batterymonitor.swipe

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxHeight

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.Card

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

 const val SIZE_PER_ACTION = 64
@Composable
fun LeftSwipeActionCard(
    modifier: Modifier = Modifier,
    startOpened: Boolean = false,
    actions: List<SwipeActionValue> = emptyList(),
    state: SwipeableActionsState = rememberSwipeableActionsState(),
    content: @Composable ColumnScope.() -> Unit,
) = BoxWithConstraints(modifier) {
    val leftSwipeSize = (actions.size * SIZE_PER_ACTION).toFloat()

    val leftSwipeThresholdPx = LocalDensity.current.run { (leftSwipeSize).dp.toPx() }
    val leftSwipeMin = LocalDensity.current.run { (leftSwipeSize / 2).dp.toPx() }

    LaunchedEffect(state) {
        state.run {
            canSwipeTowardsRight = { actions.isNotEmpty() }
            targetLeftOffset = {
                leftSwipeThresholdPx
            }
        }
    }
    if (startOpened) {
        state.offsetState.floatValue = leftSwipeThresholdPx
    }

    val offset = state.offset.value

    val thresholdCrossedLeft = abs(offset) > leftSwipeMin

    val shape = remember(offset) {
        if (offset != 0f) {
            RectangleShape
        } else {
            ShapeDefaults.Medium
        }
    }

    val scope = rememberCoroutineScope()

    Box {
        Card(
            shape = shape,
            modifier = Modifier
                .absoluteOffset {
                    IntOffset(x = (offset).roundToInt(), y = 0)
                }
                .clickable(
                    enabled = offset != 0f,
                ) {
                    if (offset != 0f) {
                        scope.launch {
                            state.resetOffset()
                        }
                    }
                }
                .draggable(
                    orientation = Orientation.Horizontal,
                    enabled = !state.isResettingOnRelease,
                    onDragStopped = {
                        scope.launch {
                            if (!thresholdCrossedLeft) {
                                state.resetOffset()
                            } else {
                                state.setToOpen()
                            }
                        }
                    },
                    state = state.draggableState,
                )
        ) {
            content()
        }

        ActionsBox(
            action = actions, reset = {
                scope.launch {
                    state.resetOffset()
                }
            }, modifier = Modifier
                .matchParentSize()
                .absoluteOffset {
                    calculateSwipeBoxOffset(
                        density = this,
                        offset = offset,
                        sizeOfSwipeBox = leftSwipeSize
                    )
                },
        )
    }
}
fun calculateSwipeBoxOffset(
    density: Density,
    offset: Float,
    sizeOfSwipeBox: Float = 96f
): IntOffset{
    return IntOffset(x = (((-(sizeOfSwipeBox * density.density))+offset)).roundToInt(), y = 0)
}
@Composable
private fun ActionBox(
    action: SwipeActionValue,
    modifier: Modifier = Modifier,
    reset: () -> Unit
) {
    Column(
        modifier = modifier
            .background(color = action.background, shape = RoundedCornerShape(
                8.dp,
                0.dp,
                0.dp,
                8.dp,
            ))
            .clickable {
                action.onclick()
                reset()
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        action.icon?.let {
            it()
        }
        action.text?.let {
            Text(
                text = it,
                color = action.textColor,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

@Composable
fun ActionsBox(
    action: List<SwipeActionValue>,
    reset: () -> Unit,
    modifier: Modifier,
) {
    Row(
        modifier = modifier,
    ) {
        action.forEach {
            ActionBox(
                action = it,
                modifier = Modifier.width(SIZE_PER_ACTION.dp).fillMaxHeight(),
                reset = reset
            )
        }
    }
}
