package com.storiq.core.ui.animations

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

object AnimationUtils {

    @Composable
    fun animateSwipeCard(
        targetOffset: Float,
        targetRotation: Float,
        dampingRatio: Float = 0.8f,
        stiffness: Float = 2000f
    ) {
        var offset by remember { mutableStateOf(targetOffset) }
        var rotation by remember { mutableStateOf(targetRotation) }

        val animatedOffset by animateFloatAsState(
            targetValue = targetOffset,
            animationSpec = spring(dampingRatio = dampingRatio, stiffness = stiffness)
        )
        val animatedRotation by animateFloatAsState(
            targetValue = targetRotation,
            animationSpec = spring(dampingRatio = 0.9f, stiffness = 1500f)
        )

        offset = animatedOffset
        rotation = animatedRotation
    }

    @Composable
    fun animateFadeIn(
        visible: Boolean,
        delay: Int = 0,
        duration: Int = 300
    ): Float {
        return animateFloatAsState(
            targetValue = if (visible) 1f else 0f,
            animationSpec = tween(duration, delayMillis = delay)
        ).value
    }

    @Composable
    fun animateSlideIn(
        visible: Boolean,
        direction: SlideDirection = SlideDirection.BOTTOM,
        delay: Int = 0,
        duration: Int = 300
    ): Float {
        return animateFloatAsState(
            targetValue = if (visible) 0f else direction.offset,
            animationSpec = tween(duration, delayMillis = delay)
        ).value
    }

    @Composable
    fun animateScale(
        visible: Boolean,
        delay: Int = 0,
        duration: Int = 200
    ): Float {
        return animateFloatAsState(
            targetValue = if (visible) 1f else 0.8f,
            animationSpec = tween(duration, delayMillis = delay)
        ).value
    }

    @Composable
    fun animateStaggeredList(
        index: Int,
        visible: Boolean,
        baseDelay: Int = 50,
        itemDelay: Int = 30
    ): Pair<Float, Float> {
        val delay = if (visible) index * itemDelay else 0
        val alpha = animateFadeIn(visible, delay)
        val translation = animateSlideIn(visible, SlideDirection.BOTTOM, delay)
        return Pair(alpha, translation)
    }

    enum class SlideDirection(val offset: Float) {
        TOP(-300f),
        BOTTOM(300f),
        LEFT(-300f),
        RIGHT(300f);
    }
}
