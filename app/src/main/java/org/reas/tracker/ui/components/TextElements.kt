package org.reas.tracker.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp

private fun Modifier.firstBaselineToTop(
    firstBaselineToTop: Dp,
) = layout { measurable, constraints ->
    // Measure the composable
    val placeable = measurable.measure(constraints)

    // Check the composable has a first baseline
    check(placeable[FirstBaseline] != AlignmentLine.Unspecified)
    val firstBaseline = placeable[FirstBaseline]

    // Height of the composable with padding - first baseline
    val placeableY = firstBaselineToTop.roundToPx() - firstBaseline
    layout(placeable.width, placeable.height) {
        // Where the composable gets placed
        placeable.placeRelative(0, placeableY)
    }
}

@Composable
fun ConstrainedText(
    text: String,
    height: Dp,
    baselineHeight: Dp,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    style: TextStyle = LocalTextStyle.current,
) {
    Box(
        modifier = modifier.height(height).wrapContentHeight(align = Alignment.Top, unbounded = true)
    ) {
        Text(
            text,
            style = style,
            color = color,
            maxLines = 1, overflow = TextOverflow.Ellipsis,
            modifier = Modifier.firstBaselineToTop(baselineHeight)
        )
    }
}


@Composable
fun AutosizingText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
) {
    Text(
        text,
        style = style,
        color = color,
        autoSize = TextAutoSize.StepBased(maxFontSize = style.fontSize),
        maxLines = 1,
        modifier = modifier
    )
}