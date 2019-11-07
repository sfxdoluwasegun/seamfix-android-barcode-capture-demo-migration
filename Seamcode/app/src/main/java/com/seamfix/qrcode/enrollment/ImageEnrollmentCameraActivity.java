package com.seamfix.qrcode.enrollment;


import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraOptions;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.Facing;
import com.otaliastudios.cameraview.Frame;
import com.otaliastudios.cameraview.FrameProcessor;
import com.otaliastudios.cameraview.Gesture;
import com.otaliastudios.cameraview.GestureAction;
import com.otaliastudios.cameraview.Size;
import com.seamfix.qrcode.FaceFeatures;
import com.seamfix.qrcode.TensorUtil;
import com.seamfix.qrcode.mtcnn.Box;
import com.seamfix.qrcode.mtcnn.MTCNN;
import com.seamfix.qrcode.opencv.CVUtil;
import com.seamfix.seamcode.R;
import com.sf.bio.lib.PictUtil;
import com.sf.bio.lib.util.FileUtils;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Vector;


public class ImageEnrollmentCameraActivity extends AppCompatActivity {
    ImageButton capture;
    CameraView camera;
    MTCNN mtcnn;
    TensorUtil tensorUtil;
    boolean cameraCaptured = false;
    byte[] model;
    ArrayList<Bitmap> sampleBitmaps = new ArrayList<>();
    private ProgressDialog progressDialog;
    ProgressBar progressBar;
    static final int IMAGE_SAMPLES = 10;
    private ImageCaptureMode mode;
    private LinearLayout frameProcessorBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.image_enrollment_activity_camera);

        camera            = findViewById(R.id.camera);
        progressBar       = findViewById(R.id.id_progress_bar);
        frameProcessorBar = findViewById(R.id.id_frame_processor_bar);


        mode = ImageCaptureMode.SHUTTER;
        camera.setLifecycleOwner(this);
        camera.mapGesture(Gesture.TAP, GestureAction.FOCUS);
        camera.setFacing(Facing.BACK);
        progressBar.setProgress(0);
        setCaptureMode();
        capture = findViewById(R.id.enrollment_camera_capture_button);
        capture.setVisibility(View.GONE);
        capture.setOnClickListener(v -> camera.captureSnapshot());
        mtcnn = new MTCNN(getAssets());
        tensorUtil = TensorUtil.getInstance();
        model = FileUtils.readAsset(this, "face_model.pb");
    }


    /**
     * Callback instance for camera view operations
     */
    private CameraListener cameraListener = new CameraListener() {
        @Override
        public void onPictureTaken(byte[] jpeg) {
            super.onPictureTaken(jpeg);
            camera.stop();
            byte[] normalizedJpeg = PictUtil.normalizeOrientation(jpeg);
            Bitmap capturedImage = BitmapFactory.decodeByteArray(normalizedJpeg, 0, normalizedJpeg.length);
            Log.e("IMAGE HEIGHT", "" + capturedImage.getHeight());

            try {
                Vector<Box> boxes = mtcnn.detectFaces(capturedImage, 40);
                if (boxes.size() == 1) {
                    Box faceBox = boxes.get(0);
                    com.seamfix.qrcode.mtcnn.Utils.drawRect(capturedImage, faceBox.transform2Rect());
                    com.seamfix.qrcode.mtcnn.Utils.drawPoints(capturedImage, faceBox.landmark);
                    Rect rect = faceBox.transform2Rect();
                    Bitmap croppedBitmap = Bitmap.createBitmap(capturedImage, rect.left, rect.top, rect.width(), rect.height());
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(croppedBitmap, 64, 64, false);

                    byte[]imageByte = PictUtil.convertBitmapToByteArray(scaledBitmap);
                    String imageString = Base64.encodeToString(imageByte, Base64.NO_WRAP);
                    String fileName = FaceFeatures.getFaceModelFileName(ImageEnrollmentCameraActivity.this);
                    Log.e("FILE:", "NAME: " + fileName);
                    Log.e("IMAGE:", "BASE 64: " + imageString);

                    float[] features = FaceFeatures.getInstance().generatepcafeatures(fileName, imageString);
                    displayPreview(capturedImage, normalizedJpeg, features);
                }else{
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(ImageEnrollmentCameraActivity.this);
                    alertDialog.setTitle(R.string.title_text_image_portrait);
                    alertDialog.setMessage(getString(R.string.message_text_portarit_error));
                    alertDialog.setPositiveButton(R.string.ok, (dialog, which) -> camera.start());
                    alertDialog.show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        @Override
        public void onCameraOpened(CameraOptions options) {
            super.onCameraOpened(options);
            if(mode == ImageCaptureMode.SHUTTER){
                capture.setVisibility(View.VISIBLE);
                frameProcessorBar.setVisibility(View.GONE);
            }else{
                capture.setVisibility(View.GONE);
                frameProcessorBar.setVisibility(View.VISIBLE);
            }
        }
    };


    /**
     * Camera Frame processor listener
     */
    private FrameProcessor frameProcessor = new FrameProcessor() {
        @Override
        public void process(@NonNull Frame frame) {
            byte[] data  = frame.getData();
            int rotation = frame.getRotation();
            Size size    = frame.getSize();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, size.getWidth(), size.getHeight(), null);
            yuvImage.compressToJpeg(new Rect(0, 0, size.getWidth(), size.getHeight()), 90, out);
            byte[] imageBytes = out.toByteArray();
            Bitmap imageBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            Bitmap bit = PictUtil.rotateImage(imageBitmap, rotation);
            Bitmap bm = com.seamfix.qrcode.mtcnn.Utils.copyBitmap(bit);

            try {
                Vector<Box> boxes = mtcnn.detectFaces(bm, 40);
                if(boxes.size() == 1 && !cameraCaptured){
                    Log.e("FACE-DETECTED", "COUNT IS: " + sampleBitmaps.size());
                    capture.post(() -> {
                        int progress = (sampleBitmaps.size()* 100)/IMAGE_SAMPLES;
                        progressBar.setProgress(progress);
                    });

                    Rect rect = boxes.get(0).transform2Rect();
                    Bitmap croppedBitmap = Bitmap.createBitmap(bm, rect.left, rect.top, rect.width(), rect.height());
                    Bitmap scaledBitmap  = Bitmap.createScaledBitmap(croppedBitmap, 200, 200, false);
                    if(sampleBitmaps.size() < IMAGE_SAMPLES){
                        sampleBitmaps.add(scaledBitmap);
                        return;
                    }

                    capture.post(() -> showProgressDialog("Processing image", false));
                    byte [] imageData = PictUtil.convertBitmapToByteArray(scaledBitmap);
                    float[] features = new CVUtil().generateEigenFace(sampleBitmaps, 50);
                    capture.post(() -> {
                        stopProgressDialog();
                        displayPreview(bm, imageData, features);
                    });
                    sampleBitmaps.clear();
                    cameraCaptured = true;
                }else{
                    sampleBitmaps.clear();
                }
            } catch (Exception e) {
                e.printStackTrace();
                capture.post(() -> {
                    stopProgressDialog();
                    Toast.makeText(ImageEnrollmentCameraActivity.this, "Hmm, something went wrong please try again", Toast.LENGTH_SHORT).show();
                });
            }
        }
    };


    /**
     * Displays captured image dialog, with an option to retry the capture or continue
     *
     * @param bitmap the captured bitmap image
     * @param features face feature extracted from the bitmap
     * @param imageData byte array representation of the Bitmap
     */
    @UiThread
    private void displayPreview(Bitmap bitmap, byte[] imageData, float[] features) {
        AlertDialog launchDialog = new AlertDialog.Builder(this).create();
        View view = LayoutInflater.from(this).inflate(R.layout.camera_preview_dialog_layout, null, false);
        Button okButton     = view.findViewById(R.id.ok);
        Button retryButton  = view.findViewById(R.id.id_retry);
        ImageView imageView = view.findViewById(R.id.image_preview);
        imageView.setImageBitmap(bitmap);

        okButton.setOnClickListener(v -> {
            try {
                String imageStringData = Base64.encodeToString(imageData, Base64.NO_WRAP);
                String imageTemplate = new Gson().toJson(features);
                DataSession.getInstance().getTextData().put(R.layout.enrollment_activity_camera, imageStringData);
                DataSession.getInstance().getTextData().put(R.id.babs_template, imageTemplate);

                Intent intent = new Intent(ImageEnrollmentCameraActivity.this, EnrollmentFingerTypeActivity.class);
                startActivity(intent);
                launchDialog.dismiss();
            }catch (Exception e){
                e.printStackTrace();
            }
        });

        retryButton.setOnClickListener(v -> {
            DataSession.getInstance().getTextData().remove(R.layout.enrollment_activity_camera);
            cameraCaptured =  false;
            progressBar.setProgress(0);
            launchDialog.dismiss();
        });

        launchDialog.setView(view);
        launchDialog.setOnDismissListener(dialog -> camera.start());
        launchDialog.setCancelable(false);
        launchDialog.show();
    }

    /**
     * Sets the capture mode of this Image capture session
     * determines if shutter should be used or frame processing which leads to auto-capture
     */
    private void setCaptureMode(){
        switch (mode){
            case SHUTTER:{
                if(camera != null) {
                    camera.addCameraListener(cameraListener);
                }
                break;
            }

            case FRAME_PROCESSING:{
                if(camera != null) {
                    camera.addFrameProcessor(frameProcessor);
                }
                break;
            }
        }
    }


    /**
     * Displays a dialog loader
     *
     * @param message text to display on the dialog
     * @param cancel indicates if this dialog is cancelable on touching outside the dialog
     */
    public void showProgressDialog(String message, boolean cancel) {
        if (!this.isFinishing() && !this.isDestroyed()) {
            stopProgressDialog();
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(message);
            progressDialog.setCancelable(cancel);
            progressDialog.show();
        }
    }


    /**
     * Dismisses any running instance of the
     * progress dialog
     */
    public void stopProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }


    /**
     * Loads image from capture pool
     */
    private void loadImageIfAvailable(){
        String imageData = DataSession.getInstance().getTextData().get(R.layout.enrollment_activity_camera);
        String babsImageTemplate = DataSession.getInstance().getTextData().get(R.id.babs_template);
        float[] templateData = new Gson().fromJson(babsImageTemplate, float[].class);

        if(imageData != null && !TextUtils.isEmpty(imageData)){
            byte[] data = Base64.decode(imageData, Base64.NO_WRAP);
            Bitmap capturedImage = BitmapFactory.decodeByteArray(data, 0, data.length);
            runOnUiThread(() -> displayPreview(capturedImage, data, templateData));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Thread t = new Thread(){
            @Override
            public void run(){
                loadImageIfAvailable();
            }
        };
        t.start();
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
        tensorUtil.closeTensorFlowInference();
        camera.stop();
    }


    /**
     * Defines the mode of image capture
     */
    public enum ImageCaptureMode{
        /**
         * Used with a capture button to acquire the image. And it employs
         * the use of face recognition technique that uses a
         * single image for its training
         */
        SHUTTER,

        /**
         * Captures multiple cropped images to be used for
         * face recognition training
         */
        FRAME_PROCESSING
    }
}
