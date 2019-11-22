package com.seamfix.qrcode.verification;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.seamfix.qrcode.Data;
import com.seamfix.qrcode.EnrollmentData;
import com.seamfix.qrcode.FingerQrCode;
import com.seamfix.qrcode.HomeActivity;
import com.seamfix.sdk.fingerprint.FingerCaptureOption;
import com.seamfix.sdk.fingerprint.FingerPrintCaptureResult;
import com.seamfix.sdk.fingerprint.FingerTypes;
import com.seamfix.sdk.fingerprint.SingleFingerCaptureView;
import com.seamfix.sdk.fingerprint.SkipReasonData;
import com.seamfix.sdk.fingerprint.slap.SlapCaptureResult;
import com.seamfix.seamcode.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class VerificationFingerprintActivity extends AppCompatActivity {

    private String fingerName;
    private String decodedIsoTemplate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_test_single_finger);
        SingleFingerCaptureView slapFingerCaptureView = findViewById(R.id.id_finger_capture_view);

        String templateData = getIntent().getStringExtra("value");
        EnrollmentData enrollmentData = FingerQrCode.decode(templateData);
        if(enrollmentData!= null){
            decodedIsoTemplate = enrollmentData.getP();
            fingerName = enrollmentData.getD();
            fingerName = fingerName.toUpperCase().replaceAll(" ","_");
        }

        if(decodedIsoTemplate != null) {
            ArrayList<SkipReasonData> data = new ArrayList<>();
            FingerCaptureOption option = new FingerCaptureOption(true, null, false);
            option.setSupportedScanner("KOJAK,FIVE-0");
            option.setSkipReasonData(data);
            option.setFingerTypes(FingerTypes.fromString(fingerName));
            slapFingerCaptureView.init(this, option, listener);
        }else{
            Toast.makeText(this, "Unrecognised data format", Toast.LENGTH_SHORT).show();
        }
    }


    SingleFingerCaptureView.SingleCaptureListener listener = new SingleFingerCaptureView.SingleCaptureListener() {
        @Override
        public void onSingleCaptureFinished(FingerPrintCaptureResult result) {

            if(decodedIsoTemplate != null) {
                double score = FingerQrCode.matchQrWithFingerprint(result.getWsqData(), decodedIsoTemplate);
                boolean isMatch = score > 40D;
                if(isMatch){
                    launchFingerVerificationDetails();
                }else{
                    matchNotFoundDialog();
                }
                return;
            }
            Toast.makeText(VerificationFingerprintActivity.this, "Process was not successful", Toast.LENGTH_SHORT).show();
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

    public static boolean isJson(String Json) {
        try {
            new JSONObject(Json);
        } catch (JSONException ex) {
            try {
                new JSONArray(Json);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }

    private void matchFoundDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.match_found_layout, null, false);
        Button okButton = view.findViewById(R.id.ok);
        okButton.setOnClickListener(v -> {
            finish();
        });

        AlertDialog.Builder launchDialog = new AlertDialog.Builder(this);
        launchDialog.setView(view);
        launchDialog.setCancelable(false);
        launchDialog.show();
    }

    private void matchNotFoundDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.match_not_found_layout, null, false);
        Button okButton = view.findViewById(R.id.ok);
        AlertDialog launchDialog = new AlertDialog.Builder(this).create();
        okButton.setOnClickListener(v -> launchDialog.dismiss());
        launchDialog.setView(view);
        launchDialog.setCancelable(true);
        launchDialog.show();
    }

    private void launchFingerVerificationDetails(){
        Bundle bundle = getIntent().getExtras();
        Intent intent = new Intent(VerificationFingerprintActivity.this, FingerVerificationDetailsActivity.class);
        assert bundle != null;
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }
}
