package com.seamfix.qrcode.enrollment;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraOptions;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.Facing;
import com.otaliastudios.cameraview.Gesture;
import com.otaliastudios.cameraview.GestureAction;
import com.seamfix.seamcode.R;
import com.sf.bio.lib.PictUtil;


public class EnrollmentCameraActivity extends AppCompatActivity {
    ImageButton capture;
    CameraView camera;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.enrollment_activity_camera);

        camera = findViewById(R.id.camera);
        camera.setLifecycleOwner(this);
        camera.mapGesture(Gesture.TAP, GestureAction.FOCUS);
        camera.setFacing(Facing.BACK);
        camera.addCameraListener(cameraListener);

        capture = findViewById(R.id.enrollment_camera_capture_button);
        capture.setVisibility(View.GONE);
        capture.setOnClickListener(v -> {
            camera.captureSnapshot();
        });
    }

    private CameraListener cameraListener = new CameraListener() {
        @Override
        public void onPictureTaken(byte[] jpeg) {
            super.onPictureTaken(jpeg);
            camera.stop();
            byte[]normalizedJpeg = PictUtil.normalizeOrientation(jpeg);
            Bitmap capturedImage = BitmapFactory.decodeByteArray(normalizedJpeg, 0, normalizedJpeg.length);
            Log.e("IMAGE HEIGHT", ""+capturedImage.getHeight());
            displayPreview(capturedImage, normalizedJpeg);
        }

        @Override
        public void onCameraOpened(CameraOptions options) {
            super.onCameraOpened(options);
            capture.setVisibility(View.VISIBLE);
        }
    };

    private void displayPreview(Bitmap bitmap, byte[] imageData) {
        AlertDialog launchDialog = new AlertDialog.Builder(this).create();
        View view = LayoutInflater.from(this).inflate(R.layout.camera_preview_dialog_layout, null, false);
        Button okButton     = view.findViewById(R.id.ok);
        Button retryButton  = view.findViewById(R.id.id_retry);
        ImageView imageView = view.findViewById(R.id.image_preview);
        imageView.setImageBitmap(bitmap);

        okButton.setOnClickListener(v -> {
            String imageStringData = Base64.encodeToString(imageData, Base64.NO_WRAP);
            DataSession.getInstance().getTextData().put(R.layout.enrollment_activity_camera, imageStringData);
            Intent intent = new Intent(EnrollmentCameraActivity.this, PreviewEnrollmentActivity.class);
            startActivity(intent);
        });

        retryButton.setOnClickListener(v -> {
            DataSession.getInstance().getTextData().remove(R.layout.enrollment_activity_camera);
            launchDialog.dismiss();
        });


        launchDialog.setView(view);
        launchDialog.setOnDismissListener(dialog -> {
            camera.start();
        });
        launchDialog.setCancelable(true);
        launchDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String imageData = DataSession.getInstance().getTextData().get(R.layout.enrollment_activity_camera);
        if(imageData != null && !TextUtils.isEmpty(imageData)){
           byte[] data = Base64.decode(imageData, Base64.NO_WRAP);
           Bitmap capturedImage = BitmapFactory.decodeByteArray(data, 0, data.length);
           displayPreview(capturedImage, data);
           return;
        }
        camera.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        camera.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        camera.stop();
    }
}
