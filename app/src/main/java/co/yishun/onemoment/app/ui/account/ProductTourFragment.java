package co.yishun.onemoment.app.ui.account;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.ui.common.BaseFragment;
import co.yishun.onemoment.app.ui.view.PageIndicatorDot;

/**
 * Created by Carlos on 2016/3/17.
 */
@EFragment(R.layout.fragment_product_tour)
public class ProductTourFragment extends BaseFragment {
    private final int pagePicRes[] = new int[]{
            R.drawable.pic_guide_001,
            R.drawable.pic_guide_002,
            R.drawable.pic_guide_003,
            R.drawable.pic_guide_004};
    private final int pageTextRes[] = new int[]{
            R.drawable.pic_guide_001txt,
            R.drawable.pic_guide_002txt,
            R.drawable.pic_guide_003txt,
            R.drawable.pic_guide_004txt};
    @ViewById
    ViewPager viewPager;
    @ViewById
    PageIndicatorDot pageIndicator;
    private int pageNum = 4;

    @Override
    public void setPageInfo() {
        mPageName = "ProductTourFragment";
    }

    @AfterViews
    void setUpViews() {
        viewPager.setAdapter(getViewPager(LayoutInflater.from(getContext())));
        viewPager.setOffscreenPageLimit(2);
        pageIndicator.setViewPager(viewPager);
        pageIndicator.setNum(pageNum);
    }

    protected View onCreatePagerView(LayoutInflater inflater, ViewGroup container, int position) {
        View rootView;
        rootView = inflater.inflate(R.layout.page_guide, container, false);
        ((ImageView) rootView.findViewById(R.id.picImage)).setImageResource(pagePicRes[position]);
        ((ImageView) rootView.findViewById(R.id.textImage)).setImageResource(pageTextRes[position]);

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
