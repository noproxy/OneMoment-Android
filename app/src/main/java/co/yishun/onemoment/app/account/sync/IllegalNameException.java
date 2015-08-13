package co.yishun.onemoment.app.account.sync;

/**
 * Created by Carlos on 2015/4/5.
 */
public class IllegalNameException extends IllegalArgumentException {
    public IllegalNameException() {
        super("Video file name is illegal.");
    }

    public IllegalNameException(String fileName) {
        super("Video file name is illegal: " + fileName);
    }
}
