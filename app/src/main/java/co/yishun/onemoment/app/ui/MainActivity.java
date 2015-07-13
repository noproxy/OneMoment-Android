package co.yishun.onemoment.app.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import co.yishun.onemoment.app.R;

public class MainActivity extends AppCompatActivity {
    private FragmentManager fragmentManager;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentManager = getSupportFragmentManager();
        setupNavigationView();
    }

    private void setupNavigationView() {
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        ((NavigationView) findViewById(R.id.navigationView))
                .setNavigationItemSelectedListener(menuItem -> {
                    menuItem.setChecked(true);
                    drawerLayout.closeDrawers();
                    navigationTo(menuItem.getItemId());
                    return true;
                });

        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.app_name, R.string.app_name);
        drawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void display(Fragment fragment) {
        fragmentManager.beginTransaction().replace(R.id.fragment, fragment).commit();
    }

    private boolean navigationTo(int itemId) {
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragment);
        switch (itemId) {
            case R.id.navigation_item_0:
                if (fragment instanceof WorldFragment)
                    return false;
                else display(new WorldFragment());
                break;
            case R.id.navigation_item_1:
                if (fragment instanceof TestFragment)
                    return false;
                else display(new TestFragment());
                break;
            case R.id.navigation_item_2:
                if (fragment instanceof WorldFragment)
                    return false;
                else display(new WorldFragment());
                break;
            case R.id.navigation_item_3:
                fragmentManager.beginTransaction().replace(R.id.fragment, new WorldFragment())
                        .commit();
                break;
            case R.id.navigation_item_4:
                Intent intent = new Intent("to setting");//TODO add intent to setting
                startActivity(intent);
                return false;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
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
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
}
