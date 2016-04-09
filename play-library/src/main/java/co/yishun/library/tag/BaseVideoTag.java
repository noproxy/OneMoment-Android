package co.yishun.library.tag;

import android.view.View;

/**
 * Created by jay on 10/3/15.
 */
public class BaseVideoTag implements VideoTag {
    private String mText;
    private float mX;
    private float mY;

    public BaseVideoTag(String text, float x, float y) {
        mText = text;
        mX = x;
        mY = y;
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

}
