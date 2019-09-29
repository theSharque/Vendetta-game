package club.vendetta.game.misc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import club.vendetta.game.Main;
import club.vendetta.game.R;

public class Debug extends ActionBarActivity {

    Handler lRefresh = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            SharedPreferences spLoader = getSharedPreferences(Main.class.getSimpleName() + "msg", Context.MODE_PRIVATE);
            SharedPreferences prefs = getSharedPreferences(Main.class.getSimpleName(), Context.MODE_PRIVATE);
            int iMin = spLoader.getInt("iMin", 0);
            int iMax = spLoader.getInt("iMax", 0);
            String sBsid = prefs.getString(Main.PROPERTY_BSSID, null);

            String sDebug = "Debug\n";
            if (Main.lMain != null) {
                sDebug += "IMEI: " + Main.lMain.sImei + "\n";
            }
            sDebug += "BSID: " + sBsid + "\n";
            sDebug += "UID: " + Main.sId + "\n";
            sDebug += "Login: " + Main.sLogin + "\n";
            sDebug += "Msg count: " + Main.sMsgCount + "\n";
            sDebug += "Photo: " + Main.sPhotoUrl + "\n";
            sDebug += "Pass: " + Main.sVictimPass + "\n";
            sDebug += "Ans: " + Main.sVictimAnswer + "\n";
            sDebug += "Is online: " + Main.lMain.isOnline + "\n";
            sDebug += "Is die: " + Main.bDie + "\n";
            sDebug += "Is in game: " + Main.bInGame + "\n";
            sDebug += "Is loged in: " + Main.bLogedIn + "\n";
            sDebug += "Game ID: " + Main.iGameId + "\n";
            sDebug += "Game state: " + Main.iGameState + "\n";
            sDebug += "Stored messages: " + (iMax - iMin) + "\n";
            sDebug += "Last ping " + Math.round((System.currentTimeMillis() - prefs.getLong("lastPing", 0)) / 1000) + " seconds ago";

            TextView tvDebug = (TextView) findViewById(R.id.debug);
            tvDebug.setText(sDebug);

            lRefresh.sendEmptyMessageDelayed(0, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        lRefresh.sendEmptyMessage(0);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
