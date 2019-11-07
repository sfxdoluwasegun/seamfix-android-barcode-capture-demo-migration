package com.seamfix.qrcode.verification;


import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;


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
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.Facing;
import com.otaliastudios.cameraview.FrameProcessor;
import com.otaliastudios.cameraview.Gesture;
import com.otaliastudios.cameraview.GestureAction;
import com.otaliastudios.cameraview.Size;
import com.seamfix.qrcode.EnrollmentData;
import com.seamfix.qrcode.FaceFeatures;
import com.seamfix.qrcode.FingerQrCode;
import com.seamfix.qrcode.enrollment.ImageEnrollmentCameraActivity;
import com.seamfix.qrcode.mtcnn.Box;
import com.seamfix.qrcode.mtcnn.MTCNN;
import com.seamfix.seamcode.R;
import com.sf.bio.lib.PictUtil;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;


public class ImageVerificationActivity extends AppCompatActivity {
    ImageButton capture;
    CameraView camera;
    ImageView image;
    MTCNN mtcnn;
    String rawValue;
    private ProgressDialog progressDialog;

    private boolean barcodeDetected =  false, pictureDetected = false;
    private EnrollmentData enrollmentData;
    Reader reader;

    ArrayList<Bitmap> sampleBitmaps = new ArrayList<>();
    ProgressBar progressBar;
    static final int IMAGE_SAMPLES = 4;

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
        progressBar = findViewById(R.id.id_progress_bar);

        image = findViewById(R.id.image);
        camera.setLifecycleOwner(this);
        camera.mapGesture(Gesture.TAP, GestureAction.FOCUS);
        camera.setFacing(Facing.BACK);
        camera.addFrameProcessor(frameProcessor);

        capture = findViewById(R.id.enrollment_camera_capture_button);
        capture.setVisibility(View.GONE);
        capture.setOnClickListener(v -> camera.captureSnapshot());

        mtcnn  = new MTCNN(getAssets());
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


        ByteArrayOutputStream out = new ByteArrayOutputStream();
        YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, size.getWidth(), size.getHeight(), null);
        yuvImage.compressToJpeg(new Rect(0, 0, size.getWidth(), size.getHeight()), 90, out);
        byte[] imageBytes = out.toByteArray();
        Bitmap imageBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        Bitmap bit = PictUtil.rotateImage(imageBitmap, rotation);
        Bitmap bm = com.seamfix.qrcode.mtcnn.Utils.copyBitmap(bit);

        int[] intArray = new int[bit.getWidth() * bit.getHeight()];
        bit.getPixels(intArray, 0, bit.getWidth(), 0, 0, bit.getWidth(), bit.getHeight());
        LuminanceSource source = new RGBLuminanceSource(bit.getWidth(), bit.getHeight(), intArray);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        Log.e("BAR", "WAITING=====");

        if (!barcodeDetected) {
            try {
                Result mResult = reader.decode(bitmap);
                Log.e("BAR", "BAR DETECTED=====" /* +text*/);
                Log.e("RAW", " DATA" + mResult.getText());
                rawValue = mResult.getText();
                enrollmentData = FingerQrCode.decodeEnrollmentData(rawValue);
                Log.e("DECODED", " DATA===" + mResult.getText());
                barcodeDetected = true;
            } catch (NotFoundException | ChecksumException | FormatException e) {
                e.printStackTrace();
            }
        }

        if (barcodeDetected && !pictureDetected) {
            try {
                Vector<Box> boxes = mtcnn.detectFaces(bm, 40);
                if(boxes.size() == 1){
                    Box box = boxes.get(0);
                    Log.e("FACE-DETECTED", "COUNT IS: " + sampleBitmaps.size());
                    capture.post(new Runnable() {
                        @Override
                        public void run() {
                            int progress = (sampleBitmaps.size()* 100)/IMAGE_SAMPLES;
                            progressBar.setProgress(progress);
                        }
                    });

                    Rect rect = box.transform2Rect();
                    Bitmap croppedBitmap = Bitmap.createBitmap(bm, rect.left, rect.top, rect.width(), rect.height());
                    Bitmap scaledBitmap  = Bitmap.createScaledBitmap(croppedBitmap, 64, 64, false);
                    if(sampleBitmaps.size() < IMAGE_SAMPLES){
                        sampleBitmaps.add(scaledBitmap);
                        return;
                    }
                    capture.post(() -> showProgressDialog("Processing image", false));
                    byte[]imageByte      = PictUtil.convertBitmapToByteArray(scaledBitmap);
                    String imageString   = Base64.encodeToString(imageByte, Base64.NO_WRAP);
                    String faceData      = enrollmentData.getF();
                    float[] templateData = new Gson().fromJson(faceData, float[].class);
                    String modelFileName = FaceFeatures.getFaceModelFileName(this);
                    Log.e("IMAGE:", "BASE 64: " + imageString);


                    float[] features = FaceFeatures.getInstance().generatepcafeatures(modelFileName, imageString);
                    System.out.println(Arrays.toString(features));

                    System.out.println(imageString);
                    System.out.println(Arrays.toString(templateData));
                    System.out.println(modelFileName);

                    float score      = FaceFeatures.getInstance().matchpcafeatures(modelFileName, imageString, templateData);
                    Log.e("SCORE===  ", score + "");


                    capture.post(new Runnable() {
                        @Override
                        public void run() {
                            stopProgressDialog();
                            if(score> 0.88) {
                                Session.getInstance().setCroppedBitmap(croppedBitmap);
                                Intent intent = new Intent(ImageVerificationActivity.this, VerificationDetailsActivity.class);
                                intent.putExtra("value", rawValue);
                                startActivity(intent);
                                finish();
                            }else{
                                Session.getInstance().setCroppedBitmap(croppedBitmap);
                                Intent intent = new Intent(ImageVerificationActivity.this, FailedVerificationDetailsActivity.class);
                                intent.putExtra("value", rawValue);
                                startActivity(intent);
                                finish();
                            }
                        }
                    });

                    sampleBitmaps.clear();
                    pictureDetected = true;
                }else{
                    sampleBitmaps.clear();
                }
            } catch (Exception e) {
                e.printStackTrace();
                capture.post(this::stopProgressDialog);
                barcodeDetected = false;
                pictureDetected = false;
            }
        }
    };


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
