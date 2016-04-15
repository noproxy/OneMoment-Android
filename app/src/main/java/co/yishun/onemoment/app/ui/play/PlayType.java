package co.yishun.onemoment.app.ui.play;

import android.support.annotation.IntDef;

/**
 * Created by carlos on 4/15/16.
 */
@IntDef({PlayType.TYPE_SINGLE, PlayType.TYPE_WORLD, PlayType.TYPE_TODAY})
public @interface PlayType {
    int TYPE_SINGLE = 0x1;
    int TYPE_WORLD = 0x2;
    int TYPE_TODAY = 0x4;
}