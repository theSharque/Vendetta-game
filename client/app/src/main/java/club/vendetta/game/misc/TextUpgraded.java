package club.vendetta.game.misc;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by Admin on 12.09.14.
 */
public class TextUpgraded extends TextView {
    public TextUpgraded(Context context) {
        super(context);
        init();
    }

    public TextUpgraded(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TextUpgraded(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        Typeface tf = null;
        try {
            tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/capture_it.ttf");
        } catch (Exception e) {
            Log.e("Vendetta", "Could not get typeface: " + e.getMessage());
        }

        setTypeface(tf);
    }
}
