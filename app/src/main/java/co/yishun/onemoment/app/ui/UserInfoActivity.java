package co.yishun.onemoment.app.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.FragmentById;
import org.androidannotations.annotations.ViewById;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.account.AccountHelper;
import co.yishun.onemoment.app.ui.common.BaseActivity;
import co.yishun.onemoment.app.ui.common.BaseFragment;

/**
 * Created by Jinge on 2015/11/12.
 */
@EActivity(R.layout.activity_user_info)
public class UserInfoActivity extends BaseActivity {
    @ViewById
    Toolbar toolbar;
    @ViewById
    RelativeLayout avatarLayout;
    @ViewById
    ImageView avatarImage;
    @FragmentById
    ItemFragment usernameFragment;
    @FragmentById
    ItemFragment weiboFragment;
    @FragmentById
    ItemFragment genderFragment;
    @FragmentById
    ItemFragment locationFragment;

    @Nullable
    @Override
    public View getSnackbarAnchorWithView(@Nullable View view) {
        return null;
    }

    @AfterViews
    void setupToolbar() {
        if (toolbar == null)
            throw new UnsupportedOperationException("You need bind Toolbar instance to" +
                    " toolbar in onCreateView(LayoutInflater, ViewGroup, Bundle");
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(getResources().getString(R.string.activity_user_info_title));
        Log.i("setupToolbar", "set home as up true");
    }

    @AfterViews
    void setupViews() {
        Picasso.with(this).load(AccountHelper.getUserInfo(this).avatarUrl).into(avatarImage);

        usernameFragment.setTitle(getResources().getString(R.string.activity_user_info_username));
        usernameFragment.setContent(AccountHelper.getUserInfo(this).nickname);

        weiboFragment.setTitle(getResources().getString(R.string.activity_user_info_weibo_id));
        weiboFragment.setContent(AccountHelper.getUserInfo(this).weiboNickname);

        genderFragment.setTitle(getResources().getString(R.string.activity_user_info_gender));
        genderFragment.setContent(AccountHelper.getUserInfo(this).gender.toString());

        locationFragment.setTitle(getResources().getString(R.string.activity_user_info_location));
        locationFragment.setContent(AccountHelper.getUserInfo(this).location);
    }

    public static class ItemFragment extends BaseFragment {
        TextView itemTitle;
        TextView itemContent;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);
            ViewGroup viewRoot = (ViewGroup) inflater.inflate(R.layout.fragment_user_info_item, container, false);
            itemTitle = (TextView) viewRoot.findViewById(R.id.itemTitle);
            itemContent = (TextView) viewRoot.findViewById(R.id.itemContent);
            return viewRoot;
        }

        void setTitle(String title) {
            itemTitle.setText(title);
        }

        void setContent(String content) {
            itemContent.setText(content);
        }
    }

}
