package com.seamfix.qrcode.verification;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
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

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.gson.Gson;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.machinezoo.sourceafis.FingerprintCompatibility;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.Facing;
import com.otaliastudios.cameraview.FrameProcessor;
import com.otaliastudios.cameraview.Gesture;
import com.otaliastudios.cameraview.GestureAction;
import com.otaliastudios.cameraview.Size;
import com.seamfix.qrcode.EnrollmentData;
import com.seamfix.qrcode.FaceFeatures;
import com.seamfix.qrcode.FingerQrCode;
import com.seamfix.qrcode.TensorUtil;
import com.seamfix.qrcode.Utils;
import com.seamfix.qrcode.VerificationSession;
import com.seamfix.qrcode.enrollment.DataSession;
import com.seamfix.qrcode.enrollment.EnrollmentFingerprintActivity;
import com.seamfix.qrcode.mtcnn.Box;
import com.seamfix.qrcode.mtcnn.MTCNN;
import com.seamfix.seamcode.R;
import com.sf.bio.lib.PictUtil;
import com.sf.bio.lib.util.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutionException;


public class VerificationCameraActivity extends AppCompatActivity {
    ImageButton capture;
    CameraView camera;
    ImageView image;
    TensorUtil tensorUtil;
    private FirebaseVisionBarcodeDetector detector;
    private FirebaseVisionFaceDetector faceDetector;
    private int adjustedRotation = 0;
    MTCNN mtcnn;
    String rawValue;
    private ProgressDialog progressDialog;
    byte[] model;
    String probe = "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";

    private boolean barcodeDetected =  false, pictatureDetected = false;
    private EnrollmentData enrollmentData;
    Reader reader;

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
        image = findViewById(R.id.image);
        camera.setLifecycleOwner(this);
        camera.mapGesture(Gesture.TAP, GestureAction.FOCUS);
        camera.setFacing(Facing.BACK);
        camera.addFrameProcessor(frameProcessor);

        FirebaseVisionBarcodeDetectorOptions detectorOptions = Utils.Companion.getBarcodeDetectorOptions();
        detector = FirebaseVision.getInstance().getVisionBarcodeDetector(detectorOptions);

        FirebaseVisionFaceDetectorOptions faceDetectionOption = Utils.Companion.getFaceDetectionOptions();
        faceDetector = FirebaseVision.getInstance().getVisionFaceDetector(faceDetectionOption);

        capture = findViewById(R.id.enrollment_camera_capture_button);
        capture.setVisibility(View.GONE);
        capture.setOnClickListener(v -> camera.captureSnapshot());

        // Get device adjusted rotation
        CameraManager cameraManager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        String cameraId;
        try {
            cameraId = cameraManager.getCameraIdList()[1];
            adjustedRotation = getRotationCompensation(cameraId, this, this);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        mtcnn = new MTCNN(getAssets());
        tensorUtil = TensorUtil.getInstance();
        model = FileUtils.readAsset(this, "face_model.pb");
        reader = new MultiFormatReader();
        Session.getInstance().destroyInstance();
    }

