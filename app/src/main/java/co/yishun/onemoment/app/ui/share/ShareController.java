package co.yishun.onemoment.app.ui.share;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WebpageObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboHandler;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.constant.WBConstants;
import com.sina.weibo.sdk.utils.Utility;
import com.tencent.connect.share.QzoneShare;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import co.yishun.onemoment.app.config.Constants;

/**
 * Created by Jinge on 2015/12/12.
 */
public class ShareController implements IWeiboHandler.Response, IWXAPIEventHandler {
    public static final int TYPE_WE_CHAT = 0;
    public static final int TYPE_WX_CIRCLE = 1;
    public static final int TYPE_WEIBO = 2;
    public static final int TYPE_QQ = 3;
    public static final int TYPE_QZONE = 4;

    private static final String TAG = "ShareController";
    private static final int THUMB_SIZE = 150;
    private Activity activity;
    private Tencent mQQShareAPI;
    private IWXAPI mWeChatShareAPI;
    private IWeiboShareAPI mWeiboShareAPI;

    private String imageUrl;
    private String shareUrl;
    private String shareContent;
    private Bitmap bitmap;
    private IUiListener qqShareListener = new QQShareListener();
    private int type;
    private ShareResultListener shareListener;

    public ShareController(Activity activity, String imageUrl, String shareUrl, String shareContent, ShareResultListener shareListener) {
        this.activity = activity;
        this.imageUrl = imageUrl;
        this.shareUrl = shareUrl;
        this.shareContent = shareContent;
        this.shareListener = shareListener;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void share() {
        switch (type) {
            case TYPE_WE_CHAT:
                mWeChatShareAPI = WXAPIFactory.createWXAPI(activity, Constants.WE_CHAT_APP_ID);
                mWeChatShareAPI.registerApp(Constants.WE_CHAT_APP_ID);
                shareToWeChat();
                break;
            case TYPE_WX_CIRCLE:
                mWeChatShareAPI = WXAPIFactory.createWXAPI(activity, Constants.WE_CHAT_APP_ID);
                mWeChatShareAPI.registerApp(Constants.WE_CHAT_APP_ID);
                shareToWXCircle();
                break;
            case TYPE_WEIBO:
                mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(activity, Constants.WEIBO_APP_ID);
                mWeiboShareAPI.registerApp();
                shareToWeibo();
                break;
            case TYPE_QQ:
                mQQShareAPI = Tencent.createInstance(Constants.QQ_APP_ID, activity);
                shareToQQ();
                break;
            case TYPE_QZONE:
                mQQShareAPI = Tencent.createInstance(Constants.QQ_APP_ID, activity);
                shareToQzone();
                break;
        }
    }

    private void shareToQQ() {
        Bundle params = new Bundle();
        params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT);
        params.putString(QzoneShare.SHARE_TO_QQ_TITLE, shareContent);
        params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, shareContent);
        params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, shareUrl);
        params.putString(QzoneShare.SHARE_TO_QQ_IMAGE_URL, imageUrl);
        mQQShareAPI.shareToQQ(activity, params, qqShareListener);
    }

    private void shareToQzone() {
        Bundle params = new Bundle();
        params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT);
        params.putString(QzoneShare.SHARE_TO_QQ_TITLE, shareContent);
        params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, shareContent);
        params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, shareUrl);
        ArrayList<String> imagePaths = new ArrayList<>();
        imagePaths.add(imageUrl);
        params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, imagePaths);
        mQQShareAPI.shareToQzone(activity, params, qqShareListener);
    }

    private void shareToWeChat() {
        WXWebpageObject webObj = new WXWebpageObject();
        webObj.webpageUrl = shareUrl;
        WXMediaMessage msg = new WXMediaMessage(webObj);
        msg.title = shareContent;
        msg.description = shareContent;
        msg.thumbData = bmpToByteArray();

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = System.currentTimeMillis() + "";
        req.message = msg;
        req.scene = SendMessageToWX.Req.WXSceneSession;
        mWeChatShareAPI.sendReq(req);
    }

    private void shareToWXCircle() {
        WXWebpageObject webObj = new WXWebpageObject();
        webObj.webpageUrl = shareUrl;
        WXMediaMessage msg = new WXMediaMessage(webObj);
        msg.title = shareContent;
        msg.description = shareContent;
        msg.thumbData = bmpToByteArray();

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = System.currentTimeMillis() + "";
        req.message = msg;
        req.scene = SendMessageToWX.Req.WXSceneTimeline;
        mWeChatShareAPI.sendReq(req);
    }

    private void shareToWeibo() {
        TextObject textObject = new TextObject();
        textObject.text = shareContent;

        ImageObject imageObject = new ImageObject();
        imageObject.setImageObject(bitmap);

        WebpageObject mediaObject = new WebpageObject();
        mediaObject.identify = Utility.generateGUID();
        mediaObject.title = shareContent;
        mediaObject.description = shareContent;
        mediaObject.setThumbImage(bitmap);
        mediaObject.actionUrl = shareUrl;
        mediaObject.defaultText = shareContent;

        WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
        weiboMessage.textObject = textObject;
        weiboMessage.imageObject = imageObject;
        weiboMessage.mediaObject = mediaObject;
        SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.multiMessage = weiboMessage;
        mWeiboShareAPI.sendRequest(activity, request);
    }

    private byte[] bmpToByteArray() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent");
        if (mWeiboShareAPI != null)
            mWeiboShareAPI.handleWeiboResponse(intent, this);
        if (mWeChatShareAPI != null)
            mWeChatShareAPI.handleIntent(intent, this);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");
        if (mQQShareAPI != null)
            Tencent.onActivityResultData(requestCode, resultCode, data, qqShareListener);
    }

    @Override public void onResponse(BaseResponse baseResponse) {
        switch (baseResponse.errCode) {
            case WBConstants.ErrorCode.ERR_OK:
                shareListener.onSuccess();
                break;
            case WBConstants.ErrorCode.ERR_FAIL:
                shareListener.onFail();
                break;
            case WBConstants.ErrorCode.ERR_CANCEL:
                shareListener.onCancel();
                break;
        }
    }

    @Override public void onReq(BaseReq baseReq) {
        shareListener.onFail();
    }

    @Override public void onResp(BaseResp baseResp) {
        shareListener.onSuccess();
    }

    public interface ShareResultListener {
        void onSuccess();

        void onFail();

        void onCancel();
    }

    class QQShareListener implements IUiListener {

        @Override public void onComplete(Object o) {
            if (shareListener != null) {
                shareListener.onSuccess();
            }
        }

        @Override public void onError(UiError uiError) {
            if (shareListener != null) {
                shareListener.onFail();
            }
        }

        @Override public void onCancel() {
            if (shareListener != null) {
                shareListener.onCancel();
            }
        }
    }
}
