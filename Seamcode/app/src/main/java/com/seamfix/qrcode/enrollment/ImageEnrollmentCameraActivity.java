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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.image_enrollment_activity_camera);


        progressBar = findViewById(R.id.id_progress_bar);
        camera = findViewById(R.id.camera);

        camera.setLifecycleOwner(this);
        camera.mapGesture(Gesture.TAP, GestureAction.FOCUS);
        camera.setFacing(Facing.BACK);
        camera.addCameraListener(cameraListener);
        camera.addFrameProcessor(frameProcessor);
        progressBar.setProgress(0);

        capture = findViewById(R.id.enrollment_camera_capture_button);
        capture.setVisibility(View.GONE);
        capture.setOnClickListener(v -> {
            camera.captureSnapshot();
        });
        mtcnn = new MTCNN(getAssets());
        tensorUtil = TensorUtil.getInstance();
        model = FileUtils.readAsset(this, "face_model.pb");
    }


    private CameraListener cameraListener = new CameraListener() {
        @Override
        public void onPictureTaken(byte[] jpeg) {
            super.onPictureTaken(jpeg);
            camera.stop();
            byte[]normalizedJpeg = PictUtil.normalizeOrientation(jpeg);
            Bitmap capturedImage = BitmapFactory.decodeByteArray(normalizedJpeg, 0, normalizedJpeg.length);
            Log.e("IMAGE HEIGHT", ""+capturedImage.getHeight());

            try {
                Vector<Box> boxes = mtcnn.detectFaces(capturedImage, 40);
                for (int i = 0; i < boxes.size(); i++) {
                    com.seamfix.qrcode.mtcnn.Utils.drawRect(capturedImage, boxes.get(i).transform2Rect());
                    com.seamfix.qrcode.mtcnn.Utils.drawPoints(capturedImage, boxes.get(i).landmark);
                    Rect rect = boxes.get(0).transform2Rect();
                    Bitmap croppedBitmap = Bitmap.createBitmap(capturedImage, rect.left, rect.top, rect.width(), rect.height());
                    Bitmap scaledBitmap  = Bitmap.createScaledBitmap(croppedBitmap, 200, 200, false);

                    float[] mat = new CVUtil().generateEigenFace(scaledBitmap, 50);

                    System.out.println("==========");

                    //boolean isInit = tensorUtil.initializeTensorflowInferenceAsyn(model);
                    //if(isInit) {
                    //    float[] features = tensorUtil.getEmbeddings(scaledBitmap);
                    //    displayPreview(capturedImage, normalizedJpeg, features);
                    //}
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            //for(int m= 0; m<=3; m++) {
//                FirebaseVisionFaceDetectorOptions options = Utils.Companion.getFaceDetectionOptions();
//                FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(capturedImage);
//                FirebaseVisionFaceDetector detector = FirebaseVision.getInstance().getVisionFaceDetector(options);
//                Task<List<FirebaseVisionFace>> result = detector.detectInImage(image);


//                result.addOnSuccessListener(faces -> {
//                    if (faces.size() != 1) {
//                        Toast.makeText(EnrollmentCameraActivity.this, "More than a face was found", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                    FirebaseVisionFace face = faces.get(0);
//                    Rect bounds = face.getBoundingBox();
//
//                    float[] x = new float[10];
//                    float[] y = new float[10];
//
//                    if (bounds != null) {
//                        x[Landmark.LEFT_LOWER_BOUNDING] = bounds.left;
//                        y[Landmark.LEFT_LOWER_BOUNDING] = bounds.bottom;
//
//                        x[Landmark.RIGHT_LOWER_BOUNDING] = bounds.right;
//                        y[Landmark.RIGHT_LOWER_BOUNDING] = bounds.bottom;
//
//                        x[Landmark.RIGHT_UPPER_BOUNDING] = bounds.right;
//                        y[Landmark.RIGHT_UPPER_BOUNDING] = bounds.top;
//
//                        x[Landmark.LEFT_UPPER_BOUNDING] = bounds.left;
//                        y[Landmark.LEFT_UPPER_BOUNDING] = bounds.top;
//
////                    float m = bounds.exactCenterX();
////
////                    x[Landmark.UPPER_FACE_CENTER] = m;
////                    y[Landmark.UPPER_FACE_CENTER] = bounds.top;
////
////                    x[Landmark.LOWER_FACE_CENTER] = m;
////                    y[Landmark.LOWER_FACE_CENTER] = bounds.bottom;
//                    }
//
//
//                    FirebaseVisionFaceLandmark rightEye = face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EYE);
//                    if (rightEye != null) {
//                        x[Landmark.RIGHT_EYE] = rightEye.getPosition().getX();
//                        y[Landmark.RIGHT_EYE] = rightEye.getPosition().getY();
//                    }
//
//                    FirebaseVisionFaceLandmark leftEye = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EYE);
//                    if (leftEye != null) {
//                        x[Landmark.LEFT_EYE] = leftEye.getPosition().getX();
//                        y[Landmark.LEFT_EYE] = leftEye.getPosition().getY();
//                    }
//
//                    FirebaseVisionFaceLandmark noseTip = face.getLandmark(FirebaseVisionFaceLandmark.NOSE_BASE);
//                    if (noseTip != null) {
//                        x[Landmark.NOSE_TIP] = noseTip.getPosition().getX();
//                        y[Landmark.NOSE_TIP] = noseTip.getPosition().getY();
//                    }
//
//                    FirebaseVisionFaceLandmark mouthCenter = face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_BOTTOM);
//                    if (mouthCenter != null) {
//                        x[Landmark.MOUTH_CENTER] = mouthCenter.getPosition().getX();
//                        y[Landmark.MOUTH_CENTER] = mouthCenter.getPosition().getY();
//                    }
//
//                    FirebaseVisionFaceLandmark mouthLeft = face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_LEFT);
//                    if (mouthLeft != null) {
//                        x[Landmark.LOWER_FACE_CENTER] = mouthLeft.getPosition().getX();
//                        y[Landmark.LOWER_FACE_CENTER] = mouthLeft.getPosition().getY();
//                    }
//
//                    FirebaseVisionFaceLandmark mouthRight = face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_LEFT);
//                    if (mouthRight != null) {
//                        x[Landmark.UPPER_FACE_CENTER] = mouthRight.getPosition().getX();
//                        y[Landmark.UPPER_FACE_CENTER] = mouthRight.getPosition().getY();
//                    }
//
//
//                    Log.e("X====", "" + Arrays.toString(x));
//
//                    Log.e("Y====", "" + Arrays.toString(y));
//
//                    int[] features = FaceFeatures.getInstance().generateFeatures(x, y);
//                    Log.e("FEATRURE", "" + Arrays.toString(features));
//
//                    int[] features1 = FaceFeatures.getInstance().generateFeatures(x, y);
//                    Log.e("FEATRURE1", "" + Arrays.toString(features1));
//
//                    int[] features2 = FaceFeatures.getInstance().generateFeatures(x, y);
//                    Log.e("FEATRURE2", "" + Arrays.toString(features2));
//
//                    int[] features3 = FaceFeatures.getInstance().generateFeatures(x, y);
//                    Log.e("FEATRURE3", "" + Arrays.toString(features3));
//
//                    int[] features4 = FaceFeatures.getInstance().generateFeatures(x, y);
//                    Log.e("FEATRURE4", "" + Arrays.toString(features4));
//
//
//                    displayPreview(capturedImage, normalizedJpeg, features);
//
//                });
//                result.addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.e("HELOO", "ERROR", e);
//                    }
//                });
            }


        @Override
        public void onCameraOpened(CameraOptions options) {
            super.onCameraOpened(options);
            //capture.setVisibility(View.VISIBLE);
        }
    };

    private FrameProcessor frameProcessor = new FrameProcessor() {
        @Override
        public void process(@NonNull Frame frame) {

            byte[] data  = frame.getData();
            Size size  = frame.getSize();
            int rotation = frame.getRotation();


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

                    float[] mat = new CVUtil().generateEigenFace(sampleBitmaps, 50);
                    capture.post(() -> {
                        stopProgressDialog();
                        displayPreview(bm, imageBytes, mat);
                    });

                    sampleBitmaps.clear();
                    cameraCaptured = true;
                }else{
                    sampleBitmaps.clear();
                }
            } catch (Exception e) {
                e.printStackTrace();
                capture.post(new Runnable() {
                    @Override
                    public void run() {
                        stopProgressDialog();
                        Toast.makeText(ImageEnrollmentCameraActivity.this, "Hmm, something went wrong please try again", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    };



    private void displayPreview(Bitmap bitmap, byte[] imageData, float[] features) {
        AlertDialog launchDialog = new AlertDialog.Builder(this).create();
        View view = LayoutInflater.from(this).inflate(R.layout.camera_preview_dialog_layout, null, false);
        Button okButton     = view.findViewById(R.id.ok);
        Button retryButton  = view.findViewById(R.id.id_retry);
        ImageView imageView = view.findViewById(R.id.image_preview);
        imageView.setImageBitmap(bitmap);
        byte[] data = PictUtil.convertBitmapToByteArray(bitmap);

        okButton.setOnClickListener(v -> {
            try {
                String imageStringData = Base64.encodeToString(data, Base64.NO_WRAP);
                String imageTemplate = new Gson().toJson(features);
                DataSession.getInstance().getTextData().put(R.layout.enrollment_activity_camera, imageStringData);
                DataSession.getInstance().getTextData().put(R.id.babs_template, imageTemplate);

                Intent intent = new Intent(ImageEnrollmentCameraActivity.this, EnrollmentFingerTypeActivity.class);
                startActivity(intent);
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
        launchDialog.setOnDismissListener(dialog -> {
            camera.start();
        });
        launchDialog.setCancelable(false);
        launchDialog.show();
    }

    public void showProgressDialog(String message, boolean cancel) {
        if (!this.isFinishing() && !this.isDestroyed() && this.hasWindowFocus()) {
            stopProgressDialog();
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(message);
            progressDialog.setCancelable(cancel);
            progressDialog.show();
        }
    }

    public void stopProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String imageData = DataSession.getInstance().getTextData().get(R.layout.enrollment_activity_camera);
        String babsImageTemplate = DataSession.getInstance().getTextData().get(R.id.babs_template);
        float[] templateData = new Gson().fromJson(babsImageTemplate, float[].class);

        if(imageData != null && !TextUtils.isEmpty(imageData)){
           byte[] data = Base64.decode(imageData, Base64.NO_WRAP);
           Bitmap capturedImage = BitmapFactory.decodeByteArray(data, 0, data.length);
           displayPreview(capturedImage, data, templateData);
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
        tensorUtil.closeTensorFlowInference();
        camera.stop();
    }
}
