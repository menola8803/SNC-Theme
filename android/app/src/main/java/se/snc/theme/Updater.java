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

import org.json.JSONArray;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import android.text.Html;
import android.util.Xml;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

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
    private static final String ATOM =
            "https://github.com/menola8803/SNC-Theme/releases.atom";
    private static final String API_LIST =
            "https://api.github.com/repos/menola8803/SNC-Theme/releases?per_page=15";
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
                final String url = downloadUrl(tag);
                a.runOnUiThread(() -> {
                    if (a.isFinishing()) return;
                    if (remote > current && url != null) {
                        new AlertDialog.Builder(a)
                                .setTitle(R.string.update_title)
                                .setMessage(a.getString(R.string.update_message, tag))
                                .setPositiveButton(R.string.update_install, (d, w) -> install(a, url))
                                .setNeutralButton(R.string.update_show, (d, w) ->
                                        a.startActivity(new Intent(a, UpdateActivity.class)))
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
                .setPositiveButton(R.string.update_open_web, (d, w) -> openInBrowser(a, LATEST_APK))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    public static void install(Activity a, String url) {
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


    /** En publicerad version. */
    public static final class Release {
        public String tag;
        public String date;
        public String notes;
        public long code;
    }

    public static long versionOf(String tag) {
        try {
            return Long.parseLong(tag.substring(tag.lastIndexOf('.') + 1));
        } catch (Exception e) {
            return -1;
        }
    }

    public static String downloadUrl(String tag) {
        return DOWNLOAD + tag + "/SNC-Tema-" + tag + ".apk";
    }

    public static String releasePage() {
        return LATEST_PAGE;
    }

    /** Fast länk som alltid laddar ner senaste APK:n. */
    public static final String LATEST_APK =
            "https://github.com/menola8803/SNC-Theme/releases/latest/download/SNC-Tema.apk";

    /**
     * Öppnar en länk i webbläsaren, inte i GitHub-appen (som inte kan ladda ner APK-filer).
     */
    public static void openInBrowser(Activity a, String url) {
        Intent view = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        try {
            android.content.pm.ResolveInfo ri = a.getPackageManager().resolveActivity(
                    new Intent(Intent.ACTION_VIEW, Uri.parse("http://example.com")),
                    android.content.pm.PackageManager.MATCH_DEFAULT_ONLY);
            String pkg = ri != null && ri.activityInfo != null ? ri.activityInfo.packageName : null;
            if (pkg != null && !"android".equals(pkg)) {
                view.setPackage(pkg);
                a.startActivity(view);
                return;
            }
        } catch (Exception ignored) {
        }
        a.startActivity(Intent.createChooser(view, url));
    }

    /** Hämtar de senaste versionerna, nyast först. */
    public static List<Release> fetchReleases() throws Exception {
        try {
            List<Release> r = fromAtom(readText(ATOM));
            if (!r.isEmpty()) return r;
        } catch (Exception ignored) {
        }
        List<Release> out = new ArrayList<>();
        JSONArray arr = new JSONArray(readText(API_LIST));
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            Release r = new Release();
            r.tag = o.getString("tag_name");
            r.code = versionOf(r.tag);
            r.date = o.optString("published_at", "");
            if (r.date.length() >= 10) r.date = r.date.substring(0, 10);
            r.notes = o.optString("body", "").trim();
            out.add(r);
        }
        return out;
    }

    private static List<Release> fromAtom(String xml) throws Exception {
        List<Release> out = new ArrayList<>();
        XmlPullParser x = Xml.newPullParser();
        x.setInput(new StringReader(xml));
        Release cur = null;
        String tagName = null;
        for (int ev = x.getEventType(); ev != XmlPullParser.END_DOCUMENT; ev = x.next()) {
            if (ev == XmlPullParser.START_TAG) {
                tagName = x.getName();
                if ("entry".equals(tagName)) cur = new Release();
                if (cur != null && "link".equals(tagName)) {
                    String href = x.getAttributeValue(null, "href");
                    if (href != null && href.contains("/tag/")) {
                        cur.tag = href.substring(href.lastIndexOf("/tag/") + 5);
                        cur.code = versionOf(cur.tag);
                    }
                }
            } else if (ev == XmlPullParser.TEXT && cur != null && tagName != null) {
                String t = x.getText();
                if ("updated".equals(tagName) && t.length() >= 10) cur.date = t.substring(0, 10);
                if ("content".equals(tagName)) {
                    cur.notes = Html.fromHtml(t, Html.FROM_HTML_MODE_COMPACT).toString().trim();
                }
            } else if (ev == XmlPullParser.END_TAG) {
                if ("entry".equals(x.getName()) && cur != null) {
                    if (cur.tag != null) out.add(cur);
                    cur = null;
                }
                tagName = null;
            }
        }
        return out;
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
