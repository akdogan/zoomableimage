package com.akdogan.zoomableimageview.zoomableImage

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import mutableState

class TransformableEntityStateHolder {

    var size: IntSize by mutableState(IntSize.Zero)

    var containerSize: IntSize by mutableState(IntSize.Zero)

    var scale: Float by mutableState(1f)

    var offset: Offset by mutableState(Offset.Zero)

    val halfContainerWidth: Int
        get() = containerSize.width / 2

    val halfContainerHeight: Int
        get() = containerSize.height / 2

    val scaledHalfWidth: Float
        get() = size.width / 2 * scale

    val scaledHalfHeight: Float
        get() = size.height / 2 * scale

    val widerThanParent: Boolean
        get() = size.width * scale > containerSize.width

    val higherThanParent: Boolean
        get() = size.height * scale > containerSize.height
}
