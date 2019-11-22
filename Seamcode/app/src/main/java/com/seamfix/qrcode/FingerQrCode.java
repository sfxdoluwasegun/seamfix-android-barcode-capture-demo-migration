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
import com.sf.bio.lib.Hex;
import com.sf.bio.lib.util.Crypter;

import net.glxn.qrgen.android.QRCode;
import net.glxn.qrgen.core.image.ImageType;

import org.apache.commons.lang3.SerializationUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
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


        byte[]encData = Crypter.encrypt("E5572DA36352063BBBA1321799B941B8FC337E64F5B4AB15EB01B9F68FB946A1", data);
        char[] encHex = Hex.encodeHex(encData);
        String main = Base64.encodeToString(encData, Base64.NO_WRAP);
        String hexString = new String(encHex);

        //String encString = new String(encHex);//getRawString(encData); //Base64.encodeToString(encData, Base64.NO_WRAP);
        //String encString = new String(encData, StandardCharsets.UTF_8);
        //byte[] dd = encString.getBytes(StandardCharsets.UTF_8);

        //String mm = Crypter.decrypt("E5572DA36352063BBBA1321799B941B8FC337E64F5B4AB15EB01B9F68FB946A1", dd);

        //encString = "5b48b743f6215074f2d1dd3f5d6c7aa4cf64519cc3aba06b224202ab2bbc9b14d09e8f29ffd070c38d64d16378087a2643574a38358281a9719f5d3782fbf633ffdd1b87bf51083dd5d84db4e77262ac2e45856e10535275b4b6b97fe11de84bd6fefb36e7c7944e05235dcd16096440cf54e25a7875b7483e9181e72c56f26f81e419229cf441100eaf260a5c62dbb1981a1aa993ce7b04d81066751500ba3230aa5f5e7b08a0e64665930ed4d62ab0733ef3bb942cc7fc672a20c242702e4a0dda57a28e6912d1bf45122f79c7cf3d6085582a56f40ea1811c48a27d9094475549a1e23946bf2f7495aae7ac5a657c11eaad2788a90d12847ebb5d4be6dff17e5cabdd5e8fa8e85102f313c3104c817e0d8f4d1d85614713fce8543e5fdf529f2a1376a15bb01d9e65b38f6a033642837a83d91bb476acf8f1a0e975565f7547bc1d5806eaf7ac39de26b7a0ad5f84a28349a16a818fe7152556933cb2bfb94de0bfd39f5aaa8d7801b47131283a8ebda0a668a70d1f02ada190373f7dc4494f5aa227cf533f746d617337b156a4f5643fd3d4a153d4a3c606b59afc3ea0d63de1f4de45399548ad3167983352c5636a97a609f2b091fc8c64fb8a8c858faab58f10e6883d135ab7a22c9c99e038a0ddc0daab8bba8170d3e217a945d6def2a1df09e6376e41e6a2d2472330ffa6d3bd026967ccb37ee1ecdff1c1ca0e9697a374a5c32351f90fd6cfa6edea7d00d4d42a047a7aa6410739b4638a0b833944f2b5c28e4e41897f981562ef361e520b733858483e417d4ba60de8885321c69af7ac90a2c95e838a680925a004c527899362366c29c41451d933a8ddad11808a58d95a900f6259ae69b0df4620cc1c206f534df1e5d309f8424c3b3cd3458e60";
//        String encString = "";
//        for(char m : encHex){
//            CharSequence sequence = new CharSequence(encHex);
//            encString = (String)m;
//        }
        String encString = encode(enrollmentData);
        Log.e("BASE=====", encString + " : " + encString.length());
        QRCode code = QRCode.from(encString);
        code.to(ImageType.PNG);
        code.withSize(1100, 1100);
        return code.bitmap();
    }

    public static EnrollmentData decodeEnrollmentData(String qrData){
        byte[] encData = Hex.decodeHex(qrData.toCharArray()); //getRawByte(qrData); //Base64.decode(qrData, Base64.NO_WRAP);
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


    private static byte[] compress(byte[] encData) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(bos);
            gzip.write(encData);
            gzip.close();
            byte[] compressed = bos.toByteArray();
            bos.close();
            return compressed;
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] decompress(byte[] compressed) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(compressed);
            GZIPInputStream gZIPInputStream = new GZIPInputStream(bis);
            ByteArrayOutputStream fos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gZIPInputStream.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
            byte[] decompressed = fos.toByteArray();
            // Keep it in finally
            fos.close();
            gZIPInputStream.close();
            return decompressed;
        }catch (Exception e){
            e.printStackTrace();
        }


//        GZIPInputStream gis = new GZIPInputStream(bis);
//        BufferedReader br = new BufferedReader(new InputStreamReader(gis, "UTF-8"));
//        StringBuilder sb = new StringBuilder();
//        String line;
//        while((line = br.readLine()) != null) {
//            sb.append(line);
//        }
//        br.close();
//        gis.close();
//        bis.close();
//        return sb.toString();
        return null;
    }

    public static String getRawString(byte[] data){
        String in = "";
        for (byte datum : data) {
            char cha = ((char) datum);
            in = in.concat(String.valueOf(cha));
        }

        System.out.println(in);
        System.out.println(in.length());
        return in;
    }

    public static byte[] getRawByte(String rawString){
        char[] charData = rawString.toCharArray();
        byte[] recovered = new byte[charData.length];

        for(int n=0; n<charData.length; n++){
            byte cha = (byte)charData[n];
            recovered[n] = cha;
        }
        System.out.println(Arrays.toString(recovered));
        System.out.println(recovered.length);
        return recovered;
    }

    public  static String encode(EnrollmentData data){
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ObjectOutput out ;
            out = new ObjectOutputStream(bos);
            out.writeObject(data);
            out.flush();
            byte[] encoded = bos.toByteArray();
            byte[] yourBytes = compress(encoded);
            byte[] encData = Crypter.encrypt("E5572DA36352063BBBA1321799B941B8FC337E64F5B4AB15EB01B9F68FB946A1", yourBytes);

            System.out.println(yourBytes.length);
            String dataString = java.util.Base64.getEncoder().encodeToString(encData);
            System.out.println(dataString);
            System.out.print(dataString.length());
            return dataString;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public  static EnrollmentData decode(String data){
        byte[] base64Data = java.util.Base64.getDecoder().decode(data);
        byte[] decrypt = Crypter.decrypt("E5572DA36352063BBBA1321799B941B8FC337E64F5B4AB15EB01B9F68FB946A1", base64Data, null);
        byte[] decompressed = decompress(decrypt);

        EnrollmentData enrollmentData = SerializationUtils.deserialize(decompressed);
        return enrollmentData;
    }
}
