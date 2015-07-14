package co.yishun.onemoment.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import co.yishun.onemoment.app.R;

public final class MainActivity extends AppCompatActivity {
    private FragmentManager fragmentManager;
    private int currentItemId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentManager = getSupportFragmentManager();
        setupNavigationView();
        navigationTo(R.id.navigation_item_0);
    }

    private void setupNavigationView() {
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        ((NavigationView) findViewById(R.id.navigationView))
                .setNavigationItemSelectedListener(menuItem -> {
                    menuItem.setChecked(true);
                    drawerLayout.closeDrawers();
                    return navigationTo(menuItem.getItemId());
                });
    }

    private boolean navigationTo(int itemId) {
        if (itemId == currentItemId) return true;
        switch (itemId) {
            case R.id.navigation_item_0:
                fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                fragmentManager.beginTransaction().replace(R.id.fragment_container, new
                        WorldFragment()).commit();
                currentItemId = itemId;
                break;
            case R.id.navigation_item_1:
                TestFragment fragment = new TestFragment();
                fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment)
                        .addToBackStack(null).commit();
                currentItemId = itemId;
                break;
            case R.id.navigation_item_2:
                currentItemId = itemId;
                break;
            case R.id.navigation_item_3:
                currentItemId = itemId;
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
}
