package se.snc.theme;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/** Startskärm: ställ in bakgrunder, välj skärmsläckare och förhandsgranska. */
public class MainActivity extends Activity {

    private static final int NAVY = 0xFF011634;
    private static final int SURFACE = 0xFF0C2550;
    private static final int GOLD = 0xFFDDAE54;
    private static final int WHITE = 0xFFF5F2EA;

    private Typeface cinzel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cinzel = getResources().getFont(R.font.cinzel);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(NAVY);
        scroll.setFitsSystemWindows(true);

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setGravity(Gravity.CENTER_HORIZONTAL);
        col.setPadding(dp(24), dp(32), dp(24), dp(32));
        scroll.addView(col);

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.logo_full);
        logo.setAdjustViewBounds(true);
        logo.setContentDescription(getString(R.string.company));
        col.addView(logo, new LinearLayout.LayoutParams(dp(280), LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView intro = new TextView(this);
        intro.setText(R.string.intro);
        intro.setTextColor(0xFFC9CCD3);
        intro.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        intro.setGravity(Gravity.CENTER);
        intro.setLineSpacing(0, 1.25f);
        col.addView(intro, lp(dp(28), 0));

        col.addView(button(R.string.btn_both, true,
                () -> setWallpapers(true, true, R.string.done_both)), lp(dp(28), dp(52)));
        col.addView(button(R.string.btn_home, false,
                () -> setWallpapers(true, false, R.string.done_home)), lp(dp(12), dp(52)));
        col.addView(button(R.string.btn_lock, false,
                () -> setWallpapers(false, true, R.string.done_lock)), lp(dp(12), dp(52)));
        col.addView(button(R.string.btn_dream_settings, false, this::openDreamSettings), lp(dp(28), dp(52)));
        col.addView(button(R.string.btn_preview, false,
                () -> startActivity(new Intent(this, SaverPreviewActivity.class))), lp(dp(12), dp(52)));

        TextView hint = new TextView(this);
        hint.setText(R.string.widget_hint);
        hint.setTextColor(0xFFC9CCD3);
        hint.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        hint.setGravity(Gravity.CENTER);
        col.addView(hint, lp(dp(28), 0));

        TextView slogan = new TextView(this);
        slogan.setText(R.string.slogan);
        slogan.setTypeface(cinzel);
        slogan.setTextColor(GOLD);
        slogan.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        slogan.setLetterSpacing(0.15f);
        slogan.setGravity(Gravity.CENTER);
        col.addView(slogan, lp(dp(36), 0));

        setContentView(scroll);
    }

    private void setWallpapers(boolean home, boolean lock, int doneMsg) {
        new Thread(() -> {
            boolean ok = true;
            try {
                WallpaperManager wm = WallpaperManager.getInstance(this);
                if (home) wm.setResource(R.drawable.wp_home, WallpaperManager.FLAG_SYSTEM);
                if (lock) wm.setResource(R.drawable.wp_lock, WallpaperManager.FLAG_LOCK);
            } catch (Exception e) {
                ok = false;
            }
            final boolean success = ok;
            runOnUiThread(() -> Toast.makeText(this,
                    success ? doneMsg : R.string.failed, Toast.LENGTH_SHORT).show());
        }).start();
    }

    private void openDreamSettings() {
        try {
            startActivity(new Intent(Settings.ACTION_DREAM_SETTINGS));
            Toast.makeText(this, R.string.dream_hint, Toast.LENGTH_LONG).show();
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Settings.ACTION_DISPLAY_SETTINGS));
        }
    }

    private Button button(int text, boolean primary, Runnable action) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        b.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        b.setTextColor(primary ? NAVY : WHITE);
        b.setStateListAnimator(null);
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(26));
        bg.setColor(primary ? GOLD : SURFACE);
        if (!primary) bg.setStroke(dp(1), 0x99DDAE54);
        b.setBackground(bg);
        b.setOnClickListener(v -> action.run());
        return b;
    }

    private LinearLayout.LayoutParams lp(int top, int height) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                height == 0 ? LinearLayout.LayoutParams.WRAP_CONTENT : height);
        p.topMargin = top;
        return p;
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
