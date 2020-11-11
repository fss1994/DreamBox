package com.didi.carmate.dreambox.core.render.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.View;

/**
 * author: chenjing
 * date: 2020/6/23
 * <p>
 * 支持圆形，圆角矩形及其他图形
 * 支持边框/背景/pressed
 */
public class DBRichView extends View {
    /**
     * 显示图片
     */
    private Bitmap mBitmap;

    private Drawable mDrawable;

    /**
     * Bitmap Paint
     */
    private Paint mBitmapPaint;

    /**
     * 边框 Paint
     */
    private Paint mBorderPaint;

    /**
     * 当前是否被按下
     */
    private boolean mIsPressed;

    private Shader mBitmapShader;

    /**
     * 变换矩阵
     */
    private Matrix mShaderMatrix;

    /**
     * 形状
     */
    private int mShape;

    /**
     * Rect Bitmap
     */
    private RectF mRcBitmap;

    /**
     * Rect Border
     */
    private RectF mRcBorder;

    /**
     * 圆角半径
     */
    private float mRoundRadius;

    /**
     * 边框半径
     */
    private float mBorderRadius;

    /**
     * 图片半径
     */
    private float mCircleRadius;

    /**
     * 边框color
     */
    private int mBorderColor;

    /**
     * 边框大小
     */
    private int mBorderWidth;

    /**
     * 按下态蒙层color
     */
    private int mCoverColor;

    /**
     * 默认形状
     */
    public static final int SHAPE_REC = 1; // 矩形
    public static final int SHAPE_CIRCLE = 2; // 圆形

    public DBRichView(Context context) {
        this(context, null);
    }

    public DBRichView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        init();
    }

    private void init() {
        mBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setColor(mBorderColor);
        mBorderPaint.setStrokeWidth(mBorderWidth);

        mRcBitmap = new RectF();
        mRcBorder = new RectF();
        mShaderMatrix = new Matrix();
    }

    public void setImageDrawable(Drawable drawable) {
        mDrawable = drawable;
        preDraw();
    }

    private Bitmap getBitmapFromDrawable(Drawable drawable) {
        if (drawable == null) {
            return null;
        }

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        try {
            Bitmap bitmap;

            if (drawable instanceof ColorDrawable) {
                bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
            } else if (drawable instanceof GradientDrawable) {
                bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            } else {
                bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            }

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (OutOfMemoryError e) {
            return null;
        }
    }

    /**
     * 初始化基本参数
     */
    private void preDraw() {
        if (mBitmap == null) {
            return;
        }

        mBitmapShader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        mBitmapPaint.setShader(mBitmapShader);

        float padding = (float) mBorderWidth / 2; // 留一个内边距，否则部分边框绘制在控件外面，导致看不全
        mRcBorder.set(padding, padding, getWidth() - padding, getHeight() - padding);
        mBorderRadius = Math.min((mRcBorder.height() - mBorderWidth) / 2, (mRcBorder.width() - mBorderWidth) / 2);

        if (mShape == SHAPE_CIRCLE) {
            mRcBitmap.set(mBorderWidth, mBorderWidth, mRcBorder.width() - mBorderWidth, mRcBorder.height() - mBorderWidth);
        } else if (mShape == SHAPE_REC) {
            mRcBitmap.set(padding, padding, getWidth() - padding, getHeight() - padding);
        }
        mCircleRadius = Math.min(mRcBitmap.height() / 2, mRcBitmap.width() / 2);
        updateShaderMatrix();
        invalidate();
    }

    /**
     * 伸缩变换
     */
    private void updateShaderMatrix() {
        float scale;
        float dx = 0;
        float dy = 0;

        mShaderMatrix.set(null);

        if (mBitmap.getWidth() * mRcBitmap.height() > mRcBitmap.width() * mBitmap.getHeight()) {
            scale = mRcBitmap.height() / (float) mBitmap.getHeight();
            dx = (mRcBitmap.width() - mBitmap.getWidth() * scale) * 0.5f;
        } else {
            scale = mRcBitmap.width() / (float) mBitmap.getWidth();
            dy = (mRcBitmap.height() - mBitmap.getHeight() * scale) * 0.5f;
        }

        mShaderMatrix.setScale(scale, scale);
        mShaderMatrix.postTranslate((int) (dx + 0.5f) + mBorderWidth, (int) (dy + 0.5f) + mBorderWidth);
        mBitmapShader.setLocalMatrix(mShaderMatrix);
    }

//    @Override
//    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
//        super.onLayout(changed, left, top, right, bottom);
//        preDraw();
//    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (null == mBitmap) {
            mBitmap = getBitmapFromDrawable(mDrawable);
        }
        preDraw();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBitmap != null) {
            if (mShape == SHAPE_CIRCLE) {
                canvas.drawCircle((float) getWidth() / 2, (float) getHeight() / 2, mCircleRadius, mBitmapPaint);
                canvas.drawCircle((float) getWidth() / 2, (float) getHeight() / 2, mBorderRadius, mBorderPaint);
            } else if (mShape == SHAPE_REC) {
                canvas.drawRoundRect(mRcBitmap, mRoundRadius, mRoundRadius, mBitmapPaint);
                canvas.drawRoundRect(mRcBorder, mRoundRadius, mRoundRadius, mBorderPaint);
            }
        }
    }

    @Override
    public void setPressed(boolean pressed) {
        super.setPressed(pressed);
        if (mIsPressed == pressed) {
            return;
        }
        mIsPressed = pressed;
        if (mIsPressed) {
            mBitmapPaint.setColorFilter(new PorterDuffColorFilter(mCoverColor, PorterDuff.Mode.SRC_ATOP));
        } else {
            mBitmapPaint.setColorFilter(null);
        }
        invalidate();
    }

    public void setBorderColor(int borderColor) {
        if (borderColor == mBorderColor) {
            return;
        }
        mBorderColor = borderColor;
        mBorderPaint.setColor(mBorderColor);
        invalidate();
    }

    public void setBorderWidth(int borderWidth) {
        if (borderWidth == mBorderWidth) {
            return;
        }
        mBorderWidth = borderWidth;
        mBorderPaint.setStrokeWidth(mBorderWidth);
        preDraw();
    }

    public void setCoverColor(int coverColor) {
        if (coverColor == mCoverColor) {
            return;
        }
        mCoverColor = coverColor;
    }

    public void setShape(int shape) {
        mShape = shape;
        preDraw();
    }

    public void setRoundRadius(float roundRadius) {
        mRoundRadius = roundRadius;
        preDraw();
    }
}