package org.reas.tracker.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import com.skydoves.landscapist.coil3.CoilImage
import com.skydoves.landscapist.components.rememberImageComponent
import com.skydoves.landscapist.palette.PalettePlugin
import com.skydoves.landscapist.palette.rememberPaletteState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val DYNAMIC_COLOR_STRENGTH = 0.1F

@Composable
fun ListEntryWithImage(
    modifier: Modifier = Modifier,
    url: suspend () -> Any? = { null },
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    dynamicColor: Boolean = false,
    alignment: Alignment.Vertical = Alignment.Top,
    content: @Composable RowScope.() -> Unit,
) {
    var model by remember { mutableStateOf<Any?>(null) }
    LaunchedEffect(Unit) {
        launch(Dispatchers.IO) {
            model = url()
        }
    }

    var palette by rememberPaletteState(null)
    val color by animateColorAsState(
        if (palette == null || !dynamicColor)
            backgroundColor
        else {
            Color(ColorUtils.blendARGB(
                backgroundColor.toArgb(),
                palette!!.getDominantColor(backgroundColor.toArgb()),
                DYNAMIC_COLOR_STRENGTH
            ))
        }
    )

    Row(
        modifier = modifier
            .background(color, RoundedCornerShape(5.dp))
            .padding(5.dp),
        verticalAlignment = alignment
    ) {
        CoilImage(
            imageModel = { model },
            component = rememberImageComponent {
                +PalettePlugin { palette = it }
            },
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1.0F)
                .clip(shape = RoundedCornerShape(5.dp))
                .background(color = Color.Gray)
        )
        Spacer(Modifier.width(10.dp))
        content()
    }
}