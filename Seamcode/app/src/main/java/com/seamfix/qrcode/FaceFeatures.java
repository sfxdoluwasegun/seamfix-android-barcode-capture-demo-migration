package com.seamfix.qrcode;


public class FaceFeatures {

    private static FaceFeatures faceFeatures;

    public native int[] generateFeatures(float[]x, float[]y);
    public native float matchFeatures (int[] initialFeatures, float[]x, float[]y);
    public native float matchEmbeddings(float[] probe, float[] candidate);

    static {
        System.loadLibrary("featureCalculation");
    }

    public static FaceFeatures getInstance(){
        if(faceFeatures ==  null){
            faceFeatures = new FaceFeatures();
        }
        return faceFeatures;
    }
}
