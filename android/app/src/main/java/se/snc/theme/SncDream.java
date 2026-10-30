package se.snc.theme;

import android.service.dreams.DreamService;

/** Skärmsläckare (Daydream) med SNC-loggan och klockan. */
public class SncDream extends DreamService {

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        setInteractive(false);
        setFullscreen(true);
        setScreenBright(false);
        setContentView(new SaverView(this));
    }
}
