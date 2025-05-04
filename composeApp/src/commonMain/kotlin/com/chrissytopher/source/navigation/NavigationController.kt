package com.chrissytopher.source.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.chrissytopher.source.LocalPlatform
import com.chrissytopher.source.getScreenSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.roundToInt

val animationStyle: AnimationSpec<Float> = spring(stiffness = Spring.StiffnessMedium)

@Composable
fun <T> NavigationController(navigationStack: NavigationStack<T>, modifier: Modifier = Modifier, contents: @Composable NavigationControllerScope<T>.() -> Unit) {
    var coroutineScope = rememberCoroutineScope()
    navigationStack.coroutineScope = coroutineScope
    val scope = remember { NavigationControllerScope(navigationStack) }
    val previousScope = remember { NavigationControllerScope(navigationStack, previous = true) }
    val platform = LocalPlatform.current
    val screenSize = getScreenSize()
    Box(modifier) {
        val stack by navigationStack.stackState
        val canGoBack by derivedStateOf { stack.size > 1 }
        val coroutineScope = rememberCoroutineScope()
        platform.BackHandler(canGoBack) {
            coroutineScope.launch {
                navigationStack.popStack(animateWidth = screenSize.width.toFloat())
            }
        }
        val dragOffset by navigationStack.dragOffset
        val previousAlpha = dragOffset.value/(screenSize.width.toFloat()/1.5f)
        if (previousAlpha != 0f) {
            Box(Modifier.offset { IntOffset((max(dragOffset.value, 0f).roundToInt()-screenSize.width)/2, 0) }.alpha(previousAlpha).clickable(MutableInteractionSource(), null) {  }) {
                contents(previousScope)
            }
        }
        Box(Modifier.offset { IntOffset(max(dragOffset.value, 0f).roundToInt(), 0) }.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainerLow)) {
            contents(scope)
        }
        if (!platform.livingInFearOfBackGestures() && canGoBack) {
            var coroutineScope = rememberCoroutineScope()
            Box(Modifier.fillMaxHeight().width(30.dp).align(Alignment.CenterStart)
                .draggable(
                    rememberDraggableState { coroutineScope.launch { dragOffset.snapTo(dragOffset.value + it) } },
                    orientation = Orientation.Horizontal,
                    onDragStopped = {
                        if (dragOffset.value > 0.33f*screenSize.width) {
                            dragOffset.animateTo(screenSize.width.toFloat(), animationSpec = animationStyle)
                            navigationStack.popStack()
                            navigationStack.dragOffset.value = Animatable(0f)
                        } else {
                            dragOffset.animateTo(0f, animationSpec = animationStyle)
                        }
                    }
                )
            )
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
    var dragOffset = mutableStateOf(Animatable(0f))
    var coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
    fun popStack(animateWidth: Float = 0f) {
        coroutineScope.launch {
            doTransition(0f, animateWidth)
            navigationStackState.value = navigationStackState.value.toMutableList().apply { removeLastOrNull() }
            if (navigationStackState.value.isEmpty()) {
                navigationStackState.value += initialRoute
            }
            _previousState.value = navigationStackState.value.getOrNull(navigationStackState.value.size-2) ?: initialRoute
            _routeState.value = navigationStackState.value.lastOrNull() ?: initialRoute
            dragOffset.value = Animatable(0f)
        }
    }

    fun navigateTo(route: T, animateWidth: Float = 0f) {
        navigationStackState.value += route
        _previousState.value = _routeState.value
        _routeState.value = route
        coroutineScope.launch { doTransition(animateWidth, 0f) }
    }

    fun clearStack(initialRoute: T, animateWidth: Float = 0f) {
        navigationStackState.value = listOf(initialRoute)
        _previousState.value = _routeState.value
        _routeState.value = initialRoute
        coroutineScope.launch { doTransition(animateWidth, 0f) }
    }

    suspend fun doTransition(start: Float, end: Float) {
        if (start != end) {
            dragOffset.value.snapTo(start)
            dragOffset.value.animateTo(end, animationSpec = animationStyle)
        } else {
            dragOffset.value.snapTo(end)
        }
    }

    val stackState: State<List<T>>
        get() = navigationStackState

    val routeState: State<T>
        get() = _routeState

    val previousState: State<T>
        get() = _previousState
}