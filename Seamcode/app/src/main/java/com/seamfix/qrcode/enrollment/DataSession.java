package com.seamfix.qrcode.enrollment;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Base64;

import com.google.gson.Gson;
import com.seamfix.qrcode.EnrollmentData;
import com.seamfix.qrcode.FingerQrCode;
import com.seamfix.seamcode.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class DataSession {
    private static DataSession ourInstance = new DataSession();
    private LinkedHashMap<Integer, String> textData = new LinkedHashMap<>();
    private Bitmap qrBitmap;

    public static DataSession getInstance() {
        if(ourInstance == null){
            ourInstance = new DataSession();
        }
        return ourInstance;
    }

    private DataSession() {
    }

    public LinkedHashMap<Integer, String> getTextData() {
        return textData;
    }

    public void setTextData(LinkedHashMap<Integer, String> textData) {
        this.textData = textData;
    }

    public boolean save(){

        EnrollmentData enrollmentData = new EnrollmentData();

        /*
        get text data
        .................
         */
        ArrayList<String> textData =  new ArrayList<>();
        textData.add(ourInstance.getTextData().get(R.id.field_first_name));
        textData.add(ourInstance.getTextData().get(R.id.field_last_name));
        textData.add(ourInstance.getTextData().get(R.id.field_registration_number));
        textData.add(ourInstance.getTextData().get(R.id.field_score));
        String text = TextUtils.join(":", textData);
        enrollmentData.setT(text);

        /*
        Get image data
        ...................................
         */
        String faceTemplate = ourInstance.getTextData().get(R.id.babs_template);
        //float[] templateData = new Gson().fromJson(faceTemplate, float[].class);
        //String finalString = FingerQrCode.encodeEmbeddings(templateData);
        enrollmentData.setF(faceTemplate);

        /*
        Get print data and extract template
        ...................................
         */
        String fingerData = ourInstance.getTextData().get(R.layout.activity_test_single_finger);
        String fingerType = ourInstance.getTextData().get(R.id.finger_capture_type);
        byte[] wsqData     = Base64.decode(fingerData, Base64.NO_WRAP);
        String wsqTemplate = FingerQrCode.getWsqTemplate(wsqData);
        enrollmentData.setP(wsqTemplate);
        enrollmentData.setD(fingerType);


        /*
        Generate QR data for enrollment
         */
        Bitmap enrollmentQr = FingerQrCode.encodeEnrollData(enrollmentData);
        ourInstance.setQrBitmap(enrollmentQr);
        return true;
    }

    public Bitmap getQrBitmap() {
        return qrBitmap;
    }

    public void setQrBitmap(Bitmap qrBitmap) {
        this.qrBitmap = qrBitmap;
    }

    public void destroy(){
        ourInstance = null;
    }
}
