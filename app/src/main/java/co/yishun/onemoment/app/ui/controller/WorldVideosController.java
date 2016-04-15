package co.yishun.onemoment.app.ui.controller;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.widget.ImageView;

import com.malinskiy.superrecyclerview.OnMoreListener;
import com.malinskiy.superrecyclerview.SuperRecyclerView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;

import java.lang.ref.WeakReference;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.account.AccountManager;
import co.yishun.onemoment.app.api.APIV4;
import co.yishun.onemoment.app.api.authentication.OneMomentV4;
import co.yishun.onemoment.app.api.modelv4.ListErrorProvider;
import co.yishun.onemoment.app.api.modelv4.World;
import co.yishun.onemoment.app.api.modelv4.WorldProvider;
import co.yishun.onemoment.app.api.modelv4.WorldVideo;
import co.yishun.onemoment.app.api.modelv4.WorldVideoListWithErrorV4;
import co.yishun.onemoment.app.function.Consumer;
import co.yishun.onemoment.app.ui.adapter.AbstractRecyclerViewAdapter;
import co.yishun.onemoment.app.ui.adapter.WorldVideoAdapter;

/**
 * Created by Jinge on 2016/1/27.
 */
@EBean
public class WorldVideosController extends RecyclerController<Integer, SuperRecyclerView, WorldVideo, WorldVideoAdapter.SimpleViewHolder>
        implements OnMoreListener {
    private static final String TAG = "WorldVideoController";
    private static Consumer<World> mNewWorldListener = null;
    private WorldProvider mWorld;
    private APIV4 mApiV4 = OneMomentV4.createAdapter().create(APIV4.class);
    private boolean mForWorld;
    private WeakReference<ImageView> mWorldPreview;
    //    private int order;
    private boolean mThumbUrlInvalid;

    protected WorldVideosController(Context context) {
        super(context);
    }

    public void setup(AbstractRecyclerViewAdapter<WorldVideo, WorldVideoAdapter.SimpleViewHolder> adapter,
                      SuperRecyclerView recyclerView, WorldProvider world, boolean forWorld, ImageView worldPreview) {
        mForWorld = forWorld;
        mWorld = world;
        mWorldPreview = new WeakReference<>(worldPreview);
        super.setUp(adapter, recyclerView, 0);
        getRecyclerView().setOnMoreListener(this);
        getWorldThumb();
    }

    public void setOnNewWorldListener(Consumer<World> consumer) {
        mNewWorldListener = consumer;
    }

    @Override
    protected ListErrorProvider<WorldVideo> onLoad() {
        ListErrorProvider<WorldVideo> list;
        if (mForWorld) {
            list = mApiV4.getWorldVideos(mWorld.getId(), AccountManager.getUserInfo(mContext)._id, getOffset(), 6);
        } else {
            list = mApiV4.getTodayVideos(mWorld.getName(), getOffset(), 6);
        }
//        if (list.isSuccess() && list.size() > 0) {
//            order = list.get(list.size() - 1).order;
//        }
        World world = ((WorldVideoListWithErrorV4) list).world;
        if (mNewWorldListener != null) {
            getRecyclerView().post(() -> {
                        mNewWorldListener.accept(world);
                        if (world.getVideosNum() != mWorld.getVideosNum())
                            getAdapter().notifyDataSetChanged();
                    }
            );
        }
        if (mThumbUrlInvalid) {
            getWorldThumb();
        }

        setOffset(getOffset() + list.size());
        return list;
    }

    @UiThread
    void getWorldThumb() {
        if (TextUtils.isEmpty(mWorld.getThumb())) {
            mThumbUrlInvalid = true;
            Picasso.with(mContext).load(R.drawable.pic_banner_default).into(mWorldPreview.get());
        } else
            Picasso.with(mContext).load(mWorld.getThumb()).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    mThumbUrlInvalid = false;
                    mWorldPreview.get().setImageBitmap(bitmap);
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                    mThumbUrlInvalid = true;
                    Picasso.with(mContext).load(R.drawable.pic_banner_default).into(mWorldPreview.get());
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                    mThumbUrlInvalid = true;
                    Picasso.with(mContext).load(R.drawable.pic_banner_default).into(mWorldPreview.get());
                }
            });
    }

    @Override
    @UiThread
    void onLoadEnd(ListErrorProvider<WorldVideo> list) {
        if (list == null || !list.isSuccess()) {
            onLoadError();
            getRecyclerView().hideMoreProgress();
        } else if (list.size() > 0) {
            ((WorldVideoAdapter) getAdapter()).addItems(list, getOffset());
        } else {
            getRecyclerView().removeMoreListener();
            getRecyclerView().hideMoreProgress();
        }
        getRecyclerView().getSwipeToRefresh().setRefreshing(false);
    }

    @Override
    public void onMoreAsked(int overallItemsCount, int itemsBeforeMore,
                            int maxLastVisiblePosition) {
        if (overallItemsCount == itemsBeforeMore) {
            getRecyclerView().hideMoreProgress();
        }
        load();
    }
}
