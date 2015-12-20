package co.yishun.onemoment.app.ui.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

import co.yishun.onemoment.app.R;


/**
 * Created by 进 on 2015/2/4.
 */
public class PageIndicatorDot extends View {

    int num;
    float radius;
    float interval;
    int background;
    int foreground;

    int current;

    private ViewPager viewPager;
    private Paint paint;

    public PageIndicatorDot(Context context) {
        super(context);
        init(context);
    }

    public PageIndicatorDot(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
        initAttr(context, attrs);
    }

    public PageIndicatorDot(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
        initAttr(context, attrs);
    }

    private void initAttr(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PageIndicatorDot);

        num = typedArray.getInteger(R.styleable.PageIndicatorDot_pid_num, num);
        radius = typedArray.getDimension(R.styleable.PageIndicatorDot_pid_dotRadius, radius);
        interval = typedArray.getDimension(R.styleable.PageIndicatorDot_pid_interval, interval);
        background = typedArray.getColor(R.styleable.PageIndicatorDot_pid_background, background);
        foreground = typedArray.getColor(R.styleable.PageIndicatorDot_pid_foreground, foreground);

        typedArray.recycle();
    }

    private void init(Context context) {
        //set default values
        Resources resources = context.getResources();
        num = 0;
        radius = resources.getDimension(R.dimen.page_indicator_dot_radius);
        interval = resources.getDimension(R.dimen.page_indicator_dot_interval);
        background = resources.getColor(android.R.color.white);
        foreground = resources.getColor(R.color.colorAccent);
        current = 0;

        paint = new Paint();
    }

    public void setViewPager(ViewPager viewPager) {
        this.viewPager = viewPager;
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override public void onPageSelected(int position) {
                setCurrent(position);
            }

            @Override public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
        invalidate();
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        float leftMargin = (width - interval * (num - 1)) / 2;
        float topMargin = height / 2;

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(background);

        for (int i = 0; i < num; i++) {
            canvas.drawCircle(leftMargin + interval * i, topMargin, radius, paint);
        }

        paint.setColor(foreground);
        canvas.drawCircle(leftMargin + interval * current, topMargin, radius, paint);
    }
}
