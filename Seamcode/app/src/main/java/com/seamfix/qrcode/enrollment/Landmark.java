package com.seamfix.qrcode.enrollment;


/**
 * 0. left-lower face bounding box
 * 1. left-upper face bounding box
 * 2. right-upper face bounding box
 * 3. right-lower face bounding box
 * 4. right-eye landmark point
 * 5. left-eye landmark point
 * 6. nose-tip landmark point
 * 7. mouth-center landmark point
 * 8. mouth-right bounding box
 * 9. mouth-left face bounding box
 */

public class Landmark {
    public static final int LEFT_LOWER_BOUNDING  = 0;
    public static final int LEFT_UPPER_BOUNDING  = 1;
    public static final int RIGHT_UPPER_BOUNDING = 2;
    public static final int RIGHT_LOWER_BOUNDING = 3;
    public static final int RIGHT_EYE            = 4;
    public static final int LEFT_EYE             = 5;
    public static final int NOSE_TIP             = 6;
    public static final int MOUTH_CENTER         = 7;
    public static final int MOUTH_RIGHT          = 8;
    public static final int MOUTH_LEFT           = 9;
}
