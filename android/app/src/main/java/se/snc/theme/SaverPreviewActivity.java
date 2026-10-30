package se.snc.theme;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

/** Visar skärmsläckaren i helskärm. Tryck för att stänga. */
public class SaverPreviewActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SaverView view = new SaverView(this);
        view.setOnClickListener(v -> finish());
        setContentView(view);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
}
