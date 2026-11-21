package se.mediabuttonrelay.app;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ClickDetectorTest {

    private static final class FakeScheduler implements ClickDetector.Scheduler {
        Runnable pendingRunnable;
        long lastDelayMs = -1;
        int scheduleCount = 0;

        @Override
        public void postDelayed(Runnable r, long delayMs) {
            pendingRunnable = r;
            lastDelayMs = delayMs;
            scheduleCount++;
        }

        @Override
        public void removeCallbacks(Runnable r) {
            if (pendingRunnable == r) {
                pendingRunnable = null;
            }
        }

        void fireTimeout() {
            if (pendingRunnable != null) {
                Runnable r = pendingRunnable;
                pendingRunnable = null;
                r.run();
            }
        }
    }

    private FakeScheduler scheduler;
    private List<String> fired;
    private ClickDetector detector;

    @Before
    public void setUp() {
        scheduler = new FakeScheduler();
        fired = new ArrayList<>();
        detector = new ClickDetector(scheduler, (count, action) ->
                fired.add(count + ":" + action.name()));
    }

    @Test
    public void singleClickFiresPlayPauseAfterTimeoutWhenDoubleConfigured() {
        detector.onClick(MediaAction.PLAY_PAUSE, MediaAction.NEXT, MediaAction.NONE, 650);

        assertEquals(0, fired.size());
        assertEquals(650L, scheduler.lastDelayMs);

        scheduler.fireTimeout();
        assertEquals(List.of("1:PLAY_PAUSE"), fired);
    }

    @Test
    public void doubleClickFiresNextImmediatelyWhenTripleIsNone() {
        detector.onClick(MediaAction.PLAY_PAUSE, MediaAction.NEXT, MediaAction.NONE, 650);
        detector.onClick(MediaAction.PLAY_PAUSE, MediaAction.NEXT, MediaAction.NONE, 650);

        assertEquals(List.of("2:NEXT"), fired);
        assertEquals(null, scheduler.pendingRunnable);
    }

    @Test
    public void singleClickFiresImmediatelyWhenDoubleAndTripleAreNone() {
        detector.onClick(MediaAction.PLAY_PAUSE, MediaAction.NONE, MediaAction.NONE, 650);

        assertEquals(List.of("1:PLAY_PAUSE"), fired);
        assertEquals(0, scheduler.scheduleCount);
    }

    @Test
    public void doubleAndTripleClicksWhenTripleConfigured() {
        // Two clicks wait for window, then fire double action
        detector.onClick(MediaAction.PLAY_PAUSE, MediaAction.NEXT, MediaAction.PREVIOUS, 650);
        detector.onClick(MediaAction.PLAY_PAUSE, MediaAction.NEXT, MediaAction.PREVIOUS, 650);
        assertEquals(0, fired.size());
        scheduler.fireTimeout();
        assertEquals(List.of("2:NEXT"), fired);

        fired.clear();

        // Three clicks fire triple action immediately without waiting for timeout
        detector.onClick(MediaAction.PLAY_PAUSE, MediaAction.NEXT, MediaAction.PREVIOUS, 650);
        detector.onClick(MediaAction.PLAY_PAUSE, MediaAction.NEXT, MediaAction.PREVIOUS, 650);
        detector.onClick(MediaAction.PLAY_PAUSE, MediaAction.NEXT, MediaAction.PREVIOUS, 650);
        assertEquals(List.of("3:PREVIOUS"), fired);
        assertEquals(null, scheduler.pendingRunnable);
    }

    @Test
    public void resetCancelsPendingClickCleanly() {
        detector.onClick(MediaAction.PLAY_PAUSE, MediaAction.NEXT, MediaAction.NONE, 650);
        detector.reset();
        scheduler.fireTimeout();

        assertEquals(0, fired.size());
    }
}
