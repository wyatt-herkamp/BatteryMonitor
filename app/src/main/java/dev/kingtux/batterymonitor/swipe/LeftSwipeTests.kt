package dev.kingtux.batterymonitor.swipe

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview(
    showBackground = true,
    device = "id:pixel_7_pro",
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun CardTest(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(all = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,

        ) {
        Column(
            modifier = modifier.align(Alignment.CenterVertically)
        ) {

            Text(
                text = "Test Device", modifier = modifier
                    .padding(16.dp)
                    .width(100.dp)
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Column(
            modifier = modifier.align(Alignment.CenterVertically)
        ) {
            Text(
                text = "50%", modifier = modifier.padding(16.dp)
            )
        }


        Spacer(modifier = modifier.weight(1f))

        Column(
            modifier = modifier
                .align(Alignment.CenterVertically)
                .padding(end = 4.dp)
        ) {
            Button(
                onClick = { },

                modifier = modifier.requiredWidth(150.dp)
            ) {
                Text(
                    text = "Headphones",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = modifier.width(150.dp)
                )
            }
        }
    }


}

@Preview(
    showBackground = true,
    device = "id:pixel_7_pro",
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun CardOpened() {
    val swipeAction = swipeAction(
        icon = rememberVectorPainter(Icons.TwoTone.Warning),
        text = "Disable",
        background = Color.Red,
        onClick = {},

        )
    LeftSwipeActionCard(
        actions = listOf(swipeAction), startOpened = true
    ) {
        CardTest()
    }
}

@Preview(
    showBackground = true,
    device = "id:pixel_7_pro",
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun CardClosed() {
    val swipeAction = swipeAction(icon = rememberVectorPainter(Icons.TwoTone.Warning),
        text = "Disable",
        background = Color.Red,
        onClick = {}

    )
    LeftSwipeActionCard(
        actions = listOf(swipeAction),

        ) {
        CardTest()
    }

}

