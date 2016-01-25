package co.yishun.onemoment.app.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import java.io.IOException;
import java.net.URL;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.model.ShareInfo;
import co.yishun.onemoment.app.ui.common.WXRespActivity;
import co.yishun.onemoment.app.ui.share.ShareController;

/**
 * Created by Jinge on 2015/12/12.
 */
@EActivity(R.layout.activity_share)
public class ShareActivity extends WXRespActivity implements ShareController.ShareResultListener {
    public static final String TAG = "ShareActivity";
    public static final int TYPE_SHARE_MOMENT = 0;
    public static final int TYPE_SHARE_WORLD = 1;
    public static final int TYPE_SHARE_BADGE = 2;

    @Extra ShareInfo shareInfo;
    @Extra int shareType;

    @ViewById TextView shareText;

    private ShareController shareController;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        shareController = new ShareController(this, shareInfo.imageUrl, shareInfo.link, shareInfo.title, this);
        LogUtil.d(TAG, shareInfo.imageUrl + shareInfo.title + shareInfo.link);
        if (savedInstanceState != null)
            shareController.onNewIntent(getIntent());
    }

    @Override protected void onWXRespIntent(Intent intent) {
        LogUtil.i(TAG, "onWxresp intent");
        shareController.onNewIntent(intent);
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        LogUtil.d(TAG, "onNewIntent");
        shareController.onNewIntent(intent);
    }

    @Override protected void onResume() {
        super.onResume();
        if (mReceiver == null) LogUtil.d(TAG, "receiver is null");
        LogUtil.d(TAG, "onResume");
    }

    @AfterViews void setUp() {
        if (shareType == TYPE_SHARE_MOMENT)
            shareText.setText(getString(R.string.activity_share_share_moment));
        else if (shareType == TYPE_SHARE_WORLD)
            shareText.setText(getString(R.string.activity_share_share_world));
        else if (shareType == TYPE_SHARE_BADGE)
            shareText.setText(getString(R.string.activity_share_share_badge));
    }

    @Click(R.id.relativeLayout) void linearLayoutClick() {
        finish();
    }

    @Click(R.id.shareWeChat) void shareWeChatClick() {
        shareController.setType(ShareController.TYPE_WE_CHAT);
        getBitmap();
    }

    @Click(R.id.shareWXCircle) void shareWXCircleClick() {
        shareController.setType(ShareController.TYPE_WX_CIRCLE);
        getBitmap();
    }

    @Click(R.id.shareWeibo) void shareWeiboClick() {
        shareController.setType(ShareController.TYPE_WEIBO);
        getBitmap();
    }

    @Click(R.id.shareQQ) void shareQQClick() {
        shareController.setType(ShareController.TYPE_QQ);
        shareController.share();
    }

    @Click(R.id.shareQzone) void shareQzoneClick() {
        shareController.setType(ShareController.TYPE_QZONE);
        shareController.share();
    }

    @Background void getBitmap() {
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(new URL(shareInfo.imageUrl).openStream());
            shareController.setBitmap(bitmap);
            shareController.share();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override public void setPageInfo() {
        mIsPage = true;
        mPageName = "ShareActivity";
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        shareController.onActivityResult(requestCode, resultCode, data);
    }

    @Override public void onSuccess() {
        showSnackMsg(R.string.activity_share_share_success);
    }

    @Override public void onFail() {
        showSnackMsg(R.string.activity_share_share_fail);
    }

    @Override public void onCancel() {
        showSnackMsg(R.string.activity_share_share_cancel);
    }
}
