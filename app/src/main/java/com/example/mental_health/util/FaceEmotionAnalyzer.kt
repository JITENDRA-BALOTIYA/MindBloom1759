package com.example.mental_health.util

import android.content.Context
import android.graphics.*
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

class FaceEmotionAnalyzer(
    context: Context,
    private val onEmotionDetected: (emotion: String, confidence: Float) -> Unit
) : ImageAnalysis.Analyzer {

    private val emotionClassifier = EmotionClassifier(context)
    
    // Performance optimized face detector options
    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
        .setMinFaceSize(0.15f)
        .build()

    private val detector = FaceDetection.getClient(options)

    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        // CameraX handles rotation for us in InputImage, 
        // but we need to ensure the Bitmap we crop from is also rotated correctly.
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val image = InputImage.fromMediaImage(mediaImage, rotationDegrees)

        detector.process(image)
            .addOnSuccessListener { faces ->
                if (faces.isNotEmpty()) {
                    // Logic: Focus on the largest face (most likely the user)
                    val largestFace = faces.maxByOrNull {
                        it.boundingBox.width() * it.boundingBox.height()
                    } ?: return@addOnSuccessListener

                    // Use the built-in ImageProxy.toBitmap() available in CameraX 1.3+
                    val bitmap = imageProxy.toBitmap()
                    
                    val faceBitmap = cropFace(bitmap, largestFace)
                    if (faceBitmap != null) {
                        // ML Inference
                        val (emotion, confidence) = emotionClassifier.classify(faceBitmap)
                        
                        // Confidence Filtering: Avoid low-confidence jitter
                        if (confidence > 0.40f) {
                            onEmotionDetected(emotion, confidence)
                        } else {
                            onEmotionDetected("Neutral", 0.5f)
                        }
                    }
                } else {
                    onEmotionDetected("Scanning...", 0f)
                }
            }
            .addOnFailureListener { e ->
                Log.e("FaceEmotionAnalyzer", "Face detection failed: ${e.message}")
            }
            .addOnCompleteListener {
                // Critical: Always close the imageProxy to prevent pipeline stall
                imageProxy.close()
            }
    }

    private fun cropFace(bitmap: Bitmap, face: Face): Bitmap? {
        return try {
            val rect = face.boundingBox
            
            // Add a 10% margin to the crop to ensure the model sees the whole face
            val margin = (rect.width() * 0.1f).toInt()
            val left = (rect.left - margin).coerceAtLeast(0)
            val top = (rect.top - margin).coerceAtLeast(0)
            val right = (rect.right + margin).coerceAtMost(bitmap.width)
            val bottom = (rect.bottom + margin).coerceAtMost(bitmap.height)
            
            val width = right - left
            val height = bottom - top

            if (width <= 0 || height <= 0) return null

            Bitmap.createBitmap(bitmap, left, top, width, height)
        } catch (e: Exception) {
            Log.e("FaceEmotionAnalyzer", "Cropping failed: ${e.message}")
            null
        }
    }
}
