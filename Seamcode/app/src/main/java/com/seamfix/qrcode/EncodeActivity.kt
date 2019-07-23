package com.seamfix.qrcode

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
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
    private lateinit var finalBitmap: Bitmap
    private lateinit var imageView: ImageView
    private lateinit var faceDetector: FirebaseVisionFaceDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_encode)

        imageView = findViewById(R.id.image)
        val qrCodeImageView = findViewById<ImageView>(R.id.qrcode_bitmap)
        val captureTextview = findViewById<TextView>(R.id.capture_button)
        val encodeButton = findViewById<Button>(R.id.encode_button)

        captureTextview.setOnClickListener {
            captureFace()
        }

        encodeButton.setOnClickListener {

            // Get Users Credentials
            val viewCount = parent_layout.childCount
            val listOfCredentials = mutableListOf<String>()
            for (position in 0..viewCount) {
                val view = parent_layout.getChildAt(position)
                if (view is EditText) {
                    listOfCredentials.add(view.text.toString())
                }
            }

            // Convert Bitmap to Bas64
            val face = finalBitmap.toBase64(20)

            listOfCredentials.add(face)

            val fullCredentials = listOfCredentials.joinToString(" ", "", "")
            Log.e(EncodeActivity::class.java.simpleName, "${fullCredentials.length}")

            val qrCode = QRCode.from(fullCredentials)
                .to(ImageType.PNG)
                .withSize(400, 400)
                .bitmap()

            qrCodeImageView.setImageBitmap(qrCode)

            qrCode.addPhotoToGallery(this, firstname_editText.text.toString())

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
     * This method, is used to detect a Face in a picture and crop out the Face as well as resize it
     * @param face to detect and crop
     */
    private fun detectFace(face: Bitmap) {

        // Show progress bar and disable encode button
        progressBar.visibility = View.VISIBLE
        encode_button.isEnabled = false

        /* Run a Face detection operation on the fireBaseVisionImage, the compress, crop and resize the detected Face*/
        val fireBaseVisionImage = FirebaseVisionImage.fromBitmap(face)
        val task = faceDetector.detectInImage(fireBaseVisionImage)
        Thread {
            val result = Tasks.await(task)
            if (result.size > 0) {
                finalBitmap = face.compress(50)
                    .cropDetectedFace(result[0])
                    .resize(120)

                // Hide progress bar, enable encode button and set compressed Bitmap on the Image view
                runOnUiThread {
                    progressBar.visibility = View.INVISIBLE
                    encode_button.isEnabled = true
                    imageView.setImageBitmap(finalBitmap)
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
