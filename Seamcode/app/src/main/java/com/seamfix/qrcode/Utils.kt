package com.seamfix.qrcode

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.support.v4.app.ActivityCompat
import android.util.Base64
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import java.io.ByteArrayOutputStream

/**
 * This method, Sets up the required settings by the Face Detector, using high specifications
 * @return FirebaseVisionFaceDetectorOptions containing settings
 */
fun getStrictFaceDetectionOptions(): FirebaseVisionFaceDetectorOptions {
    return with(FirebaseVisionFaceDetectorOptions.Builder()) {
        setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
        setMinFaceSize(0.15f)
        build()
    }
}

/**
 * This method, is used to configure Firebase Vision Barcode Detector Options
 * @return FirebaseVisionBarcodeDetectorOptions, containing the configuration for Firebase Vision Barcode Detector
 */
fun getBarcodeDetectorOptions(): FirebaseVisionBarcodeDetectorOptions {
    return FirebaseVisionBarcodeDetectorOptions.Builder()
        .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_ALL_FORMATS)
        .build()
}

/**
 * This method, is used to generate a metadata for the FirebaseVisionImage
 */
fun getFirebaseVisionImageMetaData(width: Int, height: Int, rotation: Int): FirebaseVisionImageMetadata {
    return FirebaseVisionImageMetadata.Builder()
        .setWidth(width)
        .setHeight(height)
        .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
        .setRotation(rotation)
        .build()
}

/**
 * This function is used to convert a Bitmap to Base64
 * @return the decoded Base64
 */
fun Bitmap.base64(): String {
    val outputStream = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
    val bytes = outputStream.toByteArray()
    return Base64.encodeToString(bytes, Base64.DEFAULT)
}

/**
 * This function checks for all required permissions and request for the not granted ones
 * @param activity checking permissions
 */
fun checkAllPermissions(activity: Activity) {

    val requiredPermissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA
    )

    val deniedPermissions = requiredPermissions.filter { permission ->
        ActivityCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_DENIED
    }
    if (deniedPermissions.isNotEmpty()) {
        ActivityCompat.requestPermissions(
            activity,
            deniedPermissions.toTypedArray(),
            MainActivity.REQUIRED_PERMISSIONS_REQUEST_CODE
        )
    }
}