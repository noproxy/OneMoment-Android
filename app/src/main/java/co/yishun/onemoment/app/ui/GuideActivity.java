package co.yishun.onemoment.app.ui;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.ui.view.PageIndicatorDot;

@EActivity(R.layout.activity_guide)
public class GuideActivity extends AppCompatActivity {
    @ViewById ViewPager viewPager;
    @ViewById PageIndicatorDot pageIndicator;

    private int pageNum = 5;

    @AfterViews void setUpViews() {
        viewPager.setAdapter(getViewPager(LayoutInflater.from(this)));
        viewPager.setOffscreenPageLimit(5);
        pageIndicator.setViewPager(viewPager);
        pageIndicator.setNum(pageNum);
    }

    protected View onCreatePagerView(LayoutInflater inflater, ViewGroup container, int position) {
        View rootView = inflater.inflate(R.layout.page_guide, container, false);

        container.addView(rootView);
        return rootView;
    }

    private PagerAdapter getViewPager(LayoutInflater inflater) {
        return new PagerAdapter() {
            @Override
            public int getCount() {
                return pageNum;
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                View rootView = onCreatePagerView(inflater, container, position);

                container.addView(rootView);
                return rootView;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView(((View) object));
            }
        };
    }

}
