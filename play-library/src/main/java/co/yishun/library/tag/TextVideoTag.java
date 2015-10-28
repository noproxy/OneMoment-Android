package co.yishun.library.tag;

import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

/**
 * Created on 2015/10/15.
 */
public class TextVideoTag implements VideoTag {
    TextView mTextView;
    String mText;
    float mX;
    float mY;

    public TextVideoTag(String mText, float mX, float mY) {
        this.mText = mText;
        this.mX = mX;
        this.mY = mY;
    }

    public void setText(String mText) {
        this.mText = mText;
    }

    @Override
    public String getText() {
        return mText;
    }

    @Override
    public float getX() {
        return mX;
    }

    @Override
    public void setX(float x) {
        mX = x;
    }

    @Override
    public float getY() {
        return mY;
    }

    @Override
    public void setY(float y) {
        mY = y;
    }

    public void setView(View view) {
        mTextView = (TextView) view;
        mTextView.setText(mText);
    }

    public View getView() {
        return mTextView;
    }

}
