package se.snc.theme;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.util.TypedValue;

import org.xmlpull.v1.XmlPullParser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Byter appikoner till SNC-stil med root. För varje app tar vi reda på
 * vad ikonens lager heter på just den här telefonen (adaptiva ikoner:
 * förgrund, bakgrund och monokrom) och lägger egna bilder ovanpå med
 * "cmd overlay fabricate ... drawable <png>".
 */
public final class IconTheme {

    private static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";
    private static final String LOG = "/sdcard/Download/snc-ikoner.txt";
    private static final String PREFIX = "SncI_";

    /** Paketnamn → ikon. */
    private static final Map<String, String> MAP = new HashMap<>();

    static {
        MAP.put("com.android.dialer", "telefon");
        MAP.put("com.android.messaging", "meddelanden");
        MAP.put("com.android.contacts", "kontakter");
        MAP.put("org.lineageos.aperture", "kamera");
        MAP.put("com.android.camera2", "kamera");
        MAP.put("org.lineageos.glimpse", "galleri");
        MAP.put("com.android.gallery3d", "galleri");
        MAP.put("com.android.deskclock", "klocka");
        MAP.put("org.lineageos.etar", "kalender");
        MAP.put("com.android.calendar", "kalender");
        MAP.put("com.android.calculator2", "kalkylator");
        MAP.put("com.android.settings", "installningar");
        MAP.put("com.android.documentsui", "filer");
        MAP.put("com.google.android.documentsui", "filer");
        MAP.put("org.lineageos.jelly", "webblasare");
        MAP.put("org.lineageos.twelve", "musik");
        MAP.put("org.lineageos.eleven", "musik");
        MAP.put("org.lineageos.recorder", "inspelning");
        MAP.put("com.android.email", "e-post");
        MAP.put("com.fsck.k9", "e-post");
        MAP.put("net.thunderbird.android", "e-post");
        MAP.put("com.google.android.gm", "e-post");
        MAP.put("com.aurora.store", "appbutik");
        MAP.put("org.fdroid.fdroid", "appbutik");
        MAP.put("com.android.vending", "appbutik");
        MAP.put("org.lineageos.updater", "uppdatering");
    }

    private IconTheme() {}

    public static void apply(Activity a, boolean on) {
        android.widget.Toast.makeText(a, R.string.icons_working, android.widget.Toast.LENGTH_LONG).show();
        new Thread(() -> {
            String msg;
            try {
                msg = run(a, on);
            } catch (Exception e) {
                msg = e.getClass().getSimpleName() + ": " + e.getMessage();
            }
            final String m = msg;
            a.runOnUiThread(() -> {
                if (a.isFinishing()) return;
                new AlertDialog.Builder(a)
                        .setTitle(on ? R.string.btn_icons_apply : R.string.btn_icons_reset)
                        .setMessage(m)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
            });
        }).start();
    }

    private static String run(Activity a, boolean on) throws Exception {
        PackageManager pm = a.getPackageManager();
        File dir = new File(a.getCacheDir(), "icons");
        StringBuilder sh = new StringBuilder();
        sh.append("exec 3>").append(LOG).append('\n');
        sh.append("echo \"== SNC ikoner $(date) on=").append(on).append(" ==\" >&3\n");
        // Stäng alltid av tidigare lager först, så att en ny körning börjar rent.
        sh.append("for o in $(cmd overlay list | grep -o 'com.android.shell:").append(PREFIX)
          .append("[A-Za-z0-9_]*'); do cmd overlay disable --user 0 $o >&3 2>&1; done\n");

        int apps = 0;
        if (on) {
            copyAssets(a, dir);
            Intent main = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> list = pm.queryIntentActivities(main, 0);
            Set<String> done = new HashSet<>();
            int n = 0;
            for (ResolveInfo ri : list) {
                String pkg = ri.activityInfo.packageName;
                String key = MAP.get(pkg);
                if (key == null || !done.add(pkg)) continue;
                int iconId = ri.activityInfo.getIconResource();
                if (iconId == 0) continue;
                Resources res;
                try {
                    res = pm.getResourcesForApplication(pkg);
                } catch (PackageManager.NameNotFoundException e) {
                    continue;
                }
                sh.append("echo \"-- ").append(pkg).append(" -> ").append(key).append(" icon=")
                  .append(safeName(res, iconId)).append(" path=").append(pathOf(res, iconId))
                  .append("\" >&3\n");
                String path = pathOf(res, iconId);
                boolean adaptive = false;
                if (path.endsWith(".xml")) {
                    try (XmlResourceParser p = res.getXml(iconId)) {
                        int ev;
                        while ((ev = p.next()) != XmlPullParser.END_DOCUMENT) {
                            if (ev != XmlPullParser.START_TAG) continue;
                            String tag = p.getName();
                            if ("adaptive-icon".equals(tag)) {
                                adaptive = true;
                                continue;
                            }
                            if (!adaptive) break;
                            int ref = p.getAttributeResourceValue(ANDROID_NS, "drawable", 0);
                            if (ref == 0) continue;
                            String file;
                            if ("background".equals(tag)) file = "bg.png";
                            else if ("foreground".equals(tag)) file = key + "_fg.png";
                            else if ("monochrome".equals(tag)) file = key + "_mono.png";
                            else continue;
                            n = layer(sh, res, pkg, ref, new File(dir, file), n);
                        }
                    } catch (Exception e) {
                        sh.append("echo \"   xml error ").append(e.getClass().getSimpleName()).append("\" >&3\n");
                    }
                }
                if (!adaptive) {
                    // Vanlig bildikon: byt hela bilden.
                    n = layer(sh, res, pkg, iconId, new File(dir, key + "_full.png"), n);
                }
                apps++;
            }
        }
        // Låt hemskärmen läsa in ikonerna på nytt.
        sh.append("H=$(cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME | tail -n 1 | cut -d/ -f1)\n");
        sh.append("echo \"launcher=$H\" >&3\n");
        sh.append("[ -n \"$H\" ] && rm -f /data/data/$H/databases/app_icons.db* >&3 2>&1\n");
        sh.append("[ -n \"$H\" ] && am force-stop $H >&3 2>&1\n");
        sh.append("echo ACTIVE $(cmd overlay list | grep -c '\\[x\\] com.android.shell:").append(PREFIX).append("')\n");

        String out = su(a, sh.toString());
        String active = out.contains("ACTIVE") ? out.substring(out.lastIndexOf("ACTIVE") + 6).trim() : "?";
        return on ? a.getString(R.string.icons_result, apps, active) : a.getString(R.string.icons_reset_done);
    }

