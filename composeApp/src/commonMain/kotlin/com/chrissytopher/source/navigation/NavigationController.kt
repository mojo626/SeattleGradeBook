package com.chrissytopher.source.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material.swipeable
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.chrissytopher.source.LocalPlatform
import com.chrissytopher.source.getScreenSize
import io.github.aakira.napier.Napier
import kotlinx.coroutines.runBlocking
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun <T> NavigationController(navigationStack: NavigationStack<T>, modifier: Modifier = Modifier, contents: @Composable NavigationControllerScope<T>.() -> Unit) {
    val scope = remember { NavigationControllerScope(navigationStack) }
    val previousScope = remember { NavigationControllerScope(navigationStack, previous = true) }
    val platform = LocalPlatform.current
    val screenSize = getScreenSize()
    Box(modifier) {
        val stack by navigationStack.stackState
        val canGoBack by derivedStateOf { stack.size > 1 }
        platform.BackHandler(canGoBack) {
            navigationStack.popStack()
        }
        var dragOffset = remember { Animatable(0f) }
        Box(Modifier.offset { IntOffset((max(dragOffset.value, 0f).roundToInt()-screenSize.width)/2, 0) }.alpha(dragOffset.value/(screenSize.width.toFloat()/1.5f))) {
            contents(previousScope)
        }
        Box(Modifier.offset { IntOffset(max(dragOffset.value, 0f).roundToInt(), 0) }.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
            contents(scope)
        }
        if (!platform.livingInFearOfBackGestures() && canGoBack) {
            Box(Modifier.fillMaxHeight().width(30.dp).align(Alignment.CenterStart)
                .draggable(
                    rememberDraggableState { runBlocking { dragOffset.snapTo(dragOffset.value + it) } },
                    orientation = Orientation.Horizontal,
                    onDragStopped = {
                        if (dragOffset.value > 0.33f*screenSize.width) {
                            dragOffset.animateTo(screenSize.width.toFloat())
                            dragOffset.snapTo(0f)
                            navigationStack.popStack()
                        } else {
                            dragOffset.animateTo(0f)
                        }
                    }
                ))
        }
    }
}

class NavigationControllerScope<T>(private val navigationStack: NavigationStack<T>, private val previous: Boolean = false) {
    @Composable
    fun composable(route: T, contents: @Composable () -> Unit) {
        val currentRoute by if (previous) navigationStack.previousState else navigationStack.routeState
        val active by derivedStateOf { route == currentRoute }
        if (active) {
            contents()
        }
    }
}

class NavigationStack<T>(private val initialRoute: T) {
    private val navigationStackState = mutableStateOf(listOf(initialRoute))
    private val _routeState: MutableState<T> = mutableStateOf(initialRoute)
    private val _previousState: MutableState<T> = mutableStateOf(initialRoute)
    fun popStack() {
        navigationStackState.value.lastOrNull()?.let { navigationStackState.value -= it }
        if (navigationStackState.value.isEmpty()) {
            navigationStackState.value += initialRoute
        }
        _previousState.value = navigationStackState.value.getOrNull(navigationStackState.value.size-2) ?: initialRoute
        _routeState.value = navigationStackState.value.last()
    }

    fun navigateTo(route: T) {
        navigationStackState.value += route
        _previousState.value = _routeState.value
        _routeState.value = route
    }

    fun clearStack(initialRoute: T) {
        navigationStackState.value = listOf(initialRoute)
        _previousState.value = _routeState.value
        _routeState.value = initialRoute
    }

    val stackState: State<List<T>>
        get() = navigationStackState

    val routeState: State<T>
        get() = _routeState

    val previousState: State<T>
        get() = _previousState
}