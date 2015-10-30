package co.yishun.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created on 2015/10/20.
 */
public class RoundRectImageView extends ImageView {
    private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.ARGB_8888;
    private static final int COLORDRAWABLE_DIMENSION = 2;

    private final RectF mDrawableRect = new RectF();

    private Matrix mShaderMatrix;
    private Paint mBitmapPaint;

    private Bitmap mBitmap;
    private BitmapShader mBitmapShader;

    private float mRoundRadius;
    private float mRoundRadiusX;
    private float mRoundRadiusY;
    private float mRoundRate;
    private boolean mForceSquare;

    private ColorFilter mColorFilter;

    public RoundRectImageView(Context context) {
        super(context);
        super.setScaleType(ScaleType.CENTER_CROP);

        mRoundRadius = 0;
        mRoundRate = 0;
        mForceSquare = false;

        init();
    }

    public RoundRectImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundRectImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        super.setScaleType(ScaleType.CENTER_CROP);

        mRoundRadius = 0;
        mRoundRate = 0;
        mForceSquare = false;

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RoundRectImageView, defStyleAttr, 0);
        mRoundRadius = a.getDimension(R.styleable.RoundRectImageView_rrim_roundRadius, mRoundRadius);
        mRoundRadiusX = a.getDimension(R.styleable.RoundRectImageView_rrim_roundRadius_x, mRoundRadius);
        mRoundRadiusY = a.getDimension(R.styleable.RoundRectImageView_rrim_roundRadius_y, mRoundRadius);
        mRoundRate = a.getFloat(R.styleable.RoundRectImageView_rrim_roundRate, mRoundRate);
        mForceSquare = a.getBoolean(R.styleable.RoundRectImageView_rrim_forceSquare, mForceSquare);
        a.recycle();

        init();
    }

    private void init() {
        if (mBitmapPaint == null || mShaderMatrix == null) {
            mBitmapPaint = new Paint();
            mShaderMatrix = new Matrix();
            mBitmapPaint.setAntiAlias(true);
        }
        updateShaderMatrix();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (getDrawable() == null) {
            return;
        }
        mBitmap = getBitmapFromDrawable(getDrawable());
        updateShaderMatrix();

        mDrawableRect.set(0, 0, getWidth(), getHeight());
        canvas.drawRoundRect(mDrawableRect, getWidth() / 2.0f * mRoundRate,
                getHeight() / 2.0f * mRoundRate, mBitmapPaint);
    }

    private void updateShaderMatrix() {
        if (mBitmap == null) {
            return;
        }
        float scale;
        float dx = 0;
        float dy = 0;
        int bitmapHeight = mBitmap.getHeight();
        int bitmapWidth = mBitmap.getWidth();
        mBitmapShader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        mBitmapPaint.setShader(mBitmapShader);

        mShaderMatrix.set(null);

        if (bitmapWidth * getHeight() > getWidth() * bitmapHeight) {
            scale = getHeight() / (float) bitmapHeight;
            dx = (getWidth() - bitmapWidth * scale) * 0.5f;
        } else {
            scale = getWidth() / (float) bitmapWidth;
            dy = (getHeight() - bitmapHeight * scale) * 0.5f;
        }

        mShaderMatrix.setScale(scale, scale);
        mShaderMatrix.postTranslate((int) (dx + 0.5f), (int) (dy + 0.5f));

        mBitmapShader.setLocalMatrix(mShaderMatrix);
    }

    public float getRoundRadius() {
        return mRoundRadius;
    }

    public void setRoundRadius(float mRoundRadius) {
        this.mRoundRadius = mRoundRadius;
    }

    public float getRoundRate() {
        return mRoundRate;
    }

    public void setRoundRate(float mRoundRate) {
        this.mRoundRate = mRoundRate;
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        if (cf == mColorFilter) {
            return;
        }

        mColorFilter = cf;
        mBitmapPaint.setColorFilter(mColorFilter);
        invalidate();
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
                bitmap = Bitmap.createBitmap(COLORDRAWABLE_DIMENSION, COLORDRAWABLE_DIMENSION, BITMAP_CONFIG);
            } else {
                bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), BITMAP_CONFIG);
            }

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (OutOfMemoryError e) {
            return null;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mForceSquare) {
            final int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
            setMeasuredDimension(width, width);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (mForceSquare)
            super.onSizeChanged(w, w, oldw, oldh);
        else
            super.onSizeChanged(w, h, oldw, oldh);

    }
}
