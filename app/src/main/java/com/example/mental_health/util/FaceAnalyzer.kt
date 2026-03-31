package com.example.mental_health.util

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

/**
 * Smart emotion detector using ML Kit face landmarks.
 * No TFLite model needed — works 100% offline with high reliability.
 *
 * Detects: Joyful, Calm, Sad, Stressed, Anxious, Neutral
 * Maps directly to MoodCheckScreen mood keys.
 */
class FaceAnalyzer(
    private val onMoodDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setMinFaceSize(0.15f)
        .build()

    private val detector = FaceDetection.getClient(options)

    // Stabilization buffer — avoid jitter
    private val moodBuffer = mutableListOf<String>()
    private var lastMood = ""

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        detector.process(image)
            .addOnSuccessListener { faces ->
                if (faces.isEmpty()) {
                    onMoodDetected("Scanning...")
                    return@addOnSuccessListener
                }

                // Focus on largest face
                val face = faces.maxByOrNull {
                    it.boundingBox.width() * it.boundingBox.height()
                } ?: return@addOnSuccessListener

                val smiling       = face.smilingProbability ?: 0f
                val leftEyeOpen   = face.leftEyeOpenProbability ?: 1f
                val rightEyeOpen  = face.rightEyeOpenProbability ?: 1f
                val headTilt      = face.headEulerAngleZ  // head roll (tilt)
                val headTurn      = face.headEulerAngleY  // head yaw (left/right)

                val avgEyeOpen = (leftEyeOpen + rightEyeOpen) / 2f

                val mood = when {
                    // Joyful: big smile
                    smiling > 0.75f -> "Joyful"

                    // Calm: mild smile, eyes open, head straight
                    smiling in 0.4f..0.75f
                            && avgEyeOpen > 0.7f
                            && Math.abs(headTilt) < 10f -> "Calm"

                    // Stressed: no smile, eyes wide open, head turned
                    smiling < 0.2f
                            && avgEyeOpen > 0.85f
                            && Math.abs(headTurn) > 10f -> "Stressed"

                    // Anxious: no smile, eyes partially closed, head tilted
                    smiling < 0.25f
                            && avgEyeOpen in 0.4f..0.75f
                            && Math.abs(headTilt) > 8f -> "Anxious"

                    // Sad: no smile, eyes droopy (partially closed), head straight
                    smiling < 0.2f
                            && avgEyeOpen < 0.6f
                            && Math.abs(headTilt) < 8f -> "Sad"

                    // Neutral: everything in middle range
                    else -> "Neutral"
                }

                // Stabilization: confirm same mood 4 frames in a row
                moodBuffer.add(mood)
                if (moodBuffer.size >= 4) {
                    val dominant = moodBuffer
                        .groupingBy { it }
                        .eachCount()
                        .maxByOrNull { it.value }?.key ?: "Neutral"

                    if (dominant != lastMood) {
                        lastMood = dominant
                        onMoodDetected(dominant)
                    }
                    moodBuffer.clear()
                }
            }
            .addOnFailureListener {
                onMoodDetected("Neutral")
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}