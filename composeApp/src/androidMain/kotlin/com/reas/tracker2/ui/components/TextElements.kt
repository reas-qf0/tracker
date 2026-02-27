package com.reas.tracker2.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import com.reas.tracker2.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Timestamp(
    timestamp: Long,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
) {
    val tooltipState = rememberTooltipState()
    val scope = rememberCoroutineScope()
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        tooltip = {
            PlainTooltip(
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Text(dateTimeFormatter.format(Date(timestamp)))
            }
        },
        state = tooltipState
    ) {
        var difference by remember { mutableLongStateOf(timeAgoInMs(timestamp)) }

        val showSeconds by remember { derivedStateOf { difference < 60L * 1000L } }
        if (showSeconds) {
            Text(
                stringResource(R.string.seconds_ago, difference / 1000L),
                modifier = modifier.clickable(onClick = {
                    scope.launch { tooltipState.show() }
                }),
                style = style,
                color = color
            )
            LaunchedEffect(difference) {
                delay(1000L - (difference % 1000L))
                difference = timeAgoInMs(timestamp)
            }
        }

        val showMinutes by remember { derivedStateOf {
            60L * 1000L <= difference && difference < 60L * 60L * 1000L
        } }
        if (showMinutes) {
            Text(
                stringResource(R.string.minutes_ago, difference / (60L * 1000L)),
                modifier = modifier.clickable(onClick = {
                    scope.launch { tooltipState.show() }
                }),
                style = style,
                color = color
            )
            LaunchedEffect(difference) {
                delay(1000L * 60L - (difference % (1000L * 60L)))
                difference = timeAgoInMs(timestamp)
            }
        }

        val showHours by remember { derivedStateOf {
            60L * 60L * 1000L <= difference && difference < 60L * 60L * 24L * 1000L
        } }
        if (showHours) {
            Text(
                stringResource(R.string.hours_ago, difference / (60L * 60L * 1000L)),
                modifier = modifier.clickable(onClick = {
                    scope.launch { tooltipState.show() }
                }),
                style = style,
                color = color
            )
            LaunchedEffect(difference) {
                delay(1000L * 60L * 60L - (difference % (1000L * 60L * 60L)))
                difference = timeAgoInMs(timestamp)
            }
        }

        if (!showSeconds && !showMinutes && !showHours) {
            Text(
                dateFormatter.format(Date(timestamp)),
                modifier = modifier.clickable(onClick = {
                    scope.launch { tooltipState.show() }
                }),
                style = style,
                color = color
            )
        }
    }
}

private val dateFormatter = SimpleDateFormat.getDateInstance()
private val dateTimeFormatter = SimpleDateFormat.getDateTimeInstance()
private fun timeAgoInMs(timestamp: Long) =
    System.currentTimeMillis() - timestamp