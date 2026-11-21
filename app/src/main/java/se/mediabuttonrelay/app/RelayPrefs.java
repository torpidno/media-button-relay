package se.mediabuttonrelay.app;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Persists local user preferences for button mappings, timing, and filtering.
 */
public final class RelayPrefs {

    public static final String PREFS_NAME = "relay_prefs";
    public static final int MIN_WINDOW_MS = 250;
    public static final int MAX_WINDOW_MS = 1000;
    public static final int DEFAULT_WINDOW_MS = 650;

    private static final String KEY_SINGLE_ACTION = "single_action";
    private static final String KEY_DOUBLE_ACTION = "double_action";
    private static final String KEY_TRIPLE_ACTION = "triple_action";
    private static final String KEY_WINDOW_MS = "multi_click_window_ms";
    private static final String KEY_TRIGGER_ON_RELEASE = "trigger_on_release";
    private static final String KEY_EXTERNAL_ONLY = "external_only";
    private static final String KEY_RELAY_ENABLED = "relay_enabled";

    private final SharedPreferences prefs;

    public RelayPrefs(Context context) {
        this.prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public MediaAction getSingleAction() {
        return MediaAction.fromName(prefs.getString(KEY_SINGLE_ACTION, null), MediaAction.PLAY_PAUSE);
    }

    public void setSingleAction(MediaAction action) {
        prefs.edit().putString(KEY_SINGLE_ACTION, action.name()).apply();
    }

    public MediaAction getDoubleAction() {
        return MediaAction.fromName(prefs.getString(KEY_DOUBLE_ACTION, null), MediaAction.NEXT);
    }

    public void setDoubleAction(MediaAction action) {
        prefs.edit().putString(KEY_DOUBLE_ACTION, action.name()).apply();
    }

    public MediaAction getTripleAction() {
        return MediaAction.fromName(prefs.getString(KEY_TRIPLE_ACTION, null), MediaAction.NONE);
    }

    public void setTripleAction(MediaAction action) {
        prefs.edit().putString(KEY_TRIPLE_ACTION, action.name()).apply();
    }

    public int getMultiClickWindowMs() {
        int raw = prefs.getInt(KEY_WINDOW_MS, DEFAULT_WINDOW_MS);
        return Math.max(MIN_WINDOW_MS, Math.min(MAX_WINDOW_MS, raw));
    }

    public void setMultiClickWindowMs(int windowMs) {
        int clamped = Math.max(MIN_WINDOW_MS, Math.min(MAX_WINDOW_MS, windowMs));
        prefs.edit().putInt(KEY_WINDOW_MS, clamped).apply();
    }

    public boolean isTriggerOnRelease() {
        return prefs.getBoolean(KEY_TRIGGER_ON_RELEASE, true);
    }

    public void setTriggerOnRelease(boolean enabled) {
        prefs.edit().putBoolean(KEY_TRIGGER_ON_RELEASE, enabled).apply();
    }

    public boolean isExternalOnly() {
        return prefs.getBoolean(KEY_EXTERNAL_ONLY, true);
    }

    public void setExternalOnly(boolean enabled) {
        prefs.edit().putBoolean(KEY_EXTERNAL_ONLY, enabled).apply();
    }

    public boolean isRelayEnabled() {
        return prefs.getBoolean(KEY_RELAY_ENABLED, true);
    }

    public void setRelayEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_RELAY_ENABLED, enabled).apply();
    }
}
