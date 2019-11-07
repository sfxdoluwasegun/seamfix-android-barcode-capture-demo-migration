package com.seamfix.qrcode;


import android.content.Context;

import com.seamfix.seamcode.R;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class FaceFeatures {

//    Uri path = Uri.parse("file:///android_asset/raw/sample/logout.png");
//
//    String newPath = path.toString();
    public static final String MODEL_FILE  = "file:///android_asset/datarec.dat";
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


    /**
     * Generates unique face feature for a face
     * with pca model file
     *
     * @param base64Encoded base64 encode image data
     * @param pcaFilename file path to the pca model
     * @return encoded face features
     */
    public native float[] generatepcafeatures(String pcaFilename, String base64Encoded);

    /**
     * Performs a match between extracted face features and a base64 encoded candidate image
     * with pca model file
     *
     * @param existingFeatures Raw face features
     * @param probeImageString Image data, base64 encoded
     * @param pcaFilename file path to the pca model
     * @return a score between 0 and 1
     */
    public native float matchpcafeatures(String pcaFilename, String probeImageString, float[] existingFeatures);


    /**
     * Gets file path to model from android raw resource directory
     *
     * @param context application context
     * @return the absolute path to the model file or null if file was not found
     */
    public static String getFaceModelFileName(Context context){
        File mCascadeFile;
        InputStream is = context.getResources().openRawResource(R.raw.data);
        File cascadeDir = context.getDir("model", Context.MODE_PRIVATE);
        mCascadeFile = new File(cascadeDir, "datarec.dat");
        FileOutputStream os;
        try {
            os = new FileOutputStream(mCascadeFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();
            //String data = FileUtils.readFileToString(mCascadeFile, StandardCharsets.UTF_8);
            //System.out.println(data);
            return mCascadeFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

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
