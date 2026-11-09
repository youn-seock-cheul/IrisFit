package com.example.myapplication.vision

import android.content.Context
import android.os.SystemClock
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.example.myapplication.data.PreferencesManager
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions

class GazeTracker(
    private val context: Context,
    private val preferencesManager: PreferencesManager,
    private val onGazeResult: (gazeX: Float?, gazeY: Float?, isFaceDetected: Boolean, isBlinkEvent: Boolean) -> Unit
) {

    private val mlKitDetector: FaceDetector
    private var lastAnalyzedTimeMs: Long = 0L
    private var wasEyeClosed: Boolean = false

    init {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setMinFaceSize(0.15f)
            .build()

        mlKitDetector = FaceDetection.getClient(options)
    }

    @OptIn(ExperimentalGetImage::class)
    fun processImageProxy(imageProxy: ImageProxy, screenWidthPx: Int, screenHeightPx: Int) {
        val now = SystemClock.elapsedRealtime()
        if (now - lastAnalyzedTimeMs < 200L) { // 5Hz sampling
            imageProxy.close()
            return
        }
        lastAnalyzedTimeMs = now

        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            onGazeResult(null, null, false, false)
            return
        }

        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val inputImage = InputImage.fromMediaImage(mediaImage, rotationDegrees)

        mlKitDetector.process(inputImage)
            .addOnSuccessListener { faces ->
                if (faces.isNotEmpty()) {
                    val face = faces[0]
                    val leftEyeOpen = face.leftEyeOpenProbability ?: 0.5f
                    val rightEyeOpen = face.rightEyeOpenProbability ?: 0.5f
                    val avgEyeOpen = (leftEyeOpen + rightEyeOpen) / 2f

                    // EAR 깜빡임 이벤트 감지 (눈 감김 < 0.25f -> 눈 떠짐 > 0.50f)
                    var isBlinkEvent = false
                    if (avgEyeOpen < 0.25f) {
                        wasEyeClosed = true
                    } else if (avgEyeOpen > 0.50f && wasEyeClosed) {
                        wasEyeClosed = false
                        isBlinkEvent = true
                    }

                    // 사용자가 정면을 응시하고 눈을 뜨고 있는 경우
                    if (leftEyeOpen > 0.15f || rightEyeOpen > 0.15f) {
                        val gazeX = screenWidthPx / 2f
                        val gazeY = screenHeightPx / 2f
                        onGazeResult(gazeX, gazeY, true, isBlinkEvent)
                    } else {
                        // 눈을 감았거나 시선 감지 불가능
                        onGazeResult(null, null, false, isBlinkEvent)
                    }
                } else {
                    // 고개를 돌렸거나 폰을 내려놓아 얼굴/눈동자가 카메라에 보이지 않는 경우
                    onGazeResult(null, null, false, false)
                }
            }
            .addOnFailureListener {
                onGazeResult(null, null, false, false)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    fun close() {
        try {
            mlKitDetector.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
