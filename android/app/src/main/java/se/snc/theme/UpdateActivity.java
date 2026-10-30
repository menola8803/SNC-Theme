package se.snc.theme;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;

/** Visar installerad och senaste version samt en lista över publicerade versioner. */
public class UpdateActivity extends Activity {

    private static final int NAVY = 0xFF011634;
    private static final int SURFACE = 0xFF0C2550;
    private static final int GOLD = 0xFFDDAE54;
    private static final int WHITE = 0xFFF5F2EA;
    private static final int MUTED = 0xFFC9CCD3;

    private Typeface cinzel;
    private LinearLayout col;
    private long installedCode;
    private String installedName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cinzel = getResources().getFont(R.font.cinzel);
        try {
            android.content.pm.PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            installedCode = pi.getLongVersionCode();
            installedName = "v" + pi.versionName;
        } catch (Exception ignored) {
        }

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(NAVY);
        scroll.setFitsSystemWindows(true);
        col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(dp(24), dp(32), dp(24), dp(32));
        scroll.addView(col);
        setContentView(scroll);
        load();
    }

    private void load() {
        col.removeAllViews();
        col.addView(text(getString(R.string.upd_screen_title), 15, GOLD, cinzel), lp(0, 0));
        col.addView(text(getString(R.string.upd_installed, installedName), 18, WHITE, null), lp(dp(20), 0));
        col.addView(text(getString(R.string.upd_loading), 14, MUTED, null), lp(dp(12), 0));
        new Thread(() -> {
            try {
                List<Updater.Release> list = Updater.fetchReleases();
                runOnUiThread(() -> show(list));
            } catch (Exception e) {
                String reason = e.getClass().getSimpleName()
                        + (e.getMessage() != null ? ": " + e.getMessage() : "");
                runOnUiThread(() -> showError(reason));
            }
        }).start();
    }

    private void show(List<Updater.Release> list) {
        if (isFinishing()) return;
        col.removeAllViews();
        col.addView(text(getString(R.string.upd_screen_title), 15, GOLD, cinzel), lp(0, 0));
        col.addView(text(getString(R.string.upd_installed, installedName), 18, WHITE, null), lp(dp(20), 0));

        Updater.Release latest = null;
        for (Updater.Release r : list) {
            if (latest == null || r.code > latest.code) latest = r;
        }
        if (latest != null) {
            col.addView(text(getString(R.string.upd_latest_label, latest.tag), 18, WHITE, null), lp(dp(6), 0));
            if (latest.code > installedCode) {
                final Updater.Release l = latest;
                col.addView(button(getString(R.string.upd_install_btn, l.tag), true,
                        () -> Updater.install(this, Updater.downloadUrl(l.tag))), lp(dp(18), dp(52)));
            } else {
                col.addView(text("✓ " + getString(R.string.upd_up_to_date), 15, GOLD, null), lp(dp(12), 0));
            }
        }

        col.addView(text(getString(R.string.upd_history), 13, GOLD, cinzel), lp(dp(32), 0));
        for (Updater.Release r : list) col.addView(card(r), lp(dp(12), 0));

        col.addView(button(getString(R.string.upd_open_github), false, () -> startActivity(
                new Intent(Intent.ACTION_VIEW, Uri.parse(Updater.releasePage())))), lp(dp(28), dp(52)));
    }

    private void showError(String reason) {
        if (isFinishing()) return;
        col.removeAllViews();
        col.addView(text(getString(R.string.upd_screen_title), 15, GOLD, cinzel), lp(0, 0));
        col.addView(text(getString(R.string.upd_installed, installedName), 18, WHITE, null), lp(dp(20), 0));
        col.addView(text(getString(R.string.upd_error, reason), 14, MUTED, null), lp(dp(12), 0));
        col.addView(button(getString(R.string.upd_retry), true, this::load), lp(dp(18), dp(52)));
        col.addView(button(getString(R.string.upd_open_github), false, () -> startActivity(
                new Intent(Intent.ACTION_VIEW, Uri.parse(Updater.releasePage())))), lp(dp(12), dp(52)));
    }

    private LinearLayout card(Updater.Release r) {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(16), dp(14), dp(16), dp(14));
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(18));
        bg.setColor(SURFACE);
        if (r.code == installedCode) bg.setStroke(dp(1), GOLD);
        c.setBackground(bg);

        String badge = r.code == installedCode ? "  ·  " + getString(R.string.upd_badge_installed)
                : r.code > installedCode ? "  ·  " + getString(R.string.upd_badge_new) : "";
        TextView head = text(r.tag + badge, 16, r.code >= installedCode ? GOLD : WHITE, null);
        head.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        c.addView(head);
        if (r.date != null && !r.date.isEmpty()) c.addView(text(r.date, 12, 0xFF8F9AAD, null), lp(dp(2), 0));
        if (r.notes != null && !r.notes.isEmpty()) c.addView(text(r.notes, 14, MUTED, null), lp(dp(8), 0));
        return c;
    }

    private TextView text(String s, int sp, int color, Typeface tf) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextColor(color);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        t.setLineSpacing(0, 1.2f);
        if (tf != null) {
            t.setTypeface(tf);
            t.setLetterSpacing(0.15f);
        }
        return t;
    }

    private Button button(String label, boolean primary, Runnable action) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        b.setTextColor(primary ? NAVY : WHITE);
        b.setStateListAnimator(null);
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(26));
        bg.setColor(primary ? GOLD : SURFACE);
        if (!primary) bg.setStroke(dp(1), 0x99DDAE54);
        b.setBackground(bg);
        b.setGravity(Gravity.CENTER);
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
