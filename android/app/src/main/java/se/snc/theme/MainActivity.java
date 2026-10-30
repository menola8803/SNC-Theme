package se.snc.theme;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.app.LocaleManager;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
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

        col.addView(label(R.string.gold_title), lp(dp(32), 0));
        TextView goldIntro = new TextView(this);
        goldIntro.setText(R.string.gold_intro);
        goldIntro.setTextColor(0xFFC9CCD3);
        goldIntro.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        goldIntro.setLineSpacing(0, 1.2f);
        col.addView(goldIntro, lp(dp(8), 0));
        col.addView(button(R.string.btn_live_home, true,
                () -> openLive(SncWallpaperService.Home.class, R.string.live_hint_home)), lp(dp(16), dp(52)));
        col.addView(button(R.string.btn_live_lock, true,
                () -> openLive(SncWallpaperService.Lock.class, R.string.live_hint_lock)), lp(dp(12), dp(52)));
        col.addView(button(R.string.btn_style, false, this::openStyle), lp(dp(12), dp(52)));

        col.addView(label(R.string.static_title), lp(dp(32), 0));
        col.addView(button(R.string.btn_both, false,
                () -> setWallpapers(true, true, R.string.done_both)), lp(dp(12), dp(52)));
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

        col.addView(label(R.string.root_title), lp(dp(36), 0));
        TextView rootIntro = new TextView(this);
        rootIntro.setText(R.string.root_intro);
        rootIntro.setTextColor(0xFFC9CCD3);
        rootIntro.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        rootIntro.setLineSpacing(0, 1.2f);
        col.addView(rootIntro, lp(dp(8), 0));
        col.addView(button(R.string.btn_root_apply, true, () -> RootTheme.choose(this)), lp(dp(16), dp(52)));
        col.addView(button(R.string.btn_root_restore, false, () -> RootTheme.restore(this)), lp(dp(12), dp(52)));
        col.addView(button(R.string.btn_diag, false, () -> RootTheme.diagnostics(this)), lp(dp(12), dp(52)));

        col.addView(label(R.string.dialer_title), lp(dp(36), 0));
        col.addView(button(R.string.btn_dialer_compact, true, () -> DialerTweak.apply(this, true)), lp(dp(12), dp(52)));
        col.addView(button(R.string.btn_dialer_reset, false, () -> DialerTweak.apply(this, false)), lp(dp(12), dp(52)));

        col.addView(label(R.string.settings_title), lp(dp(36), 0));
        Button lang = button(R.string.btn_language, false, this::chooseLanguage);
        lang.setText(getString(R.string.btn_language, currentLanguageName()));
        col.addView(lang, lp(dp(12), dp(52)));
        col.addView(button(R.string.btn_updates, false,
                () -> startActivity(new Intent(this, UpdateActivity.class))), lp(dp(12), dp(52)));

        TextView version = new TextView(this);
        String vn = "";
        try {
            vn = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception ignored) {
        }
        version.setText(getString(R.string.version_label, vn));
        version.setTextColor(0xFF8F9AAD);
        version.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        version.setGravity(Gravity.CENTER);
        col.addView(version, lp(dp(14), 0));

        setContentView(scroll);
        Updater.check(this, true);
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

    private void openLive(Class<?> service, int hint) {
        Intent i = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
        i.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                new android.content.ComponentName(this, service));
        try {
            startActivity(i);
            Toast.makeText(this, hint, Toast.LENGTH_LONG).show();
        } catch (ActivityNotFoundException e) {
            try {
                startActivity(new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER));
            } catch (ActivityNotFoundException e2) {
                Toast.makeText(this, R.string.failed, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openStyle() {
        // Systemets egen sida "Bakgrund och utseende" (WallpaperPicker2), inte Galleri.
        Intent[] tries = {
                new Intent(Intent.ACTION_SET_WALLPAPER).setPackage("com.android.wallpaper"),
                new Intent(Intent.ACTION_MAIN).setComponent(new android.content.ComponentName(
                        "com.android.wallpaper",
                        "com.android.customization.picker.CustomizationPickerActivity")),
                new Intent(Intent.ACTION_SET_WALLPAPER).setPackage("com.android.wallpaperpicker"),
                new Intent(Settings.ACTION_DISPLAY_SETTINGS)
        };
        for (Intent i : tries) {
            try {
                startActivity(i);
                return;
            } catch (ActivityNotFoundException | SecurityException ignored) {
            }
        }
    }

    private TextView label(int text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTypeface(cinzel);
        t.setTextColor(GOLD);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        t.setLetterSpacing(0.15f);
        return t;
    }

    private String currentLanguageName() {
        if (Build.VERSION.SDK_INT >= 33) {
            LocaleList l = getSystemService(LocaleManager.class).getApplicationLocales();
            if (!l.isEmpty()) {
                return "sv".equals(l.get(0).getLanguage()) ? "Svenska" : "English";
            }
        }
        return getString(R.string.lang_system);
    }

    private void chooseLanguage() {
        String[] names = {getString(R.string.lang_system), "Svenska", "English"};
        new android.app.AlertDialog.Builder(this)
                .setTitle(R.string.lang_title)
                .setItems(names, (d, which) -> {
                    if (Build.VERSION.SDK_INT < 33) return;
                    LocaleList list = which == 0 ? LocaleList.getEmptyLocaleList()
                            : LocaleList.forLanguageTags(which == 1 ? "sv" : "en");
                    getSystemService(LocaleManager.class).setApplicationLocales(list);
                })
                .show();
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
