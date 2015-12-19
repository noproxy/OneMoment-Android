package co.yishun.onemoment.app.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.loader.VideoTask;
import co.yishun.onemoment.app.api.model.TagVideo;
import co.yishun.onemoment.app.api.model.Video;

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
        Picasso.with(mContext).load(R.drawable.pic_slider_loading).fit().into(holder.itemImageView);
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
            videoTask = new VideoTask(mContext, video, VideoTask.TYPE_VIDEO_IMAGE)
                    .setImageListener(this::setImage)
                    .setVideoListener(this::setVideo)
                    .start();
        }

        void setImage(File large) {
            Log.d("Video", "get " + large.getName());
            Picasso.with(mContext).load(large).into(itemImageView);
        }

        void setVideo(Video video) {
            Log.d("Video", "get a video " + video.fileName);
        }

    }

}
