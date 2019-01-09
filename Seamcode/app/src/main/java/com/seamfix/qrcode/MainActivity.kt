package com.seamfix.qrcode

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.seamfix.seamcode.R

class MainActivity : AppCompatActivity() {

    companion object {
        const val  CAMERA_ACTIVITY_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val encodeCardview = findViewById<CardView>(R.id.encode_cardview)
        val decodeCardview  = findViewById<CardView>(R.id.decode_cardview)

        encodeCardview.setOnClickListener {
            Intent(this, EncodeActivity::class.java).also { intent ->
                startActivity(intent)
            }
        }

        decodeCardview.setOnClickListener {
            Intent(this, CameraActivity::class.java).also { intent ->
                startActivityForResult(intent, CAMERA_ACTIVITY_REQUEST_CODE)
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA), 100)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode) {
            CAMERA_ACTIVITY_REQUEST_CODE -> {
                if(resultCode == Activity.RESULT_OK) {
                    val value = data?.getStringExtra("value")
                    Toast.makeText(this, value, Toast.LENGTH_LONG).show()
                }

            }
        }
    }
}
