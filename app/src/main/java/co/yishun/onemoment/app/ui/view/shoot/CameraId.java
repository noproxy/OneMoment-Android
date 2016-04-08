package co.yishun.onemoment.app.ui.view.shoot;

/**
 * Created by Carlos on 2015/10/9.
 */
public class CameraId {
    public final int back;
    public final int front;

    public CameraId(int back, int front) {
        this.back = back;
        this.front = front;
    }

    public static CameraId create(int back, int front) {
        return new CameraId(back, front);
    }
}
