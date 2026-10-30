package se.snc.theme;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.format.DateFormat;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Date;
import java.util.Locale;
import java.util.Random;

/**
 * Skärmsläckarens innehåll: nedtonad strandbild, loggan, klockan i Cinzel med
 * guldkolon, datum och slogan. Innehållet flyttas lite varje minut för att
 * skydda OLED-skärmen mot inbränning.
 */
public class SaverView extends FrameLayout {

    private static final int WHITE = 0xFFF5F2EA;
    private static final int GOLD = 0xFFDDAE54;
    private static final int GOLD_LIGHT = 0xFFE9C27A;

    private final TextView clock;
    private final TextView date;
    private final LinearLayout column;
    private final Random random = new Random();

    private final BroadcastReceiver tick = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent i) {
            update();
            drift();
        }
    };

    public SaverView(Context context) {
        super(context);
        setBackgroundColor(0xFF000510);
        Typeface cinzel = getResources().getFont(R.font.cinzel);

        ImageView bg = new ImageView(context);
        bg.setImageResource(R.drawable.wp_home);
        bg.setScaleType(ImageView.ScaleType.CENTER_CROP);
        bg.setAlpha(0.38f);
        addView(bg, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        column = new LinearLayout(context);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setGravity(Gravity.CENTER_HORIZONTAL);

        ImageView logo = new ImageView(context);
        logo.setImageResource(R.drawable.logo_full);
        logo.setAdjustViewBounds(true);
        logo.setContentDescription(getResources().getString(R.string.company));
        column.addView(logo, new LinearLayout.LayoutParams(dp(270), LinearLayout.LayoutParams.WRAP_CONTENT));

        clock = new TextView(context);
        clock.setTypeface(cinzel);
        clock.setTextColor(WHITE);
        clock.setTextSize(TypedValue.COMPLEX_UNIT_SP, 92);
        clock.setIncludeFontPadding(false);
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        clp.topMargin = dp(56);
        column.addView(clock, clp);

        date = new TextView(context);
        date.setTypeface(cinzel);
        date.setTextColor(GOLD_LIGHT);
        date.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        date.setLetterSpacing(0.18f);
        LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dlp.topMargin = dp(14);
        column.addView(date, dlp);

        LayoutParams colLp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                Gravity.CENTER_HORIZONTAL | Gravity.TOP);
        colLp.topMargin = dp(130);
        addView(column, colLp);

        TextView slogan = new TextView(context);
        slogan.setTypeface(cinzel);
        slogan.setText(R.string.slogan);
        slogan.setTextColor(0xFFD6D2C8);
        slogan.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        slogan.setLetterSpacing(0.15f);
        LayoutParams slp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
        slp.bottomMargin = dp(64);
        addView(slogan, slp);

        View line = new View(context);
        line.setBackgroundColor(GOLD);
        line.setAlpha(0.6f);
        LayoutParams llp = new LayoutParams(dp(220), dp(1), Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
        llp.bottomMargin = dp(92);
        addView(line, llp);

        update();
    }

    private void update() {
        Date now = new Date();
        String hh = DateFormat.format("HH", now).toString();
        String mm = DateFormat.format("mm", now).toString();
        SpannableString s = new SpannableString(hh + ":" + mm);
        s.setSpan(new ForegroundColorSpan(GOLD), 2, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        clock.setText(s);
        String d = DateFormat.format("EEEE d MMMM", now).toString();
        date.setText(d.toUpperCase(Locale.getDefault()));
    }

    private void drift() {
        column.animate()
                .translationX(dp(random.nextInt(41) - 20))
                .translationY(dp(random.nextInt(41) - 20))
                .setDuration(2000)
                .start();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        IntentFilter f = new IntentFilter();
        f.addAction(Intent.ACTION_TIME_TICK);
        f.addAction(Intent.ACTION_TIME_CHANGED);
        f.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            getContext().registerReceiver(tick, f, Context.RECEIVER_NOT_EXPORTED);
        } else {
            getContext().registerReceiver(tick, f);
        }
        update();
    }

    @Override
    protected void onDetachedFromWindow() {
        getContext().unregisterReceiver(tick);
        super.onDetachedFromWindow();
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
