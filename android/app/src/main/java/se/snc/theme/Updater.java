package se.snc.theme;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.net.Uri;
import android.provider.Settings;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Söker efter nya versioner bland GitHub-releaserna i menola8803/SNC-Theme
 * och installerar dem med Androids paketinstallerare.
 */
public final class Updater {

    private static final String API =
            "https://api.github.com/repos/menola8803/SNC-Theme/releases/latest";
    private static final String LATEST_PAGE =
            "https://github.com/menola8803/SNC-Theme/releases/latest";
    private static final String DOWNLOAD =
            "https://github.com/menola8803/SNC-Theme/releases/download/";

    private Updater() {}

    /** @param silent visa inget om appen redan är uppdaterad eller om något går fel */
    public static void check(Activity a, boolean silent) {
        if (!silent) toast(a, R.string.update_checking);
        new Thread(() -> {
            try {
                String tag = latestTag();
                long remote = Long.parseLong(tag.substring(tag.lastIndexOf('.') + 1));
                long current = a.getPackageManager()
                        .getPackageInfo(a.getPackageName(), 0).getLongVersionCode();
                final String url = DOWNLOAD + tag + "/SNC-Tema-" + tag + ".apk";
                a.runOnUiThread(() -> {
                    if (a.isFinishing()) return;
                    if (remote > current && url != null) {
                        new AlertDialog.Builder(a)
                                .setTitle(R.string.update_title)
                                .setMessage(a.getString(R.string.update_message, tag))
                                .setPositiveButton(R.string.update_install, (d, w) -> install(a, url))
                                .setNegativeButton(R.string.update_later, null)
                                .show();
                    } else if (!silent) {
                        Toast.makeText(a, a.getString(R.string.update_latest, "v1.0." + current),
                                Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                if (!silent) a.runOnUiThread(() -> failed(a, e));
            }
        }).start();
    }

    /**
     * Läser senaste versionens tagg från webbsidans omdirigering
     * (/releases/latest → /releases/tag/v1.0.N). Den har ingen frågegräns,
     * till skillnad från GitHubs API. API:t används bara som reserv.
     */
    private static String latestTag() throws Exception {
        try {
            HttpURLConnection c = open(LATEST_PAGE);
            c.setInstanceFollowRedirects(false);
            c.setRequestMethod("HEAD");
            int code = c.getResponseCode();
            String loc = c.getHeaderField("Location");
            c.disconnect();
            if (code >= 300 && code < 400 && loc != null && loc.contains("/tag/")) {
                return loc.substring(loc.lastIndexOf("/tag/") + 5);
            }
        } catch (Exception ignored) {
        }
        return new JSONObject(readText(API)).getString("tag_name");
    }

    /** Visar orsaken och erbjuder att öppna releasesidan i webbläsaren. */
    private static void failed(Activity a, Exception e) {
        if (a.isFinishing()) return;
        String reason = e.getClass().getSimpleName()
                + (e.getMessage() != null ? ": " + e.getMessage() : "");
        new AlertDialog.Builder(a)
                .setTitle(R.string.update_failed_title)
                .setMessage(a.getString(R.string.update_failed_reason, reason))
                .setPositiveButton(R.string.update_open_web, (d, w) -> a.startActivity(
                        new Intent(Intent.ACTION_VIEW, Uri.parse(LATEST_PAGE))))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private static void install(Activity a, String url) {
        if (!a.getPackageManager().canRequestPackageInstalls()) {
            toast(a, R.string.update_allow);
            a.startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:" + a.getPackageName())));
            return;
        }
        toast(a, R.string.update_downloading);
        new Thread(() -> {
            try {
                PackageInstaller pi = a.getPackageManager().getPackageInstaller();
                PackageInstaller.SessionParams params =
                        new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
                params.setAppPackageName(a.getPackageName());
                int id = pi.createSession(params);
                try (PackageInstaller.Session s = pi.openSession(id)) {
                    HttpURLConnection c = open(url);
                    try (InputStream in = c.getInputStream();
                         OutputStream out = s.openWrite("snc.apk", 0, -1)) {
                        byte[] buf = new byte[64 * 1024];
                        int n;
                        while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
                        s.fsync(out);
                    } finally {
                        c.disconnect();
                    }
                    Intent cb = new Intent(a, InstallReceiver.class);
                    PendingIntent p = PendingIntent.getBroadcast(a, id, cb,
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
                    s.commit(p.getIntentSender());
                }
            } catch (Exception e) {
                a.runOnUiThread(() -> failed(a, e));
            }
        }).start();
    }

    private static HttpURLConnection open(String url) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setRequestProperty("User-Agent", "SNC-Tema");
        c.setRequestProperty("Accept", "application/vnd.github+json, */*");
        c.setInstanceFollowRedirects(true);
        c.setConnectTimeout(15000);
        c.setReadTimeout(30000);
        return c;
    }

    private static String readText(String url) throws Exception {
        HttpURLConnection c = open(url);
        try (InputStream in = c.getInputStream()) {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) bo.write(buf, 0, n);
            return bo.toString("UTF-8");
        } finally {
            c.disconnect();
        }
    }

    private static void toast(Context c, int msg) {
        Toast.makeText(c, msg, Toast.LENGTH_LONG).show();
    }
}
