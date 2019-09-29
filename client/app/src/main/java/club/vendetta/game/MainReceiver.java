package club.vendetta.game;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;

import club.vendetta.game.misc.Messager;
import club.vendetta.game.misc.PingSend;

public class MainReceiver extends BroadcastReceiver {

    public static void resendMessage(Context con) {
        Loader loader = new Loader(con);
        SharedPreferences spLoader = con.getSharedPreferences(Main.class.getSimpleName() + "msg", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = spLoader.edit();
        int iMin = spLoader.getInt("iMin", 0);
        int iMax = spLoader.getInt("iMax", 0);

        while (iMin < iMax) {
            String msg = spLoader.getString("msg" + iMin, "");
            if (msg.equals("")) {
                ++iMin;
            } else {
                String[] sVals = msg.split(":");
                boolean bRet;

                if (sVals.length == 3) {
                    bRet = loader.SendMessage(Integer.valueOf(sVals[0]), Integer.valueOf(sVals[1]), Integer.valueOf(sVals[2]), "");
                } else {
                    bRet = loader.SendMessage(Integer.valueOf(sVals[0]), Integer.valueOf(sVals[1]), Integer.valueOf(sVals[2]), sVals[3]);
                }

                if (bRet) {
                    editor.remove("msg" + iMin);
                    ++iMin;
                } else {
                    break;
                }
            }
        }

        if (iMin == iMax) {
            editor.putInt("iMin", 0);
            editor.putInt("iMax", 0);
        } else {
            editor.putInt("iMin", iMin);
        }

        editor.apply();
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences(Main.class.getSimpleName(), Context.MODE_PRIVATE);
        String sAction = intent.getAction();
        context.startService(new Intent(context.getApplicationContext(), MainService.class));

        if (sAction != null && sAction.equals("club.vendetta.game.RESTART_SOCKET")) {
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent iAlarm = new Intent(context, MainReceiver.class);
            iAlarm.setAction("club.vendetta.game.RESTART_SOCKET");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
                    iAlarm, PendingIntent.FLAG_CANCEL_CURRENT);
            am.cancel(pendingIntent);
            am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60000, pendingIntent);
        }

        if (Main.iGameId > 0 ) {
            if(Main.iGameState == 2) {
                if (prefs.getLong("lastPing", 0) + 5 * 60 * 1000 < System.currentTimeMillis()) {
                    PingSend.ping(context);
                }
            } else if(Main.iGameState == 1) {
                if (prefs.getLong("lastPing", 0) + 60 * 60 * 1000 < System.currentTimeMillis()) {
                    PingSend.ping(context);
                }
            }
        }

        if (sAction != null && sAction.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_OFF) {
                if (!Main.bLogedIn) {
                    Main.lMain.login();
                }

                if ((Main.iGameId > 0 && Main.iGameState == 2)) {
                    Main.btOn(context);
                }
            }

            if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_ON) {
                String sBsid = prefs.getString(Main.PROPERTY_BSSID, null);
                if (sBsid == null) {
                    sBsid = BluetoothAdapter.getDefaultAdapter().getAddress();
                    prefs.edit().putString(Main.PROPERTY_BSSID, sBsid).apply();
                }
            }
        }

        if (sAction != null && sAction.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            if (!intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)) {
                Loader.isOnline = true;

                resendMessage(context);

                Messager msgClass = new Messager();
                msgClass.doExchange(context);
            } else {
                Loader.isOnline = false;

                if( !Loader.isOnline ) {
                    Main.bLogedIn = false;
                }
            }
        }
    }
}
