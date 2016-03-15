package co.yishun.onemoment.app.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.loader.VideoTask;
import co.yishun.onemoment.app.api.model.TagVideo;
import co.yishun.onemoment.app.api.modelv4.VideoProvider;

/**
 * Created by Carlos on 2015/8/17.
 */
public class TagAdapter extends AbstractRecyclerViewAdapter<TagVideo, TagAdapter.SimpleViewHolder> {
    private static final String TAG = "TagAdapter";

    public TagAdapter(Context context, OnItemClickListener<TagVideo> listener) {
        super(context, listener);
    }

    @Override
    public void onBindViewHolder(TagAdapter.SimpleViewHolder holder, TagVideo item, int position) {
        Picasso.with(mContext).load(R.drawable.pic_world_default).fit().into(holder.itemImageView);
        holder.setUp(item);
    }

    public void addItems(List<? extends TagVideo> collection, int offset) {
        for (int i = 0; i < collection.size(); i++) {
            if (mItems.size() < offset) {
                mItems.add(collection.get(i));
            } else {
                mItems.set(i + offset - collection.size(), collection.get(i));
            }
        }
        notifyItemRangeChanged(offset - collection.size(), collection.size());
    }

    @Override
    public TagAdapter.SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SimpleViewHolder(LayoutInflater.from(mContext).inflate(R.layout.layout_item_video_like, parent, false));
    }

    public class SimpleViewHolder extends RecyclerView.ViewHolder {
        final ImageView itemImageView;
        //        private VideoDownloadTask task;
        private VideoTask videoTask;

        public SimpleViewHolder(View itemView) {
            super(itemView);
            itemImageView = (ImageView) itemView.findViewById(R.id.itemImageView);
        }

        protected void setUp(TagVideo video) {
            if (videoTask != null) {
                videoTask.cancel();
            }
            videoTask = new VideoTask(mContext, video, VideoTask.TYPE_VIDEO | VideoTask.TYPE_IMAGE)
                    .setImageListener(this::setImage)
                    .setVideoListener(this::setVideo)
                    .start();
        }

        void setImage(File large, File small) {
            LogUtil.d(TAG, "get " + small.getName());
            Picasso.with(mContext).load(small).error(R.drawable.pic_world_default).into(itemImageView);
        }

        void setVideo(VideoProvider video) {
            LogUtil.d(TAG, "get a video " + video.getFilename());
        }

    }

}
