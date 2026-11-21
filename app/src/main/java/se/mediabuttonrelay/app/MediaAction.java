package se.mediabuttonrelay.app;

import android.view.KeyEvent;

/**
 * Supported media playback actions triggered by intercepted remote button presses.
 */
public enum MediaAction {
    PLAY_PAUSE("Play / Pause", KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE),
    NEXT("Next Track", KeyEvent.KEYCODE_MEDIA_NEXT),
    PREVIOUS("Previous Track", KeyEvent.KEYCODE_MEDIA_PREVIOUS),
    STOP("Stop", KeyEvent.KEYCODE_MEDIA_STOP),
    MUTE("Mute / Unmute", KeyEvent.KEYCODE_VOLUME_MUTE),
    NONE("Do Nothing", KeyEvent.KEYCODE_UNKNOWN);

    private final String label;
    private final int keyCode;

    MediaAction(String label, int keyCode) {
        this.label = label;
        this.keyCode = keyCode;
    }

    public String getLabel() {
        return label;
    }

    public int getKeyCode() {
        return keyCode;
    }

    /**
     * Resolves a stored enum name, returning {@code defaultAction} when missing or invalid.
     */
    public static MediaAction fromName(String name, MediaAction defaultAction) {
        if (name == null || name.isEmpty()) {
            return defaultAction;
        }
        try {
            return MediaAction.valueOf(name);
        } catch (IllegalArgumentException ignored) {
            return defaultAction;
        }
    }

    /**
     * Returns user-facing action labels in enum order for dropdown adapters.
     */
    public static String[] displayLabels() {
        MediaAction[] all = values();
        String[] labels = new String[all.length];
        for (int i = 0; i < all.length; i++) {
            labels[i] = all[i].label;
        }
        return labels;
    }
}
