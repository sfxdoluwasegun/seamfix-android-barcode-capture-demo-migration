package com.seamfix.qrcode.enrollment;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import com.seamfix.sdk.fingerprint.FingerCaptureOption;
import com.seamfix.sdk.fingerprint.FingerPrintCaptureResult;
import com.seamfix.sdk.fingerprint.FingerTypes;
import com.seamfix.sdk.fingerprint.SingleFingerCaptureView;
import com.seamfix.sdk.fingerprint.SkipReasonData;
import com.seamfix.sdk.fingerprint.slap.SlapCaptureResult;
import com.seamfix.seamcode.R;


import java.util.ArrayList;

public class EnrollmentFingerprintActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_test_single_finger);
        SingleFingerCaptureView slapFingerCaptureView = findViewById(R.id.id_finger_capture_view);

        ArrayList<SkipReasonData> data = new ArrayList<>();
        FingerCaptureOption option = new FingerCaptureOption(true, null, false);
        option.setSupportedScanner("KOJAK,FIVE-0");
        option.setSkipReasonData(data);
        option.setFingerTypes(FingerTypes.LEFT_THUMB);
        slapFingerCaptureView.init(this, option, listener);
    }


    SingleFingerCaptureView.SingleCaptureListener listener = new SingleFingerCaptureView.SingleCaptureListener() {
        @Override
        public void onSingleCaptureFinished(FingerPrintCaptureResult result) {


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
}
