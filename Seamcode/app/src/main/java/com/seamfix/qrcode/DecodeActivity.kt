package com.seamfix.qrcode

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.util.SparseIntArray
import android.view.Surface
import androidx.annotation.RequiresApi
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.Gesture
import com.otaliastudios.cameraview.GestureAction
import com.seamfix.seamcode.R

class DecodeActivity : AppCompatActivity() {

    companion object {
        private const val REAR_CAMERA_ID = 1
    }

    private lateinit var detector: FirebaseVisionBarcodeDetector

    // Vibrator and Vibrator Effect (devices running Oreo and above)
    private lateinit var vibrator: Vibrator
    private lateinit var vibratorEffect: VibrationEffect

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_decode)

        // Obtain instance of Camera, and set it's LifeCycle owner i.e When the DecodeActivity, is destroyed, the Camera is also destroyed
        val camera = findViewById<CameraView>(R.id.camera)
        camera.setLifecycleOwner(this)
        camera.mapGesture(Gesture.TAP, GestureAction.FOCUS) // Enable tap to focus Camera

        // Obtain an instance of the firebase instance using the detector options set
        val detectorOptions = getBarcodeDetectorOptions()
        detector = FirebaseVision.getInstance().getVisionBarcodeDetector(detectorOptions)

        // Set up vibration and vibration feedback
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibratorEffect = VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
        }

        // Get device adjusted rotation
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = cameraManager.cameraIdList[REAR_CAMERA_ID]
        val adjustedRotation = FirebaseBarcodeDetector().getRotationCompensation(cameraId, this, this)

        // Attach a listener for incoming Frames from the Camera source
        var hasDetectedBarcode = false
        camera.addFrameProcessor { frame ->

            try {
               // val frozenFrame = frame.freeze()

                val data = frame.data
                val size = frame.size

                // Generate Metadata
                val metaData = getFirebaseVisionImageMetaData(size.width, size.height, adjustedRotation)

                // Generate Image from metadata
                val image = FirebaseVisionImage.fromByteArray(data, metaData)

                // Run detection
                val task = detector.detectInImage(image)

                //frozenFrame.release()

                task.addOnSuccessListener(this) { barcodes ->

                    if (barcodes.isNotEmpty()) {

                        if (hasDetectedBarcode.not()) {

                            vibrate()

                            val rawValue = barcodes[0].rawValue
                            Intent(this, ResultActivity::class.java).also { intent ->
                                intent.putExtra("value", rawValue)
                                startActivity(intent)
                            }

                            finish()

                            hasDetectedBarcode = true
                        }
                    }
                }
            } catch (ex: NullPointerException) {
                // Do nothing
            }

        }
    }

    /**
     * This method, is used to vibrate a device
     */
    private fun vibrate() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(vibratorEffect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(100)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        detector.close()
    }

    inner class FirebaseBarcodeDetector {

        private val ORIENTATIONS_TABLE = SparseIntArray().apply {
            append(Surface.ROTATION_0, 90)
            append(Surface.ROTATION_90, 0)
            append(Surface.ROTATION_180, 270)
            append(Surface.ROTATION_270, 180)
        }

        /**
         * Get the angle by which an image must be rotated given the device's current
         * orientation.
         */
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Throws(CameraAccessException::class)
        fun getRotationCompensation(cameraId: String, activity: AppCompatActivity, context: Context): Int {
            // Get the device's current rotation relative to its "native" orientation.
            // Then, from the ORIENTATIONS_TABLE table, look up the angle the image must be
            // rotated to compensate for the device's rotation.
            val deviceRotation = activity.windowManager.defaultDisplay.rotation
            var rotationCompensation = ORIENTATIONS_TABLE[deviceRotation]

            // On most devices, the sensor orientation is 90 degrees, but for some
            // devices it is 270 degrees. For devices with a sensor orientation of
            // 270, rotate the image an additional 180 ((270 + 270) % 360) degrees.
            val cameraManager = context.getSystemService(CAMERA_SERVICE) as CameraManager
            val sensorOrientation = cameraManager
                .getCameraCharacteristics(cameraId)
                .get(CameraCharacteristics.SENSOR_ORIENTATION)!!
            rotationCompensation = (rotationCompensation + sensorOrientation + 270) % 360

            // Return the corresponding FirebaseVisionImageMetadata rotation value.
            return when (rotationCompensation) {
                0 -> FirebaseVisionImageMetadata.ROTATION_0
                90 -> FirebaseVisionImageMetadata.ROTATION_90
                180 -> FirebaseVisionImageMetadata.ROTATION_180
                270 -> FirebaseVisionImageMetadata.ROTATION_270
                else -> {
                    FirebaseVisionImageMetadata.ROTATION_0
                    Log.e(DecodeActivity::class.simpleName, "Bad rotation value: $rotationCompensation")
                }
            }
        }

    }


}
