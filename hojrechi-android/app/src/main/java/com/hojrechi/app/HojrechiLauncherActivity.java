package com.hojrechi.app;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.browser.trusted.TrustedWebActivityDisplayMode;
import com.google.androidbrowserhelper.trusted.LauncherActivity;

/**
 * Branded 1.5 second splash in the *existing* TWA launcher. Unlike a separate
 * SplashActivity, this avoids Android 12's duplicate system/native splash.
 * Launch remains a real TWA; no WebView or extra browser toolbar is introduced.
 */
public final class HojrechiLauncherActivity extends LauncherActivity {
    private static final String TAG = "HojrechiSplash";
    private static final long MIN_SPLASH_MS = 1500L;
    private static final int BRAND_ORANGE = Color.rgb(249, 91, 0);
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable launch = this::proceedToTwa;
    private long splashBegan;
    private boolean launched;

    @Override
    protected boolean shouldLaunchImmediately() {
        // Supported extension point in Android Browser Helper's LauncherActivity.
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // The base activity can immediately finish when the TWA is already open.
        if (isFinishing()) return;

        splashBegan = SystemClock.uptimeMillis();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(BRAND_ORANGE);
        getWindow().setNavigationBarColor(BRAND_ORANGE);
        if (Build.VERSION.SDK_INT >= 28) {
            WindowManager.LayoutParams attributes = getWindow().getAttributes();
            attributes.layoutInDisplayCutoutMode = (Build.VERSION.SDK_INT >= 30)
                ? WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
                : WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            getWindow().setAttributes(attributes);
        }

        ImageView brand = new ImageView(this);
        brand.setBackgroundColor(BRAND_ORANGE);
        brand.setImageResource(R.drawable.twa_splash);
        brand.setScaleType(ImageView.ScaleType.FIT_CENTER);
        brand.setContentDescription("حجره‌چی");
        setContentView(brand);

        if (Build.VERSION.SDK_INT >= 30) {
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
        }
        Log.i(TAG, "Branded splash visible");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFinishing() || launched || splashBegan == 0L) return;
        long elapsed = SystemClock.uptimeMillis() - splashBegan;
        handler.removeCallbacks(launch);
        handler.postDelayed(launch, Math.max(0L, MIN_SPLASH_MS - elapsed));
    }

    private void proceedToTwa() {
        if (isFinishing() || isDestroyed() || launched) return;
        launched = true;
        Log.i(TAG, "Opening trusted web activity after " +
            (SystemClock.uptimeMillis() - splashBegan) + " ms");
        launchTwa();
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(launch);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

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
