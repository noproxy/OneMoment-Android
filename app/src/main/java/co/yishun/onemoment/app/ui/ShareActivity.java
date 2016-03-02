package co.yishun.onemoment.app.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.view.View;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.MaterialDialog;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

import java.io.IOException;
import java.net.URL;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.modelv4.ShareInfoProvider;
import co.yishun.onemoment.app.ui.common.WXRespActivity;
import co.yishun.onemoment.app.ui.share.ShareController;

/**
 * Created by Jinge on 2015/12/12.
 */
@EActivity(R.layout.activity_share)
public class ShareActivity extends WXRespActivity implements ShareController.ShareResultListener {
    public static final String TAG = "ShareActivity";
    public static final int RESULT_FAIL = RESULT_FIRST_USER;
    @Extra
    ShareInfoProvider shareInfo;
    @Extra
    int to = -1;
    MaterialDialog mProgressDialog;
    private ShareController shareController;

    public static void showShareChooseDialog(Context context, ShareInfoProvider shareInfo, int
            requestCode) {
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        dialog.setContentView(R.layout.layout_dialog_share);
        LinearLayout root = (LinearLayout) dialog.findViewById(R.id.container);

        View.OnClickListener clickListener = view -> {
            int type;
            switch (view.getId()) {
                case R.id.shareWeChat:
                    type = ShareController.TYPE_WE_CHAT;
                    break;
                case R.id.shareWXCircle:
                    type = ShareController.TYPE_WX_CIRCLE;
                    break;
                case R.id.shareWeibo:
                    type = ShareController.TYPE_WEIBO;
                    break;
                case R.id.shareQQ:
                    type = ShareController.TYPE_QQ;
                    break;
                case R.id.shareQzone:
                    type = ShareController.TYPE_QZONE;
                    break;
                default:
                    type = -1;
            }
            dialog.dismiss();
            if (type >= 0)
                ShareActivity_.intent(context).shareInfo(shareInfo).to(type).startForResult(requestCode);
        };

        int count = root.getChildCount();
        for (int i = 0; i < count; i++) {
            View v = root.getChildAt(i);
            v.setOnClickListener(clickListener);
        }

        dialog.show();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        shareController = new ShareController(this, shareInfo.getImageUrl(), shareInfo.getLink(), shareInfo.getTitle(), this);
        LogUtil.d(TAG, shareInfo.getImageUrl() + shareInfo.getTitle() + shareInfo.getLink());
        if (savedInstanceState != null)
            shareController.onNewIntent(getIntent());
        mProgressDialog = new MaterialDialog.Builder(this).progress(true, 0).content
                (R.string.activity_share_share_loading).cancelable(false).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    protected void onWXRespIntent(Intent intent) {
        LogUtil.i(TAG, "onWxresp intent");
        shareController.onNewIntent(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        LogUtil.d(TAG, "onNewIntent");
        shareController.onNewIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mReceiver == null) LogUtil.d(TAG, "receiver is null");
        LogUtil.d(TAG, "onResume");
    }

    @Background
    @AfterViews
    void getBitmap() {
        try {
            shareController.setType(to);
            Bitmap bitmap = BitmapFactory.decodeStream(new URL(shareInfo.getImageUrl()).openStream());
            shareController.setBitmap(bitmap);
            shareController.share();
        } catch (IOException e) {
            e.printStackTrace();
            onFail();
        }
    }

    @Override
    public void setPageInfo() {
        mIsPage = true;
        mPageName = "ShareActivity";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        shareController.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSuccess() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onFail() {
        setResult(RESULT_FAIL);
        finish();
    }

    @Override
    public void onCancel() {
        setResult(RESULT_CANCELED);
        finish();
    }
}
