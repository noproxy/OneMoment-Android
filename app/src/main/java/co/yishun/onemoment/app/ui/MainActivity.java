package co.yishun.onemoment.app.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionMenu;
import com.squareup.picasso.Picasso;
import com.umeng.analytics.MobclickAgent;
import com.umeng.message.PushAgent;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;

import java.lang.ref.WeakReference;
import java.util.Date;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.account.AccountManager;
import co.yishun.onemoment.app.account.SyncManager;
import co.yishun.onemoment.app.api.Account;
import co.yishun.onemoment.app.api.authentication.OneMomentV3;
import co.yishun.onemoment.app.api.model.User;
import co.yishun.onemoment.app.config.Constants;
import co.yishun.onemoment.app.data.RealmHelper;
import co.yishun.onemoment.app.ui.common.BaseActivity;
import co.yishun.onemoment.app.ui.home.DiaryFragment;
import co.yishun.onemoment.app.ui.home.DiaryFragment_;
import co.yishun.onemoment.app.ui.home.DiscoveryFragment_;
import co.yishun.onemoment.app.ui.home.MeFragment_;
import co.yishun.onemoment.app.ui.home.WorldFragment;
import co.yishun.onemoment.app.ui.home.WorldFragment_;

import static org.android.agoo.client.BaseRegistrar.getRegistrationId;

