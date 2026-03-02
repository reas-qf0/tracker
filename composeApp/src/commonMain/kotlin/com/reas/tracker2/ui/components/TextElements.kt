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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.format.char
import org.jetbrains.compose.resources.stringResource
import tracker2.composeapp.generated.resources.Res
import tracker2.composeapp.generated.resources.hours_ago
import tracker2.composeapp.generated.resources.minutes_ago
import tracker2.composeapp.generated.resources.seconds_ago
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlin.time.toJavaInstant

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
    timestamp: Instant,
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
                contentColor = MaterialTheme.colorScheme.secondary
            ) {
                Text(timestamp.printLong())
            }
        },
        state = tooltipState
    ) {
        var difference by remember { mutableStateOf(Clock.System.now() - timestamp) }

        val showSeconds by remember { derivedStateOf { difference < 1.minutes } }
        if (showSeconds) {
            Text(
                stringResource(Res.string.seconds_ago, difference.inWholeSeconds),
                modifier = modifier.clickable(onClick = {
                    scope.launch { tooltipState.show() }
                }),
                style = style,
                color = color
            )
            LaunchedEffect(difference) {
                delay((difference.inWholeSeconds + 1).seconds)
                difference = Clock.System.now() - timestamp
            }
        }

        val showMinutes by remember { derivedStateOf {
            1.minutes <= difference && difference < 1.hours
        } }
        if (showMinutes) {
            Text(
                stringResource(Res.string.minutes_ago, difference.inWholeMinutes),
                modifier = modifier.clickable(onClick = {
                    scope.launch { tooltipState.show() }
                }),
                style = style,
                color = color
            )
            LaunchedEffect(difference) {
                delay((difference.inWholeMinutes + 1).minutes)
                difference = Clock.System.now() - timestamp
            }
        }

        val showHours by remember { derivedStateOf {
            1.hours <= difference && difference < 1.days
        } }
        if (showHours) {
            Text(
                stringResource(Res.string.hours_ago, difference.inWholeHours),
                modifier = modifier.clickable(onClick = {
                    scope.launch { tooltipState.show() }
                }),
                style = style,
                color = color
            )
            LaunchedEffect(difference) {
                delay((difference.inWholeHours + 1).hours)
                difference = Clock.System.now() - timestamp
            }
        }

        if (!showSeconds && !showMinutes && !showHours) {
            Text(
                timestamp.printShort(),
                modifier = modifier.clickable(onClick = {
                    scope.launch { tooltipState.show() }
                }),
                style = style,
                color = color
            )
        }
    }
}

fun Instant.printShort() = this.format(DateTimeComponents.Format {
    monthName(MonthNames.ENGLISH_ABBREVIATED)
    char(' ')
    day()
    char(',')
    char(' ')
    year()
})


fun Instant.printLong() = this.format(DateTimeComponents.Format {
    monthName(MonthNames.ENGLISH_FULL)
    char(' ')
    day()
    char(',')
    char(' ')
    year()
    char(' ')
    hour()
    char(':')
    minute()
    char(':')
    second()
})
