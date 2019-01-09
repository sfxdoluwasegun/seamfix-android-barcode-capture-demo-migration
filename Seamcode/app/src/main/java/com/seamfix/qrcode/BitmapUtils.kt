package com.seamfix.qrcode

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import android.util.Base64
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


/**
 * This method, is used to compress a Bitmap
 * @param quality of the Bitmap compression
 */
fun Bitmap.compress(quality: Int): Bitmap {
    val bmpStream = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.JPEG, quality, bmpStream)
    return BitmapFactory.decodeByteArray(bmpStream.toByteArray(), 0, bmpStream.size())
}

/**
 * This method, is used to rotate an Image
 * @param imageFile
 */
fun Bitmap.rotate(imageFile: File): Bitmap {

    val exifInterface = ExifInterface(imageFile.absolutePath)
    val rotation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    val rotationDegree = when (rotation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90
        ExifInterface.ORIENTATION_ROTATE_180 -> 180
        ExifInterface.ORIENTATION_ROTATE_270 -> 270
        else -> 0
    }

    val matrix = Matrix()
    if (rotation.toFloat() != 0f) {
        matrix.preRotate(rotationDegree.toFloat())
    }

    return Bitmap.createBitmap(this, 0, 0, this.width, this.height, matrix, true)
}

/**
 * This method, is used to crop out the detected face from a photo
 * @param fireBaseVisionFace containing the bounding box of a detected faces
 * @return croppedFace
 */
fun Bitmap.cropDetectedFace(fireBaseVisionFace: FirebaseVisionFace): Bitmap {
    val boundingBox = fireBaseVisionFace.boundingBox
    val croppedFace = Bitmap.createBitmap(boundingBox.width(), boundingBox.height(), Bitmap.Config.ARGB_8888)
    val canvas = Canvas(croppedFace)
    canvas.drawBitmap(this, boundingBox, Rect(0, 0, croppedFace.width, croppedFace.height), null)
    return croppedFace
}

/**
 * This method, is used to resize a Bitmap to a desired size
 *
 * @param size desired size
 * @return Bitmap that has been resized
 */
fun Bitmap.resize(size: Int): Bitmap {
    if (size > 0) {
        val width = this.width
        val height = this.height
        val ratioBitmap = width.toFloat() / height.toFloat()
        var finalWidth = size
        var finalHeight = size
        if (ratioBitmap < 1) {
            finalWidth = (size.toFloat() * ratioBitmap).toInt()
        } else {
            finalHeight = (size.toFloat() / ratioBitmap).toInt()
        }
        return Bitmap.createScaledBitmap(this, finalWidth, finalHeight, true)
    }
    return this
}

/**
 * This method, is used to convert a Bitmap, to a Base64 format
 * @param quality of the Bitmap compression
 * @return toBase64 string
 */
fun Bitmap.toBase64(quality: Int): String {
    val byteArrayOutputStream = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
    return Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.NO_WRAP)
}

/**
 * This method, saves a Photo and adds it to the Users Gallery
 * @param photo to be saved
 */
fun Bitmap.addPhotoToGallery(context: Context, name: String) {

    val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath
        .plus("/")
        .plus("Seamfix Qrcode")
    val photoFile = File(path, "${name}_${System.currentTimeMillis()}.png")

    this.compress(Bitmap.CompressFormat.PNG, 100, FileOutputStream(photoFile))

    Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { intent ->
        intent.data = Uri.fromFile(photoFile)
        context.sendBroadcast(intent)
    }
}

/**
 * This method, is used to convert a Base64 String to a Bitmap
 * @return Bitmap
 */
fun String.toBitmap(): Bitmap {
    val imageBytes = Base64.decode(this, Base64.DEFAULT)
    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
}

