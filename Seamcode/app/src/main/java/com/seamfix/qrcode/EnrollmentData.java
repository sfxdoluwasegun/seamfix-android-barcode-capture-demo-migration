package com.seamfix.qrcode;

/**
 * t = text
 * p = print template
 * d = finger type
 * f = face template - json encoded
 */
public class EnrollmentData {
   private String f;
   private String p;
   private String d;
   private String t;

    public String getF() {
        return f;
    }

    public void setF(String f) {
        this.f = f;
    }

    public String getP() {
        return p;
    }

    public void setP(String p) {
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

    public void setT(String t) {
        this.t = t;
    }
}
