package co.yishun.onemoment.app.ui.common;

import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;

import co.yishun.onemoment.app.R;

/**
 * Created by Carlos on 2015/8/12.
 */
@EActivity
public abstract class BaseActivity extends AppCompatActivity {
    private MaterialDialog mProgressDialog;

    @Nullable
    public abstract View getSnackbarAnchorWithView(@Nullable View view);

    @UiThread
    public void showSnackMsg(String msg) {
        Snackbar.make(getSnackbarAnchorWithView(null), msg, Snackbar.LENGTH_SHORT).show();
    }

    public void showSnackMsg(@StringRes int msgRes) {
        showSnackMsg(getString(msgRes));
    }

    public void showProgress() {
        showProgress(R.string.progress_loading_msg);
    }

    @UiThread
    public void showProgress(String msg) {
        if (mProgressDialog == null)
            mProgressDialog = new MaterialDialog.Builder(this).theme(Theme.LIGHT).cancelable(false).content(msg).progress(true, 0).build();
        mProgressDialog.setContent(msg);
        mProgressDialog.show();
    }

    public void showProgress(@StringRes int msgRes) {
        showProgress(getString(msgRes));
    }

    @UiThread
    public void hideProgress() {
        if (mProgressDialog != null) {
            mProgressDialog.hide();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    @UiThread(delay = 300)
    public void exit() {
        finish();
    }

}
