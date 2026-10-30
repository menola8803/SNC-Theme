package se.snc.theme;

import android.app.Activity;
import android.app.AlertDialog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Gör telefonappens knappsats mer kompakt med egna lager (root,
 * "cmd overlay fabricate"). Måtten kommer från LineageOS Dialer:
 * dialpad_space_above_keys, dialpad_bottom_space_height och
 * dialpad_key_numbers_default_size.
 */
public final class DialerTweak {

    private static final String TARGET = "com.android.dialer";
    private static final String LOG = "/sdcard/Download/snc-knappsats.txt";

    /** {namn, värde i dp} – standard är 14, 80 och 36 dp. */
    private static final int[][] DP = {{150}, {110}, {30}};
    private static final String[] NAMES = {
            "dialpad_space_above_keys",
            "dialpad_bottom_space_height",
            "dialpad_key_numbers_default_size"
    };

    private DialerTweak() {}

    /** TYPE_DIMENSION-kodning för ett heltal i dp (radix 23p0, enhet dip). */
    private static String dp(int v) {
        return "0x" + Integer.toHexString((v << 8) | 0x01);
    }

    public static void apply(Activity a, boolean on) {
        new Thread(() -> {
            String out;
            try {
                StringBuilder sh = new StringBuilder();
                sh.append("exec 3>").append(LOG).append('\n');
                sh.append("echo \"== SNC knappsats $(date) on=").append(on).append(" ==\" >&3\n");
                for (int i = 0; i < NAMES.length; i++) {
                    String id = "com.android.shell:SncD_" + NAMES[i];
                    if (on) {
                        sh.append("o=$(cmd overlay fabricate --target ").append(TARGET)
                          .append(" --name SncD_").append(NAMES[i]).append(' ')
                          .append(TARGET).append(":dimen/").append(NAMES[i])
                          .append(" 0x05 ").append(dp(DP[i][0]))
                          .append(" 2>&1); echo \"fab ").append(NAMES[i]).append(" r=$? $o\" >&3\n");
                        sh.append("o=$(cmd overlay enable --user 0 ").append(id)
                          .append(" 2>&1); echo \"en r=$? $o\" >&3\n");
                    } else {
                        sh.append("cmd overlay disable --user 0 ").append(id).append(" >&3 2>&1\n");
                    }
                }
                sh.append("am force-stop ").append(TARGET).append(" >&3 2>&1\n");
                sh.append("echo '== overlays ==' >&3; cmd overlay list ").append(TARGET).append(" >&3 2>&1\n");
                sh.append("echo ACTIVE $(cmd overlay list ").append(TARGET)
                  .append(" | grep -c '\\[x\\] com.android.shell:SncD_')\n");
                File f = new File(a.getCacheDir(), "snc_dialer.sh");
                try (FileOutputStream o = new FileOutputStream(f)) {
                    o.write(sh.toString().getBytes("UTF-8"));
                }
                f.setReadable(true, false);
                Process p = new ProcessBuilder("su", "-c", "sh " + f.getAbsolutePath())
                        .redirectErrorStream(true).start();
                ByteArrayOutputStream bo = new ByteArrayOutputStream();
                try (InputStream in = p.getInputStream()) {
                    byte[] buf = new byte[4096];
                    int n;
                    while ((n = in.read(buf)) > 0) bo.write(buf, 0, n);
                }
                p.waitFor();
                String res = bo.toString("UTF-8").trim();
                String active = res.contains("ACTIVE") ? res.substring(res.lastIndexOf("ACTIVE") + 6).trim() : "?";
                out = a.getString(R.string.dialer_result, active, NAMES.length);
            } catch (Exception e) {
                out = e.getClass().getSimpleName() + ": " + e.getMessage();
            }
            final String msg = out;
            a.runOnUiThread(() -> {
                if (a.isFinishing()) return;
                new AlertDialog.Builder(a)
                        .setTitle(on ? R.string.btn_dialer_compact : R.string.btn_dialer_reset)
                        .setMessage(msg)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
            });
        }).start();
    }
}
