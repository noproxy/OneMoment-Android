package co.yishun.onemoment.app.ui;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.malinskiy.superrecyclerview.SuperRecyclerView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.model.WorldTag;
import co.yishun.onemoment.app.ui.adapter.AbstractRecyclerViewAdapter;
import co.yishun.onemoment.app.ui.adapter.SearchAdapter;
import co.yishun.onemoment.app.ui.common.BaseActivity;
import co.yishun.onemoment.app.ui.controller.SearchController_;
import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;

/**
 * Created on 2015/10/19.
 */
@EActivity(R.layout.activity_search)
public class SearchActivity extends BaseActivity implements AbstractRecyclerViewAdapter.OnItemClickListener<WorldTag> {
    @ViewById
    Toolbar toolbar;
    @ViewById
    SuperRecyclerView recyclerView;
    @ViewById
    EditText queryText;
    @ViewById
    AppBarLayout appBar;

    SearchAdapter adapter;

    @UiThread
    @AfterViews
    void preTransition() {
        appBar.setVisibility(View.INVISIBLE);
        appBar.post(() -> {
            appBar.setVisibility(View.VISIBLE);
            queryText.requestFocus();
            showKeyboard();
            int cx = appBar.getRight() - 72;
            int cy = appBar.getHeight() / 2;
            int finalRadius = (int) Math.hypot(appBar.getWidth(), appBar.getHeight());

            SupportAnimator animator = ViewAnimationUtils.createCircularReveal(appBar, cx, cy, 32, finalRadius);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.setDuration(300);
            animator.start();
        });

        ObjectAnimator animator = ObjectAnimator.ofInt(recyclerView, "backgroundColor",
                0x00ffffff, getResources().getColor(R.color.colorWindowTranslucent)).setDuration(500);
        animator.setEvaluator(new ArgbEvaluator());
        animator.start();
    }


    @AfterViews
    void setViews() {
        setupToolbar(this, toolbar);

        setupQueryText();
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);

        adapter = new SearchAdapter(this, this);
        recyclerView.setAdapter(adapter);
    }

    void setupQueryText() {
        queryText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                search();
                hideKeyboard();
                return true;
            }
            return false;
        });
        queryText.addTextChangedListener(new TextWatcher() {
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
        });
    }

    @CallSuper
    protected ActionBar setupToolbar(AppCompatActivity activity, Toolbar toolbar) {
        if (toolbar == null)
            throw new UnsupportedOperationException("You need bind Toolbar instance to" +
                    " toolbar in onCreateView(LayoutInflater, ViewGroup, Bundle");
        activity.setSupportActionBar(toolbar);

        final ActionBar ab = activity.getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowTitleEnabled(false);
        Log.i("setupToolbar", "set home as up true");
        return ab;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.fragment_world_action_search) {
            search();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void search() {
        if (TextUtils.isEmpty(queryText.getText())) {
            return;
        }
        SearchController_.getInstance_(this).setUp(adapter, recyclerView, queryText.getText().toString());
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
    public void setPageInfo() {
        mPageName = "SearchActivity";
    }

    @Override
    public void onClick(View view, WorldTag item) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        TagActivity_.intent(this).tag(item).top(location[1]).from(TagActivity.FROM_SEARCH_ACTIVITY).start();
    }
}
