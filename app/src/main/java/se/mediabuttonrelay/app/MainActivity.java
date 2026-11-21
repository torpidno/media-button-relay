package se.mediabuttonrelay.app;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.InputDevice;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Settings and live diagnostic UI for Media Button Relay.
 */
public class MainActivity extends AppCompatActivity implements MediaRelayService.EventListener {

    private RelayPrefs prefs;
    private TextView tvStatusBadge;
    private TextView tvDetectedRemotes;
    private MaterialButton btnAccessibilitySettings;
    private MaterialSwitch switchRelayEnabled;
    private Spinner spinnerSingleAction;
    private Spinner spinnerDoubleAction;
    private Spinner spinnerTripleAction;
    private TextView tvWindowLabel;
    private Slider sliderWindowMs;
    private MaterialSwitch switchTriggerOnRelease;
    private MaterialSwitch switchExternalOnly;
    private TextView tvLiveTest;
    private boolean bindingUi = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = new RelayPrefs(this);
        applyEdgeToEdgeInsets(findViewById(R.id.rootContainer));
        bindViews();
        configureSpinners();
        configureListeners();
        syncUiFromPrefs();
    }

    @Override
    protected void onStart() {
        super.onStart();
        MediaRelayService.setEventListener(this);
        refreshStatusUi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshStatusUi();
    }

    @Override
    protected void onStop() {
        MediaRelayService.setEventListener(null);
        super.onStop();
    }

    @Override
    public void onServiceStateChanged(boolean connected) {
        runOnUiThread(this::refreshStatusUi);
    }

    @Override
    public void onRelayEvent(String keyName, String deviceName, int clickCount, MediaAction action) {
        runOnUiThread(() -> tvLiveTest.setText(getString(
                R.string.test_event_format, keyName, deviceName, clickCount, action.getLabel())));
    }

    private void applyEdgeToEdgeInsets(View root) {
        final int baseLeft = root.getPaddingLeft();
        final int baseTop = root.getPaddingTop();
        final int baseRight = root.getPaddingRight();
        final int baseBottom = root.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    baseLeft + bars.left,
                    baseTop + bars.top,
                    baseRight + bars.right,
                    baseBottom + bars.bottom);
            return windowInsets;
        });
    }

    private void bindViews() {
        tvStatusBadge = findViewById(R.id.tvStatusBadge);
        tvDetectedRemotes = findViewById(R.id.tvDetectedRemotes);
        btnAccessibilitySettings = findViewById(R.id.btnAccessibilitySettings);
        switchRelayEnabled = findViewById(R.id.switchRelayEnabled);
        spinnerSingleAction = findViewById(R.id.spinnerSingleAction);
        spinnerDoubleAction = findViewById(R.id.spinnerDoubleAction);
        spinnerTripleAction = findViewById(R.id.spinnerTripleAction);
        tvWindowLabel = findViewById(R.id.tvWindowLabel);
        sliderWindowMs = findViewById(R.id.sliderWindowMs);
        switchTriggerOnRelease = findViewById(R.id.switchTriggerOnRelease);
        switchExternalOnly = findViewById(R.id.switchExternalOnly);
        tvLiveTest = findViewById(R.id.tvLiveTest);
    }

    private void configureSpinners() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, MediaAction.displayLabels());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerSingleAction.setAdapter(adapter);
        spinnerDoubleAction.setAdapter(adapter);
        spinnerTripleAction.setAdapter(adapter);

        bindSpinnerListener(spinnerSingleAction, prefs::setSingleAction);
        bindSpinnerListener(spinnerDoubleAction, prefs::setDoubleAction);
        bindSpinnerListener(spinnerTripleAction, prefs::setTripleAction);
    }

    private void bindSpinnerListener(Spinner spinner, ActionConsumer onSelect) {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (bindingUi) {
                    return;
                }
                MediaAction[] actions = MediaAction.values();
                if (position >= 0 && position < actions.length) {
                    onSelect.accept(actions[position]);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void configureListeners() {
        btnAccessibilitySettings.setOnClickListener(v -> {
            if (isRelayServiceEnabled()) {
                openAccessibilitySettings();
            } else {
                showProminentDisclosure(true);
            }
        });

        findViewById(R.id.btnPrivacyInfo).setOnClickListener(v -> showProminentDisclosure(false));

        switchRelayEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!bindingUi) {
                prefs.setRelayEnabled(isChecked);
                refreshStatusUi();
            }
        });

        sliderWindowMs.addOnChangeListener((slider, value, fromUser) -> {
            int ms = Math.round(value);
            tvWindowLabel.setText(getString(R.string.label_window_ms, ms));
            if (fromUser) {
                prefs.setMultiClickWindowMs(ms);
            }
        });

        switchTriggerOnRelease.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!bindingUi) {
                prefs.setTriggerOnRelease(isChecked);
            }
        });

        switchExternalOnly.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!bindingUi) {
                prefs.setExternalOnly(isChecked);
            }
        });
    }

    private void syncUiFromPrefs() {
        bindingUi = true;
        try {
            spinnerSingleAction.setSelection(prefs.getSingleAction().ordinal());
            spinnerDoubleAction.setSelection(prefs.getDoubleAction().ordinal());
            spinnerTripleAction.setSelection(prefs.getTripleAction().ordinal());

            int windowMs = prefs.getMultiClickWindowMs();
            tvWindowLabel.setText(getString(R.string.label_window_ms, windowMs));
            sliderWindowMs.setValue(windowMs);

            switchRelayEnabled.setChecked(prefs.isRelayEnabled());
            switchTriggerOnRelease.setChecked(prefs.isTriggerOnRelease());
            switchExternalOnly.setChecked(prefs.isExternalOnly());
        } finally {
            bindingUi = false;
        }
    }

    private void refreshStatusUi() {
        boolean serviceOn = isRelayServiceEnabled();
        boolean relayOn = prefs.isRelayEnabled();

        if (!serviceOn) {
            applyBadge(R.string.status_inactive, R.color.status_inactive, R.color.status_inactive_bg);
            btnAccessibilitySettings.setText(R.string.btn_enable_service);
            tvLiveTest.setText(R.string.test_waiting_inactive);
        } else if (!relayOn) {
            applyBadge(R.string.status_paused, R.color.status_paused, R.color.status_paused_bg);
            btnAccessibilitySettings.setText(R.string.btn_open_settings);
            tvLiveTest.setText(R.string.test_waiting_inactive);
        } else {
            applyBadge(R.string.status_active, R.color.status_active, R.color.status_active_bg);
            btnAccessibilitySettings.setText(R.string.btn_open_settings);
            if (tvLiveTest.getText().toString().equals(getString(R.string.test_waiting_inactive))) {
                tvLiveTest.setText(R.string.test_waiting_active);
            }
        }

        List<String> remotes = findExternalRemotes();
        if (remotes.isEmpty()) {
            tvDetectedRemotes.setText(R.string.remotes_none);
        } else {
            tvDetectedRemotes.setText(getString(R.string.remotes_found, TextUtils.join(", ", remotes)));
        }
    }

    private void applyBadge(int textRes, int textColorRes, int bgColorRes) {
        tvStatusBadge.setText(textRes);
        tvStatusBadge.setTextColor(ContextCompat.getColor(this, textColorRes));
        tvStatusBadge.setBackgroundTintList(
                ColorStateList.valueOf(ContextCompat.getColor(this, bgColorRes)));
    }

    private boolean isRelayServiceEnabled() {
        if (MediaRelayService.isServiceConnected()) {
            return true;
        }
        AccessibilityManager am =
                (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (am == null) {
            return false;
        }
        List<AccessibilityServiceInfo> enabled =
                am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
        String expectedPkg = getPackageName();
        String expectedCls = MediaRelayService.class.getName();
        for (AccessibilityServiceInfo info : enabled) {
            if (info.getResolveInfo() != null && info.getResolveInfo().serviceInfo != null) {
                String pkg = info.getResolveInfo().serviceInfo.packageName;
                String cls = info.getResolveInfo().serviceInfo.name;
                if (expectedPkg.equals(pkg) && expectedCls.equals(cls)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static List<String> findExternalRemotes() {
        Set<String> names = new LinkedHashSet<>();
        for (int id : InputDevice.getDeviceIds()) {
            InputDevice dev = InputDevice.getDevice(id);
            if (dev != null && !dev.isVirtual() && MediaRelayService.isTargetExternalDevice(dev)) {
                String name = dev.getName();
                if (name != null && !name.trim().isEmpty()) {
                    names.add(name.trim());
                }
            }
        }
        return new ArrayList<>(names);
    }

    /**
     * Shows Google Play's required prominent in-app disclosure before routing
     * the user to Android's Accessibility Settings screen.
     */
    private void showProminentDisclosure(boolean allowOpenSettings) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.disclosure_title)
                .setMessage(R.string.disclosure_message);

        if (allowOpenSettings) {
            builder.setNegativeButton(R.string.disclosure_decline, (dialog, which) -> dialog.dismiss())
                    .setPositiveButton(R.string.disclosure_confirm, (dialog, which) -> openAccessibilitySettings());
        } else {
            builder.setPositiveButton(R.string.disclosure_close, (dialog, which) -> dialog.dismiss());
        }
        builder.show();
    }

    private void openAccessibilitySettings() {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private interface ActionConsumer {
        void accept(MediaAction action);
    }
}
