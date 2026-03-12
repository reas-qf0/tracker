package com.reas.tracker2.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.navigation3.runtime.NavBackStack
import kotlinx.serialization.serializer

class ApplicationStateData(
    val startRoute: Route,
    val backStack: NavBackStack<Route>,
    val title: MutableState<String>,
    val snackbarHostState: SnackbarHostState,
    val isFloatingButtonVisible: MutableState<Boolean>,
    val floatingButtonContents: MutableState<(@Composable () -> Unit)>,
    val floatingButtonOnClick: MutableState<() -> Unit>,
) {
    init {
        backStack.add(startRoute)
    }
}

interface ApplicationState {
    fun navigate(route: Route)
    fun goBack()
    fun canNavigateBack(): Boolean
    fun currentTab(): Int
    fun getTitle(): String
    fun setTitle(title: String)

    suspend fun snackbar(
        message: String,
        actionLabel: String? = null,
        withDismissAction: Boolean = false,
        duration: SnackbarDuration = SnackbarDuration.Indefinite,
        onAction: () -> Unit = {},
        onDismiss: () -> Unit = {}
    )

    @Composable
    fun floatingActionButton(
        onClick: () -> Unit = {},
        visibleIf: Boolean = true,
        content: @Composable () -> Unit,
    )
}

class TrackerApplicationState(val state: ApplicationStateData) : ApplicationState {
    override fun navigate(route: Route) {
        if (state.backStack.last() is DialogRoute) {
            state.backStack.removeLastOrNull()
        }
        state.backStack.add(route)
    }

    override fun goBack() {
        if (state.backStack.size > 1)
            state.backStack.removeLastOrNull()
    }

    override fun canNavigateBack() =
        state.backStack.filter { it !is DialogRoute }.size > 1

    override fun currentTab(): Int =
        when (state.backStack.last { it !is DialogRoute }) {
            History -> 0
            is TrackHistory -> 0
            is AlbumInfo -> 1
            is ArtistInfo -> 1
            is Charts -> 1
            is TrackInfo -> 1
            Settings -> 2
            else -> -1
        }

    override fun getTitle() = state.title.value

    override fun setTitle(title: String) {
        state.title.value = title
    }

    fun snackbarHostState() = state.snackbarHostState

    override suspend fun snackbar(
        message: String,
        actionLabel: String?,
        withDismissAction: Boolean,
        duration: SnackbarDuration,
        onAction: () -> Unit,
        onDismiss: () -> Unit
    ) {
        val result = state.snackbarHostState.showSnackbar(message, actionLabel, withDismissAction, duration)
        when (result) {
            SnackbarResult.ActionPerformed -> onAction()
            SnackbarResult.Dismissed -> onDismiss()
        }
    }

    @Composable
    override fun floatingActionButton(
        onClick: () -> Unit,
        visibleIf: Boolean,
        content: @Composable (() -> Unit),
    ) {
        state.floatingButtonContents.value = content
        state.floatingButtonOnClick.value = onClick
        state.isFloatingButtonVisible.value = visibleIf
    }

    @Composable
    fun showActionButton() {
        AnimatedVisibility(
            visible = state.isFloatingButtonVisible.value,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            FloatingActionButton(
                onClick = state.floatingButtonOnClick.value,
                content = state.floatingButtonContents.value
            )
        }
    }
}


class PreviewApplicationState : ApplicationState {
    override fun navigate(route: Route) {}
    override fun goBack() {}
    override fun canNavigateBack() = true
    override fun currentTab() = 0
    override fun getTitle() = "Preview"
    override fun setTitle(title: String) {}

    override suspend fun snackbar(
        message: String,
        actionLabel: String?,
        withDismissAction: Boolean,
        duration: SnackbarDuration,
        onAction: () -> Unit,
        onDismiss: () -> Unit
    ) {}

    @Composable
    override fun floatingActionButton(
        onClick: () -> Unit,
        visibleIf: Boolean,
        content: @Composable (() -> Unit),
    ) {}
}


@Composable
fun rememberApplicationState(
    startRoute: Route,
): TrackerApplicationState {
    val backStack = rememberSerializable(serializer = serializer()) { NavBackStack<Route>() }
    val title = remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val isFloatingButtonVisible = remember { mutableStateOf(false) }
    val floatingButtonContents = remember { mutableStateOf(@Composable {}) }
    val floatingButtonOnClick = remember { mutableStateOf({}) }

    return remember(startRoute) {
        TrackerApplicationState(ApplicationStateData(
            startRoute = startRoute,
            backStack = backStack,
            title = title,
            snackbarHostState = snackbarHostState,
            isFloatingButtonVisible = isFloatingButtonVisible,
            floatingButtonContents = floatingButtonContents,
            floatingButtonOnClick = floatingButtonOnClick,
        ))
    }
}