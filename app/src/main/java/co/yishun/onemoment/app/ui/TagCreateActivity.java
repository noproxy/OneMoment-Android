package co.yishun.onemoment.app.ui;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
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

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.solovyev.android.views.llm.LinearLayoutManager;

import java.io.IOException;
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
public class TagCreateActivity extends BaseActivity implements AbstractRecyclerViewAdapter.OnItemClickListener<String>, LocationListener, TextView.OnEditorActionListener, TextWatcher {
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
    private LocationManager locationManager;
    private boolean searching = false;

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
        defaultTag.add(AccountHelper.getUserInfo(this).location);
        defaultTag.add(new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(new Date()));
        if (forWorld) {
            defaultTag.add(worldTag.name);
        }
        adapter.addFixedItems(defaultTag);
        getLocation();
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
        checkPermission();
        locationManager.removeUpdates(this);
    }

    @AfterViews
    void setEditTagContainer() {
        editTagContainer.setOnAddTagListener((x, y) -> {
            //TODO startActivity to add TAG
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

    @Background
    void getLocation() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        String provider = LocationManager.NETWORK_PROVIDER;

        checkPermission();
        Location lastKnownLocation = locationManager.getLastKnownLocation(provider);
        if (lastKnownLocation != null) {
            formatLocation(lastKnownLocation);
        }

        locationManager.requestSingleUpdate(provider, this, Looper.getMainLooper());
    }

    void formatLocation(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        String result = null;
        try {
            List<Address> addressList = geocoder.getFromLocation(
                    latitude, longitude, 1);
            if (addressList != null && addressList.size() > 0) {
                Address address = addressList.get(0);
                String province = address.getAdminArea();
                String city = address.getLocality();
                if (province.endsWith("省")) {
                    province = province.substring(0, province.lastIndexOf("省"));
                }
                if (city.endsWith("市")) {
                    city = city.substring(0, city.lastIndexOf("市"));
                }
                result = province + " " + city;
                Log.d(TAG, "get location " + result);
                addItem(0, result);
            }
        } catch (IOException e) {
            Log.e(TAG, "Unable connect to Geocoder", e);
        }
    }

    @UiThread
    void addItem(int position, String item) {
        if (position < 0) {
            adapter.add(item);
        } else {
            adapter.replaceItem(position, item);
        }
    }

    void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "permission error on M");
            }
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

    @Override
    public void onLocationChanged(Location location) {
        formatLocation(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

}
