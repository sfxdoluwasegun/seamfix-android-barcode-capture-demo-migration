package com.seamfix.qrcode;

import android.graphics.Bitmap;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.ByteArrayInputStream;

public class TensorUtil extends ImageOperator{

    private final static String TENSORFLOW_MODEL_INPUT_NAME = "input_2";
    private final static String TENSORFLOW_MODEL_OUTPUT_NAME = "fc_14/bias/read";
    private static TensorFlowInferenceInterface tensorFlowInferenceInterface;
    private static TensorUtil ourInstance;

    public static TensorUtil getInstance() {
        if (ourInstance == null) {
            ourInstance = new TensorUtil();
        }
        return ourInstance;
    }


    /**
     * Initialises tensor flow sync with the supplied model
     * @param modelBytes model
     */
    public boolean initializeTensorflowInferenceAsyn(byte[] modelBytes) {
        if(tensorFlowInferenceInterface == null && modelBytes != null) {
            tensorFlowInferenceInterface = new TensorFlowInferenceInterface(new ByteArrayInputStream(modelBytes));
            return true;
        }
        return tensorFlowInferenceInterface != null;
    }

    @Override
    protected float[] normalizeBitmapPixels(float[] scaledBitmapPixels) {
        return new float[0];
    }

    /**
     * Closes and release tensorflow intstance
     */
    public void closeTensorFlowInference() {
        if(tensorFlowInferenceInterface != null) {
            tensorFlowInferenceInterface.close();
            tensorFlowInferenceInterface = null;
        }
    }


    /**
     * Generates the embedding from a given bitmap image source
     *
     * @param bitmap bitmap source input
     * @return the embedding
     */
    public float[] getEmbeddings(Bitmap bitmap) {
        float[]bitmapPixels = super.extractBitmapPixels(bitmap);
        if (tensorFlowInferenceInterface == null) {
            throw new IllegalStateException("Tensor-flowInference not initialized, call the initializeTensorFlow method");
        } else {
            float[] predictionResults = new float[136];
            String[] outputNames = new String[]{TENSORFLOW_MODEL_OUTPUT_NAME};
            // Pass the normalizedPixels pixels to the Tensor-flow Model
            tensorFlowInferenceInterface.feed(TENSORFLOW_MODEL_INPUT_NAME, bitmapPixels, 1, 64, 64, 3);
            // Run the prediction
            tensorFlowInferenceInterface.run(outputNames);
            // Get the predication results
            tensorFlowInferenceInterface.fetch(TENSORFLOW_MODEL_OUTPUT_NAME, predictionResults);
            return  predictionResults;
        }
    }
}
