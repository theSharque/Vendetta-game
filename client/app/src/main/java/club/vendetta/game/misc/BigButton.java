package club.vendetta.game.misc;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Button;

/**
 * Created by sharque on 16.08.14.
 * <p/>
 * My own button with other font on it
 */
public class BigButton extends Button {
    public BigButton(Context context) {
        super(context);
        init();
    }

    public BigButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BigButton(Context context, AttributeSet attrs, int defStyle) {
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
