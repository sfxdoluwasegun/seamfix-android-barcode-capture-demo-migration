package com.seamfix.qrcode.opencv;


import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import static org.opencv.core.CvType.CV_32F;
import static org.opencv.core.CvType.CV_32FC3;

public class CVUtil {

    public CVUtil() {

    }


    public float[] generateEigenFace(Bitmap bitmap, int eigenFaces){

        Mat image = new Mat(new Size(bitmap.getWidth(), bitmap.getHeight()), CvType.CV_8UC3);
        Bitmap modifiedBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(modifiedBitmap, image);

        double [] img = image.get(0, 0);
        Log.e("Image Matrix", "====" + img);

        image.convertTo(image, CV_32FC3, 1/255.0);

        double [] img2 = image.get(0, 0);
        Log.e("Image Matrix", "====" + img2);



        Mat imgFlip = new Mat();
        Core.flip(image, imgFlip, 1);
        double [] f = imgFlip.get(0, 0);
        Log.e("Image Matrix", "====" + f);


        Mat imgHalfFlip = new Mat();
        Core.flip(image, imgHalfFlip, -1);
        double [] hf = imgHalfFlip.get(0, 0);
        Log.e("Image Matrix", "====" + hf);


        Vector <Mat>trainingImages = new Vector<>();
        trainingImages.add(image);
        trainingImages.add(imgFlip);
        trainingImages.add(imgHalfFlip);
        Mat imageMatrix = createDataMatrix(trainingImages);

        double [] checkMtr = imageMatrix.get(0, 0);
        Log.e("Image Matrix", "====" + checkMtr);

        Mat mean = new Mat();
        Mat eigenVectors = new Mat();
        Log.e("MATRIX", "====" + imageMatrix.size());

        Core.PCACompute(imageMatrix, mean, eigenVectors, eigenFaces);
        Log.e("MEAN", "====" + mean.size());
        Log.e("VECTOR", "====" + eigenVectors.size());
        Log.e("MEAN", "====" + mean.size());
        Log.e("VECTOR", "====" + eigenVectors);

        Mat result = new Mat();
        Mat m = imageMatrix.row(0);
        Log.e("OBJECT", "====" + m.size());

//        Mat eig = eigenVectors.row(0);
//        float[] buff = new float[(int) eig.total() * eig.channels()];
//        eig.get(0, 0, buff);
//        float[] out = new float[40];
//        System.arraycopy(buff, 0, out, 0, out.length);
//        Log.e("FINAL RESULT", "====" + Arrays.toString(out));
//        return out;

        Core.PCAProject(m, mean, eigenVectors, result);

        double [] check = result.get(0, 0);
        double [] checkEg = eigenVectors.get(0, 0);
        double [] checkMean = mean.get(0, 0);

        Log.e("RESULT", "====" + Arrays.toString(check));
        Log.e("RESULT", "====" + Arrays.toString(checkEg));
        Log.e("RESULT", "====" + Arrays.toString(checkMean));



        float[] buff = new float[(int) result.total() * result.channels()];
        result.get(0, 0, buff);

        Log.e("FINAL RESULT", "====" + Arrays.toString(buff));
        return buff;

    }


