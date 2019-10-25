package com.seamfix.qrcode.verification;

import android.graphics.Rect;
import android.util.Log;

import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;
import com.seamfix.qrcode.FaceFeatures;
import com.seamfix.qrcode.enrollment.Landmark;
import com.seamfix.qrcode.mtcnn.Box;

import java.util.Arrays;

public class VerificationUtil {


    public static Result getFaceFeatureScore(FirebaseVisionFace face, int[] candidateFeatures) {
        Rect bounds = face.getBoundingBox();

        float[] x = new float[10];
        float[] y = new float[10];

        if (bounds != null) {
            x[Landmark.LEFT_LOWER_BOUNDING] = bounds.left;
            y[Landmark.LEFT_LOWER_BOUNDING] = bounds.bottom;

            x[Landmark.RIGHT_LOWER_BOUNDING] = bounds.right;
            y[Landmark.RIGHT_LOWER_BOUNDING] = bounds.bottom;

            x[Landmark.RIGHT_UPPER_BOUNDING] = bounds.right;
            y[Landmark.RIGHT_UPPER_BOUNDING] = bounds.top;

            x[Landmark.LEFT_UPPER_BOUNDING] = bounds.left;
            y[Landmark.LEFT_UPPER_BOUNDING] = bounds.top;

            float m = bounds.exactCenterX();

            //x[Landmark.UPPER_FACE_CENTER] = m;
            //y[Landmark.UPPER_FACE_CENTER] = bounds.top;

            //x[Landmark.LOWER_FACE_CENTER] = m;
           //y[Landmark.LOWER_FACE_CENTER] = bounds.bottom;
        }


        FirebaseVisionFaceLandmark rightEye = face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EYE);
        if (rightEye != null) {
            x[Landmark.RIGHT_EYE] = rightEye.getPosition().getX();
            y[Landmark.RIGHT_EYE] = rightEye.getPosition().getY();
        }

