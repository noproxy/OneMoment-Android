package co.yishun.onemoment.app.util;

import java.util.ArrayDeque;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by carlos on 4/20/16.
 */
public class SerialExecutor implements Executor {
    private static final Executor DEFAULT_EXECUTOR = Executors.newSingleThreadExecutor();
    final ArrayDeque<Runnable> mTasks = new ArrayDeque<Runnable>();
    Runnable mActive;

    public synchronized void execute(final Runnable r) {
        mTasks.offer(() -> {
            try {
                r.run();
            } finally {
                scheduleNext();
            }
        });
        if (mActive == null) {
            scheduleNext();
        }
    }

    public synchronized void clear() {
        mTasks.clear();
    }

    protected synchronized void scheduleNext() {
        if ((mActive = mTasks.poll()) != null) {
            DEFAULT_EXECUTOR.execute(mActive);
        }
    }
}
