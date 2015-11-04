package co.yishun.onemoment.app.ui;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.solovyev.android.views.llm.LinearLayoutManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import co.yishun.library.EditTagContainer;
import co.yishun.library.tag.BaseVideoTag;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.account.AccountHelper;
import co.yishun.onemoment.app.api.model.WorldTag;
import co.yishun.onemoment.app.ui.adapter.AbstractRecyclerViewAdapter;
import co.yishun.onemoment.app.ui.adapter.TagSearchAdapter;
import co.yishun.onemoment.app.ui.common.BaseActivity;
import co.yishun.onemoment.app.ui.controller.TagSearchController_;

/**
 * Created by Carlos on 2015/11/2.
 */
@EActivity(R.layout.activity_tag_create)
public class TagCreateActivity extends BaseActivity implements AbstractRecyclerViewAdapter.OnItemClickListener<String>, TextView.OnEditorActionListener, TextWatcher {
    public static final int REQUEST_CODE_SEARCH = 1;
    private static final String TAG = "TagCreateActivity";
    @ViewById
    Toolbar toolbar;
    @ViewById
    EditText queryText;
    @ViewById
    ImageView addView;
    @Extra
    WorldTag worldTag;
    @Extra
    boolean forWorld = false;
    @Extra
    String videoPath;
    @ViewById
    EditTagContainer editTagContainer;
    @ViewById
    ImageView momentPreviewImageView;
    @ViewById
    FrameLayout searchFrame;
    @ViewById
    RecyclerView recyclerView;
    @ViewById
    Button nextBtn;

    TagSearchAdapter adapter;
    private boolean searching = false;
    private LocationClient locationClient;

    private float tagX;
    private float tagY;

    @Nullable
    @Override
    public View getSnackbarAnchorWithView(@Nullable View view) {
        return null;
    }

    @AfterViews
    void setupViews() {
        queryText.setVisibility(View.GONE);
        queryText.setOnEditorActionListener(this);
        queryText.addTextChangedListener(this);
        addView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        searchFrame.setVisibility(View.GONE);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);

        adapter = new TagSearchAdapter(this, this);
        recyclerView.setAdapter(adapter);
    }

    @AfterViews
    void setupToolbar() {
        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(R.string.activity_moment_create_title_text);
        Log.i("setupToolbar", "set home as up true");
    }

    void setupSearch() {
        searching = true;
        queryText.setVisibility(View.VISIBLE);
        queryText.requestFocus();
        queryText.setText("");
        addView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
        nextBtn.setVisibility(View.GONE);
        searchFrame.setVisibility(View.VISIBLE);
        showKeyboard();

        List<String> defaultTag = new ArrayList<>();
        if (locationClient.getLastKnownLocation() == null) {
            defaultTag.add(AccountHelper.getUserInfo(this).location);
        } else {
            defaultTag.add(formatLocation(locationClient.getLastKnownLocation()));
        }

        defaultTag.add(new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(new Date()));
        if (forWorld && worldTag != null && !"".equals(worldTag.name)) {
            defaultTag.add(worldTag.name);
        }
        adapter.addFixedItems(defaultTag);
        locationClient.start();
    }

    void recoverSearch() {
        searching = false;
        queryText.setVisibility(View.GONE);
        queryText.requestFocus();
        addView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        nextBtn.setVisibility(View.VISIBLE);
        searchFrame.setVisibility(View.GONE);
        hideKeyboard();
        locationClient.stop();
    }

    @AfterViews
    void setEditTagContainer() {
        editTagContainer.setOnAddTagListener((x, y) -> {
            tagX = x;
            tagY = y;
            setupSearch();
        });
    }

    boolean addTag(String tag) {
        if ("".equals(tag)) {
            showSnackMsg("empty tag not permit");
            return false;
        }
        editTagContainer.addTag(new BaseVideoTag(tag, tagX, tagY));
        return true;
    }

    @Click
    void nextBtnClicked(View view) {

    }

    @Override
    public void onClick(View view, String item) {
        if (addTag(item)) {
            recoverSearch();
        }
    }

    @Click
    void addViewClicked(View view) {
        if (addTag(queryText.getText().toString())) {
            recoverSearch();
        }
    }

    @Override
    public void onBackPressed() {
        if (searching)
            recoverSearch();
        else
            super.onBackPressed();
    }

    void search() {
        if ("".equals(queryText.getText().toString())) {
            return;
        }
        TagSearchController_.getInstance_(this).setUp(adapter, recyclerView, queryText.getText().toString());
    }

    @AfterInject
    void setupLocation() {
        locationClient = new LocationClient(getApplicationContext());
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);
        option.setIsNeedLocationDescribe(false);
        option.SetIgnoreCacheException(false);
        option.setEnableSimulateGps(false);
        locationClient.setLocOption(option);
        locationClient.registerLocationListener(bdLocation -> {
            if ("".equals(formatLocation(bdLocation))){
                return;
            }
            addItem(0, formatLocation(bdLocation));
        });
    }

    String formatLocation(BDLocation location) {
        if (location == null || "".equals(location.getProvince()) || "".equals(location.getCity())) {
            return null;
        }
        String province = location.getProvince();
        String city = location.getCity();
        if (province.endsWith("省")) {
            province = province.substring(0, province.lastIndexOf("省"));
        }
        if (city.endsWith("市")) {
            city = city.substring(0, city.lastIndexOf("市"));
        }
        return province + " " + city;
    }

    @UiThread
    void addItem(int position, String item) {
        if (position < 0) {
            adapter.add(item);
        } else {
            adapter.replaceItem(position, item);
        }
    }

    void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(queryText, 0);
    }

    void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(queryText.getWindowToken(), 0);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        search();
        return true;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        search();
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

}
