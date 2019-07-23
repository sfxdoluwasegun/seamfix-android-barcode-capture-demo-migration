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
import com.google.gson.JsonObject
import com.seamfix.qrcode.callbacks.FaceMatchCallback
import com.seamfix.qrcode.rest.FaceMatchClient
import com.seamfix.qrcode.rest.FaceMatchService
import com.seamfix.seamcode.R
import kotlinx.android.synthetic.main.activity_decoded.*
import retrofit2.Call
import retrofit2.Callback
import java.io.File

class DecodedActivity : AppCompatActivity() {

    companion object {
        private const val CAMERA_REQUEST_CODE = 100
        private const val FACE_MATCH_ENGINE_API_URL = "http://logs.seamfix.com:9293/pred_client/imagepred"
        private const val FACE_MATCH_ENGINE_API_KEY =
            "MOPTihS-DQkfDCNjMCzBM50QgZNC5giTU9apdw1Wr1kGWAm_Q43OzXn31NN5vHCHuR67hWwO3WRrRihxAr-V6w"
    }

    private lateinit var imageFile: File
    private val faceMatchService = FaceMatchClient.getRetrofitInstance()?.create(FaceMatchService::class.java)
    private lateinit var faceMatchRetrofitCall: Call<JsonObject>
    private lateinit var faceInBarcode: Bitmap

    private lateinit var faceMatchCallback: FaceMatchCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_decoded)

        val rawValue = intent.getStringExtra("value")
        val rawValues = rawValue.split(" ")

        if (rawValues.size == 4) {
            firstname_textview.text = rawValues[0]

            lastname_textview.text = rawValues[1]

            phonenumber_textview.text = rawValues[2]

            faceInBarcode = rawValues[3].toBitmap()
            preview.setImageBitmap(faceInBarcode)
        } else {
            firstname_textview.text = rawValue
        }

        match_button.setOnClickListener {
            captureFace()
        }

        faceMatchCallback = object : FaceMatchCallback() {
            override fun onFaceMatchResponse(isMatch: Boolean) {
                Log.e(DecodedActivity::class.simpleName, "$isMatch")
                match_textview.text = "Match: $isMatch"
            }

            override fun onFaceMatchError(message: String) {
                Log.e(DecodedActivity::class.simpleName, message)
            }

            override fun onWebServiceInactive() {
                Log.e(DecodedActivity::class.simpleName, "CountdownTimer Finished")
                faceMatchRetrofitCall.cancel()
            }

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val rotatedBitmap = BitmapFactory.decodeFile(imageFile.absolutePath).rotate(imageFile)
                    matchFaces(faceInBarcode, rotatedBitmap, faceMatchCallback)
                }
            }
        }
    }

    /**
     * This method, is used to match any face with the face contained in this IdCard
     * @param idCardFace to match otherFace with
     * @param otherFace to match with IdCard face
     * @param callback to notify about events
     */
    private fun matchFaces(idCardFace: Bitmap, otherFace: Bitmap, callback: FaceMatchCallback) {

        val jsonObject = JsonObject()
        jsonObject.addProperty("image1", idCardFace.base64())
        jsonObject.addProperty("image2", otherFace.base64())

        FaceMatchClient.startWebServiceCountdownTimer(callback)

        faceMatchService?.let {
            faceMatchRetrofitCall =
                faceMatchService.matchFace(FACE_MATCH_ENGINE_API_KEY, FACE_MATCH_ENGINE_API_URL, jsonObject)
            faceMatchRetrofitCall.enqueue(object : Callback<JsonObject> {
                override fun onFailure(call: Call<JsonObject>, t: Throwable) {

                    FaceMatchClient.stopWebServiceCountdownTimer()

                    callback.onFaceMatchError("Unable to match faces")
                }

                override fun onResponse(call: Call<JsonObject>, response: retrofit2.Response<JsonObject>) {

                    FaceMatchClient.stopWebServiceCountdownTimer()

                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        val message = responseBody?.get("Message")?.asString
                        val isMatch = message == "Match"
                        callback.onFaceMatchResponse(isMatch)
                    } else {
                        callback.onFaceMatchError("Unable to match faces")
                    }
                }
            })
        }
    }
}
