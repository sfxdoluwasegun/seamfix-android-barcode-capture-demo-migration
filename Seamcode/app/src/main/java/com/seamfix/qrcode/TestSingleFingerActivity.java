package com.seamfix.qrcode;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.machinezoo.sourceafis.FingerprintCompatibility;
import com.machinezoo.sourceafis.FingerprintMatcher;
import com.machinezoo.sourceafis.FingerprintTemplate;
import com.seamfix.sdk.fingerprint.FingerCaptureOption;
import com.seamfix.sdk.fingerprint.FingerPrintCaptureResult;
import com.seamfix.sdk.fingerprint.SingleFingerCaptureView;
import com.seamfix.sdk.fingerprint.SkipReasonData;
import com.seamfix.sdk.fingerprint.slap.SlapCaptureResult;
import com.seamfix.sdk.fingerprint.threshold.FingerThresholds;
import com.seamfix.sdk.fingerprint.threshold.ThresholdEntity;
import com.seamfix.seamcode.R;
import com.sf.bio.lib.util.FileUtils;

import java.util.ArrayList;

public class TestSingleFingerActivity extends AppCompatActivity {
    SingleFingerCaptureView singleFingerCaptureView;
    FingerprintTemplate candidate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_single_finger);
        singleFingerCaptureView = findViewById(R.id.id_finger_capture_view);

        ArrayList<SkipReasonData> data = new ArrayList<>();
        SkipReasonData data1 = new SkipReasonData();
        data1.setLabel("wrong finger");
        data1.setValue("twi");
        data.add(data1);
        data1.setLabel("amputed finger");
        data1.setValue("ap");

        byte[] defaultData = FileUtils.readAsset(getApplicationContext(), "optimal.json");
        String defaultString = new String(defaultData);
        ThresholdEntity entity = new Gson().fromJson(defaultString, ThresholdEntity.class);

        FingerThresholds thresholds = entity != null? entity.getFingerThreshold() : null;
        FingerCaptureOption option = new FingerCaptureOption(true, thresholds, false);
        option.setSupportedScanner("KOJAK,FIVE-0");
        option.setSkipReasonData(data);

        singleFingerCaptureView.init(this, option, new SingleFingerCaptureView.SingleCaptureListener() {
            @Override
            public void onSingleCaptureFinished(FingerPrintCaptureResult result) {
                FingerprintTemplate probe = new FingerprintTemplate().dpi(500).create(result.getWsqData());
                String probeSerial = probe.serialize();
                Log.e("LENGTH", "DATA OF SERIALIZED: "+ probeSerial);
                Log.e("LENGTH", "LENGTH OF SERIALIZED"+ probeSerial.length());

                byte[] isoTemp = FingerprintCompatibility.toAnsiIncits378v2009(probe);
                String base64 = Base64.encodeToString(isoTemp, Base64.NO_WRAP);

                Log.e("LENGTH", "RAW LENGTH OF ISO: "+ isoTemp.length);
                Log.e("LENGTH", "DATA OF ISO: "+ base64);
                Log.e("LENGTH", "LENGTH OF ISO :" + base64.length());

                if(candidate == null) {
                    candidate = FingerprintCompatibility.convert(isoTemp);
                    String candidateSerial = candidate.serialize();
                    Log.e("LENGTH", "DATA OF SERIALIZED: " + candidateSerial);
                }

                double score = new FingerprintMatcher().index(probe).match(candidate);
                Log.e("SCORE", "MATCH SCORE: "+ score);
                Toast.makeText(TestSingleFingerActivity.this, ""+score, Toast.LENGTH_LONG).show();
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
        });
    }
}
