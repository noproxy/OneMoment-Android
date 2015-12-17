package co.yishun.onemoment.app.ui.share;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

import java.io.IOException;
import java.net.URL;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.model.ShareInfo;
import co.yishun.onemoment.app.ui.common.BaseActivity;

/**
 * Created by Jinge on 2015/12/12.
 */
@EActivity(R.layout.activity_share)
public class ShareActivity extends BaseActivity implements ShareController.ShareResultListener {
    public static final String TAG = "ShareActivity";

    @Extra ShareInfo shareInfo;

    private ShareController shareController;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        shareController = new ShareController(this, shareInfo.imageUrl, shareInfo.link, shareInfo.title, this);
        if (savedInstanceState != null)
            shareController.onNewIntent(getIntent());
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent");
        shareController.onNewIntent(intent);
    }

    @Override protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @AfterViews void setUp() {

    }

    @Click(R.id.linearLayout) void linearLayoutClick() {
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
        showSnackMsg("Share Success");
    }

    @Override public void onFail() {
        showSnackMsg("Share Fail");
    }

    @Override public void onCancel() {
        showSnackMsg("Share Cancel");
    }
}
