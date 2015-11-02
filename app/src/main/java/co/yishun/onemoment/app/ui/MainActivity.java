package co.yishun.onemoment.app.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.androidannotations.annotations.EActivity;

import java.lang.ref.WeakReference;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.account.AccountHelper;
import co.yishun.onemoment.app.api.Account;
import co.yishun.onemoment.app.api.authentication.OneMomentV3;
import co.yishun.onemoment.app.api.model.User;
import co.yishun.onemoment.app.data.RealmHelper;
import co.yishun.onemoment.app.ui.common.BaseActivity;
import co.yishun.onemoment.app.ui.home.DiscoveryFragment_;
import co.yishun.onemoment.app.ui.home.MeFragment_;
import co.yishun.onemoment.app.ui.home.VerifyFragment_;
import co.yishun.onemoment.app.ui.home.WorldFragment;
import co.yishun.onemoment.app.ui.home.WorldFragment_;
import co.yishun.onemoment.app.ui.view.floatingactionbutton.FloatingActionButton;
import co.yishun.onemoment.app.ui.view.floatingactionbutton.FloatingActionsMenu;

@EActivity
public class MainActivity extends BaseActivity implements AccountHelper.OnUserInfoChangeListener {
    private static final String TAG = "MainActivity";
    private static WeakReference<FloatingActionsMenu> floatingActionMenu;
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
        FloatingActionsMenu fab = floatingActionMenu.get();
        if (fab != null) {
            return fab;
        } else return view;
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
            if (menuItem.getGroupId() == R.id.group_switch) {
                menuItem.setChecked(true);
            }
            drawerLayout.closeDrawers();
            return navigationTo(menuItem.getItemId());
        });

        invalidateUserInfo(AccountHelper.getUserInfo(this));
        AccountHelper.setOnUserInfoChangeListener(this);

        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.app_name, R.string.app_name);
        drawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void setActionMenu() {
        FloatingActionsMenu fam = (FloatingActionsMenu) findViewById(R.id.fab);
        floatingActionMenu = new WeakReference<>(fam);

        FloatingActionButton momentShootBtn = new FloatingActionButton(this);
        momentShootBtn.setIcon(R.drawable.pic_fab_menu_diary);
        momentShootBtn.setTitle("Moment");
        momentShootBtn.setOnClickListener(v -> {
            startShoot(v, false);
            fam.collapse();
        });
        FloatingActionButton worldShootBtn = new FloatingActionButton(this);
        worldShootBtn.setIcon(R.drawable.pic_fab_menu_world);
        worldShootBtn.setTitle("World");
        worldShootBtn.setOnClickListener(v -> {
            startShoot(v, true);
            fam.collapse();
        });

        fam.addButton(momentShootBtn);
        fam.addButton(worldShootBtn);

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        Log.i(TAG, "onTouch: " + event);
        // collapse fab if click outside of fab
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            Rect outRect = new Rect();
            FloatingActionsMenu fam = floatingActionMenu.get();
            if (fam != null) {
                fam.getGlobalVisibleRect(outRect);
                if (!outRect.contains(((int) event.getRawX()), ((int) event.getRawY()))) {
                    fam.clearFocus();
                    fam.collapse();
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    private void invalidateUserInfo(User user) {
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

    private boolean navigationTo(int itemId) {
        if (itemId == currentItemId) return true;
        //TODO add delay to let drawer close
        switch (itemId) {
            case R.id.navigation_item_0:
                if (worldFragment == null) {
                    worldFragment = new WorldFragment_();
                }
                fragmentManager.beginTransaction().setCustomAnimations(R.anim.fragment_fade_in, R.anim.fragment_fade_out)
                        .replace(R.id.fragment_container, worldFragment).commit();
                currentItemId = itemId;
                break;
            case R.id.navigation_item_1:
                fragmentManager.beginTransaction().setCustomAnimations(R.anim.fragment_fade_in, R.anim.fragment_fade_out)
                        .replace(R.id.fragment_container, new VerifyFragment_()).commit();
                currentItemId = itemId;
                break;
            case R.id.navigation_item_2:
                DiscoveryFragment_ fragment2 = new DiscoveryFragment_();
                fragmentManager.beginTransaction().setCustomAnimations(R.anim.fragment_fade_in, R.anim.fragment_fade_out)
                        .replace(R.id.fragment_container, fragment2).commit();
                currentItemId = itemId;
                break;
            case R.id.navigation_item_3:
                MeFragment_ fragment3 = new MeFragment_();
                fragmentManager.beginTransaction().setCustomAnimations(R.anim.fragment_fade_in, R.anim.fragment_fade_out)
                        .replace(R.id.fragment_container, fragment3).commit();
                currentItemId = itemId;
                break;
            case R.id.navigation_item_4:
                Intent intent = new Intent(this, SettingsActivity_.class);
                startActivity(intent);
                return false;
            case R.id.main_search:
                Intent i = new Intent(this, SearchActivity_.class);
                startActivity(i);
                break;
        }
        return true;
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        switch (currentItemId) {
            case R.id.navigation_item_0:
                menu.findItem(R.id.main_search).setIcon(R.drawable.ic_action_search);
                break;
            case R.id.navigation_item_1:
                menu.findItem(R.id.main_search).setIcon(R.drawable.ic_diary);
                break;
            case R.id.navigation_item_2:
                menu.findItem(R.id.main_search).setIcon(R.drawable.ic_me);
                break;
            default:
                menu.findItem(R.id.main_search).setVisible(false);
                break;
        }
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) return true;
        switch (item.getItemId()) {
            case R.id.main_search:
//                Intent intent = new Intent(this, SearchActivity_.class);
//                startActivity(intent);
                navigationTo(R.id.main_search);
//                mDrawerToggle.setHomeAsUpIndicator();
                return true;
        }
        return false;
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
    public void onUserInfoChange(User info) {
        invalidateUserInfo(info);
    }
}
