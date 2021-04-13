package com.example.followbot

import android.annotation.SuppressLint
import android.media.Image
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage

private class BallImageAnalyzer : ImageAnalysis.Analyzer {

    @SuppressLint("UnsafeExperimentalUsageError")
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun analyze(imageProxy: ImageProxy) {
        var mediaImage: Image? = null
        imageProxy.image?.let {
           mediaImage = it
        }

        mediaImage?.let{
            val image = InputImage.fromMediaImage(it, imageProxy.imageInfo.rotationDegrees)
            // Pass image to an ML Kit Vision API
            // ...
        }
    }
}