        FirebaseVisionFaceLandmark leftEye = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EYE);
        if (leftEye != null) {
            x[Landmark.LEFT_EYE] = leftEye.getPosition().getX();
            y[Landmark.LEFT_EYE] = leftEye.getPosition().getY();
        }

        FirebaseVisionFaceLandmark noseTip = face.getLandmark(FirebaseVisionFaceLandmark.NOSE_BASE);
        if (noseTip != null) {
            x[Landmark.NOSE_TIP] = noseTip.getPosition().getX();
            y[Landmark.NOSE_TIP] = noseTip.getPosition().getY();
        }

        FirebaseVisionFaceLandmark mouthCenter = face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_BOTTOM);
        if (mouthCenter != null) {
            x[Landmark.MOUTH_CENTER] = mouthCenter.getPosition().getX();
            y[Landmark.MOUTH_CENTER] = mouthCenter.getPosition().getY();
        }

        FirebaseVisionFaceLandmark mouthLeft = face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_LEFT);
        if (mouthLeft != null) {
            x[Landmark.MOUTH_LEFT] = mouthLeft.getPosition().getX();
            y[Landmark.MOUTH_LEFT] = mouthLeft.getPosition().getY();
        }

        FirebaseVisionFaceLandmark mouthRight = face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_LEFT);
        if (mouthRight != null) {
            x[Landmark.MOUTH_RIGHT] = mouthRight.getPosition().getX();
            y[Landmark.MOUTH_RIGHT] = mouthRight.getPosition().getY();
        }

        Log.e("X====", "" + Arrays.toString(x));

        Log.e("Y====", "" + Arrays.toString(y));

        int[] features = FaceFeatures.getInstance().generateFeatures(x, y);
        Log.e("FEATRURE", "" + Arrays.toString(features));

        int[] features1 = FaceFeatures.getInstance().generateFeatures(x, y);
        Log.e("FEATRURE1", "" + Arrays.toString(features1));

        int[] features2 = FaceFeatures.getInstance().generateFeatures(x, y);
        Log.e("FEATRURE2", "" + Arrays.toString(features2));

        int[] features3 = FaceFeatures.getInstance().generateFeatures(x, y);
        Log.e("FEATRURE3", "" + Arrays.toString(features3));

        int[] features4 = FaceFeatures.getInstance().generateFeatures(x, y);


        float score = FaceFeatures.getInstance().matchFeatures(candidateFeatures, x, y);
        Log.e("SCORE", "MATCH SCORE===" + score);
        return new Result(score, "Successful", 0);
    }

    public static int[] getFaceFeatures(Box box) {

        float[] x = new float[10];
        float[] y = new float[10];

        x[Landmark.LEFT_LOWER_BOUNDING] = 0;
        y[Landmark.LEFT_LOWER_BOUNDING] = 0;

        x[Landmark.RIGHT_LOWER_BOUNDING] = 0;
        y[Landmark.RIGHT_LOWER_BOUNDING] = 0;

        x[Landmark.RIGHT_UPPER_BOUNDING] = 0;
        y[Landmark.RIGHT_UPPER_BOUNDING] = 0;

        x[Landmark.LEFT_UPPER_BOUNDING] = 0;
        y[Landmark.LEFT_UPPER_BOUNDING] = 0;


        x[Landmark.RIGHT_EYE] = box.landmark[0].x;
        y[Landmark.RIGHT_EYE] = box.landmark[0].y;

        x[Landmark.LEFT_EYE] = box.landmark[1].x;
        y[Landmark.LEFT_EYE] = box.landmark[1].y;

        x[Landmark.NOSE_TIP] = box.landmark[2].x;
        y[Landmark.NOSE_TIP] = box.landmark[2].y;

        x[Landmark.MOUTH_RIGHT] = box.landmark[3].x;
        y[Landmark.MOUTH_RIGHT] = box.landmark[3].y;

        x[Landmark.MOUTH_LEFT] = box.landmark[4].x;
        y[Landmark.MOUTH_LEFT] = box.landmark[4].y;


        Log.e("X====", "" + Arrays.toString(x));

        Log.e("Y====", "" + Arrays.toString(y));

        int[] features = FaceFeatures.getInstance().generateFeatures(x, y);
        Log.e("FEATRURE", "" + Arrays.toString(features));

        int[] features1 = FaceFeatures.getInstance().generateFeatures(x, y);
        Log.e("FEATRURE1", "" + Arrays.toString(features1));

        int[] features2 = FaceFeatures.getInstance().generateFeatures(x, y);
        Log.e("FEATRURE2", "" + Arrays.toString(features2));

        int[] features3 = FaceFeatures.getInstance().generateFeatures(x, y);
        Log.e("FEATRURE3", "" + Arrays.toString(features3));

        return FaceFeatures.getInstance().generateFeatures(x, y);


        //float score = FaceFeatures.getInstance().matchFeatures(candidateFeatures, x, y);
        //Log.e("SCORE", "MATCH SCORE===" + score);
    }

    public static Result getFeatureScore(Box box, int[] candidateFeatures){
        float[] x = new float[10];
        float[] y = new float[10];

        x[Landmark.LEFT_LOWER_BOUNDING] = 0;
        y[Landmark.LEFT_LOWER_BOUNDING] = 0;

        x[Landmark.RIGHT_LOWER_BOUNDING] = 0;
        y[Landmark.RIGHT_LOWER_BOUNDING] = 0;

        x[Landmark.RIGHT_UPPER_BOUNDING] = 0;
        y[Landmark.RIGHT_UPPER_BOUNDING] = 0;

        x[Landmark.LEFT_UPPER_BOUNDING] = 0;
        y[Landmark.LEFT_UPPER_BOUNDING] = 0;


        x[Landmark.RIGHT_EYE] = box.landmark[0].x;
        y[Landmark.RIGHT_EYE] = box.landmark[0].y;

        x[Landmark.LEFT_EYE] = box.landmark[1].x;
        y[Landmark.LEFT_EYE] = box.landmark[1].y;

        x[Landmark.NOSE_TIP] = box.landmark[2].x;
        y[Landmark.NOSE_TIP] = box.landmark[2].y;

        x[Landmark.MOUTH_RIGHT] = box.landmark[3].x;
        y[Landmark.MOUTH_RIGHT] = box.landmark[3].y;

        x[Landmark.MOUTH_LEFT] = box.landmark[4].x;
        y[Landmark.MOUTH_LEFT] = box.landmark[4].y;


        Log.e("X====", "" + Arrays.toString(x));

        Log.e("Y====", "" + Arrays.toString(y));

        int[] features = FaceFeatures.getInstance().generateFeatures(x, y);
        Log.e("FEATRURE", "" + Arrays.toString(features));

        float score = FaceFeatures.getInstance().matchFeatures(candidateFeatures, x, y);
        Log.e("SCORE", "MATCH SCORE===" + score);
        return new Result(score, "Successful", 0);
    }


    public static class Result{
        private float score;
        private String message;
        private int code;

        public Result(float score, String message, int code) {
            this.score = score;
            this.message = message;
            this.code = code;
        }

        public float getScore() {
            return score;
        }

        public void setScore(float score) {
            this.score = score;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }
    }

}
