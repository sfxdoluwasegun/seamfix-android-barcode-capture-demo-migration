package com.seamfix.qrcode

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import com.seamfix.seamcode.R
import kotlinx.android.synthetic.main.activity_decoded.*
import java.io.File

class DecodedActivity : AppCompatActivity() {

    companion object {
        private const val CAMERA_REQUEST_CODE = 100
    }

    private lateinit var imageFile: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_decoded)

        val rawValue = intent.getStringExtra("value")
        val rawValues = rawValue.split(" ")

        if (rawValues.size == 4) {
            firstname_textview.text = rawValues[0]

            lastname_textview.text = rawValues[1]

            phonenumber_textview.text = rawValues[2]

            val bitmap = rawValues[3].toBitmap()
            imageView.setImageBitmap(bitmap)
        } else {
            firstname_textview.text = rawValue
        }

        match_button.setOnClickListener {
            captureFace()
        }
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

    private fun matchFaces() {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val rotatedBitmap = BitmapFactory.decodeFile(imageFile.absolutePath).rotate(imageFile)

                }
            }
        }
    }
}
