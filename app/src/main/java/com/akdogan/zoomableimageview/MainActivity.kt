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

}

@Composable
fun ZoomableImageView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        val entity = remember {
            TransformableEntityStateHolder()
        }

        val state = rememberTransformableState { zoomChange, offsetChange, _ ->
            val newScale = entity.scale * zoomChange
            entity.scale = when {
                newScale > maxZoom -> maxZoom
                newScale < minZoom -> minZoom
                else -> newScale
            }

            if (entity.scale in minZoom..maxZoom) {
                var newOffset = entity.offset + offsetChange * entity.scale

                // Should block towards start
                val calculatedDistanceFromStart = entity.offset.x + entity.halfWidth
                val doesItTouchStart = calculatedDistanceFromStart >= entity.scaledHalfWidth
                val doesItMoveTowardsEnd = offsetChange.x < 0

                if (doesItTouchStart && !doesItMoveTowardsEnd) {
                    val xOffset = entity.scaledHalfWidth - entity.halfWidth
                    newOffset = newOffset.copy(x = xOffset)
                }

                // Should block towards end
                val calculatedDistanceFromEnd = (entity.offset.x - entity.halfWidth).absoluteValue
                val doesItTouchEnd = calculatedDistanceFromEnd >= entity.scaledHalfWidth
                val doesItMoveTowardsStart = offsetChange.x > 0

                if (doesItTouchEnd && !doesItMoveTowardsStart) {
                    val xOffset = (entity.scaledHalfWidth - entity.halfWidth) * -1
                    newOffset = newOffset.copy(x = xOffset)
                }

                // Should block towards top
                val calculatedDistanceFromTop = entity.offset.y + entity.halfHeight
                val doesItTouchTop = calculatedDistanceFromTop >= entity.scaledHalfHeight
                val doesItMoveTowardsBottom = offsetChange.y < 0

                if (doesItTouchTop && !doesItMoveTowardsBottom) {
                    val yOffset = (entity.scaledHalfHeight - entity.halfHeight)
                    newOffset = newOffset.copy(y = yOffset)
                }

                // Should block towards bottom
                val calculatedDistanceFromBottom =
                    (entity.offset.y - entity.halfHeight).absoluteValue
                val doesItTouchBottom = calculatedDistanceFromBottom >= entity.scaledHalfHeight
                val doesItMoveTowardsTop = offsetChange.y > 0

                if (doesItTouchBottom && !doesItMoveTowardsTop) {
                    val yOffset = (entity.scaledHalfHeight - entity.halfHeight) * -1
                    newOffset = newOffset.copy(y = yOffset)
                }

                log("offset=${entity.offset} -- offSetChange=$offsetChange -- newOffset=$newOffset")

                entity.offset = newOffset
            }
        }


        LaunchedEffect(key1 = state.isTransformInProgress) {
            val shouldSnapToMinZoom = entity.scale - minZoom <= snapZoomRange
            if (!state.isTransformInProgress && shouldSnapToMinZoom) {
//                   todo state.animateZoomBy()
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
                        log("onGloballyPositioned size: ${it.size}")
                        entity.size = it.size
                    }
                }
                .transformable(state = state)
                .fillMaxSize(),
            painter = painterResource(id = R.drawable.testportrait),
            contentDescription = null,
        )
    }
}

private fun Float.shouldSnapToDefault(): Boolean = this - minZoom <= snapZoomRange


private fun log(msg: String) = Log.d("ZoomableImageView", msg)
