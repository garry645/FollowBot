package com.example.followbot

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetectorOptionsBase.STREAM_MODE
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService

typealias LumaListener = (detectedObjects: MutableList<DetectedObject>) -> Unit

class CameraActivity : AppCompatActivity() {


    private var imageCapture: ImageCapture? = null

    private var outputDirectory: File? = null
    private var cameraExecutor: ExecutorService? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private val address = "98:D3:C1:FD:AE:9A"
    val myUUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
    private var outputStream: OutputStream? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.camera_activity)


        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        connect()

        //startCamera()

        val cameraCaptureButton = findViewById<Button>(R.id.camera_capture_button)
        cameraCaptureButton.setOnClickListener {
            val x = "100.0"
            val y = "99.0"
            val direction = 1
            val data = "$direction"
            writeData(data) }

        //outputDirectory = getOutputDirectory()

        //cameraExecutor = Executors.newSingleThreadExecutor()*/

    }

    private fun connect() {
        bluetoothAdapter?.let {
            if (it.isEnabled) {
                val device = it.getRemoteDevice(address)
                it.cancelDiscovery()
                try {
                    bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(myUUID)

                    bluetoothSocket?.connect()
                    Toast.makeText(applicationContext, "Connected", Toast.LENGTH_SHORT).show()


                    val points = "1"

                } catch( e: java.lang.Exception) {
                    try {
                        bluetoothSocket?.close()
                    } catch(e2: java.lang.Exception) {
                        Toast.makeText(applicationContext, "Failed", Toast.LENGTH_SHORT).show()
                    }
                }

            }
        }
    }

    private fun writeData(data: String) {
        bluetoothAdapter?.let{
            if(it.isEnabled) {
                try {
                    outputStream = bluetoothSocket?.outputStream

                } catch (e: java.lang.Exception) {
                    Log.e(TAG, e.toString())
                }
                try{
                    outputStream?.write(data.toByteArray())
                    outputStream?.flush()
                    Log.e(TAG, "Wrote data: $data")
                } catch (e: java.lang.Exception) {
                    Log.e(TAG, e.toString())
                }
            }

        }
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(FILENAME_FORMAT, Locale.US
            ).format(System.currentTimeMillis()) + ".jpg")

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)

                    val msg = "Photo capture succeeded: $savedUri"

                    var image: InputImage? = null

                    try {
                        image = InputImage.fromFilePath(applicationContext, savedUri)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                    image?.let {

                    }
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                }
            })
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener( {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            var myString: String? = null
            if (myString != null) {
                Log.d(TAG, myString)
            }

            val viewFinder = findViewById<PreviewView>(R.id.viewFinder)
            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            imageCapture = ImageCapture.Builder()
                .build()

            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    cameraExecutor?.let { it1 ->
                        it.setAnalyzer(it1, BallImageAnalyzer { detectedObjects ->
                            Log.d(TAG, "Detected Objects: $detectedObjects")
                        })
                    }
                }

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, imageAnalyzer)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))


    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() } }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor?.shutdown()
    }

    companion object {
        private const val TAG = "CameraXBasic"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }

    private class BallImageAnalyzer(private val listener: LumaListener) : ImageAnalysis.Analyzer {

        val options = ObjectDetectorOptions.Builder()
            .setDetectorMode(STREAM_MODE)
            .build()

        val objectDetector = ObjectDetection.getClient(options)

        private fun ByteBuffer.toByteArray(): ByteArray {
            rewind()    // Rewind the buffer to zero
            val data = ByteArray(remaining())
            get(data)   // Copy the buffer into a byte array
            return data // Return the byte array
        }

        @SuppressLint("UnsafeExperimentalUsageError")
        override fun analyze(imageProxy: ImageProxy) {
            imageProxy.image?.let {
                val mediaImage = it
                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                objectDetector.process(image)
                    .addOnSuccessListener { detectedObjects ->
                        //send data to bot
                        /*if(detectedObjects.size > 0) {
                            Toast.makeText(
                                ,
                                "Detected: ${detectedObjects[0].labels}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }*/
                        Log.e(TAG, detectedObjects.toString())
                        listener(detectedObjects)
                    }
                    .addOnFailureListener {
                        //do nothing (silent fail)
                        Log.e(TAG, "Nothing detected")
                    }

                //image.close()
            } ?: Log.e(TAG, "image null")



        }
    }
}