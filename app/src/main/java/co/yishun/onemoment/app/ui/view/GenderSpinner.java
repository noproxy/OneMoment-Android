package co.yishun.onemoment.app.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.Account;

/**
 * Created by Carlos on 2015/8/11.
 */
public class GenderSpinner extends LinearLayout implements View.OnClickListener {
    private static final int[] GENDER_TEXT = {R.string.view_gender_spinner_female, R.string.view_gender_spinner_male, R.string.view_gender_spinner_other};
    private TextView mItemTextView;
    private ImageView mRightImageView;
    private @ColorInt int mTextColor = getResources().getColor(android.R.color.darker_gray);
    private Drawable mRightDrawable = getResources().getDrawable(R.drawable.ic_right);
    private float mTextSize = 16;
    private Account.Gender mSelectGender = Account.Gender.OTHER;
    private OnGenderSelectedListener mListener;

    public GenderSpinner(Context context) {
        super(context);
        init(null, 0, 0);
    }

    public GenderSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0, 0);
    }

    public GenderSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public GenderSpinner(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs, defStyleAttr, defStyleRes);
    }

    private void init(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.GenderSpinner, defStyleAttr, defStyleRes);
        if (a.hasValue(R.styleable.GenderSpinner_gs_drawableRight)) {
            mRightDrawable = a.getDrawable(R.styleable.GenderSpinner_gs_drawableRight);
        }
        mTextColor = a.getColor(R.styleable.GenderSpinner_gs_textColor, mTextColor);
        mTextSize = a.getDimension(R.styleable.GenderSpinner_gs_textSize, mTextSize);

        this.setOrientation(HORIZONTAL);
        LayoutInflater.from(getContext()).inflate(R.layout.merge_spinner, this, true);
        mItemTextView = (TextView) findViewById(R.id.itemTextView);
        mRightImageView = (ImageView) findViewById(R.id.rightImageView);

        mItemTextView.setTextSize(mTextSize);
        mItemTextView.setTextColor(mTextColor);
        mItemTextView.setText(R.string.view_gender_spinner_default);
        mRightImageView.setImageDrawable(mRightDrawable);

        a.recycle();
        this.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        new MaterialDialog.Builder(getContext())
                .theme(Theme.LIGHT)
                .title(R.string.view_gender_spinner_title)
                .items(R.array.view_gender_spinner_items)
                .itemsCallbackSingleChoice(mSelectGender.toInt() % 2, (dialog, view1, which, text) -> {
                    Account.Gender gender = Account.Gender.format(which);
                    updateGender(gender);
                    if (mListener != null) {
                        mListener.onGenderSelected(gender);
                    }
                    return true; // allow selection
                })
                .positiveText(R.string.view_gender_spinner_positive_btn)
                .show();
    }

    private void updateGender(Account.Gender gender) {
        mSelectGender = gender;
        mItemTextView.setText(GENDER_TEXT[gender.toInt()]);
    }

    public void setSelectedGender(Account.Gender gender) {
        updateGender(gender);
    }

    public Account.Gender getSelectGender() {
        return mSelectGender;
    }

    public void setOnGenderSelectedListener(OnGenderSelectedListener listener) {
        mListener = listener;
    }

    public interface OnGenderSelectedListener {
        void onGenderSelected(Account.Gender gender);
    }

}
