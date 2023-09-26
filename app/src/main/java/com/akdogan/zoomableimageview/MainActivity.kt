package com.akdogan.zoomableimageview

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import com.akdogan.zoomableimageview.ui.theme.ZoomableImageViewTheme
import com.akdogan.zoomableimageview.zoomableImage.MoveDirection
import com.akdogan.zoomableimageview.zoomableImage.ZoomableImageView
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import mutableState
import kotlin.math.absoluteValue


class MainActivity : ComponentActivity() {

    private val landscapePicture =
        "https://4kwallpapers.com/images/wallpapers/scenery-landscape-mountains-lake-evening-reflections-scenic-4096x2304-8821.jpg"

    private val portraitPicture =
        "https://4kwallpapers.com/images/wallpapers/solar-system-1284x2778-12831.jpg"

    @OptIn(ExperimentalGlideComposeApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ZoomableImageViewTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ZoomableImageView(portraitPicture)
                }
            }
        }
    }
}
