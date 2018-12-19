package com.seamfix.qrcode

import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions

/**
 * This method, Sets up the required settings by the Face Detector, using high specifications
 * @return FirebaseVisionFaceDetectorOptions containing settings
 */
fun getStrictFaceDetectionOptions(): FirebaseVisionFaceDetectorOptions {
    return with(FirebaseVisionFaceDetectorOptions.Builder()) {
        setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
        setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
        setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
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