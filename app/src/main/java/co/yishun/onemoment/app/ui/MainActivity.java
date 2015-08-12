package co.yishun.onemoment.app.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.lang.ref.WeakReference;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.ui.common.BaseActivity;
import co.yishun.onemoment.app.ui.home.DiscoveryFragment_;
import co.yishun.onemoment.app.ui.home.MeFragment_;
import co.yishun.onemoment.app.ui.home.VerifyFragment_;
import co.yishun.onemoment.app.ui.home.WorldFragment;

public final class MainActivity extends BaseActivity {
    private static WeakReference<FloatingActionButton> floatingActionButton;
    public ActionBarDrawerToggle mDrawerToggle;
    DrawerLayout drawerLayout;
    private FragmentManager fragmentManager;
    private int currentItemId = 0;
    private WorldFragment worldFragment;

    /**
     * get fab to display SnackBar
     *
     * @param view where SnackBar called
     * @return FloatingActionBar view if exists, the origin param view if not exists.
     */
    public static
    @NonNull
    View withView(@NonNull View view) {
        FloatingActionButton fab = floatingActionButton.get();
        if (fab != null) {
            return fab;
        } else return view;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentManager = getSupportFragmentManager();
        setupNavigationView();
        navigationTo(R.id.navigation_item_0);
    }

    private void setupNavigationView() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        ((NavigationView) findViewById(R.id.navigationView))
                .setNavigationItemSelectedListener(menuItem -> {
                    menuItem.setChecked(true);
                    drawerLayout.closeDrawers();
                    return navigationTo(menuItem.getItemId());
                });
        floatingActionButton = new WeakReference<>((FloatingActionButton) findViewById(R.id.fab));
        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.app_name, R.string.app_name);
        drawerLayout.setDrawerListener(mDrawerToggle);
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
                    worldFragment = new WorldFragment();
                }
                fragmentManager.beginTransaction().replace(R.id.fragment_container, worldFragment).commit();
                currentItemId = itemId;
                break;
            case R.id.navigation_item_1:
                fragmentManager.beginTransaction().replace(R.id.fragment_container, new VerifyFragment_()).commit();
                currentItemId = itemId;
                break;
            case R.id.navigation_item_2:
                DiscoveryFragment_ fragment2 = new DiscoveryFragment_();
                fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment2).commit();
                currentItemId = itemId;
                break;
            case R.id.navigation_item_3:
                MeFragment_ fragment3 = new MeFragment_();
                fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment3).commit();
                currentItemId = itemId;
                break;
            case R.id.navigation_item_4:
                Intent intent = new Intent(this, SettingsActivity_.class);
                startActivity(intent);
                return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (currentItemId != R.id.navigation_item_0) {
            navigationTo(R.id.navigation_item_0);
        } else
            supportFinishAfterTransition();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) return true;
        switch (item.getItemId()) {
            case R.id.action_settings:
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
        return floatingActionButton.get();
    }
}
