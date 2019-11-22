package com.seamfix.qrcode;

import android.text.TextUtils;

import java.io.Serializable;

/**
 * t = text
 * p = print template
 * d = finger type
 * f = face template - json encoded
 */
public class EnrollmentData2 implements Serializable {
   private float[] f;
   private byte[] p;
   private String d;
   private String t;
   private String g;


    public float[] getF() {
        return f;
    }

    public void setF(float[] f) {
        this.f = f;
    }

    public void setT(String t) {
        this.t = t;
    }

    public byte[] getP() {
        return p;
    }

    public void setP(byte[] p) {
        this.p = p;
    }

    public String getD() {
        return d;
    }

    public void setD(String d) {
        this.d = d;
    }

    public String getT() {
        return t;
    }

    public String getG() {
        return g;
    }

    public void setG(String g) {
        this.g = g;
    }
}
