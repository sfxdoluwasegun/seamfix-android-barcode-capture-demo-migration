package com.seamfix.qrcode.enrollment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.WindowManager;

import com.seamfix.sdk.fingerprint.FingerCaptureOption;
import com.seamfix.sdk.fingerprint.FingerPrintCaptureResult;
import com.seamfix.sdk.fingerprint.FingerTypes;
import com.seamfix.sdk.fingerprint.SingleFingerCaptureView;
import com.seamfix.sdk.fingerprint.SkipReasonData;
import com.seamfix.sdk.fingerprint.slap.SlapCaptureResult;
import com.seamfix.seamcode.R;


import org.tensorflow.Session;

import java.util.ArrayList;

public class EnrollmentFingerprintActivity extends AppCompatActivity {

    private FingerTypes fingerTypes;

    private ProgressDialog progressDialog;
    SingleFingerCaptureView slapFingerCaptureView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_test_single_finger);
        slapFingerCaptureView = findViewById(R.id.id_finger_capture_view);


        String fingerValue = getIntent().getStringExtra("FINGER_TYPE");
        fingerTypes = FingerTypes.fromString(fingerValue);

        ArrayList<SkipReasonData> data = new ArrayList<>();
        FingerCaptureOption option = new FingerCaptureOption(true, null, false);
        option.setSupportedScanner("KOJAK,FIVE-0");
        option.setSkipReasonData(data);
        option.setFingerTypes(fingerTypes != null? fingerTypes :FingerTypes.LEFT_THUMB);
        slapFingerCaptureView.init(this, option, listener);
    }


    SingleFingerCaptureView.SingleCaptureListener listener = new SingleFingerCaptureView.SingleCaptureListener() {
        @Override
        public void onSingleCaptureFinished(FingerPrintCaptureResult result) {
            captureSuccessful(result.getWsqData());
        }

        @Override
        public void onSingleCaptureError(int i, String s) {

        }

        @Override
        public void onSingleMetricAvailable(SlapCaptureResult slapCaptureResult) {

        }

        @Override
        public void onSingleCaptureCancelled() {

        }

        @Override
        public void onSingleCaptureStarted() {

        }
    };

    private void captureSuccessful(byte[] wsqData) {
        AlertDialog.Builder launchDialog = new AlertDialog.Builder(this);
        launchDialog.setMessage(R.string.message_capture);
        launchDialog.setTitle(R.string.text_save_enrollment);

        launchDialog.setPositiveButton(R.string.text_save_enrollment_button, (dialog, which) -> {
            String wsqString = Base64.encodeToString(wsqData, Base64.NO_WRAP);
            DataSession.getInstance().getTextData().put(R.layout.activity_test_single_finger, wsqString);
            DataSession.getInstance().getTextData().put(R.id.finger_capture_type, fingerTypes.getValue());

            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(this::collateData);

        });

        launchDialog.setNegativeButton(R.string.text_recapture_fprint, null);
        launchDialog.setCancelable(false);
        launchDialog.show();
    }

    private void collateData(){
        showProgressDialog("Preparing data...", false);

        Thread t = new Thread(){
            @Override
            public void run(){
                boolean isSaved = DataSession.getInstance().save();
                if(isSaved) {
                    Intent intent = new Intent(EnrollmentFingerprintActivity.this, PreviewEnrollmentActivity.class);
                    startActivity(intent);
                }
                runOnUiThread(() -> stopProgressDialog());
            }
        };
        t.start();
    }


    public void showProgressDialog(String message, boolean cancel) {
        if (!this.isFinishing() && !this.isDestroyed()) {
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
}
