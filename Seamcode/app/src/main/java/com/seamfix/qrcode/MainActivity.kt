package com.seamfix.qrcode

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.app.DialogFragment
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.seamfix.seamcode.R

class MainActivity : AppCompatActivity() {

    companion object {
        const val CAMERA_ACTIVITY_REQUEST_CODE = 100
        const val REQUIRED_PERMISSIONS_REQUEST_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkAllPermissions(this)

        val encodeCardview = findViewById<CardView>(R.id.encode_cardview)
        val decodeCardview = findViewById<CardView>(R.id.decode_cardview)

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
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            CAMERA_ACTIVITY_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val value = data?.getStringExtra("value")
                    Toast.makeText(this, value, Toast.LENGTH_LONG).show()
                }

            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        if (requestCode == REQUIRED_PERMISSIONS_REQUEST_CODE) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        permission
                    ) == PackageManager.PERMISSION_DENIED
                ) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                        supportFragmentManager.let {
                            val dialog = RequestPermissionsDialog()
                            dialog.isCancelable = false
                            dialog.show(it, "verification")
                        }
                        break
                    } else {
                        supportFragmentManager.let {
                            val dialog = GoToSettingsPermissionsDialog()
                            dialog.isCancelable = false
                            dialog.show(it, "verification")
                        }
                        break
                    }
                }
            }
        }
    }


    /**
     * This class displays a dialog that displays information about permission request
     */
    class RequestPermissionsDialog : DialogFragment() {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

            val builder = AlertDialog.Builder(activity)
            builder.setMessage(R.string.request_permissions_information)
                .setPositiveButton(R.string.request_permissions_information_positive) { _, _ ->
                    checkAllPermissions(requireActivity())
                }
                .setNegativeButton(R.string.request_permissions_information_negative) { _, _ ->
                    activity?.finish()
                }
            return builder.create()
        }

    }

    /**
     * This class displays a dialog that displays information about permission request
     */
    class GoToSettingsPermissionsDialog : DialogFragment() {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

            val builder = AlertDialog.Builder(activity)
            builder.setMessage(R.string.settings_permissions_information)
                .setPositiveButton(R.string.settings_permissions_information_positive) { _, _ ->
                    activity?.let {
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri = Uri.fromParts("package", it.packageName, null)
                        intent.data = uri
                        it.startActivity(intent)
                    }
                }
                .setNegativeButton(R.string.settings_permissions_information_negative) { _, _ ->
                    activity?.finish()
                }
            return builder.create()
        }

    }
}
