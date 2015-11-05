package co.yishun.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2015/10/29.
 */
public class AvatarRecyclerView extends RecyclerView {
    private AvatarAdapter adapter;
    private LinearLayoutManager layoutManager;
    private float normalSize;
    private float selectSize;
    private float dividerSize;
    private List<String> avatarUrls = new ArrayList<>();
    private int width;
    private int height;
    private int index;

    public AvatarRecyclerView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public AvatarRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public AvatarRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        return true;
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        normalSize = context.getResources().getDimension(R.dimen.arv_normalSize);
        selectSize = context.getResources().getDimension(R.dimen.arv_selectSize);
        dividerSize = context.getResources().getDimension(R.dimen.arv_dividerSize);
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AvatarRecyclerView, defStyle, 0);
            normalSize = a.getDimension(R.styleable.AvatarRecyclerView_arv_normalSize, normalSize);
            selectSize = a.getDimension(R.styleable.AvatarRecyclerView_arv_selectSize, selectSize);
            dividerSize = a.getDimension(R.styleable.AvatarRecyclerView_arv_dividerSize, dividerSize);
            a.recycle();
        }

        adapter = new AvatarAdapter();
        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        setLayoutManager(layoutManager);
        setAdapter(adapter);
        index = 0;

        addOnScrollListener(new AvatarOnScrollListener());
    }

    public void addAvatar(String url) {
        avatarUrls.add(url);
        adapter.notifyItemInserted(avatarUrls.size());
//        Log.d("[ARV]", "data set change");
    }

    public void scrollToZero() {
        View view = findViewWithTag(index);
        if (index == 0 && view != null) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) findViewWithTag(index).getLayoutParams();
            layoutParams.width = (int) selectSize;
            findViewWithTag(index).setLayoutParams(layoutParams);
            return;
        }
        if (view != null) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
            layoutParams.width = (int) normalSize;
            view.setLayoutParams(layoutParams);
            Log.d("view", "not null");
        }
        index = 0;
        smoothScrollToPosition(0);
    }

    public void scrollToNext() {
        if (index == avatarUrls.size() - 1) {
            Log.d("[ARV]", index + " " + avatarUrls.size());
            scrollToZero();
            return;
        }
        smoothScrollBy((int) (normalSize + dividerSize), 0);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) findViewWithTag(index).getLayoutParams();
        layoutParams.width = (int) normalSize;
        findViewWithTag(index).setLayoutParams(layoutParams);
        index++;
        layoutParams = (RelativeLayout.LayoutParams) findViewWithTag(index).getLayoutParams();
        layoutParams.width = (int) selectSize;
        findViewWithTag(index).setLayoutParams(layoutParams);
    }

    private void scrollOver() {
        if (index == 0) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) findViewWithTag(index).getLayoutParams();
            layoutParams.width = (int) selectSize;
            findViewWithTag(index).setLayoutParams(layoutParams);
        }
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        int widthMode = MeasureSpec.getMode(widthSpec);
        int widthSize = MeasureSpec.getSize(widthSpec);
        int heightMode = MeasureSpec.getMode(heightSpec);
        int heightSize = MeasureSpec.getSize(heightSpec);
        if (widthMode == MeasureSpec.EXACTLY && widthSize > 0) {
            width = widthSize;
        }
        if (heightMode == MeasureSpec.EXACTLY && heightSize > 0) {
            height = heightSize;
        }
        super.onMeasure(widthSpec, heightSpec);
    }

    public class AvatarAdapter extends RecyclerView.Adapter<ViewHolder> {
        private static final int TYPE_SPACE = 0;
        private static final int TYPE_ITEM = 1;

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_SPACE) {
                return new SpaceViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.avatar_space, parent, false));
            } else {
                return new AvatarViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.avatar_item, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Log.d("[ARV]", position + " create");
            if (holder instanceof SpaceViewHolder) {
                return;
            }
            ImageView avatar = ((AvatarViewHolder) holder).avatarImageView;
            Picasso.with(getContext()).load(avatarUrls.get(position - 1))
                    .into(((AvatarViewHolder) holder).avatarImageView);
            avatar.setTag(position - 1);
            if (position == 1) {
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) avatar.getLayoutParams();
                layoutParams.width = (int) selectSize;
                avatar.setLayoutParams(layoutParams);
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0 || position == avatarUrls.size() + 1) {
                return TYPE_SPACE;
            }
            return TYPE_ITEM;
        }

        @Override
        public int getItemCount() {
            return avatarUrls.size() + 2;
        }
    }

    public class AvatarViewHolder extends RecyclerView.ViewHolder {
        final ImageView avatarImageView;

        public AvatarViewHolder(View itemView) {
            super(itemView);
            itemView.setPadding((int) (dividerSize / 2), 0, (int) (dividerSize / 2), 0);
            avatarImageView = (ImageView) itemView.findViewById(R.id.avatarItem);
        }
    }

    public class SpaceViewHolder extends RecyclerView.ViewHolder {
        final View spaceView;

        public SpaceViewHolder(View itemView) {
            super(itemView);
            spaceView = itemView.findViewById(R.id.avatarSpace);
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) spaceView.getLayoutParams();
            params.width = (int) ((width - selectSize - dividerSize) / 2);
        }
    }

    public class AvatarOnScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            if (newState == SCROLL_STATE_IDLE) {
                scrollOver();
            }
            super.onScrollStateChanged(recyclerView, newState);
        }
    }
}
