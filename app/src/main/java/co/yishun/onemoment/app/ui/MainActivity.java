package co.yishun.onemoment.app.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionMenu;
import com.squareup.picasso.Picasso;
import com.umeng.analytics.MobclickAgent;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;

import java.lang.ref.WeakReference;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.account.AccountHelper;
import co.yishun.onemoment.app.api.Account;
import co.yishun.onemoment.app.api.authentication.OneMomentV3;
import co.yishun.onemoment.app.api.model.User;
import co.yishun.onemoment.app.config.Constants;
import co.yishun.onemoment.app.data.RealmHelper;
import co.yishun.onemoment.app.ui.common.BaseActivity;
import co.yishun.onemoment.app.ui.home.DiaryFragment_;
import co.yishun.onemoment.app.ui.home.DiscoveryFragment_;
import co.yishun.onemoment.app.ui.home.MeFragment_;
import co.yishun.onemoment.app.ui.home.WorldFragment;
import co.yishun.onemoment.app.ui.home.WorldFragment_;

@EActivity
public class MainActivity extends BaseActivity implements AccountHelper.OnUserInfoChangeListener {
    private static final String TAG = "MainActivity";
    private static WeakReference<FloatingActionMenu> floatingActionMenu;
    private static boolean pendingUserInfoUpdate = false;
    public ActionBarDrawerToggle mDrawerToggle;
    DrawerLayout drawerLayout;
    private FragmentManager fragmentManager;
    private int currentItemId = 0;
    private WorldFragment worldFragment;
    private Account mAccount = OneMomentV3.createAdapter().create(Account.class);
    private ImageView profileImageView;
    private TextView usernameTextView;
    private TextView locationTextView;

    /**
     * get fab to display SnackBar
     *
     * @param view where SnackBar called
     * @return FloatingActionBar view if exists, the origin param view if not exists.
     */
    public static
    @NonNull
    View withView(@NonNull View view) {
        FloatingActionMenu fab = floatingActionMenu.get();
        if (fab != null) {
            return fab;
        } else return view;
    }

    @Override protected void onResume() {
        super.onResume();
        // refresh diary in case moment update
        if (currentItemId == R.id.navigation_item_1) {
            fragmentManager.beginTransaction().replace(R.id.fragment_container, DiaryFragment_.builder().build()).commit();
        }
    }

    private void startShoot(View view, boolean forWorld) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        ShootActivity_.intent(this).transitionX(location[0] + view.getWidth() / 2)
                .transitionY(location[1] + view.getHeight() / 2).forWorld(forWorld).start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RealmHelper.setup(this);

