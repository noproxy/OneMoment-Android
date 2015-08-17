package co.yishun.onemoment.app.ui.home;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.malinskiy.superrecyclerview.SuperRecyclerView;

import org.androidannotations.annotations.EFragment;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.model.WorldTag;
import co.yishun.onemoment.app.ui.adapter.AbstractRecyclerViewAdapter;
import co.yishun.onemoment.app.ui.adapter.MeAdapter;
import co.yishun.onemoment.app.ui.common.TabPagerFragment;
import co.yishun.onemoment.app.ui.other.MeController_;

/**
 * Created by yyz on 7/21/15.
 */
@EFragment(R.layout.fragment_me)
public class MeFragment extends TabPagerFragment implements AbstractRecyclerViewAdapter.OnItemClickListener<WorldTag> {

    @Override
    protected int getTitleDrawableRes() {
        return R.drawable.pic_me_tittle;
    }


    @Override
    protected int getTabTitleArrayResources() {
        return R.array.me_page_title;
    }

    @NonNull
    @Override
    protected View onCreatePagerView(LayoutInflater inflater, ViewGroup container, int position) {
        View rootView = inflater.inflate(R.layout.page_world, container, false);

        SuperRecyclerView recyclerView = (SuperRecyclerView) rootView.findViewById(R.id.recyclerView);
        LinearLayoutManager manager = new LinearLayoutManager(inflater.getContext());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);

        MeAdapter adapter = new MeAdapter(inflater.getContext(), this);
        recyclerView.setAdapter(adapter);
        MeController_.getInstance_(inflater.getContext()).setUp(adapter, recyclerView, position == 0);

        container.addView(rootView);
        return rootView;
    }

    @Override
    protected int getContentViewId(Bundle savedInstanceState) {
        return R.layout.fragment_me;
    }


    @Override
    public void onClick(View view, WorldTag item) {

    }
}
