package co.yishun.onemoment.app.ui.share;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;

import java.io.IOException;
import java.net.URL;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.model.ShareInfo;
import co.yishun.onemoment.app.ui.common.BaseFragment;

/**
 * Created by Jinge on 2015/12/12.
 */
@EFragment(R.layout.fragment_share)
public class ShareFragment extends BaseFragment {

    @FragmentArg ShareInfo shareInfo;

    private ShareController shareController;

    @AfterViews void setUp() {
        shareController = new ShareController(getActivity(), shareInfo.imageUrl, shareInfo.link, shareInfo.title);
    }

    @Click(R.id.linearLayout) void linearLayoutClick() {
        getFragmentManager().beginTransaction().remove(this).commit();
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
        mPageName = "ShareFragment";
    }
}
