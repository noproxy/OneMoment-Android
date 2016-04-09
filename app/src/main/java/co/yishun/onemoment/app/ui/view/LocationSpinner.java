package co.yishun.onemoment.app.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;

import java.util.Arrays;
import java.util.List;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.Util;

/**
 * Created by Carlos on 2015/8/11.
 */
public class LocationSpinner extends LinearLayout implements View.OnClickListener {
    public final static int[] provincesItemsRes = {R.array.beijinProvinceItem, R.array.tianjinProvinceItem, R.array.hebeiProvinceItem, R.array.shanxi1ProvinceItem, R.array.neimengguProvinceItem, R.array.liaoningProvinceItem, R.array.jilinProvinceItem, R.array.heilongjiangProvinceItem, R.array.shanghaiProvinceItem, R.array.jiangsuProvinceItem, R.array.zhejiangProvinceItem, R.array.anhuiProvinceItem, R.array.fujianProvinceItem, R.array.jiangxiProvinceItem, R.array.shandongProvinceItem, R.array.henanProvinceItem, R.array.hubeiProvinceItem, R.array.hunanProvinceItem, R.array.guangdongProvinceItem, R.array.guangxiProvinceItem, R.array.hainanProvinceItem, R.array.chongqingProvinceItem, R.array.sichuanProvinceItem, R.array.guizhouProvinceItem, R.array.yunnanProvinceItem, R.array.xizangProvinceItem, R.array.shanxi2ProvinceItem, R.array.gansuProvinceItem, R.array.qinghaiProvinceItem, R.array.ningxiaProvinceItem, R.array.xinjiangProvinceItem, R.array.hongkongProvinceItem, R.array.aomenProvinceItem, R.array.taiwanProvinceItem};
    private TextView mItemTextView;
    private ImageView mRightImageView;
    private
    @ColorInt
    int mTextColor = getResources().getColor(android.R.color.darker_gray);
    private Drawable mRightDrawable = getResources().getDrawable(R.drawable.ic_right);
    private float mTextSize = 16;
    private OnLocationSelectedListener mListener;
    private String[] provinces;
    private String mProvince = "";
    private String mDistrict = "";

    public LocationSpinner(Context context) {
        super(context);
        init(null, 0, 0);
    }

    public LocationSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0, 0);
    }


    public LocationSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LocationSpinner(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs, defStyleAttr, defStyleRes);
    }

    private void init(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        provinces = getResources().getStringArray(R.array.provinces);

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.LocationSpinner, defStyleAttr, defStyleRes);
        if (a.hasValue(R.styleable.LocationSpinner_ls_drawableRight)) {
            mRightDrawable = a.getDrawable(R.styleable.LocationSpinner_ls_drawableRight);
        }
        mTextColor = a.getColor(R.styleable.LocationSpinner_ls_textColor, mTextColor);
        mTextSize = a.getDimension(R.styleable.LocationSpinner_ls_textSize, mTextSize);

        this.setOrientation(HORIZONTAL);
        LayoutInflater.from(getContext()).inflate(R.layout.merge_spinner, this, true);
        mItemTextView = (TextView) findViewById(R.id.itemTextView);
        mRightImageView = (ImageView) findViewById(R.id.rightImageView);

        mItemTextView.setTextSize(mTextSize);
        mItemTextView.setTextColor(mTextColor);
        mItemTextView.setText(R.string.view_location_spinner_default);
        mRightImageView.setImageDrawable(mRightDrawable);

        a.recycle();
        this.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        MaterialDialog dialog = new MaterialDialog.Builder(getContext()).theme(Theme.LIGHT).title(R.string.view_location_spinner_title).positiveText(R.string.view_location_spinner_positive_btn).customView(R.layout.layout_dialog_area_pick, false).build();
        View dialogView = dialog.getCustomView();
        assert dialogView != null;
        Spinner provinceSpinner = (Spinner) dialogView.findViewById(R.id.provinceSpinner);
        Spinner districtSpinner = (Spinner) dialogView.findViewById(R.id.districtSpinner);

        districtSpinner.setEnabled(false);
        provinceSpinner.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, provinces));
        provinceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                districtSpinner.setEnabled(true);
                String dis[] = getResources().getStringArray(provincesItemsRes[position]);
                districtSpinner.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, dis));
                int select = Arrays.asList(dis).indexOf(mDistrict);
                districtSpinner.setSelection(select == -1 ? 0 : select);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                districtSpinner.setEnabled(false);
            }
        });
        int select = Arrays.asList(provinces).indexOf(mProvince);
        provinceSpinner.setSelection(select == -1 ? 0 : select);
        dialog.setOnDismissListener(dialog1 -> {
            String province = (String) provinceSpinner.getSelectedItem();
            String district = (String) districtSpinner.getSelectedItem();
            setProvinceAndDistrict(province, district);
            if (mListener != null) {
                mListener.onLocationSelected(getSelectedLocation(), getSelectedProvinceAndDistrict());
            }
        });
        dialog.show();
    }

    private void setProvinceAndDistrict(String pro, String dis) {
        mProvince = pro;
        mDistrict = dis;
        mItemTextView.setText(pro + " " + dis);
    }

    @Nullable
    public String getSelectedLocation() {
        if (TextUtils.isEmpty(mProvince) || TextUtils.isEmpty(mDistrict))
            return null;
        return mProvince + " " + mDistrict;
    }

    public void setSelectedLocation(@NonNull String location) {
        List<String> list = Util.split(' ', location);
        if (list.size() == 2) {
            setProvinceAndDistrict(list.get(0), list.get(1));
        }
    }

    @Nullable
    public Pair<String, String> getSelectedProvinceAndDistrict() {
        if (TextUtils.isEmpty(mProvince) || TextUtils.isEmpty(mDistrict))
            return null;
        return new Pair<>(mProvince, mDistrict);
    }

    public void setOnLocationSelectedListener(OnLocationSelectedListener listener) {
        mListener = listener;
    }

    public interface OnLocationSelectedListener {
        void onLocationSelected(String location, Pair<String, String> provinceAndDistrict);
    }
}
