package com.seamfix.qrcode.verification;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;
import com.google.gson.Gson;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraOptions;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.Facing;
import com.otaliastudios.cameraview.FrameProcessor;
import com.otaliastudios.cameraview.Gesture;
import com.otaliastudios.cameraview.GestureAction;
import com.otaliastudios.cameraview.Size;
import com.seamfix.qrcode.EnrollmentData;
import com.seamfix.qrcode.FaceFeatures;
import com.seamfix.qrcode.FingerQrCode;
import com.seamfix.qrcode.Utils;
import com.seamfix.qrcode.enrollment.DataSession;
import com.seamfix.qrcode.enrollment.EnrollmentFingerprintActivity;
import com.seamfix.qrcode.enrollment.Landmark;
import com.seamfix.seamcode.R;
import com.sf.bio.lib.PictUtil;

import java.util.List;
import java.util.concurrent.ExecutionException;


public class VerificationCameraActivity2 extends AppCompatActivity {
    ImageButton capture;
    CameraView camera;
    private FirebaseVisionBarcodeDetector detector;
    private boolean barcodeDetected =  false;
    private EnrollmentData enrollmentData;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }


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
        camera.addFrameProcessor(frameProcessor);

        FirebaseVisionBarcodeDetectorOptions detectorOptions = Utils.Companion.getBarcodeDetectorOptions();
        detector = FirebaseVision.getInstance().getVisionBarcodeDetector(detectorOptions);

        capture = findViewById(R.id.enrollment_camera_capture_button);
        capture.setVisibility(View.GONE);
        capture.setOnClickListener(v -> camera.captureSnapshot());
    }

    /**
     * Firebase barcode processor
     */
    private FrameProcessor frameProcessor = frame -> {
        byte[] data = frame.getData();
        Size   size = frame.getSize();

        FirebaseVisionImageMetadata metaData = Utils.Companion.getFirebaseVisionImageMetaData(size.getWidth(), size.getHeight(), 0);
        FirebaseVisionImage image = FirebaseVisionImage.fromByteArray(data, metaData);
        Task<List<FirebaseVisionBarcode>> task = detector.detectInImage(image);
        try {
            List<FirebaseVisionBarcode> result = Tasks.await(task);
            if(result != null && !result.isEmpty() && !barcodeDetected){
                FirebaseVisionBarcode code = result.get(0);
                String rawValue = code.getRawValue();
                barcodeDetected = true;
                enrollmentData = FingerQrCode.decodeEnrollmentData(rawValue);
                if(enrollmentData != null){
                    capture.post(() -> capture.setVisibility(View.VISIBLE));
                }
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    };


    /**
     * Camera listener
     */
    private CameraListener cameraListener = new CameraListener() {
        @Override
        public void onPictureTaken(byte[] jpeg) {
            super.onPictureTaken(jpeg);
            camera.stop();
            byte[]normalizedJpeg = PictUtil.normalizeOrientation(jpeg);
            Bitmap capturedImage = BitmapFactory.decodeByteArray(normalizedJpeg, 0, normalizedJpeg.length);
            Log.e("IMAGE HEIGHT", ""+capturedImage.getHeight());

            FirebaseVisionFaceDetectorOptions options = Utils.Companion.getFaceDetectionOptions();
            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(capturedImage);
            FirebaseVisionFaceDetector detector = FirebaseVision.getInstance().getVisionFaceDetector(options);
            Task<List<FirebaseVisionFace>> result  = detector.detectInImage(image);

            result.addOnSuccessListener(faces -> {
                if (faces.size() != 1) {
                    Toast.makeText(VerificationCameraActivity2.this, "More than a face was found", Toast.LENGTH_SHORT).show();
                    return;
                }
                FirebaseVisionFace face = faces.get(0);
                Rect bounds = face.getBoundingBox();

                float[] x = new float[10];
                float[] y = new float[10];

                if (bounds != null) {
                    x[Landmark.LEFT_LOWER_BOUNDING] = bounds.left;
                    y[Landmark.LEFT_LOWER_BOUNDING] = bounds.bottom;

                    x[Landmark.RIGHT_LOWER_BOUNDING] = bounds.right;
                    y[Landmark.RIGHT_LOWER_BOUNDING] = bounds.bottom;

                    x[Landmark.RIGHT_UPPER_BOUNDING] = bounds.right;
                    y[Landmark.RIGHT_UPPER_BOUNDING] = bounds.top;

                    x[Landmark.LEFT_UPPER_BOUNDING] = bounds.left;
                    y[Landmark.LEFT_UPPER_BOUNDING] = bounds.top;

                    float m = bounds.exactCenterX();

                    x[Landmark.UPPER_FACE_CENTER] = m;
                    y[Landmark.UPPER_FACE_CENTER] = bounds.top;

                    x[Landmark.LOWER_FACE_CENTER] = m;
                    y[Landmark.LOWER_FACE_CENTER] = bounds.bottom;
                }


                FirebaseVisionFaceLandmark rightEye = face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EYE);
                if (rightEye != null) {
                    x[Landmark.RIGHT_EYE] = rightEye.getPosition().getX();
                    y[Landmark.RIGHT_EYE] = rightEye.getPosition().getY();
                }

                FirebaseVisionFaceLandmark leftEye = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EYE);
                if (leftEye != null) {
                    x[Landmark.LEFT_EYE] = leftEye.getPosition().getX();
                    y[Landmark.LEFT_EYE] = leftEye.getPosition().getY();
                }

                FirebaseVisionFaceLandmark noseTip = face.getLandmark(FirebaseVisionFaceLandmark.NOSE_BASE);
                if (noseTip != null) {
                    x[Landmark.NOSE_TIP] = noseTip.getPosition().getX();
                    y[Landmark.NOSE_TIP] = noseTip.getPosition().getY();
                }

                FirebaseVisionFaceLandmark mouthCenter = face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_BOTTOM);
                if (mouthCenter != null) {
                    x[Landmark.MOUTH_CENTER] = mouthCenter.getPosition().getX();
                    y[Landmark.MOUTH_CENTER] = mouthCenter.getPosition().getY();
                }

                String babsImageTemplate = enrollmentData.getF();
                int[] candidateFeatures = new Gson().fromJson(babsImageTemplate, int[].class);

                //int[] features = FaceFeatures.getInstance().generateFeatures(x, y);
                float score = FaceFeatures.getInstance().matchFeatures(candidateFeatures, x, y);
                Toast.makeText(VerificationCameraActivity2.this, ""+score, Toast.LENGTH_SHORT).show();
                displayPreview(capturedImage, normalizedJpeg, candidateFeatures);

            });

            result.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("HELOO", "ERROR", e);
                }
            });
        }

        @Override
        public void onCameraOpened(CameraOptions options) {
            super.onCameraOpened(options);
            //capture.setVisibility(View.VISIBLE);
        }
    };


    private void displayPreview(Bitmap bitmap, byte[] imageData, int[] features) {
        AlertDialog launchDialog = new AlertDialog.Builder(this).create();
        View view = LayoutInflater.from(this).inflate(R.layout.camera_preview_dialog_layout, null, false);
        Button okButton     = view.findViewById(R.id.ok);
        Button retryButton  = view.findViewById(R.id.id_retry);
        ImageView imageView = view.findViewById(R.id.image_preview);
        imageView.setImageBitmap(bitmap);

        okButton.setOnClickListener(v -> {
            String imageStringData = Base64.encodeToString(imageData, Base64.NO_WRAP);
            String imageTemplate = new Gson().toJson(features);

            DataSession.getInstance().getTextData().put(R.layout.enrollment_activity_camera, imageStringData);
            DataSession.getInstance().getTextData().put(R.id.babs_template, imageTemplate);

            Intent intent = new Intent(VerificationCameraActivity2.this, EnrollmentFingerprintActivity.class);
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


    /**
     * Get the angle by which an image must be rotated given the device's current
     * orientation.
     */
    @SuppressWarnings("all")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private int getRotationCompensation(String cameraId, Activity activity, Context context) throws CameraAccessException {
        // Get the device's current rotation relative to its "native" orientation.
        // Then, from the ORIENTATIONS table, look up the angle the image must be
        // rotated to compensate for the device's rotation.
        int deviceRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int rotationCompensation = ORIENTATIONS.get(deviceRotation);

        // On most devices, the sensor orientation is 90 degrees, but for some
        // devices it is 270 degrees. For devices with a sensor orientation of
        // 270, rotate the image an additional 180 ((270 + 270) % 360) degrees.
        CameraManager cameraManager = (CameraManager) context.getSystemService(CAMERA_SERVICE);
        int sensorOrientation = cameraManager.getCameraCharacteristics(cameraId).get(CameraCharacteristics.SENSOR_ORIENTATION);
        rotationCompensation = (rotationCompensation + sensorOrientation + 270) % 360;

        // Return the corresponding FirebaseVisionImageMetadata rotation value.
        int result;
        switch (rotationCompensation) {
            case 0:
                result = FirebaseVisionImageMetadata.ROTATION_0;
                break;
            case 90:
                result = FirebaseVisionImageMetadata.ROTATION_90;
                break;
            case 180:
                result = FirebaseVisionImageMetadata.ROTATION_180;
                break;
            case 270:
                result = FirebaseVisionImageMetadata.ROTATION_270;
                break;
            default:
                result = FirebaseVisionImageMetadata.ROTATION_0;
                Log.e("ROTATION", "Bad rotation value: " + rotationCompensation);
        }
        return result;
    }



    @Override
    protected void onResume() {
        super.onResume();
        String imageData = DataSession.getInstance().getTextData().get(R.layout.enrollment_activity_camera);
        String babsImageTemplate = DataSession.getInstance().getTextData().get(R.id.babs_template);
        int[] templateData = new Gson().fromJson(babsImageTemplate, int[].class);
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
        camera.stop();
    }
}
