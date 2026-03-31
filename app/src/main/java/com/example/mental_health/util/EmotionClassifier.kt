package com.example.mental_health.util

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.TransformToGrayscaleOp
import java.nio.MappedByteBuffer

class EmotionClassifier(context: Context) {
    private var interpreter: Interpreter? = null
    private val labels = listOf("Angry", "Disgust", "Fear", "Happy", "Sad", "Surprise", "Neutral")
    
    // Model input shape (Adjust based on your specific model)
    private val inputSize = 48 
    
    init {
        try {
            val model: MappedByteBuffer = FileUtil.loadMappedFile(context, "emotion_model.tflite")
            val options = Interpreter.Options().apply {
                setNumThreads(4)
            }
            interpreter = Interpreter(model, options)
            Log.d("EmotionClassifier", "Model loaded successfully")
        } catch (e: Exception) {
            Log.e("EmotionClassifier", "Error loading model: ${e.message}")
        }
    }

    fun classify(faceBitmap: Bitmap): Pair<String, Float> {
        if (interpreter == null) return "Model Error" to 0f

        try {
            // 1. Preprocess: Resize and Convert to Grayscale (most FER models use grayscale 48x48)
            val imageProcessor = ImageProcessor.Builder()
                .add(ResizeOp(inputSize, inputSize, ResizeOp.ResizeMethod.BILINEAR))
                .add(TransformToGrayscaleOp())
                .build()

            var tensorImage = TensorImage(org.tensorflow.lite.DataType.FLOAT32)
            tensorImage.load(faceBitmap)
            tensorImage = imageProcessor.process(tensorImage)

            // 2. Run Inference
            val outputBuffer = Array(1) { FloatArray(labels.size) }
            interpreter?.run(tensorImage.buffer, outputBuffer)

            // 3. Post-process: Find highest confidence
            val results = outputBuffer[0]
            var maxIdx = 0
            var maxConf = 0f
            for (i in results.indices) {
                if (results[i] > maxConf) {
                    maxConf = results[i]
                    maxIdx = i
                }
            }

            Log.d("EmotionClassifier", "Detected: ${labels[maxIdx]} with confidence $maxConf")
            return labels[maxIdx] to maxConf
            
        } catch (e: Exception) {
            Log.e("EmotionClassifier", "Inference error: ${e.message}")
            return "Error" to 0f
        }
    }
    
    fun close() {
        interpreter?.close()
    }
}
