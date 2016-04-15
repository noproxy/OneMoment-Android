package co.yishun.onemoment.app.ui.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.jenzz.materialpreference.Preference;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.account.SyncManager;

/**
 * Created by zuliangwang on 16/4/13.
 */
public class SyncPreference extends com.jenzz.materialpreference.Preference {
    private ViewGroup mLayout;
    private int mlayoutWidth;
    private int mlayoutHeight;
    private Paint mPaint;
    SyncBroadcastReceiver mBroadcastReceiver;



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

    public void syncBackground(int allSyncTaskNum,int finishedSyncTaskNum){
        int oneTaskWidth = mlayoutWidth/allSyncTaskNum;
        int finishedTasksWidth = oneTaskWidth*finishedSyncTaskNum;
        Drawable background = mLayout.getBackground();
        Canvas canvas = new Canvas();
        Rect rect = new Rect(finishedTasksWidth,0,finishedTasksWidth+oneTaskWidth,mlayoutHeight);
        canvas.drawRect(rect, mPaint);
        background.draw(canvas);
        mLayout.invalidate();
    }

    private void beginSyncBackground(){
        mlayoutWidth = mLayout.getWidth();
        mlayoutHeight = mLayout.getHeight();
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(0);
        mPaint.setColor(getContext().getResources().getColor(R.color.bgSelectedColor));
    }

    private void endSyncBackground(){

    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        LogUtil.d("SyncPreference","onCreateView");
        registerBroadcastReceiver();
        mLayout = parent;
        return super.onCreateView(parent);
    }

    private void registerBroadcastReceiver(){
        LogUtil.d("SyncPreference","re");
        if (mBroadcastReceiver == null){
            mBroadcastReceiver = new SyncBroadcastReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(SyncManager.SYNC_BROADCAST_ACTION_START);
            intentFilter.addAction(SyncManager.SYNC_BROADCAST_ACTION_LOCAL_UPDATE);
            intentFilter.addAction(SyncManager.SYNC_BROADCAST_ACTION_UPDATA_FAIL);
            intentFilter.addAction(SyncManager.SYNC_BROADCAST_ACTION_END);
            getContext().registerReceiver(mBroadcastReceiver,intentFilter);
        }

    }




    private class SyncBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action){
                case SyncManager.SYNC_BROADCAST_ACTION_START:
                    LogUtil.d("SyncPreference","ACTION_START");
                    beginSyncBackground();
                    break;
                case SyncManager.SYNC_BROADCAST_ACTION_LOCAL_UPDATE:
                    LogUtil.d("SyncPreference","ACTION_LOCAL");
                    Bundle bundle = intent.getExtras();
                    int allTask = bundle.getInt("allTask");
                    int finishedTask = bundle.getInt("finishedTask");
                    syncBackground(allTask,finishedTask);
                    break;
                case SyncManager.SYNC_BROADCAST_ACTION_UPDATA_FAIL:
                    LogUtil.d("SyncPreference","ACTION_FAIL");
                    break;
                case SyncManager.SYNC_BROADCAST_ACTION_END:
                    LogUtil.d("SyncPreference","ACTION_END");
                    endSyncBackground();
                    break;
            }
        }
    }
}
