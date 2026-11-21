package se.mediabuttonrelay.app;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

import java.lang.ref.WeakReference;
import java.util.Locale;

/**
 * Intercepts hardware volume button events from external Bluetooth remotes
 * and dispatches user-configured media playback keys without modifying UI content.
 */
public class MediaRelayService extends AccessibilityService {

    private static final int CHEERPLAY_VID = 1452; // 0x05AC
    private static final int CHEERPLAY_PID = 556;  // 0x022C

    public interface EventListener {
        void onServiceStateChanged(boolean connected);
        void onRelayEvent(String keyName, String deviceName, int clickCount, MediaAction action);
    }

    private static volatile boolean serviceConnected = false;
    private static volatile WeakReference<EventListener> uiListenerRef = new WeakReference<>(null);

    private AudioManager audioManager;
    private RelayPrefs prefs;
    private ClickDetector clickDetector;
    private String lastKeyName = "";
    private String lastDeviceName = "";

    public static boolean isServiceConnected() {
        return serviceConnected;
    }

    public static void setEventListener(EventListener listener) {
        uiListenerRef = new WeakReference<>(listener);
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        prefs = new RelayPrefs(this);

        final Handler handler = new Handler(Looper.getMainLooper());
        ClickDetector.Scheduler scheduler = new ClickDetector.Scheduler() {
            @Override
            public void postDelayed(Runnable r, long delayMs) {
                handler.postDelayed(r, delayMs);
            }

            @Override
            public void removeCallbacks(Runnable r) {
                handler.removeCallbacks(r);
            }
        };

        clickDetector = new ClickDetector(scheduler, (clickCount, action) -> {
            if (action != MediaAction.NONE) {
                sendMediaKey(action.getKeyCode());
            }
            notifyRelayEvent(lastKeyName, lastDeviceName, clickCount, action);
        });

        serviceConnected = true;
        notifyServiceStateChanged(true);
    }

    @Override
    public boolean onUnbind(android.content.Intent intent) {
        tearDown();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        tearDown();
        super.onDestroy();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Intentionally unused: this service only filters hardware key events.
    }

    @Override
    public void onInterrupt() {
        if (clickDetector != null) {
            clickDetector.reset();
        }
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        if (prefs == null || clickDetector == null || !prefs.isRelayEnabled()) {
            return super.onKeyEvent(event);
        }

        int keyCode = event.getKeyCode();
        if (keyCode != KeyEvent.KEYCODE_VOLUME_UP && keyCode != KeyEvent.KEYCODE_VOLUME_DOWN) {
            return super.onKeyEvent(event);
        }

        InputDevice device = event.getDevice();
        if (prefs.isExternalOnly() && !isTargetExternalDevice(device)) {
            return super.onKeyEvent(event);
        }

        int triggerAction = prefs.isTriggerOnRelease() ? KeyEvent.ACTION_UP : KeyEvent.ACTION_DOWN;
        if (event.getAction() == triggerAction && event.getRepeatCount() == 0) {
            lastKeyName = keyDisplayName(keyCode);
            lastDeviceName = deviceDisplayName(device);
            clickDetector.onClick(
                    prefs.getSingleAction(),
                    prefs.getDoubleAction(),
                    prefs.getTripleAction(),
                    prefs.getMultiClickWindowMs());
        }

        // Consume both ACTION_DOWN and ACTION_UP (plus repeats) so system volume does not change.
        return true;
    }

    static boolean isTargetExternalDevice(InputDevice device) {
        if (device == null) {
            return false;
        }
        if (device.isExternal()) {
            return true;
        }
        String name = device.getName();
        if (name != null && name.toLowerCase(Locale.ROOT).contains("cheer")) {
            return true;
        }
        return device.getVendorId() == CHEERPLAY_VID && device.getProductId() == CHEERPLAY_PID;
    }

    private void sendMediaKey(int keyCode) {
        if (keyCode == KeyEvent.KEYCODE_UNKNOWN) {
            return;
        }
        if (audioManager == null) {
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        }
        if (audioManager != null) {
            long now = SystemClock.uptimeMillis();
            KeyEvent down = new KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 0);
            KeyEvent up = new KeyEvent(now, now, KeyEvent.ACTION_UP, keyCode, 0);
            audioManager.dispatchMediaKeyEvent(down);
            audioManager.dispatchMediaKeyEvent(up);
        }
    }

    private void tearDown() {
        serviceConnected = false;
        if (clickDetector != null) {
            clickDetector.reset();
        }
        notifyServiceStateChanged(false);
    }

    private static void notifyServiceStateChanged(boolean connected) {
        EventListener listener = uiListenerRef.get();
        if (listener != null) {
            listener.onServiceStateChanged(connected);
        }
    }

    private static void notifyRelayEvent(
            String keyName, String deviceName, int clickCount, MediaAction action) {
        EventListener listener = uiListenerRef.get();
        if (listener != null) {
            listener.onRelayEvent(keyName, deviceName, clickCount, action);
        }
    }

    private static String deviceDisplayName(InputDevice device) {
        if (device == null || device.getName() == null || device.getName().isEmpty()) {
            return "External Remote";
        }
        return device.getName();
    }

    private static String keyDisplayName(int keyCode) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            return "Volume Up";
        }
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            return "Volume Down";
        }
        return KeyEvent.keyCodeToString(keyCode);
    }
}
