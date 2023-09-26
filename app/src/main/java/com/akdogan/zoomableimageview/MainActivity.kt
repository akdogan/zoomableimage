package com.akdogan.zoomableimageview

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.akdogan.zoomableimageview.ui.theme.ZoomableImageViewTheme
import com.akdogan.zoomableimageview.zoomableImage.ZoomableImageView
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi


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
