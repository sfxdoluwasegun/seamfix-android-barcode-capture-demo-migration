package com.seamfix.qrcode;

import android.graphics.Bitmap;

/**
 * @author Biose Nonso Emmanuel
 * @since 10/08/2018
 * This is the base class for Image operations using Sdk-Titanic-Droid
 */

public abstract class ImageOperator {

    /**
     * This method, extracts pixels from a scaledBitmap. The pixels are used by a Tensorflow model to make predictions
     *
     * @param scaledBitmap a scaled Bitmap
     * @return scaledBitmap pixels
     */

    protected final float[] extractBitmapPixels(Bitmap scaledBitmap) {
        int scaledBitmapWidth = scaledBitmap.getWidth();
        int scaledBitmapHeight = scaledBitmap.getHeight();

        // Declare and initialize the scaledBitmapPixels Array to hold the final result of the extracted pixels
        float[] scaledBitmapPixels = new float[scaledBitmapWidth * scaledBitmapHeight * 3];

        // Declare and initialize the pixels Array to hold the pixels extracted from the scaledBitmap
        int[] pixels = new int[scaledBitmap.getHeight() * scaledBitmap.getWidth()];

        // Extract pixels from the scaledBitmap and store in the pixelsArray
        scaledBitmap.getPixels(pixels, 0, scaledBitmap.getWidth(), 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight());

        //
        for (int i = 0; i < pixels.length; ++i) {
            final int val = pixels[i];
            scaledBitmapPixels[i * 3] = ((val >> 16) & 0xFF);
            scaledBitmapPixels[i * 3 + 1] = ((val >> 8) & 0xFF);
            scaledBitmapPixels[i * 3 + 2] = (val & 0xFF);
        }
        return scaledBitmapPixels;
    }

    /**
     * This method, normalizes the pixels containing a scaledBitmap
     *
     * @param scaledBitmapPixels
     */

    protected abstract float[] normalizeBitmapPixels(float[] scaledBitmapPixels);


    /**
     * This method is used to free up the Tensorflow object and close the session
     */
    public abstract void closeTensorFlowInference();
}
