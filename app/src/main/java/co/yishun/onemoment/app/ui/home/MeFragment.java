package co.yishun.onemoment.app.ui.home;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.malinskiy.superrecyclerview.SuperRecyclerView;
import com.squareup.picasso.Picasso;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.account.AccountHelper;
import co.yishun.onemoment.app.api.model.User;
import co.yishun.onemoment.app.api.model.WorldTag;
import co.yishun.onemoment.app.ui.adapter.AbstractRecyclerViewAdapter;
import co.yishun.onemoment.app.ui.adapter.MeAdapter;
import co.yishun.onemoment.app.ui.common.TabPagerFragment;
import co.yishun.onemoment.app.ui.controller.MeController_;

/**
 * Created by yyz on 7/21/15.
 */
@EFragment(R.layout.fragment_me)
public class MeFragment extends TabPagerFragment implements AbstractRecyclerViewAdapter.OnItemClickListener<WorldTag> {
    @ViewById TextView nickNameTextView;
    @ViewById TextView votedCountTextView;
    @ViewById TextView voteCountTextView;// "Voted 21"
    @ViewById ImageView profileImageView;// "Vote 3"

    @AfterViews
    void setHeader() {
        User user = AccountHelper.getUserInfo(getContext());
        invalidateUserInfo(user);
        AccountHelper.addOnUserInfoChangedListener(this::invalidateUserInfo);
    }

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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_me, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.fragment_me_action_modify_info:
                //TODO start activity to update user info
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View view, WorldTag item) {

    }

    private void invalidateUserInfo(User user) {
        if (user == null) {
            return;
        }
        Picasso.with(getContext()).load(user.avatarUrl).into(profileImageView);
        nickNameTextView.setText(user.nickname);
        String voted = String.format(getResources().getString(R.string.fragment_me_voted_format_text), String.valueOf(user.likedWorlds.length));
        votedCountTextView.setText(voted);
        String vote = String.format(getResources().getString(R.string.fragment_me_vote_format_text), String.valueOf(user.joinedWorld.length));
        voteCountTextView.setText(vote);
    }
}
