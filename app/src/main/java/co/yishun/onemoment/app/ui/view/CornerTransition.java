package co.yishun.onemoment.app.ui.view;

import android.animation.Animator;
import android.animation.FloatEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

import com.transitionseverywhere.Transition;
import com.transitionseverywhere.TransitionValues;

/**
 * Created by Jinge on 2015/11/27.
 */
public class CornerTransition extends Transition {
    private static final String PROPERTY = "cornerRadio";
    private static final String TAG = "CornerTransition";
    float startCornerRadio;
    float stopCornerRadio;

    public CornerTransition() {
    }

    public CornerTransition(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void captureStartValues(TransitionValues startValues) {
        startCornerRadio = ((RadioCornerImageView) (startValues.view)).getCornerRadio();
        startValues.values.put(PROPERTY, startCornerRadio);
    }

    @Override
    public void captureEndValues(TransitionValues endValues) {
        stopCornerRadio = ((RadioCornerImageView) (endValues.view)).getCornerRadio();
        endValues.values.put(PROPERTY, stopCornerRadio);
    }

    @Override
    public Animator createAnimator(ViewGroup sceneRoot, TransitionValues startValues, TransitionValues endValues) {
        ObjectAnimator radiusAnimator = ObjectAnimator.ofFloat(endValues.view,
                "cornerRadio", ((RadioCornerImageView) (startValues.view)).getCornerRadio(),
                ((RadioCornerImageView) (endValues.view)).getCornerRadio());
        radiusAnimator.setEvaluator(new FloatEvaluator());
        return radiusAnimator;
    }
}
