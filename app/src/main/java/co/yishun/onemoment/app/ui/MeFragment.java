package co.yishun.onemoment.app.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.androidannotations.annotations.EFragment;

import co.yishun.onemoment.app.R;

/**
 * Created by yyz on 7/21/15.
 */
@EFragment(R.layout.fragment_me)
public class MeFragment extends TabPagerFragment {

    @Override protected int getTitleDrawableRes() {
        return R.drawable.pic_me_tittle;
    }


    @Override int getTabTitleArrayResources() {
        return R.array.me_page_title;
    }

    @NonNull @Override View onCreatePagerView(LayoutInflater inflater, ViewGroup container, int position) {
        View rootView = inflater.inflate(R.layout.page_world, container, false);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        LinearLayoutManager manager = new LinearLayoutManager(inflater.getContext());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        recyclerView.setHasFixedSize(true);

        RecyclerView.Adapter adapter = new WorldFragment.WorldAdapter(inflater.getContext(), Test.res, false);

        recyclerView.setAdapter(adapter);
        container.addView(rootView);
        return rootView;
    }

    @Override int getContentViewId(Bundle savedInstanceState) {
        return R.layout.fragment_me;
    }


}
