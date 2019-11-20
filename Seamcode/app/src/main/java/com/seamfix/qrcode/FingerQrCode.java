package com.seamfix.qrcode;


import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

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
import com.sf.bio.lib.util.Crypter;

import net.glxn.qrgen.android.QRCode;
import net.glxn.qrgen.core.image.ImageType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

public class FingerQrCode {

    private static final String SEPARATION = ",";
    private static Gson gson = new Gson();

    public static Bitmap encodeWsqTemplate(byte[] wsqData, String name){
        FingerprintTemplate probe = new FingerprintTemplate().dpi(500).create(wsqData);
        String probeSerial = probe.serialize();
        Log.e("LENGTH", "DATA OF SERIALIZED: "+ probeSerial);
        Log.e("LENGTH", "LENGTH OF SERIALIZED"+ probeSerial.length());

        byte[] isoTemp = FingerprintCompatibility.toAnsiIncits378v2009(probe);
        String isoTempString = Base64.encodeToString(isoTemp, Base64.NO_WRAP);
        String data = gson.toJson(new Data(name, isoTempString));
        byte[]encData = Crypter.encrypt("E5572DA36352063BBBA1321799B941B8FC337E64F5B4AB15EB01B9F68FB946A1", data);
        String encString = Base64.encodeToString(encData, Base64.NO_WRAP);

        QRCode code = QRCode.from(encString);
        code.to(ImageType.PNG);
        code.withSize(400, 400);
        return code.bitmap();
    }

    public static Bitmap encodeEnrollData(EnrollmentData enrollmentData){
        String data = gson.toJson(enrollmentData);

        try (ByteArrayOutputStream fos = new ByteArrayOutputStream();
             GZIPOutputStream gos = new GZIPOutputStream(fos);
             ObjectOutputStream oos = new ObjectOutputStream(gos)) {

            oos.writeObject(enrollmentData);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[]encData = Crypter.encrypt("E5572DA36352063BBBA1321799B941B8FC337E64F5B4AB15EB01B9F68FB946A1", data);
        String encString = Base64.encodeToString(encData, Base64.NO_WRAP);
        Log.e("BASE=====", encString);
        QRCode code = QRCode.from(encString);
        code.to(ImageType.PNG);
        code.withSize(800, 800);
        return code.bitmap();
    }

    public static EnrollmentData decodeEnrollmentData(String qrData){
        byte[] encData = Base64.decode(qrData, Base64.NO_WRAP);
        String jsonData = Crypter.decrypt("E5572DA36352063BBBA1321799B941B8FC337E64F5B4AB15EB01B9F68FB946A1", encData);
        return gson.fromJson(jsonData, EnrollmentData.class);
    }


    public static String getWsqTemplate(byte[] wsqData){
        FingerprintTemplate probe = new FingerprintTemplate().dpi(500).create(wsqData);
        String probeSerial = probe.serialize();
        Log.e("LENGTH", "DATA OF SERIALIZED: "+ probeSerial);
        Log.e("LENGTH", "LENGTH OF SERIALIZED"+ probeSerial.length());
        byte[] isoTemp = FingerprintCompatibility.toAnsiIncits378v2009(probe);
        return Base64.encodeToString(isoTemp, Base64.NO_WRAP);
    }



    public static FingerprintTemplate decodeWsqTemplate(Bitmap qrBitmap){
        int[] intArray = new int[qrBitmap.getWidth() * qrBitmap.getHeight()];
        qrBitmap.getPixels(intArray, 0, qrBitmap.getWidth(), 0, 0, qrBitmap.getWidth(), qrBitmap.getHeight());
        LuminanceSource source = new RGBLuminanceSource(qrBitmap.getWidth(), qrBitmap.getHeight(), intArray);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        Reader reader = new MultiFormatReader();

        try {
            Result mResult = reader.decode(bitmap);
            String text = mResult.getText();
            byte[] decodedIsoTemp = Base64.decode(text, Base64.NO_WRAP);
            return FingerprintCompatibility.convert(decodedIsoTemp);
        } catch (NotFoundException | ChecksumException | FormatException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static double matchQrWithFingerprint(byte[] wsqData, Bitmap qrBitmapImage){
        FingerprintTemplate probe     = new FingerprintTemplate().dpi(500).create(wsqData);
        FingerprintTemplate candidate = decodeWsqTemplate(qrBitmapImage);
        if(candidate != null) {
            return new FingerprintMatcher().index(probe).match(candidate);
        }
        return -4D;
    }


    public static double matchQrWithFingerprint(byte[] probeWsqData, String decodedIsoTemp){
        FingerprintTemplate probe     = new FingerprintTemplate().dpi(500).create(probeWsqData);
        byte[] isoTempByte = Base64.decode(decodedIsoTemp, Base64.NO_WRAP);
        FingerprintTemplate candidate = FingerprintCompatibility.convert(isoTempByte);
        if(candidate != null) {
            return new FingerprintMatcher().index(probe).match(candidate);
        }
        return -4D;
    }

    public static Data getDecodeQrData(String qrData){
        byte[] encData = Base64.decode(qrData, Base64.NO_WRAP);
        String jsonData = Crypter.decrypt("E5572DA36352063BBBA1321799B941B8FC337E64F5B4AB15EB01B9F68FB946A1", encData);
        return gson.fromJson(jsonData, Data.class);
    }


    public static String encodeEmbeddings(float[] embedding){
        String finalString = "";
        for(float d: embedding){
            finalString = finalString.concat(String.valueOf(d));
        }
        return finalString;
    }

    public static float[] decodeEmbeddings(String encodedEmbeddings){
        String[] construct = encodedEmbeddings.split("0\\.");
        float[] embeds = new float[136];
        for(int m = 0; m< construct.length; m++){
            String d = construct[m];
            if(d.isEmpty()){
                continue;
            }
            String k = "0." +construct[m];
            float f = Float.parseFloat(k);
            embeds[m-1] = f;
        }
        return embeds;
    }
}