        fragmentManager = getSupportFragmentManager();
        setupNavigationView();
        setActionMenu();
        navigationTo(R.id.navigation_item_0);
    }

    private void setupNavigationView() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        NavigationView navigationView = ((NavigationView) findViewById(R.id.navigationView));
        // views in NavigationView can only be found from NavigationView since support 23.1.0
        // http://stackoverflow.com/questions/33199764/android-api-23-change-navigation-view-headerlayout-textview
        View header = LayoutInflater.from(this).inflate(R.layout.layout_nav_header, navigationView, false);
        navigationView.addHeaderView(header);

        profileImageView = (ImageView) header.findViewById(R.id.profileImageView);
        usernameTextView = (TextView) header.findViewById(R.id.usernameTextView);
        locationTextView = (TextView) header.findViewById(R.id.locationTextView);

        navigationView.setNavigationItemSelectedListener(menuItem -> {
            drawerLayout.closeDrawers();
            int id = menuItem.getItemId();
            if (id == R.id.navigation_item_4) {
                delayStartSettingsActivity();
                return false;
            } else
                return navigationTo(menuItem.getItemId());
        });

        invalidateUserInfo(AccountHelper.getUserInfo(this));
        AccountHelper.addOnUserInfoChangedListener(this);

        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.app_name, R.string.app_name);
        drawerLayout.setDrawerListener(mDrawerToggle);
    }

    @UiThread(delay = 300)
    void delayStartSettingsActivity() {
        SettingsActivity_.intent(this).start();
        //TODO add animation
    }

    private void setActionMenu() {
        FloatingActionMenu fam = (FloatingActionMenu) findViewById(R.id.fab);
        floatingActionMenu = new WeakReference<>(fam);
        fam.findViewById(R.id.worldFABBtn).setOnClickListener(v -> {
            MobclickAgent.onEvent(this, Constants.UmengData.FAB_WORLD_CLICK);
            startShoot(v, true);
            fam.close(false);
        });
        fam.findViewById(R.id.diaryFABBtn).setOnClickListener(v -> {
            MobclickAgent.onEvent(this, Constants.UmengData.FAB_DIARY_CLICK);
            startShoot(v, false);
            fam.close(false);
        });

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        // collapse fab if click outside of fab
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            Rect outRect = new Rect();
            FloatingActionMenu fam = floatingActionMenu.get();
            if (fam != null) {
                fam.getGlobalVisibleRect(outRect);
                if (!outRect.contains(((int) event.getRawX()), ((int) event.getRawY()))) {
                    fam.clearFocus();
                    fam.close(false);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    @UiThread
    void invalidateUserInfo(User user) {
        if (user == null) {
            return;
        }
        Picasso.with(this).load(user.avatarUrl).into(profileImageView);
        usernameTextView.setText(user.nickname);
        locationTextView.setText(user.location);
    }

    public void syncToggle() {
        if (mDrawerToggle != null) {
            mDrawerToggle.syncState();
        }
    }

    /**
     * switch main ui to selected fragment
     *
     * @param itemId id of the targeted fragment
     * @return whether the fragment is checked.
     */
    private boolean navigationTo(int itemId) {
        if (itemId == currentItemId) return true;
        //TODO add delay to let drawer close
        @SuppressLint("CommitTransaction")
        Fragment targetFragment;
        switch (itemId) {
            case R.id.navigation_item_0:
                if (worldFragment == null) {
                    worldFragment = WorldFragment_.builder().build();
                }
                targetFragment = worldFragment;
                break;
            case R.id.navigation_item_1:
                targetFragment = DiaryFragment_.builder().build();
                break;
            case R.id.navigation_item_2:
                targetFragment = DiscoveryFragment_.builder().build();
                break;
            case R.id.navigation_item_3:
                targetFragment = MeFragment_.builder().build();
                break;
            default:
                return false;
        }
        currentItemId = itemId;
        delayCommit(targetFragment);
        return true;
    }

    @UiThread(delay = 300)
    void delayCommit(Fragment targetFragment) {
        fragmentManager.beginTransaction().setCustomAnimations(R.anim.fragment_fade_in, R.anim.fragment_fade_out).replace(R.id.fragment_container, targetFragment).commit();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            drawerLayout.closeDrawers();
        } else {
            if (currentItemId != R.id.navigation_item_0) {
                navigationTo(R.id.navigation_item_0);
                ((NavigationView) findViewById(R.id.navigationView)).setCheckedItem(R.id.navigation_item_0);
            } else
                supportFinishAfterTransition();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AccountHelper.removeOnUserInfoChangedListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // no global option menu, but fragment would add menu
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // no global option menu to handle, fragment handles itself.
        // Just forwarding to DrawerToggle to handle item in NavigationDrawer
        return mDrawerToggle.onOptionsItemSelected(item);
    }

    public void onEspressoBtnClick(View view) {
        startActivity(new Intent(this, EspressoTestActivity.class));
    }

    public void onUIAutomatorBtnClick(View view) {
        startActivity(new Intent(this, UIAutomatorTestActivity.class));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        syncToggle();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        syncToggle();
    }

    @Nullable
    @Override
    public View getSnackbarAnchorWithView(@Nullable View view) {
        return floatingActionMenu.get();
    }

    @Override
    public void setPageInfo() {
        mIsPage = false;
        mPageName = "MainActivity";
    }

    @Override
    public void onUserInfoChange(User info) {
        invalidateUserInfo(info);
    }
}
