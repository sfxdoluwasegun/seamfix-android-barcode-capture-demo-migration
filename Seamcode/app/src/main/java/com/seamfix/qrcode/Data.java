package com.seamfix.qrcode;

import android.util.Log;

import com.google.gson.Gson;

/**
 * Data format
 */

public class Data {



    /**
     * Type of finger
     */
    private String n;

    /**
     * Base64 encoded finger template data
     */
    private String d;

    public Data(String name, String data) {
        this.n = name;
        this.d = data;
    }

    public String getAsCharSeparation(String separation){
        String concatData =  String.format("%s%s%s", n, separation, d );
        Log.e("CONCATENATED DATA:==", concatData);
        return concatData;
    }

    public String getN() {
        return n;
    }

    public void setN(String n) {
        this.n = n;
    }

    public String getD() {
        return d;
    }

    public void setD(String d) {
        this.d = d;
    }

    /**
     * Finger names
     */
    public enum FingerName {
        LT,LI,LM,LR,LP,RT,RI,RM,RR,RP
    }
}