    /** Lägger ett lager för en resurs, både för dess egen konfiguration och som standard. */
    private static int layer(StringBuilder sh, Resources res, String pkg, int ref, File png, int n) {
        String name = safeName(res, ref);
        if (name == null) return n;
        String type = res.getResourceTypeName(ref);
        String cfg = configOf(pathOf(res, ref));
        String[] cfgs = cfg.isEmpty() ? new String[]{""} : new String[]{cfg, ""};
        for (String c : cfgs) {
            String id = PREFIX + pkg.replace('.', '_') + "_" + (n++);
            sh.append("o=$(cmd overlay fabricate --target ").append(pkg).append(" --name ").append(id);
            if (!c.isEmpty()) sh.append(" --config ").append(c);
            if ("color".equals(type)) {
                sh.append(' ').append(name).append(" color 0xff0e2c37");
            } else {
                sh.append(' ').append(name).append(" drawable ").append(png.getAbsolutePath());
            }
            sh.append(" 2>&1); echo \"   fab ").append(name).append(" [").append(c).append("] r=$? $o\" >&3\n");
            sh.append("cmd overlay enable --user 0 com.android.shell:").append(id).append(" >&3 2>&1\n");
        }
        return n;
    }

    private static String safeName(Resources res, int id) {
        try {
            return res.getResourceName(id);
        } catch (Resources.NotFoundException e) {
            return null;
        }
    }

    private static String pathOf(Resources res, int id) {
        try {
            TypedValue tv = new TypedValue();
            res.getValue(id, tv, true);
            return tv.string != null ? tv.string.toString() : "";
        } catch (Exception e) {
            return "";
        }
    }

    /** "res/mipmap-xxhdpi-v4/ic.png" → "xxhdpi-v4". */
    private static String configOf(String path) {
        String[] parts = path.split("/");
        if (parts.length < 3) return "";
        String d = parts[parts.length - 2];
        int i = d.indexOf('-');
        return i < 0 ? "" : d.substring(i + 1);
    }

    private static void copyAssets(Activity a, File dir) throws Exception {
        if (!dir.exists() && !dir.mkdirs()) throw new Exception("mkdir " + dir);
        for (String f : a.getAssets().list("icons")) {
            File out = new File(dir, f);
            try (InputStream in = a.getAssets().open("icons/" + f);
                 OutputStream o = new FileOutputStream(out)) {
                byte[] buf = new byte[16384];
                int n;
                while ((n = in.read(buf)) > 0) o.write(buf, 0, n);
            }
            out.setReadable(true, false);
        }
        dir.setExecutable(true, false);
        dir.setReadable(true, false);
    }

    private static String su(Activity a, String script) throws Exception {
        File f = new File(a.getCacheDir(), "snc_icons.sh");
        try (FileOutputStream o = new FileOutputStream(f)) {
            o.write(script.getBytes("UTF-8"));
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
        return bo.toString("UTF-8");
    }
}
