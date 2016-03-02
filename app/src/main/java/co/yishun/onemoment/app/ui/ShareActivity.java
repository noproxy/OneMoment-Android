package co.yishun.onemoment.app.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.view.View;
import android.widget.LinearLayout;

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


    @Extra
    ShareInfoProvider shareInfo;

    private ShareController shareController;
    private BottomSheetDialog dialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        shareController = new ShareController(this, shareInfo.getImageUrl(), shareInfo.getLink(), shareInfo.getTitle(), this);
        LogUtil.d(TAG, shareInfo.getImageUrl() + shareInfo.getTitle() + shareInfo.getLink());
        if (savedInstanceState != null)
            shareController.onNewIntent(getIntent());
        show();
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

    private void show() {
        dialog = new BottomSheetDialog(this);
        dialog.setContentView(R.layout.layout_dialog_share);
        LinearLayout root = (LinearLayout) dialog.findViewById(R.id.container);

        View.OnClickListener clickListener = view -> onShare(dialog, view);

        int count = root.getChildCount();
        for (int i = 0; i < count; i++) {
            View v = root.getChildAt(i);
            v.setOnClickListener(clickListener);
        }

        dialog.setOnDismissListener(it -> this.finish());
        dialog.show();
    }

    void onShare(BottomSheetDialog dialog, View view) {
        switch (view.getId()) {
            case R.id.shareWeChat:
                shareController.setType(ShareController.TYPE_WE_CHAT);
                break;
            case R.id.shareWXCircle:
                shareController.setType(ShareController.TYPE_WX_CIRCLE);
                break;
            case R.id.shareWeibo:
                shareController.setType(ShareController.TYPE_WEIBO);
                break;
            case R.id.shareQQ:
                shareController.setType(ShareController.TYPE_QQ);
                break;
            case R.id.shareQzone:
                shareController.setType(ShareController.TYPE_QZONE);
                break;
        }
        dialog.hide();
        getBitmap();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dialog.setOnDismissListener(null);
        dialog.dismiss();
    }

    @Background
    void getBitmap() {
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(new URL(shareInfo.getImageUrl()).openStream());
            shareController.setBitmap(bitmap);
            shareController.share();
        } catch (IOException e) {
            e.printStackTrace();
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
        showSnackMsg(R.string.activity_share_share_success);
        finish();
    }

    @Override
    public void onFail() {
        showSnackMsg(R.string.activity_share_share_fail);
        finish();
    }

    @Override
    public void onCancel() {
        showSnackMsg(R.string.activity_share_share_cancel);
        finish();
    }
}
