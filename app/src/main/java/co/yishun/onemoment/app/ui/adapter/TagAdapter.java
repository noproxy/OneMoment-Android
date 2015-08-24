package co.yishun.onemoment.app.ui.adapter;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.model.TagVideo;
import co.yishun.onemoment.app.data.FileUtil;
import co.yishun.onemoment.app.data.VideoUtil;

/**
 * Created by Carlos on 2015/8/17.
 */
public class TagAdapter extends AbstractRecyclerViewAdapter<TagVideo, TagAdapter.SimpleViewHolder> {
    public TagAdapter(Context context, OnItemClickListener<TagVideo> listener) {
        super(context, listener);
    }

    @Override
    public void onBindViewHolder(TagAdapter.SimpleViewHolder holder, TagVideo item, int position) {
        Picasso.with(mContext).load(R.drawable.pic_slider_loading).fit().into(holder.itemImageView);
        holder.setUp(item);
    }


    @Override
    public TagAdapter.SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SimpleViewHolder(LayoutInflater.from(mContext).inflate(R.layout.layout_item_video_like, parent, false));
    }

    public static class SimpleViewHolder extends RecyclerView.ViewHolder {
        final ImageView itemImageView;
        private final Context context;
        private TagVideoDownloaderTask task;

        public SimpleViewHolder(View itemView) {
            super(itemView);
            itemImageView = (ImageView) itemView.findViewById(R.id.itemImageView);
            context = itemView.getContext();
        }

        protected void setUp(TagVideo video) {
            if (task != null) {
                task.cancel(true);
            }
            task = new TagVideoDownloaderTask();
            task.execute(video);

        }

        public class TagVideoDownloaderTask extends AsyncTask<TagVideo, Integer, Boolean> {
            private static final String TAG = "TagVideoDownloaderTask";
            private String largeThumbImage;
            private String thumbImage;


            @Override
            protected Boolean doInBackground(TagVideo... tagVideos) {
                final TagVideo tagVideo = tagVideos[0];
                try {

                    // if video exists
                    File fileSynced = FileUtil.getWorldVideoStoreFile(context, tagVideo);
                    if (fileSynced.exists()) {

                        // check whether thumbnail exists
                        File large = FileUtil.getThumbnailStoreFile(context, tagVideo, FileUtil.Type.LARGE_THUMB);
                        File small = FileUtil.getThumbnailStoreFile(context, tagVideo, FileUtil.Type.MICRO_THUMB);
                        boolean re = true;
                        try {
                            if (large.exists()) {
                                largeThumbImage = large.getPath();
                            } else {
                                largeThumbImage = VideoUtil.createLargeThumbImage(context, tagVideo, fileSynced.getPath());
                            }
                            if (small.exists()) {
                                thumbImage = small.getPath();
                            } else {
                                thumbImage = VideoUtil.createThumbImage(context, tagVideo, fileSynced.getPath());
                            }
                        } catch (IOException e) {
                            // create thumbnail failed, video file may be damaged, redownload
                            Log.e(TAG, "IOException when create thumbImage of the old video");
                            e.printStackTrace();
                            if (!fileSynced.delete()) {
                                return false;
                            }
                            re = false;
                        }
                        if (re) return true;
                    }

                    OkHttpClient httpClient = new OkHttpClient();
                    Call call = httpClient.newCall(new Request.Builder().url(tagVideo.domain + tagVideo.fileName).get().build());
                    Response response = call.execute();

                    if (response.code() == 200) {
                        InputStream input = null;
                        FileOutputStream output = null;
                        try {
                            input = response.body().byteStream();
                            output = new FileOutputStream(fileSynced);
                            long fileLength = response.body().contentLength();

                            byte data[] = new byte[4096];
                            long total = 0;
                            int count;
                            while ((count = input.read(data)) != -1) {
                                // allow canceling
                                if (isCancelled()) {
                                    input.close();
                                    return false;
                                }
                                total += count;
                                // publishing the progress....
                                if (fileLength > 0) // only if total length is known
                                    publishProgress((int) (total * 100 / fileLength));
                                output.write(data, 0, count);
                            }
                            largeThumbImage = VideoUtil.createLargeThumbImage(context, tagVideo, fileSynced.getPath());
                            thumbImage = VideoUtil.createThumbImage(context, tagVideo, fileSynced.getPath());

                            return total == fileLength;
                        } catch (IOException ignore) {
                            return false;
                        } finally {
                            try {
                                if (output != null)
                                    output.close();
                                if (input != null)
                                    input.close();
                            } catch (IOException ignored) {
                            }
                        }
                    } else {
                        return false;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void onProgressUpdate(Integer... values) {

            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (result) {
                    Picasso.with(context).load(new File(thumbImage)).into(itemImageView);
                }
            }
        }


    }

}
