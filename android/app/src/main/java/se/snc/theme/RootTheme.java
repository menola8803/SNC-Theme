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
        android.widget.Toast.makeText(a, R.string.root_working, android.widget.Toast.LENGTH_LONG).show();
        new Thread(() -> {
            String result;
            boolean ok;
            try {
                SharedPreferences p = a.getSharedPreferences(PREFS, Activity.MODE_PRIVATE);
                if (!p.contains("original")) {
                    p.edit().putString("original", su("settings get secure " + KEY).trim()).apply();
                }
                // Android 13+ använder en enda grundfärg för hela paletten, så
                // grundfärgen är alltid guld. Marinblå ytor läggs som ett eget lager.
                String neutral = GOLD;
                String style = "TONAL_SPOT";
                String json = "{\"android.theme.customization.system_palette\":\"" + neutral + "\","
                        + "\"android.theme.customization.accent_color\":\"" + GOLD + "\","
                        + "\"android.theme.customization.theme_style\":\"" + style + "\","
                        + "\"android.theme.customization.color_source\":\"preset\","
                        + "\"android.theme.customization.color_both\":\"1\","
                        + "\"android.theme.customization.color_index\":\"-1\","
                        + "\"_applied_timestamp\":" + System.currentTimeMillis() + "}";
                su("settings put secure " + KEY + " '" + json + "'");
                String clock = clockGold(p);
                String overlays = navy ? navySurfaces(a, true) : navySurfaces(a, false);
                result = su("settings get secure " + KEY).trim() + "\n\nClock: " + clock + "\n\n" + overlays;
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
                navySurfaces(a, false);
                String oc = p.getString("original_clock", null);
                if (oc != null) {
                    if (oc.isEmpty() || "null".equals(oc)) su("settings delete secure " + CLOCK_KEY);
                    else su("settings put secure " + CLOCK_KEY + " '" + oc + "'");
                    p.edit().remove("original_clock").apply();
                }
                result = su("settings get secure " + KEY).trim();
            } catch (Exception e) {
                result = e.getClass().getSimpleName() + ": " + e.getMessage();
            }
            show(a, R.string.root_restored, result);
        }).start();
    }



    private static final String CLOCK_KEY = "lock_screen_custom_clock_face";

    /** Sätter låsskärmsklockans färg till SNC-guld. Returnerar den nya inställningen. */
    private static String clockGold(SharedPreferences p) throws Exception {
        String cur = su("settings get secure " + CLOCK_KEY).trim();
        if (!p.contains("original_clock")) p.edit().putString("original_clock", cur).apply();
        org.json.JSONObject o;
        try {
            o = new org.json.JSONObject(cur);
        } catch (Exception e) {
            o = new org.json.JSONObject();
        }
        if (!o.has("clockId")) o.put("clockId", "DEFAULT");
        o.put("seedColor", 0xFFDDAE54);
        org.json.JSONObject meta = o.optJSONObject("metadata");
        if (meta == null) meta = new org.json.JSONObject();
        meta.put("appliedTimestamp", System.currentTimeMillis());
        o.put("metadata", meta);
        su("settings put secure " + CLOCK_KEY + " '" + o.toString() + "'");
        return su("settings get secure " + CLOCK_KEY).trim();
    }

    /** Samlar uppgifter om klocka, typsnitt och färger i Download/snc-diagnostik.txt. */
    public static void diagnostics(Activity a) {
        new Thread(() -> {
            String out;
            try {
                String sh = "OUT=/sdcard/Download/snc-diagnostik.txt\n"
                        + "{\n"
                        + "echo \"== SNC diagnostik $(date) ==\"\n"
                        + "getprop ro.lineage.build.version; getprop ro.build.version.release; getprop ro.product.model\n"
                        + "echo '== clock setting =='; settings get secure " + CLOCK_KEY + "\n"
                        + "echo '== theme =='; settings get secure " + KEY + "\n"
                        + "echo '== secure clock/font =='; settings list secure | grep -i -E 'clock|font'\n"
                        + "echo '== system clock/font =='; settings list system | grep -i -E 'clock|font'\n"
                        + "echo '== font overlays =='; cmd overlay list | grep -i -E 'font|clock'\n"
                        + "echo '== fonts.xml families =='\n"
                        + "for f in /system/etc/font_fallback.xml /system/etc/fonts.xml /product/etc/fonts_customization.xml; do [ -f $f ] && { echo \"-- $f\"; grep -o 'family[^>]*name=\"[^\"]*\"' $f | sort -u; }; done\n"
                        + "echo '== clock-ish fonts on disk =='; ls -la /system/fonts /product/fonts /system_ext/fonts 2>/dev/null | grep -i -E 'clock|flex|sans|lineage|rubik'\n"
                        + "echo '== fonts referenced with clock =='; grep -i -B2 -A6 'clock' /system/etc/fonts.xml /product/etc/fonts_customization.xml 2>/dev/null | head -80\n"
                        + "echo '== systemui =='; pm path com.android.systemui\n"
                        + "echo '== clock registry =='; dumpsys activity service com.android.systemui/.SystemUIService 2>/dev/null | grep -i -E 'clockId|ClockRegistry|currentClock|clock_face' | head -40\n"
                        + "} > $OUT 2>&1\n"
                        + "echo saved\n";
                java.io.File f = new java.io.File(a.getCacheDir(), "snc_diag.sh");
                try (java.io.FileOutputStream o = new java.io.FileOutputStream(f)) {
                    o.write(sh.getBytes("UTF-8"));
                }
                f.setReadable(true, false);
                su("sh " + f.getAbsolutePath());
                out = "Download/snc-diagnostik.txt";
            } catch (Exception e) {
                out = e.getClass().getSimpleName() + ": " + e.getMessage();
            }
            show(a, R.string.diag_done, out);
        }).start();
    }

    /** Marinblå neutrala toner och mörka ytor (beräknade från #011634, HCT-ton 263). */
    private static final String[][] NAVY_COLORS = {
            {"system_neutral1_0", "0xffffffff"},
            {"system_neutral2_0", "0xffffffff"},
            {"system_neutral1_10", "0xfffdfbff"},
            {"system_neutral2_10", "0xfffdfbff"},
            {"system_neutral1_50", "0xffecf0ff"},
            {"system_neutral2_50", "0xffecf0ff"},
            {"system_neutral1_100", "0xffdae2f9"},
            {"system_neutral2_100", "0xffd7e3ff"},
            {"system_neutral1_200", "0xffbec6dc"},
            {"system_neutral2_200", "0xffb8c7e6"},
            {"system_neutral1_300", "0xffa2abc0"},
            {"system_neutral2_300", "0xff9dabca"},
            {"system_neutral1_400", "0xff8891a5"},
            {"system_neutral2_400", "0xff8391ae"},
            {"system_neutral1_500", "0xff6e778b"},
            {"system_neutral2_500", "0xff697794"},
            {"system_neutral1_600", "0xff565e71"},
            {"system_neutral2_600", "0xff515f7a"},
            {"system_neutral1_700", "0xff3e4759"},
            {"system_neutral2_700", "0xff394761"},
            {"system_neutral1_800", "0xff283141"},
            {"system_neutral2_800", "0xff223149"},
            {"system_neutral1_900", "0xff131c2b"},
            {"system_neutral2_900", "0xff0c1b33"},
            {"system_neutral1_1000", "0xff000000"},
            {"system_neutral2_1000", "0xff000000"},
            {"system_background_dark", "0xff0a1323"},
            {"system_surface_dark", "0xff0a1323"},
            {"system_surface_dim_dark", "0xff0a1323"},
            {"system_surface_bright_dark", "0xff31394a"},
            {"system_surface_container_lowest_dark", "0xff050e1d"},
            {"system_surface_container_low_dark", "0xff131c2b"},
            {"system_surface_container_dark", "0xff172030"},
            {"system_surface_container_high_dark", "0xff212a3b"},
            {"system_surface_container_highest_dark", "0xff2c3546"},
            {"system_on_background_dark", "0xffdae2f9"},
            {"system_on_surface_dark", "0xffdae2f9"},
            {"system_surface_variant_dark", "0xff394761"},
            {"system_on_surface_variant_dark", "0xffb8c7e6"},
            {"system_outline_dark", "0xff8391ae"},
            {"system_outline_variant_dark", "0xff394761"}
    };

    /**
     * Skapar (eller stänger av) egna färglager ovanpå systemets med
     * "cmd overlay fabricate". Returnerar en kort sammanfattning.
     */
    private static String navySurfaces(Activity a, boolean on) throws Exception {
        StringBuilder sh = new StringBuilder();
        String log = "/sdcard/Download/snc-farglager.txt";
        sh.append("exec 3>").append(log).append('\n');
        sh.append("echo \"== SNC färglager $(date) on=").append(on).append(" ==\" >&3\n");
        if (on) {
            // Vänta tills SystemUI har byggt om sina egna färger efter inställningsändringen.
            sh.append("sleep 8\n");
        }
        for (String[] c : NAVY_COLORS) {
            String id = "com.android.shell:SncN_" + c[0];
            if (on) {
                sh.append("o=$(cmd overlay fabricate --target android --name SncN_").append(c[0])
                  .append(" android:color/").append(c[0]).append(" 0x1c ").append(c[1])
                  .append(" 2>&1); r=$?; echo \"fab ").append(c[0]).append(" r=$r $o\" >&3; ")
                  .append("[ $r -eq 0 ] || echo \"FAIL_FAB ").append(c[0]).append(": $o\"\n");
                sh.append("o=$(cmd overlay enable --user 0 ").append(id)
                  .append(" 2>&1); r=$?; echo \"en r=$r $o\" >&3; ")
                  .append("[ $r -eq 0 ] || echo \"FAIL_EN ").append(c[0]).append(": $o\"\n");
            } else {
                sh.append("cmd overlay disable --user 0 ").append(id).append(" >&3 2>&1\n");
            }
        }
        if (on) {
            // Lägg lagren överst sist, efter att allt annat är klart.
            sh.append("sleep 2\n");
            for (String[] c : NAVY_COLORS) {
                sh.append("cmd overlay set-priority com.android.shell:SncN_").append(c[0])
                  .append(" highest >&3 2>&1\n");
            }
        }
        sh.append("echo '== overlay list android ==' >&3\n");
        sh.append("cmd overlay list android >&3 2>&1\n");
        sh.append("echo '== dump background ==' >&3\n");
        sh.append("cmd overlay dump com.android.shell:SncN_system_background_dark >&3 2>&1\n");
        sh.append("echo ACTIVE $(cmd overlay list android | grep -c '\\[x\\] com.android.shell:SncN_')\n");
        java.io.File f = new java.io.File(a.getCacheDir(), "snc_navy.sh");
        try (java.io.FileOutputStream o = new java.io.FileOutputStream(f)) {
            o.write(sh.toString().getBytes("UTF-8"));
        }
        f.setReadable(true, false);
        String out = su("sh " + f.getAbsolutePath()).trim();
        String[] lines = out.split("\n");
        StringBuilder sum = new StringBuilder();
        int fails = 0;
        for (String l : lines) {
            if (l.startsWith("FAIL")) {
                if (fails < 3) sum.append(l).append('\n');
                fails++;
            } else if (l.startsWith("ACTIVE")) {
                sum.append("Navy overlays active: ").append(l.substring(6).trim())
                   .append(" / ").append(NAVY_COLORS.length).append('\n');
            }
        }
        if (fails > 3) sum.append("... ").append(fails).append(" errors\n");
        sum.append("Log: Download/snc-farglager.txt");
        return sum.toString().trim();
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
