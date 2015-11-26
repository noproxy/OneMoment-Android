package co.yishun.onemoment.app.ui.view;

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

import co.yishun.onemoment.app.R;

/**
 * Created on 2015/10/20.
 */
public class RadioCornerImageView extends ImageView {
    private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.ARGB_8888;
    private static final int COLOR_DRAWABLE_DIMENSION = 2;

    private final RectF mDrawableRect = new RectF();
    int mWidthRadio;
    int mHeightRadio;
    float mCorner;
    float mCornerRadio;
    private Matrix mShaderMatrix;
    private Paint mBitmapPaint;
    private Bitmap mBitmap;
    private BitmapShader mBitmapShader;
    private ColorFilter mColorFilter;

    public RadioCornerImageView(Context context) {
        this(context, null);
    }

    public RadioCornerImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RadioCornerImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        super.setScaleType(ScaleType.CENTER_CROP);
        mWidthRadio = 1;
        mHeightRadio = 1;
        mCorner = 0;
        mCornerRadio = 0;

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RadioCornerImageView, defStyleAttr, 0);
            mWidthRadio = a.getInt(R.styleable.RadioCornerImageView_rc_widthRadio, mWidthRadio);
            mHeightRadio = a.getInt(R.styleable.RadioCornerImageView_rc_heightRadio, mHeightRadio);
            mCorner = a.getDimension(R.styleable.RadioCornerImageView_rc_corner, mCorner);
            mCornerRadio = a.getFloat(R.styleable.RadioCornerImageView_rc_cornerRadio, mCornerRadio);
            a.recycle();
        }
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
        canvas.drawRoundRect(mDrawableRect, mCorner, mCorner, mBitmapPaint);
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

    public float getCorner() {
        return mCorner;
    }

    public void setCorner(float corner) {
        this.mCorner = corner;
    }

    public float getCornerRadio() {
        return mCornerRadio;
    }

    public void setCornerRadio(float cornerRadio) {
        this.mCornerRadio = cornerRadio;
        mCorner = (getHeight() < getWidth() ? getHeight() : getWidth()) / 2 * mCornerRadio;
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
                bitmap = Bitmap.createBitmap(COLOR_DRAWABLE_DIMENSION, COLOR_DRAWABLE_DIMENSION, BITMAP_CONFIG);
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
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (heightMode == MeasureSpec.EXACTLY) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            height = (int) (width * mHeightRadio * 1.0f / mWidthRadio);
            super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        }
        if (mCorner == 0 && mCornerRadio != 0) {
            mCorner = (height < width ? height : width) / 2 * mCornerRadio;
        }
    }

}
