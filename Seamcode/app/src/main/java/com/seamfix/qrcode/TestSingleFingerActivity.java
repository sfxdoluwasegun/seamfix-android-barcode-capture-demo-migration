package com.seamfix.qrcode;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

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
import com.machinezoo.sourceafis.FingerprintMatcher;
import com.machinezoo.sourceafis.FingerprintTemplate;
import com.seamfix.sdk.fingerprint.FingerCaptureOption;
import com.seamfix.sdk.fingerprint.FingerPrintCaptureResult;
import com.seamfix.sdk.fingerprint.FingerTypes;
import com.seamfix.sdk.fingerprint.SingleFingerCaptureView;
import com.seamfix.sdk.fingerprint.SkipReasonData;
import com.seamfix.sdk.fingerprint.slap.SlapCaptureResult;
import com.seamfix.sdk.fingerprint.threshold.FingerThresholds;
import com.seamfix.sdk.fingerprint.threshold.ThresholdEntity;
import com.seamfix.seamcode.R;
import com.sf.bio.lib.util.FileUtils;

import net.glxn.qrgen.android.QRCode;
import net.glxn.qrgen.core.image.ImageType;

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
                performEncoder(result);
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


    private void performEncoder(FingerPrintCaptureResult result){
        Bitmap bitmap  = FingerQrCode.encodeWsqTemplate(result.getWsqData(), FingerTypes.RIGHT_THUMB.getValue());
        Log.e("BITMAP", bitmap.toString());
    }


    private void endToEndRaw(FingerPrintCaptureResult result){
        FingerprintTemplate probe = new FingerprintTemplate().dpi(500).create(result.getWsqData());
        String probeSerial = probe.serialize();
        Log.e("LENGTH", "DATA OF SERIALIZED: "+ probeSerial);
        Log.e("LENGTH", "LENGTH OF SERIALIZED"+ probeSerial.length());

        byte[] isoTemp = FingerprintCompatibility.toAnsiIncits378v2009(probe);
        String base64 = Base64.encodeToString(isoTemp, Base64.NO_WRAP);

        Log.e("LENGTH", "RAW LENGTH OF ISO: "+ isoTemp.length);
        Log.e("LENGTH", "DATA OF ISO: "+ base64);
        Log.e("LENGTH", "LENGTH OF ISO :" + base64.length());


        QRCode code = QRCode.from(base64);
        code.to(ImageType.PNG);
        code.withSize(400, 400);
        Bitmap qrImage  = code.bitmap();

        int[] intArray = new int[qrImage.getWidth() * qrImage.getHeight()];
        qrImage.getPixels(intArray, 0, qrImage.getWidth(), 0, 0, qrImage.getWidth(),qrImage.getHeight());
        LuminanceSource source = new RGBLuminanceSource(qrImage.getWidth(), qrImage.getHeight(), intArray);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        Reader reader = new MultiFormatReader();

        byte[] decodedIsoTemp;
        try {
            Result mResult = reader.decode(bitmap);
            String text = mResult.getText();
            decodedIsoTemp = Base64.decode(text, Base64.NO_WRAP);
        } catch (NotFoundException | ChecksumException | FormatException e) {
            e.printStackTrace();
            Toast.makeText(TestSingleFingerActivity.this, "" + e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }


        if(candidate == null) {
            candidate = FingerprintCompatibility.convert(decodedIsoTemp);
            String candidateSerial = candidate.serialize();
            Log.e("LENGTH", "DATA OF SERIALIZED: " + candidateSerial);
        }

        double score = new FingerprintMatcher().index(probe).match(candidate);
        Log.e("SCORE", "MATCH SCORE: "+ score);
        Toast.makeText(TestSingleFingerActivity.this, ""+score, Toast.LENGTH_LONG).show();
    }
}
