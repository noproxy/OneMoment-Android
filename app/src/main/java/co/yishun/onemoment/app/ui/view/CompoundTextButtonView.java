package co.yishun.onemoment.app.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import co.yishun.onemoment.app.R;

/**
 * Created by yyz on 7/31/15.
 */
public class CompoundTextButtonView extends LinearLayout implements View.OnClickListener {
    private CharSequence mText = "Button";
    private Drawable mDrawable;
    private TextView mTextView;
    private ImageView mImageView;
    private OnClickListener mListener;

    public CompoundTextButtonView(Context context) {
        super(context);
        init(null, 0, 0);
    }

    public CompoundTextButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0, 0);
    }

    public CompoundTextButtonView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP) public CompoundTextButtonView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs, defStyleAttr, defStyleRes);
    }

    private void init(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this.setOrientation(VERTICAL);
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CompoundTextButtonView, defStyleAttr, defStyleRes);
        if (a.hasValue(R.styleable.CompoundTextButtonView_android_text)) {
            mText = a.getText(R.styleable.CompoundTextButtonView_android_text);
        }
        if (a.hasValue(R.styleable.CompoundTextButtonView_android_src)) {
            mDrawable = a.getDrawable(R.styleable.CompoundTextButtonView_android_src);
        } else {
            if (Build.VERSION.SDK_INT > 21)
                mDrawable = getContext().getResources().getDrawable(R.drawable.ic_alarm, null);
            else
                mDrawable = getContext().getResources().getDrawable(R.drawable.ic_alarm);
        }
        a.recycle();
        LayoutInflater.from(getContext()).inflate(R.layout.merge_text_button, this, true);

        mTextView = (TextView) findViewById(R.id.buttonTextView);
        mImageView = (ImageView) findViewById(R.id.buttonImageView);

        mTextView.setText(mText);
        mImageView.setImageDrawable(mDrawable);
        mImageView.setOnClickListener(this);
    }

    @Override public void setOnClickListener(OnClickListener l) {
        if (!isClickable()) {
            setClickable(true);
        }
        mListener = l;
    }

    @Override public void onClick(View v) {
        Toast.makeText(getContext(), "clicked", Toast.LENGTH_SHORT).show();
        if (mListener != null) {
            mListener.onClick(v);
        }
    }
}
