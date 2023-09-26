package com.akdogan.zoomableimageview.zoomableImage

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import kotlin.math.absoluteValue

@ExperimentalGlideComposeApi
@Composable
fun ZoomableImageView(
    url: String,
    maxZoom: Float = 6f,
    minZoom: Float = 1f,
    snapZoomRange: Float = 0.2f,
) {
    val entity = remember {
        TransformableEntityStateHolder()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .onGloballyPositioned {
                val newSize = it.size
                if (newSize != entity.containerSize) {
                    entity.containerSize = newSize
                }
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {


        val state = rememberTransformableState { zoomChange, offsetChange, _ ->
            val newScale = entity.scale * zoomChange
            // don't allow zooming below min or above max
            entity.scale = when {
                newScale > maxZoom -> maxZoom
                newScale < minZoom -> minZoom
                else -> newScale
            }

            // only allow scrolling if the image is zoomed
            if (entity.scale > minZoom) {
                var newOffset = entity.offset + offsetChange * entity.scale

                if (entity.higherThanParent) {
                    // Prevent top of image scrolling away from top of parent
                    val calculatedDistanceFromTop = entity.offset.y + entity.halfContainerHeight
                    val doesItTouchTop = calculatedDistanceFromTop >= entity.scaledHalfHeight

                    if (doesItTouchTop && offsetChange.doesItMoveTo(MoveDirection.BOTTOM)) {
                        val yOffset = (entity.scaledHalfHeight - entity.halfContainerHeight)
                        newOffset = newOffset.copy(y = yOffset)
                    }

                    // prevent bottom of image scrolling away from bottom of parent
                    val calculatedDistanceFromBottom =
                        (entity.offset.y - entity.halfContainerHeight).absoluteValue
                    val doesItTouchBottom = calculatedDistanceFromBottom >= entity.scaledHalfHeight

                    if (doesItTouchBottom && offsetChange.doesItMoveTo(MoveDirection.TOP)) {
                        val yOffset = (entity.scaledHalfHeight - entity.halfContainerHeight) * -1
                        newOffset = newOffset.copy(y = yOffset)
                    }

                } else {
                    // if the image is smaller than parent, block vertical scrolling
                    newOffset = newOffset.copy(y = entity.offset.y)
                }

                if (entity.widerThanParent) {

                    // Prevent start of image scrolling away from start of parent
                    val calculatedDistanceFromStart = entity.offset.x + entity.halfContainerWidth
                    val doesItTouchStart = calculatedDistanceFromStart >= entity.scaledHalfWidth

                    if (doesItTouchStart && offsetChange.doesItMoveTo(MoveDirection.END)) {
                        val xOffset = entity.scaledHalfWidth - entity.halfContainerWidth
                        newOffset = newOffset.copy(x = xOffset)
                    }

                    // Prevent end of image scrolling away from end of parent
                    val calculatedDistanceFromEnd =
                        (entity.offset.x - entity.halfContainerWidth).absoluteValue
                    val doesItTouchEnd = calculatedDistanceFromEnd >= entity.scaledHalfWidth

                    if (doesItTouchEnd && offsetChange.doesItMoveTo(MoveDirection.START)) {
                        val xOffset = (entity.scaledHalfWidth - entity.halfContainerWidth) * -1
                        newOffset = newOffset.copy(x = xOffset)
                    }

                } else {
                    // if the image is smaller than parent, block horizontal scrolling
                    newOffset = newOffset.copy(x = entity.offset.x)
                }

                entity.offset = newOffset
            }
        }


        LaunchedEffect(key1 = state.isTransformInProgress) {
            val shouldSnapToMinZoom = entity.scale - minZoom <= snapZoomRange
            if (!state.isTransformInProgress && shouldSnapToMinZoom) {
                entity.scale = minZoom
                entity.offset = Offset.Zero
            }
        }


        GlideImage(
            modifier = Modifier
                .graphicsLayer(
                    scaleX = entity.scale,
                    scaleY = entity.scale,
                    translationX = entity.offset.x,
                    translationY = entity.offset.y,
                )
                .onGloballyPositioned {
                    val newSize = it.size
                    if (newSize != entity.size) {
                        entity.size = newSize
                    }
                }
                .transformable(state = state),
            model = url,
            contentDescription = ""
        )
    }
}

private fun Offset.doesItMoveTo(direction: MoveDirection): Boolean {
    return when (direction) {
        MoveDirection.START -> this.x < 0
        MoveDirection.TOP -> this.y < 0
        MoveDirection.END -> this.x > 0
        MoveDirection.BOTTOM -> this.y > 0
    }
}