    public float[] generateEigenFace(ArrayList<Bitmap> bitmaps, int eigenFaces){
        Vector <Mat>trainingImages = new Vector<>();

        for(Bitmap bmp : bitmaps){
            Mat image = new Mat(new Size(bmp.getWidth(), bmp.getHeight()), CvType.CV_8UC3);
            Bitmap modifiedBitmap = bmp.copy(Bitmap.Config.ARGB_8888, true);
            Utils.bitmapToMat(modifiedBitmap, image);
            image.convertTo(image, CV_32FC3, 1/255.0);


//            Mat imgFlip = new Mat();
//            Core.flip(image, imgFlip, 1);
//            double [] f = imgFlip.get(0, 0);
//            Log.e("Image Matrix", "====" + f);
//
//
//            Mat imgHalfFlip = new Mat();
//            Core.flip(image, imgHalfFlip, -1);
//            double [] hf = imgHalfFlip.get(0, 0);
//            Log.e("Image Matrix", "====" + hf);

            trainingImages.add(image);
//            trainingImages.add(imgFlip);
//            trainingImages.add(imgHalfFlip);
        }

        Mat imageMatrix = createDataMatrix2(trainingImages);

        double [] checkMtr = imageMatrix.get(0, 0);
        Log.e("Image Matrix", "====" + checkMtr);

        Mat mean = new Mat();
        Mat eigenVectors = new Mat();
        Log.e("MATRIX", "====" + imageMatrix.size());

        Core.PCACompute(imageMatrix, mean, eigenVectors, eigenFaces);
        Log.e("MEAN", "====" + mean.size());
        Log.e("VECTOR", "====" + eigenVectors.size());
        Log.e("MEAN", "====" + mean.size());
        Log.e("VECTOR", "====" + eigenVectors);

        Mat result = new Mat();
        Mat m = imageMatrix.row(0);
        Log.e("OBJECT", "====" + m.size());

        Core.PCAProject(m, mean, eigenVectors, result);

        double [] check = result.get(0, 0);
        double [] checkEg = eigenVectors.get(0, 0);
        double [] checkMean = mean.get(0, 0);

        Log.e("RESULT", "====" + Arrays.toString(check));
        Log.e("RESULT", "====" + Arrays.toString(checkEg));
        Log.e("RESULT", "====" + Arrays.toString(checkMean));

        float[] buff = new float[(int) result.total() * result.channels()];
        result.get(0, 0, buff);

        Log.e("FINAL RESULT", "====" + Arrays.toString(buff));
        return buff;

    }

    private Mat createDataMatrix2(Vector<Mat> trainingImages){
        Mat x = trainingImages.get(0);
        int total = x.rows() * x.cols() * 3;
        Mat mat = new Mat(trainingImages.size(), total, CV_32F);
        for(int i = 0; i < trainingImages.size(); i++) {
            //Mat X = mat.row(i);
            Mat c = trainingImages.get(i);

            printMatAsFloat(c, "C- BEFORE R-RESHAPE");
            Mat d = c.reshape(1,1);
            printMatAsFloat(d, "AFTER-RESHAPE");
            Mat row_i = mat.row(i);

            Log.e("SIZE OF MAT", "" +  row_i.size());
            Log.e("SIZE OF RESHAPE", "" + row_i.size());
            d.copyTo(row_i);
            printMatAsFloat(mat, "AFTER-CONVERT");
        }

        printMatAsFloat(mat, "FINAL-MAT VALUE");
        return mat;
    }


    private Mat createDataMatrix(Vector<Mat> trainingImages){


        Mat x = trainingImages.get(0);
        int total = x.rows() * x.cols() * 3;

        // build matrix (column)
        // This matrix will have one col for each image and imagerows x imagecols rows
        Mat mat = new Mat(trainingImages.size(), total, CV_32F);
        for(int i = 0; i < trainingImages.size(); i++) {
            //Mat X = mat.row(i);
            Mat c = trainingImages.get(i);

            printMatAsFloat(c, "C- BEFORE R-RESHAPE");


            Mat d = c.reshape(1,1);


            printMatAsFloat(d, "AFTER-RESHAPE");
            Mat row_i = mat.row(i);

            Log.e("SIZE OF MAT", "" +  row_i.size());
            Log.e("SIZE OF RESHAPE", "" + row_i.size());


            d.copyTo(row_i);

            printMatAsFloat(mat, "AFTER-CONVERT");
        }

        printMatAsFloat(mat, "FINAL-MAT VALUE");


//        Mat x = trainingImages.get(0);
//        int total = x.rows() * x.cols() * 3;
//        // build matrix (column)
//        // This matrix will have one col for each image and imageRows x imageCols rows
//        Mat mat = new Mat(trainingImages.size(), total, CvType.CV_32FC1);
//        for(int i = 0; i < trainingImages.size(); i++) {
//            Mat c = trainingImages.get(i);
//            Mat d = c.reshape(1,total);
//            Log.e("RESHAPE", "====" + d.size());
//            d.convertTo(mat.col(i),);
//        }
        return mat;
    }


    private void printMatAsFloat(Mat mat, String tag){
        float[] buff = new float[(int) mat.total() * mat.channels()];
        mat.get(0, 0, buff);
        Log.e(tag, "====" + Arrays.toString(buff));

    }

}
