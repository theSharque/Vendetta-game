package club.vendetta.game.misc;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;

/**
 * Created by Admin on 14.09.14.
 */
public class LoginEdit extends EditText {
    public LoginEdit(Context context) {
        super(context);
        init();
    }

    public LoginEdit(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LoginEdit(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        Typeface tf = null;
        try {
            tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/TTWPGOTT.ttf");
        } catch (Exception e) {
            Log.e("Vendetta", "Could not get typeface: " + e.getMessage());
        }

        setTypeface(tf);
    }
}
