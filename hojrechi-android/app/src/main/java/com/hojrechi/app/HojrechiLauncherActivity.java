package com.hojrechi.app;

import android.os.Build;
import android.view.WindowManager;

import androidx.browser.trusted.TrustedWebActivityDisplayMode;
import com.google.androidbrowserhelper.trusted.LauncherActivity;

/**
 * The actual TWA is drawn by the selected browser, not our launcher window.
 * Passing an explicit cutout mode to the browser fixes the letterboxed black
 * band that the stock "sticky-immersive" manifest metadata produces: its
 * default cutout mode is LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT.
 *
 * The website already supplies viewport-fit=cover and its own cream theme.
 * CSS safe-area-inset-top must protect tappable header controls from the camera.
 */
public final class HojrechiLauncherActivity extends LauncherActivity {
    @Override
    protected TrustedWebActivityDisplayMode getDisplayMode() {
        int cutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT;
        if (Build.VERSION.SDK_INT >= 30) {
            cutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;
        } else if (Build.VERSION.SDK_INT >= 28) {
            cutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        return new TrustedWebActivityDisplayMode.ImmersiveMode(true, cutoutMode);
    }
}
