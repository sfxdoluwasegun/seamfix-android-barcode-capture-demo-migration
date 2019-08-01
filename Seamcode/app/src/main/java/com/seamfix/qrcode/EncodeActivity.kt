package com.seamfix.qrcode

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.*
import com.google.android.gms.tasks.Tasks
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector
import com.seamfix.seamcode.R
import kotlinx.android.synthetic.main.activity_encode.*
import net.glxn.qrgen.android.QRCode
import net.glxn.qrgen.core.image.ImageType
import java.io.File

class EncodeActivity : AppCompatActivity() {

    companion object {
        private const val CAMERA_REQUEST_CODE = 100
    }

    private lateinit var imageFile: File
    private lateinit var compressedCapturedImage: Bitmap
    private lateinit var capturedImageImageView: ImageView
    private lateinit var faceDetector: FirebaseVisionFaceDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_encode)

        capturedImageImageView = findViewById(R.id.image)
        val bioEditTextView = findViewById<EditText>(R.id.bio_editText)
        val qrCodeImageView = findViewById<ImageView>(R.id.qrcode_bitmap)
        val captureTextView = findViewById<TextView>(R.id.capture_button)
        val encodeButton = findViewById<Button>(R.id.encode_button)

        captureTextView.setOnClickListener {
            captureFace()
        }

        encodeButton.setOnClickListener {

            // Get Users Credentials
            val listOfCredentials = mutableListOf<String>()
            val bioData = bioEditTextView.text.toString().split(Regex.fromLiteral("\n"))
            listOfCredentials.addAll(bioData)

            // Convert Bitmap to Bas64
            listOfCredentials.add(compressedCapturedImage.toBase64(100))
            Log.e(EncodeActivity::class.java.simpleName, "Base64: ${compressedCapturedImage.toBase64(100).length}")

            val fullCredentials = listOfCredentials.joinToString(" ", "", "")
            Log.e(EncodeActivity::class.java.simpleName, "Full Credentials: ${fullCredentials.length}")

            val qrCode = QRCode.from(fullCredentials)
                .to(ImageType.PNG)
                .withSize(400, 400)
                .bitmap()

            qrCodeImageView.setImageBitmap(qrCode)

            qrCode.addPhotoToGallery(this, bio_editText.text.toString())

            Toast.makeText(this, "QR CODE SAVED", Toast.LENGTH_LONG).show()

            finish()
        }

        faceDetector = FirebaseVision.getInstance().getVisionFaceDetector(getStrictFaceDetectionOptions())
    }

    /**
     * This method, is used to capture a picture, using the User's Camera
     */
    private fun captureFace() {

        imageFile = File.createTempFile("originalBitmap", ".jpeg", getExternalFilesDir(Environment.DIRECTORY_PICTURES))

        val uri = FileProvider.getUriForFile(this, "com.seamfix.fileprovider", imageFile)

        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivityForResult(intent, CAMERA_REQUEST_CODE)
        }
    }

    /**
     * This method, is used to detect a Face in a picture and crop out the Face as well as compress resize it
     * @param capturedImage to detect and crop
     */
    private fun detectFace(capturedImage: Bitmap) {

        // Show progress bar and disable encode button
        capture_button.text = "PROCESSING"
        encode_button.isEnabled = false

        /* Run a Face detection operation on the fireBaseVisionImage, the compress, crop and resize the detected Face*/
        val fireBaseVisionImage = FirebaseVisionImage.fromBitmap(capturedImage)
        val task = faceDetector.detectInImage(fireBaseVisionImage)
        Thread {
            val firebaseVisionFaces = Tasks.await(task)
            val firebaseVisionFace = firebaseVisionFaces[0]
            if (firebaseVisionFaces.isNotEmpty()) {
                compressedCapturedImage = capturedImage.compress(100)
                    .crop(firebaseVisionFace)
                    .resize(240)

                // Hide progress bar, enable encode button and set compressedCapturedImage on the Image view
                runOnUiThread {
                    encode_button.text = "CAPTURE"
                    encode_button.isEnabled = true
                    capturedImageImageView.setImageBitmap(compressedCapturedImage)
                }
            }
        }.start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val rotatedBitmap = BitmapFactory.decodeFile(imageFile.absolutePath).rotate(imageFile)
                    detectFace(rotatedBitmap)
                }
            }
        }
    }
}
