package club.vendetta.game;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class Install extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null && extras.getString("referrer") != null) {
            context.getSharedPreferences(Main.PREFS_NAME, 0)
                    .edit()
                    .putString("referal", extras.getString("referrer"))
                    .apply();
        }
    }
}
