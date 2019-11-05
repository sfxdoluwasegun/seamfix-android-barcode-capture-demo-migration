package com.seamfix.qrcode;


public class FaceFeatures {

    private static FaceFeatures faceFeatures;

    public native int[]   generateFeatures(float[]x, float[]y);
    public native float   matchFeatures (int[] initialFeatures, float[]x, float[]y);
    public native float   matchEmbeddings(float[] probe, float[] candidate);


    /**
     * Generates unique face feature for a face
     *
     * @param base64Encoded base64 encode image data
     * @return encoded face features
     */
    public native float[] generateFaceFeatures(String base64Encoded);

    /**
     * Performs a match between extracted face features and a base64 encoded candidate image
     *
     * @param rawFeatures Raw face features
     * @param candidate Image data, base64 encoded
     * @return a score between 0 and 1
     */
    public native float matchFaces(float[] rawFeatures, String candidate);

    static {
        System.loadLibrary("native-lib");
    }

    public static FaceFeatures getInstance(){
        if(faceFeatures ==  null){
            faceFeatures = new FaceFeatures();
        }
        return faceFeatures;
    }
}
