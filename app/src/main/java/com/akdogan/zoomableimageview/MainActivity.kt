package com.akdogan.zoomableimageview

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import com.akdogan.zoomableimageview.ui.theme.ZoomableImageViewTheme
import kotlin.math.absoluteValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ZoomableImageViewTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ZoomableImageView()
                }
            }
        }
    }
}

const val maxZoom = 6f
const val minZoom = 1f
const val snapZoomRange = 0.2f

//class MutableStateDelegate<V>(): ReadWriteProperty<MutableState<V>, V> {
//
//    override fun getValue(thisRef: MutableState<V>, property: KProperty<*>): V {
//        return thisRef.value
//    }
//
//    override fun setValue(thisRef: MutableState<V>, property: KProperty<*>, value: V) {
//        thisRef.value = value
//    }
//}
//
//fun <T> mutableState(): ReadWriteProperty<MutableState<T>, T>{
//    val delegate = MutableStateDelegate<T>()
//
//}


enum class MoveDirection {START, TOP, END, BOTTOM}

fun Offset.doesItMoveTo(direction: MoveDirection): Boolean {
    return when (direction) {
        MoveDirection.START -> this.x < 0
        MoveDirection.TOP -> this.y < 0
        MoveDirection.END -> this.x > 0
        MoveDirection.BOTTOM -> this.y > 0
    }
}


class TransformableEntityStateHolder(

) {
    init {
        log("ENTITITY INIT ${(1111..9999).random()} -- if you see this twice somethings fuckend up")
    }

    private val _size: MutableState<IntSize> = mutableStateOf(IntSize.Zero)
    var size: IntSize
        get() = _size.value
        set(value) {
            _size.value = value
        }

    private val _containerSize: MutableState<IntSize> = mutableStateOf(IntSize.Zero)
    var containerSize: IntSize
        get() = _containerSize.value
        set(value) {
            _containerSize.value = value
        }

    private val _scale: MutableState<Float> = mutableStateOf(1f)
    var scale: Float
        get() = _scale.value
        set(value) {
            _scale.value = value
        }

    private val _offset: MutableState<Offset> = mutableStateOf(Offset.Zero)
    var offset: Offset
        get() = _offset.value
        set(value) {
            _offset.value = value
        }

    val halfWidth: Int
        get() = size.width / 2

    val halfHeight: Int
        get() = size.height / 2

    val scaledHalfWidth: Float
        get() = size.width / 2 * scale

    val scaledHalfHeight: Float
        get() = size.height / 2 * scale

    val widerThanParent: Boolean
        get() = size.width * scale > containerSize.width

    val higherThanParent: Boolean
        get() = size.height * scale > containerSize.height
}

@Composable
fun ZoomableImageView() {
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
                    log("onGloballyPositioned container size: ${newSize}")
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
                    val calculatedDistanceFromTop = entity.offset.y + entity.containerSize.height / 2
                    val doesItTouchTop = calculatedDistanceFromTop >= entity.scaledHalfHeight
                    val doesItMoveTowardsBottom = offsetChange.y < 0

                    if (doesItTouchTop && offsetChange.doesItMoveTo(MoveDirection.BOTTOM)) {
                        val yOffset = (entity.scaledHalfHeight - entity.containerSize.height / 2)
                        newOffset = newOffset.copy(y = yOffset)
                    }

                    // prevent bottom of image scrolling away from bottom of parent
                    val calculatedDistanceFromBottom =
                        (entity.offset.y - entity.containerSize.height / 2).absoluteValue
                    val doesItTouchBottom = calculatedDistanceFromBottom >= entity.scaledHalfHeight
                    val doesItMoveTowardsTop = offsetChange.y > 0

                    if (doesItTouchBottom && offsetChange.doesItMoveTo(MoveDirection.TOP)) {
                        val yOffset = (entity.scaledHalfHeight - entity.containerSize.height / 2) * -1
                        newOffset = newOffset.copy(y = yOffset)
                    }

                } else {
                    // if the image is smaller than parent, block vertical scrolling
                    newOffset = newOffset.copy(y = entity.offset.y)
                }

                if (entity.widerThanParent) {

                    // Prevent start of image scrolling away from start of parent
                    val calculatedDistanceFromStart = entity.offset.x + entity.containerSize.width / 2
                    val doesItTouchStart = calculatedDistanceFromStart >= entity.scaledHalfWidth

                    if (doesItTouchStart && offsetChange.doesItMoveTo(MoveDirection.END)) {
                        val xOffset = entity.scaledHalfWidth - entity.containerSize.width / 2
                        newOffset = newOffset.copy(x = xOffset)
                    }

                    // Prevent end of image scrolling away from end of parent
                    val calculatedDistanceFromEnd = (entity.offset.x - entity.containerSize.width / 2).absoluteValue
                    val doesItTouchEnd = calculatedDistanceFromEnd >= entity.scaledHalfWidth
                    val doesItMoveTowardsStart = offsetChange.x > 0

                    if (doesItTouchEnd && offsetChange.doesItMoveTo(MoveDirection.START)) {
                        val xOffset = (entity.scaledHalfWidth - entity.halfWidth) * -1
                        newOffset = newOffset.copy(x = xOffset)
                    }

                } else {
                    // if the image is smaller than parent, block horzizontal scrolling
                    newOffset = newOffset.copy(x = entity.offset.x)
                }

                log("offset=${entity.offset} -- scaledOffset=${entity.offset * entity.scale} -- offSetChange=$offsetChange -- newOffset=$newOffset")
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

        Image(
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
                        log("onGloballyPositioned size: $newSize")
                        entity.size = newSize
                    }
                }
                .transformable(state = state),
            painter = painterResource(id = R.drawable.testimage),
            contentDescription = null,
        )
    }
}

private fun Float.shouldSnapToDefault(): Boolean = this - minZoom <= snapZoomRange


private fun log(msg: String) = Log.d("ZoomableImageView", msg)
