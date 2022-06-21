package com.smartheard.facedetectorapp

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.smartheard.facedetectorapp.databinding.ActivityMainBinding
import java.io.IOException

class MainActivity : AppCompatActivity() {

    lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        checkPermission()
        startCamera()

        // High-accuracy landmark detection and face classification
        val highAccuracyOpts = FirebaseVisionFaceDetectorOptions.Builder()
            .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
            .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
            .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
            .build()

// Real-time contour detection of multiple faces
        val realTimeOpts = FirebaseVisionFaceDetectorOptions.Builder()
            .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
            .build()

        val image = FirebaseVisionImage.fromMediaImage(mediaImage, rotation)

        val imageTwo: FirebaseVisionImage
        try {
            image = FirebaseVisionImage.fromFilePath(this, uri)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val metadata = FirebaseVisionImageMetadata.Builder()
            .setWidth(480) // 480x360 is typically sufficient for
            .setHeight(360) // image recognition
            .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
            .setRotation(rotation)
            .build()

        val image = FirebaseVisionImage.fromByteBuffer(buffer, metadata)


        val result = detector.detectInImage(image)
            .addOnSuccessListener { faces ->
                // Task completed successfully
                // ...
            }
            .addOnFailureListener { e ->
                // Task failed with an exception
                // ...
            }


    }

    fun hasCameraPermission() = ActivityCompat.checkSelfPermission(this,android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    fun hasWriteToExternalPermission() = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    fun checkPermission(){
        val permissionList = mutableListOf<String>()
        if (!hasCameraPermission()) permissionList.add(android.Manifest.permission.CAMERA)
        if (!hasWriteToExternalPermission()) permissionList.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (permissionList.isNotEmpty()){
            ActivityCompat.requestPermissions(this,permissionList.toTypedArray(),0)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode ==0 && grantResults.isNotEmpty()){
            for (i in grantResults){
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "Permission $i", Toast.LENGTH_SHORT).show()

                    startCamera()
                }
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFeature = ProcessCameraProvider.getInstance(this)
        cameraProviderFeature.addListener({
            val cameraProvider : ProcessCameraProvider = cameraProviderFeature.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider((binding.cameraPreview.surfaceProvider))
            }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            try {

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this,cameraSelector,preview)

            }catch (e:Exception){
                Log.e("MainActivity", "startCamera: ${e.message}", )
            }
        },ContextCompat.getMainExecutor(this))
    }
}