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

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.account.SyncManager;

/**
 * Created by zuliangwang on 16/4/13.
 */
public class SyncPreference extends Preference {
    private ViewGroup mLayout;
    private int mlayoutWidth;
    private int mlayoutHeight;
    private Paint mPaint;



    public SyncPreference(Context context) {
        super(context);
        registeBroadcastReceiver();
    }

    public SyncPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        registeBroadcastReceiver();
    }


    public SyncPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        registeBroadcastReceiver();
    }

    public SyncPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        registeBroadcastReceiver();
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

    @Override
    protected View onCreateView(ViewGroup parent) {
        mLayout = parent;
        return super.onCreateView(parent);
    }

    private void registeBroadcastReceiver(){
        SyncBroadcastReceiver broadcastReceiver = new SyncBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SyncManager.SYNC_BROADCAST_ACTION_START);
        intentFilter.addAction(SyncManager.SYNC_BROADCAST_ACTION_LOCAL_UPDATE);
        intentFilter.addAction(SyncManager.SYNC_BROADCAST_ACTION_UPDATA_FAIL);
        intentFilter.addAction(SyncManager.SYNC_BROADCAST_ACTION_END);
        getContext().registerReceiver(broadcastReceiver,intentFilter);
    }


    private class SyncBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action){
                case SyncManager.SYNC_BROADCAST_ACTION_START:
                    beginSyncBackground();
                    break;
                case SyncManager.SYNC_BROADCAST_ACTION_LOCAL_UPDATE:
                    Bundle bundle = intent.getExtras();
                    int allTask = bundle.getInt("allTask");
                    int finishedTask = bundle.getInt("finishedTask");
                    syncBackground(allTask,finishedTask);
                    break;
                case SyncManager.SYNC_BROADCAST_ACTION_UPDATA_FAIL:
                    break;
                case SyncManager.SYNC_BROADCAST_ACTION_END:
                    break;
            }
        }
    }
}
