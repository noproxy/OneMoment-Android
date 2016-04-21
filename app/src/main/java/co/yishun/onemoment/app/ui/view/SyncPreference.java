package co.yishun.onemoment.app.ui.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.account.SyncManager;

/**
 * Created by zuliangwang on 16/4/13.
 */
public class SyncPreference extends com.jenzz.materialpreference.Preference {
    SyncBroadcastReceiver mBroadcastReceiver;
    private View mView;
    private int mViewWidth;
    private int mViewHeight;
    private Paint mPaint;
    private Canvas mCanvas;
    private Bitmap mBitmap;
    private BitmapDrawable mBitmapDrawable;


    public SyncPreference(Context context) {
        super(context);
        LogUtil.d("SyncPreference", "Sync1");
    }

    public SyncPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        LogUtil.d("SyncPreference", "Sync2");
    }


    public SyncPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LogUtil.d("SyncPreference", "Sync3");
    }

    public SyncPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        LogUtil.d("SyncPreference", "Sync4");
    }

    public void syncBackground(int allSyncTaskNum, int finishedSyncTaskNum) {
        int oneTaskWidth = mViewWidth / allSyncTaskNum;
        int finishedTasksWidth = oneTaskWidth * finishedSyncTaskNum;
        LogUtil.d("SyncPre", "finishedTsk=" + finishedSyncTaskNum);

        Rect rect = new Rect(finishedTasksWidth, 0, finishedTasksWidth + oneTaskWidth, mViewHeight);
        mCanvas.drawRect(rect, mPaint);
        mBitmapDrawable.draw(mCanvas);

        mView.invalidate();
    }

    private void beginSyncBackground() {
        mViewWidth = mView.getWidth();
        mViewHeight = mView.getHeight();
        mPaint = new Paint();
        mBitmap = Bitmap.createBitmap(mViewWidth, mViewHeight, Bitmap.Config.RGB_565);
        mCanvas = new Canvas(mBitmap);
        mBitmapDrawable = new BitmapDrawable(null, mBitmap);

        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(0);
        mPaint.setColor(getContext().getResources().getColor(R.color.colorWhite));
        mCanvas.drawRect(0, 0, mViewWidth, mViewHeight, mPaint);

        mPaint.setColor(getContext().getResources().getColor(R.color.bgSelectedColor));


        mBitmapDrawable.draw(mCanvas);
//        mLayout.setBackgroundColor(getContext().getResources().getColor(R.color.bgSelectedColor));
        mView.setBackground(mBitmapDrawable);
    }

    private void endSyncBackground() {
        getContext().unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {

        LogUtil.d("SyncPreference", "onCreateView");
        registerBroadcastReceiver();
        return super.onCreateView(parent);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        mView = view;
        setSummary("sync finished");

    }

    private void registerBroadcastReceiver() {
        LogUtil.d("SyncPreference", "re");
        if (mBroadcastReceiver == null) {
            mBroadcastReceiver = new SyncBroadcastReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(SyncManager.SYNC_BROADCAST_ACTION_START);
            intentFilter.addAction(SyncManager.SYNC_BROADCAST_ACTION_LOCAL_UPDATE);
            intentFilter.addAction(SyncManager.SYNC_BROADCAST_ACTION_UPDATA_FAIL);
            intentFilter.addAction(SyncManager.SYNC_BROADCAST_ACTION_END);
            getContext().registerReceiver(mBroadcastReceiver, intentFilter);
        }

    }


    private class SyncBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtil.d("SyncPreference", "onReceive");
            String action = intent.getAction();
            switch (action) {
                case SyncManager.SYNC_BROADCAST_ACTION_START:
                    LogUtil.d("SyncPreference", "ACTION_START");
                    beginSyncBackground();
                    break;
                case SyncManager.SYNC_BROADCAST_ACTION_LOCAL_UPDATE:
                    LogUtil.d("SyncPreference", "ACTION_LOCAL");
                    Bundle bundle = intent.getExtras();
                    int allTask = bundle.getInt("allTask");
                    int finishedTask = bundle.getInt("finishedTask");
                    LogUtil.d("SyncPreference", "allTask=" + allTask + "  finishedTask=" + finishedTask);
                    syncBackground(allTask, finishedTask - 1);
                    break;
                case SyncManager.SYNC_BROADCAST_ACTION_UPDATA_FAIL:
                    LogUtil.d("SyncPreference", "ACTION_FAIL");
                    break;
                case SyncManager.SYNC_BROADCAST_ACTION_END:
                    LogUtil.d("SyncPreference", "ACTION_END");
                    endSyncBackground();
                    break;
            }
        }
    }
}