@EActivity
public class MainActivity extends BaseActivity implements AccountManager.OnUserInfoChangeListener {
    private static final String TAG = "MainActivity";
    private static WeakReference<FloatingActionMenu> floatingActionMenu;
    private static boolean pendingUserInfoUpdate = false;
    public ActionBarDrawerToggle mDrawerToggle;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    private FragmentManager fragmentManager;
    private int currentItemId = 0;
    private WorldFragment worldFragment;
    private Account mAccount = OneMomentV3.createAdapter().create(Account.class);
    private ImageView profileImageView;
    private TextView usernameTextView;
    private TextView locationTextView;
    private BroadcastReceiver mSyncChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (currentItemId == R.id.navigation_item_1) {
                Bundle extra = intent.getExtras();
                long unixTimeStamp = extra.getLong(SyncManager.SYNC_BROADCAST_EXTRA_LOCAL_UPDATE_TIMESTAMP);

                boolean needUpdate = ((DiaryFragment) fragmentManager.findFragmentById(R.id.fragment_container)).isCurrentMonth(new Date(unixTimeStamp * 1000));
                if (needUpdate) updateDiary();
            }

        }
    };

    /**
     * Flag means just going to shoot diary ui.
     */
    private boolean goToShootDiary = false;

    @Override
    protected void onResume() {
        super.onResume();
        // refresh diary in case moment update
        if (currentItemId == R.id.navigation_item_1 && goToShootDiary) {
            goToShootDiary = false;
            updateDiary();
        }
        registerSyncListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mSyncChangedReceiver);
    }

    private void updateDiary() {
        fragmentManager.beginTransaction().replace(R.id.fragment_container, DiaryFragment_.builder().build()).commitAllowingStateLoss();
    }

    private void startShoot(View view, boolean forWorld) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        ShootActivity_.intent(this).transitionX(location[0] + view.getWidth() / 2)
                .transitionY(location[1] + view.getHeight() / 2).forWorld(forWorld).start();

        if (!forWorld) {
            goToShootDiary = true;
            navigationTo(R.id.navigation_item_1);
            navigationView.setCheckedItem(R.id.navigation_item_1);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RealmHelper.setup(this);
        PushAgent mPushAgent = PushAgent.getInstance(this);
        LogUtil.d(TAG, "token " + getRegistrationId(this));
        mPushAgent.enable();

        fragmentManager = getSupportFragmentManager();
        setupNavigationView();
        setActionMenu();
        navigationTo(R.id.navigation_item_0);
    }

    private void setupNavigationView() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        navigationView = ((NavigationView) findViewById(R.id.navigationView));
        // views in NavigationView can only be found from NavigationView since support 23.1.0
        // http://stackoverflow.com/questions/33199764/android-api-23-change-navigation-view-headerlayout-textview
        View header = LayoutInflater.from(this).inflate(R.layout.layout_nav_header, navigationView, false);
        navigationView.addHeaderView(header);

        profileImageView = (ImageView) header.findViewById(R.id.profileImageView);
        usernameTextView = (TextView) header.findViewById(R.id.usernameTextView);
        locationTextView = (TextView) header.findViewById(R.id.locationTextView);
        header.setOnClickListener(v -> {
            drawerLayout.closeDrawers();
            delayStartUserInfoActivity();
        });
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            drawerLayout.closeDrawers();
            int id = menuItem.getItemId();
            if (id == R.id.navigation_item_4) {
                delayStartSettingsActivity();
                return false;
            } else
                return navigationTo(menuItem.getItemId());
        });

        invalidateUserInfo(AccountManager.getUserInfo(this));
        AccountManager.addOnUserInfoChangedListener(this);

        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.app_name, R.string.app_name);
        drawerLayout.setDrawerListener(mDrawerToggle);
    }

    @UiThread(delay = 300) void delayStartSettingsActivity() {
        SettingsActivity_.intent(this).start();
        //TODO add animation
    }

    @UiThread(delay = 300) void delayStartUserInfoActivity() {
        UserInfoActivity_.intent(this).start();
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
        createCustomAnimation(fam);
    }

    private void createCustomAnimation(FloatingActionMenu menu) {
        ImageView menuIcon = menu.getMenuIconView();

        AnimatorSet openSet = new AnimatorSet();
        ObjectAnimator openBackground = ObjectAnimator.ofInt(menu, "menuButtonColorNormal",
                getResources().getColor(R.color.colorAccent), getResources().getColor(R.color.colorPrimary));
        openBackground.setEvaluator(new ArgbEvaluator());
        openBackground.setDuration(200);

        ObjectAnimator openRotate = ObjectAnimator.ofFloat(menuIcon, "rotation", 0f, -180f);
        openRotate.setDuration(200);
        openRotate.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationStart(Animator animation) {
                menu.setMenuButtonColorNormalResId(R.color.colorPrimary);
                menu.setMenuButtonColorPressedResId(R.color.colorPrimaryDark);
                menuIcon.setImageResource(R.drawable.ic_action_close);
            }
        });
        openSet.play(openRotate).with(openBackground);
        openSet.setInterpolator(new OvershootInterpolator(1));

        AnimatorSet closeSet = new AnimatorSet();
        ObjectAnimator closeBackground = ObjectAnimator.ofInt(menu, "menuButtonColorNormal",
                getResources().getColor(R.color.colorPrimary), getResources().getColor(R.color.colorAccent));
        closeBackground.setEvaluator(new ArgbEvaluator());
        closeBackground.setDuration(200);

        ObjectAnimator closeRotate = ObjectAnimator.ofFloat(menuIcon, "rotation", -90, 0f);
        closeRotate.setDuration(200);
        closeRotate.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationStart(Animator animation) {
                menu.setMenuButtonColorNormalResId(R.color.colorAccent);
                menu.setMenuButtonColorPressedResId(R.color.colorAccentDark);
                menuIcon.setImageResource(R.drawable.ic_fab);
            }
        });
        closeSet.play(closeRotate).with(closeBackground);
        closeSet.setInterpolator(new OvershootInterpolator(1));

        menu.setIconToggleAnimatorSet(openSet);
        menu.setOnMenuToggleListener(opened -> menu.setIconToggleAnimatorSet(opened ? closeSet : openSet));
    }

    private void registerSyncListener() {
        registerReceiver(mSyncChangedReceiver, new IntentFilter(SyncManager.SYNC_BROADCAST_ACTION_LOCAL_UPDATE));
    }

    private void unregisterSyncListener() {
        unregisterReceiver(mSyncChangedReceiver);
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

    @UiThread void invalidateUserInfo(User user) {
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

    @UiThread(delay = 300) void delayCommit(Fragment targetFragment) {
        fragmentManager.beginTransaction().setCustomAnimations(R.anim.fragment_fade_in, R.anim.fragment_fade_out).replace(R.id.fragment_container, targetFragment).commitAllowingStateLoss();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
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
        AccountManager.removeOnUserInfoChangedListener(this);
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

    @NonNull
    @Override
    public View getSnackbarAnchorWithView(@Nullable View view) {
        FloatingActionMenu fab = floatingActionMenu.get();
        if (fab != null) {
            return fab;
        } else
            return super.getSnackbarAnchorWithView(view);
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
