package uk.co.bitethebullet.android.token.util;

import android.content.Context;
import android.graphics.Typeface;

public class FontManager {
    public static final String ROOT = "fonts/",
            FONTAWESOME = ROOT + "fa-regular-400.ttf",
            FONTAWESOME_BRANDS = ROOT + "fa-brands-400.ttf",
            FONTAWESOME_SOLID = ROOT + "fa-solid-900.ttf";

    public static Typeface getTypeface(Context context, String font) {
        return Typeface.createFromAsset(context.getAssets(), font);
    }
}
