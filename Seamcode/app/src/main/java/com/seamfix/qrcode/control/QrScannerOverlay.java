package com.seamfix.qrcode.control;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;


public class QrScannerOverlay extends View {
    private Paint mTransparentPaint;
    private Path mPath = new Path();

    float scanYOffset = 200;
    float scanXOffset = 0;

    float width = 600;
    float height = 600;
    float left = 0;
    float top  = 0;
    RectF rectF = new RectF(left, top, width + left, height + top);


    public QrScannerOverlay(Context context) {
        super(context);
        initPaints();
    }

    public QrScannerOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaints();
    }

    public QrScannerOverlay(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaints();
    }

    private void initPaints() {
        mTransparentPaint = new Paint();
        mTransparentPaint.setColor(Color.TRANSPARENT);
        mTransparentPaint.setStrokeWidth(10);
        mTransparentPaint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        /*
        Get the center of the canvas
         */
        int x = getWidth() / 2;
        int y = getHeight() / 2;

        /*
        Get new left and top from the center using the mid of height and width
         */
        float mLeft = (x - (width / 2))  + scanXOffset;
        float mTop  = (y - (height / 2)) + scanYOffset;


        /*
        Apply new dimension to rect to draw the rectangle
         */
        rectF.top = mTop;
        rectF.left = mLeft;
        rectF.right = mLeft + width;
        rectF.bottom = mTop + height;

        mPath.reset();
        mPath.addRect(rectF, Path.Direction.CW);
        mPath.setFillType(Path.FillType.INVERSE_EVEN_ODD);


        canvas.drawPath(mPath, mTransparentPaint);
        canvas.clipPath(mPath);
        canvas.drawColor(Color.parseColor("#A6000000"));
    }
}