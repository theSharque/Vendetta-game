package club.vendetta.game;

import android.app.Activity;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.TextView;

public class SoundListener implements View.OnClickListener {
    private final TextView tvSound;
    Activity aMain;

    public SoundListener(Activity drumMenu, TextView tvSound) {
        this.tvSound = tvSound;
        this.aMain = drumMenu;
    }

    @Override
    public void onClick(View view) {
        SharedPreferences settings = aMain.getSharedPreferences(Main.PREFS_NAME, 0);

        if (Main.bSound) {
            Main.bSound = false;
            tvSound.setText(R.string.off);
            settings.edit().putBoolean("sound", false).apply();
        } else {
            Main.bSound = true;
            tvSound.setText(R.string.on);
            settings.edit().putBoolean("sound", true).apply();
        }
    }
}
