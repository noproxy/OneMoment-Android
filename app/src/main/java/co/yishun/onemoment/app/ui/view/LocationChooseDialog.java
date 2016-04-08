package co.yishun.onemoment.app.ui.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;

import java.util.Arrays;

import co.yishun.onemoment.app.R;

/**
 * Created by Jinge on 2015/11/13.
 */
public class LocationChooseDialog extends MaterialDialog {
    public final static int[] provincesItemsRes = {
            R.array.beijinProvinceItem,
            R.array.tianjinProvinceItem,
            R.array.hebeiProvinceItem,
            R.array.shanxi1ProvinceItem,
            R.array.neimengguProvinceItem,
            R.array.liaoningProvinceItem,
            R.array.jilinProvinceItem,
            R.array.heilongjiangProvinceItem,
            R.array.shanghaiProvinceItem,
            R.array.jiangsuProvinceItem,
            R.array.zhejiangProvinceItem,
            R.array.anhuiProvinceItem,
            R.array.fujianProvinceItem,
            R.array.jiangxiProvinceItem,
            R.array.shandongProvinceItem,
            R.array.henanProvinceItem,
            R.array.hubeiProvinceItem,
            R.array.hunanProvinceItem,
            R.array.guangdongProvinceItem,
            R.array.guangxiProvinceItem,
            R.array.hainanProvinceItem,
            R.array.chongqingProvinceItem,
            R.array.sichuanProvinceItem,
            R.array.guizhouProvinceItem,
            R.array.yunnanProvinceItem,
            R.array.xizangProvinceItem,
            R.array.shanxi2ProvinceItem,
            R.array.gansuProvinceItem,
            R.array.qinghaiProvinceItem,
            R.array.ningxiaProvinceItem,
            R.array.xinjiangProvinceItem,
            R.array.hongkongProvinceItem,
            R.array.aomenProvinceItem,
            R.array.taiwanProvinceItem
    };
    private String[] provinces;
    private String mProvince = "";
    private String mDistrict = "";
    private OnLocationSelectedListener mListener;

    protected LocationChooseDialog(Builder builder) {
        super(builder);

        View dialogView = getCustomView();
        assert dialogView != null;
        Spinner provinceSpinner = (Spinner) dialogView.findViewById(R.id.provinceSpinner);
        Spinner districtSpinner = (Spinner) dialogView.findViewById(R.id.districtSpinner);
        provinces = builder.getContext().getResources().getStringArray(R.array.provinces);

        districtSpinner.setEnabled(false);
        provinceSpinner.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, provinces));
        provinceSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        districtSpinner.setEnabled(true);
                        String dis[] = builder.getContext().getResources().getStringArray(provincesItemsRes[position]);
                        districtSpinner.setAdapter(new ArrayAdapter<>(getContext(),
                                android.R.layout.simple_spinner_dropdown_item, dis
                        ));
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

        builder.callback(new ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) {
                super.onPositive(dialog);
                mProvince = (String) provinceSpinner.getSelectedItem();
                mDistrict = (String) districtSpinner.getSelectedItem();
                if (mListener != null) {
                    mListener.onLocationSelected(getSelectedLocation(), getSelectedProvinceAndDistrict());
                }
            }
        });
    }

    public LocationChooseDialog setLocationSelectedListener(OnLocationSelectedListener listener) {
        mListener = listener;
        return this;
    }

    @Nullable
    public String getSelectedLocation() {
        if (TextUtils.isEmpty(mProvince) || TextUtils.isEmpty(mDistrict))
            return null;
        return mProvince + " " + mDistrict;
    }

    @Nullable
    public Pair<String, String> getSelectedProvinceAndDistrict() {
        if (TextUtils.isEmpty(mProvince) || TextUtils.isEmpty(mDistrict))
            return null;
        return new Pair<>(mProvince, mDistrict);
    }

    public interface OnLocationSelectedListener {
        void onLocationSelected(String location, Pair<String, String> provinceAndDistrict);
    }

    public static class Builder extends MaterialDialog.Builder {

        public Builder(Context context) {
            super(context);
            this.theme(Theme.LIGHT)
                    .title(R.string.view_location_spinner_title)
                    .positiveText(R.string.view_location_spinner_positive_btn)
                    .customView(R.layout.layout_dialog_area_pick, false);
        }

        @Override
        public LocationChooseDialog build() {
            return new LocationChooseDialog(this);
        }
    }
}
