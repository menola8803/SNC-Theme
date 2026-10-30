package se.snc.theme;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Sätter systemets färgtema (Material You) till SNC-guld med root,
 * via inställningen theme_customization_overlay_packages.
 */
public final class RootTheme {

    private static final String KEY = "theme_customization_overlay_packages";
    private static final String PREFS = "snc_root_theme";
    private static final String GOLD = "DDAE54";
    private static final String NAVY = "011634";

    private RootTheme() {}

    /** Låter användaren välja stil och tillämpar den. */
    public static void choose(Activity a) {
        String[] styles = {a.getString(R.string.root_style_navy), a.getString(R.string.root_style_gold)};
        new AlertDialog.Builder(a)
                .setTitle(R.string.root_style_title)
                .setItems(styles, (d, which) -> apply(a, which == 0))
                .show();
    }

    /** @param navy marinblå ytor med guldaccenter, annars guld överallt */
    public static void apply(Activity a, boolean navy) {
        new Thread(() -> {
            String result;
            boolean ok;
            try {
                SharedPreferences p = a.getSharedPreferences(PREFS, Activity.MODE_PRIVATE);
                if (!p.contains("original")) {
                    p.edit().putString("original", su("settings get secure " + KEY).trim()).apply();
                }
                String neutral = navy ? NAVY : GOLD;
                String style = navy ? "VIBRANT" : "TONAL_SPOT";
                String json = "{\"android.theme.customization.system_palette\":\"" + neutral + "\","
                        + "\"android.theme.customization.accent_color\":\"" + GOLD + "\","
                        + "\"android.theme.customization.theme_style\":\"" + style + "\","
                        + "\"android.theme.customization.color_source\":\"preset\","
                        + "\"android.theme.customization.color_both\":\"1\","
                        + "\"android.theme.customization.color_index\":\"-1\","
                        + "\"_applied_timestamp\":" + System.currentTimeMillis() + "}";
                su("settings put secure " + KEY + " '" + json + "'");
                result = su("settings get secure " + KEY).trim();
                ok = result.contains(GOLD);
            } catch (Exception e) {
                result = e.getClass().getSimpleName() + ": " + e.getMessage();
                ok = false;
            }
            show(a, ok ? R.string.root_applied : R.string.root_failed, result);
        }).start();
    }

    public static void restore(Activity a) {
        new Thread(() -> {
            String result;
            try {
                SharedPreferences p = a.getSharedPreferences(PREFS, Activity.MODE_PRIVATE);
                String old = p.getString("original", "null");
                if (old.isEmpty() || "null".equals(old)) {
                    su("settings delete secure " + KEY);
                } else {
                    su("settings put secure " + KEY + " '" + old + "'");
                }
                p.edit().remove("original").apply();
                result = su("settings get secure " + KEY).trim();
            } catch (Exception e) {
                result = e.getClass().getSimpleName() + ": " + e.getMessage();
            }
            show(a, R.string.root_restored, result);
        }).start();
    }

    private static void show(Activity a, int title, String detail) {
        a.runOnUiThread(() -> {
            if (a.isFinishing()) return;
            new AlertDialog.Builder(a)
                    .setTitle(title)
                    .setMessage(a.getString(R.string.root_detail, detail))
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        });
    }

    /** Kör ett kommando som root och returnerar utdata (stdout + stderr). */
    private static String su(String cmd) throws Exception {
        Process p = new ProcessBuilder("su", "-c", cmd).redirectErrorStream(true).start();
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        try (InputStream in = p.getInputStream()) {
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) > 0) bo.write(buf, 0, n);
        }
        int code = p.waitFor();
        String out = bo.toString("UTF-8");
        if (code != 0) throw new Exception("su exit " + code + " " + out.trim());
        return out;
    }
}
