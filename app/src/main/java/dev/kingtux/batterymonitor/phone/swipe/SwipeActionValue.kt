package dev.kingtux.batterymonitor.phone.swipe

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter

class SwipeActionValue(
    val onclick: () -> Unit,
    val icon: (@Composable () -> Unit)? = null,
    val background: Color,
    val text: String? = null,
    val textColor : Color = Color.White,
) {
}
fun swipeAction(
    onClick: () -> Unit,
    icon: Painter,
    text: String? = null,
    background: Color,
    textColor : Color = Color.White,

    ): SwipeActionValue {
    return SwipeActionValue(
        icon = {
            Image(
                modifier = Modifier,
                painter = icon,
                contentDescription = text
            )
        },
        text= text,
        background = background,
        onclick = onClick,
        textColor = textColor
    )
}
