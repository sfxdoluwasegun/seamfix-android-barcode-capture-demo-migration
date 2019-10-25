package com.seamfix.qrcode.verification;

import android.graphics.Bitmap;

public class Session {
    private static Session ourInstance = new Session();
    private Bitmap croppedBitmap;

    public static Session getInstance() {
        if(ourInstance == null){
            ourInstance = new Session();
        }
        return ourInstance;
    }

    private Session() {
    }

    public Bitmap getCroppedBitmap() {
        return croppedBitmap;
    }

    public void setCroppedBitmap(Bitmap croppedBitmap) {
        this.croppedBitmap = croppedBitmap;
    }

    public void destroyInstance(){
        ourInstance = null;
    }
}
