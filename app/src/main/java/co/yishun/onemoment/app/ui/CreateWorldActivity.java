package co.yishun.onemoment.app.ui;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.io.File;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.config.Constants;
import co.yishun.onemoment.app.data.FileUtil;
import co.yishun.onemoment.app.ui.hybrd.BaseWebActivity;
import co.yishun.onemoment.app.ui.hybrd.BaseWebFragment;
import co.yishun.onemoment.app.ui.hybrd.BlockNextWebFragment_;
import co.yishun.onemoment.app.ui.view.OMWebView;

/**
 * Created by Jinge on 2016/1/22.
 */
@EActivity(R.layout.activity_create_world)
public class CreateWorldActivity extends BaseWebActivity implements OMWebView.OnBlockKeyListener {

    @ViewById
    Button finishButton;

    @AfterInject
    void setDefault() {
        title = getString(R.string.activity_create_world_title);
        File hybrdDir = FileUtil.getInternalFile(this, Constants.HYBRD_UNZIP_DIR);
        url = Constants.FILE_URL_PREFIX + new File(hybrdDir, "build/pages/create_world/create_world.html").getPath();
    }

    @AfterViews
    void setupViews() {
        setupToolbar();
        setupFragment();
    }

    @Click(R.id.finishButton)
    void finishClick(View view) {
        hideKeyboard();
        mWebFragment.sendFinish();
    }

    @Override
    protected void setupFragment() {
        mWebFragment = BlockNextWebFragment_.builder().mUrl(url).build();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerFrameLayout, mWebFragment, BaseWebFragment.TAG_WEB).commit();
        OMWebView.setListener(this);
    }

    void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(finishButton, 0);
    }

    void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(finishButton.getWindowToken(), 0);
    }

    @Override
    public void setPageInfo() {
        mPageName = "CreateWorldActivity";
    }

    @Override
    public void onBlockKey() {
        finishButton.performClick();
    }
}
