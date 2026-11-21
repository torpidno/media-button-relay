package se.mediabuttonrelay.app;

/**
 * Pure state machine that distinguishes single, double, and triple button clicks
 * while firing immediately once the highest configured click count is reached.
 */
public final class ClickDetector {

    public interface Scheduler {
        void postDelayed(Runnable r, long delayMs);
        void removeCallbacks(Runnable r);
    }

    public interface Listener {
        void onAction(int clickCount, MediaAction action);
    }

    private final Scheduler scheduler;
    private final Listener listener;
    private int pendingClicks = 0;
    private MediaAction scheduledSingle = MediaAction.NONE;
    private MediaAction scheduledDouble = MediaAction.NONE;
    private MediaAction scheduledTriple = MediaAction.NONE;

    private final Runnable timeoutRunnable = new Runnable() {
        @Override
        public void run() {
            int count = pendingClicks;
            pendingClicks = 0;
            if (count <= 0) {
                return;
            }
            MediaAction resolved = actionForCount(
                    count, scheduledSingle, scheduledDouble, scheduledTriple);
            listener.onAction(count, resolved);
        }
    };

    public ClickDetector(Scheduler scheduler, Listener listener) {
        this.scheduler = scheduler;
        this.listener = listener;
    }

    /**
     * Records a button click and either dispatches immediately or waits {@code windowMs}
     * for additional clicks if higher click counts are configured.
     */
    public void onClick(
            MediaAction singleAction,
            MediaAction doubleAction,
            MediaAction tripleAction,
            long windowMs) {
        int maxClicks = resolveMaxClicks(doubleAction, tripleAction);
        pendingClicks++;
        scheduler.removeCallbacks(timeoutRunnable);

        if (pendingClicks >= maxClicks) {
            int count = pendingClicks;
            pendingClicks = 0;
            MediaAction resolved = actionForCount(count, singleAction, doubleAction, tripleAction);
            listener.onAction(count, resolved);
            return;
        }

        scheduledSingle = singleAction;
        scheduledDouble = doubleAction;
        scheduledTriple = tripleAction;
        scheduler.postDelayed(timeoutRunnable, windowMs);
    }

    /**
     * Cancels any pending multi-click timer without firing an action.
     */
    public void reset() {
        pendingClicks = 0;
        scheduler.removeCallbacks(timeoutRunnable);
    }

    private static int resolveMaxClicks(MediaAction doubleAction, MediaAction tripleAction) {
        if (tripleAction != MediaAction.NONE) {
            return 3;
        }
        if (doubleAction != MediaAction.NONE) {
            return 2;
        }
        return 1;
    }

    private static MediaAction actionForCount(
            int count,
            MediaAction singleAction,
            MediaAction doubleAction,
            MediaAction tripleAction) {
        if (count >= 3) {
            return tripleAction;
        }
        if (count == 2) {
            return doubleAction;
        }
        return singleAction;
    }
}