    /**
     * Firebase barcode processor
     */
    private FrameProcessor frameProcessor = frame -> {
        byte[] data  = frame.getData();
        Size   size  = frame.getSize();
        int rotation = frame.getRotation();

        FirebaseVisionImageMetadata metaData = Utils.Companion.getFirebaseVisionImageMetaData(size.getWidth(), size.getHeight(), 1);
        FirebaseVisionImage image = FirebaseVisionImage.fromByteArray(data, metaData);
        Task<List<FirebaseVisionBarcode>> task = detector.detectInImage(image);
        try {
            List<FirebaseVisionBarcode> result = Tasks.await(task);
            Log.e("BAR", "WAITING=====");
            if(result != null && !result.isEmpty() && !barcodeDetected){
                Log.e("BAR", "BAR DETECTED=====");
                FirebaseVisionBarcode code = result.get(0);
                rawValue = code.getRawValue();
                barcodeDetected = true;
                enrollmentData = FingerQrCode.decode(rawValue);
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }


        if (barcodeDetected && !pictatureDetected) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            YuvImage yuvImage = new YuvImage(frame.getData(), ImageFormat.NV21, frame.getSize().getWidth(), frame.getSize().getHeight(), null);
            yuvImage.compressToJpeg(new Rect(0, 0, frame.getSize().getWidth(), frame.getSize().getHeight()), 90, out);
            byte[] imageBytes = out.toByteArray();
            Bitmap imageBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            Bitmap bit = PictUtil.rotateImage(imageBitmap, rotation);
            Bitmap bm = com.seamfix.qrcode.mtcnn.Utils.copyBitmap(bit);

            try {
                Vector<Box> boxes = mtcnn.detectFaces(bm, 40);
                for (int i = 0; i < boxes.size(); i++) {
                    pictatureDetected = true;
                    Rect rect = boxes.get(0).transform2Rect();
                    Bitmap croppedBitmap = Bitmap.createBitmap(bm, rect.left, rect.top, rect.width(), rect.height());
                    if(enrollmentData != null && enrollmentData.validate()){



                        Session.getInstance().setCroppedBitmap(croppedBitmap);
                        Intent intent = new Intent(VerificationCameraActivity.this, VerificationDetailsActivity.class);
                        intent.putExtra("value", rawValue);
                        startActivity(intent);
                        finish();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

//
//
//        int[] intArray = new int[bit.getWidth() * bit.getHeight()];
//        bit.getPixels(intArray, 0, bit.getWidth(), 0, 0, bit.getWidth(), bit.getHeight());
//        LuminanceSource source = new RGBLuminanceSource(bit.getWidth(), bit.getHeight(), intArray);
//        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
//
//        Log.e("BAR", "WAITING=====");
//
//        if (!barcodeDetected) {
//            try {
//                Log.e("BAR", "BAR DETECTED=====" /* +text*/);
//                barcodeDetected = true;
//                enrollmentData = FingerQrCode.decodeEnrollmentData(probe);
//                Result mResult = reader.decode(bitmap);
//                String text = mResult.getText();
//            } catch (NotFoundException | ChecksumException | FormatException e) {
//                e.printStackTrace();
//            }
//        }




//        if(barcodeDetected)
//        try {
//            Vector<Box> boxes = mtcnn.detectFaces(bm, 40);
//            for (int i = 0; i < boxes.size(); i++) {
//                Rect rect = boxes.get(0).transform2Rect();
//                Bitmap croppedBitmap = Bitmap.createBitmap(bm, rect.left, rect.top, rect.width(), rect.height());
//                Bitmap scaledBitmap  = Bitmap.createScaledBitmap(croppedBitmap, 64, 64, false);
//                this.image.post(() -> VerificationCameraActivity.this.image.setImageBitmap(scaledBitmap));
//                if (enrollmentData != null && enrollmentData.validate()) {
//                    String babsImageTemplate = enrollmentData.getF();
//                    boolean isInit = tensorUtil.initializeTensorflowInferenceAsyn(model);
//                    if(isInit) {
//                        float[] candidateEmbedding = tensorUtil.getEmbeddings(scaledBitmap);
//                        float[] probeEmbedding = FingerQrCode.decodeEmbeddings(babsImageTemplate);
//                        float score = FaceFeatures.getInstance().matchEmbeddings(probeEmbedding, candidateEmbedding);
//                        Log.e("SCORE===", " "+ score);
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }


//        FirebaseVisionImage faceImage = FirebaseVisionImage.fromBitmap(imageBitmap);
//        Task<List<FirebaseVisionFace>> faceTask = faceDetector.detectInImage(faceImage);
//        try {
//            List<FirebaseVisionFace> result = Tasks.await(faceTask);
//            Log.e("FACE", "WAITING=====");
//            if(result != null && !result.isEmpty() && barcodeDetected){
//                Log.e("FACE", "FACE DETECTED=====");
//                FirebaseVisionFace face = result.get(0);
//                Bitmap faceBitmap = Bitmap.createBitmap(imageBitmap, (face.getBoundingBox().top), (face.getBoundingBox().left),face.getBoundingBox().width(),face.getBoundingBox().height());
//                this.image.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        VerificationCameraActivity.this.image.setImageBitmap(faceBitmap);
//                    }
//                });
//                if(enrollmentData != null && enrollmentData.validate()){
//                    String babsImageTemplate = enrollmentData.getF();
//                    int[] candidateFeatures = new Gson().fromJson(babsImageTemplate, int[].class);
//                    VerificationUtil.Result matchResult = VerificationUtil.getFaceFeatureScore(face,candidateFeatures);
//                }
//            }
//        } catch (ExecutionException | InterruptedException e) {
//            e.printStackTrace();
//        }
    };




    /**
     * Camera listener
     */
//    private CameraListener cameraListener = new CameraListener() {
//        @Override
//        public void onPictureTaken(byte[] jpeg) {
//            super.onPictureTaken(jpeg);
//            camera.stop();
//            byte[]normalizedJpeg = PictUtil.normalizeOrientation(jpeg);
//            Bitmap capturedImage = BitmapFactory.decodeByteArray(normalizedJpeg, 0, normalizedJpeg.length);
//            Log.e("IMAGE HEIGHT", ""+capturedImage.getHeight());
//
//            FirebaseVisionFaceDetectorOptions options = Utils.Companion.getFaceDetectionOptions();
//            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(capturedImage);
//            FirebaseVisionFaceDetector detector = FirebaseVision.getInstance().getVisionFaceDetector(options);
//            Task<List<FirebaseVisionFace>> result  = detector.detectInImage(image);
//
//            result.addOnSuccessListener(faces -> {
//                if (faces.size() != 1) {
//                    Toast.makeText(VerificationCameraActivity.this, "More than a face was found", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                FirebaseVisionFace face = faces.get(0);
//                Rect bounds = face.getBoundingBox();
//
//                float[] x = new float[10];
//                float[] y = new float[10];
//
//                if (bounds != null) {
//                    x[Landmark.LEFT_LOWER_BOUNDING] = bounds.left;
//                    y[Landmark.LEFT_LOWER_BOUNDING] = bounds.bottom;
//
//                    x[Landmark.RIGHT_LOWER_BOUNDING] = bounds.right;
//                    y[Landmark.RIGHT_LOWER_BOUNDING] = bounds.bottom;
//
//                    x[Landmark.RIGHT_UPPER_BOUNDING] = bounds.right;
//                    y[Landmark.RIGHT_UPPER_BOUNDING] = bounds.top;
//
//                    x[Landmark.LEFT_UPPER_BOUNDING] = bounds.left;
//                    y[Landmark.LEFT_UPPER_BOUNDING] = bounds.top;
//
//                    float m = bounds.exactCenterX();
//
//                    x[Landmark.UPPER_FACE_CENTER] = m;
//                    y[Landmark.UPPER_FACE_CENTER] = bounds.top;
//
//                    x[Landmark.LOWER_FACE_CENTER] = m;
//                    y[Landmark.LOWER_FACE_CENTER] = bounds.bottom;
//                }
//
//
//                FirebaseVisionFaceLandmark rightEye = face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EYE);
//                if (rightEye != null) {
//                    x[Landmark.RIGHT_EYE] = rightEye.getPosition().getX();
//                    y[Landmark.RIGHT_EYE] = rightEye.getPosition().getY();
//                }
//
//                FirebaseVisionFaceLandmark leftEye = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EYE);
//                if (leftEye != null) {
//                    x[Landmark.LEFT_EYE] = leftEye.getPosition().getX();
//                    y[Landmark.LEFT_EYE] = leftEye.getPosition().getY();
//                }
//
//                FirebaseVisionFaceLandmark noseTip = face.getLandmark(FirebaseVisionFaceLandmark.NOSE_BASE);
//                if (noseTip != null) {
//                    x[Landmark.NOSE_TIP] = noseTip.getPosition().getX();
//                    y[Landmark.NOSE_TIP] = noseTip.getPosition().getY();
//                }
//
//                FirebaseVisionFaceLandmark mouthCenter = face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_BOTTOM);
//                if (mouthCenter != null) {
//                    x[Landmark.MOUTH_CENTER] = mouthCenter.getPosition().getX();
//                    y[Landmark.MOUTH_CENTER] = mouthCenter.getPosition().getY();
//                }
//
//                String babsImageTemplate = enrollmentData.getF();
//                int[] candidateFeatures = new Gson().fromJson(babsImageTemplate, int[].class);
//
//                //int[] features = FaceFeatures.getInstance().generateFeatures(x, y);
//                float score = FaceFeatures.getInstance().matchFeatures(candidateFeatures, x, y);
//                Toast.makeText(VerificationCameraActivity.this, ""+score, Toast.LENGTH_SHORT).show();
//                displayPreview(capturedImage, normalizedJpeg, candidateFeatures);
//
//            });
//
//            result.addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                    Log.e("HELOO", "ERROR", e);
//                }
//            });
//        }
//
//        @Override
//        public void onCameraOpened(CameraOptions options) {
//            super.onCameraOpened(options);
//            //capture.setVisibility(View.VISIBLE);
//        }
//    };





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

            Intent intent = new Intent(VerificationCameraActivity.this, EnrollmentFingerprintActivity.class);
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
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private int getRotationCompensation(String cameraId, Activity activity, Context context)
            throws CameraAccessException {
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
                Log.e("TAG", "Bad rotation value: " + rotationCompensation);
        }
        return result;
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
        try {
            detector.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.stop();
    }
}
