package club.vendetta.game;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.ProgressBar;

public class BlueActivity extends Activity {

    private BluetoothAdapter BA = null;
    private boolean bTurnOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transparent);

        final Context context = BlueActivity.this;

        BA = BluetoothAdapter.getDefaultAdapter();
        bTurnOn = true;
        if (!BA.isEnabled()) {
            final AlertDialog.Builder ad = new AlertDialog.Builder(context);
            ad.setTitle(getString(R.string.msgBluetoothOnTitle)); // сообщение
            ad.setMessage(getString(R.string.msgBluetoothOnText)); // сообщение

            ad.setPositiveButton(R.string.btDialogOn, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg1) {
                    BA.enable();
                    String sBsid = BluetoothAdapter.getDefaultAdapter().getAddress();

                    SharedPreferences prefs = context.getSharedPreferences(Main.class.getSimpleName(), Context.MODE_PRIVATE);
                    if (prefs != null && sBsid != null) {
                        prefs.edit().putString(Main.PROPERTY_BSSID, sBsid).apply();
                    }
                    finish();
                }
            });

            ad.setNegativeButton(R.string.btDialogOff, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg1) {
                    bTurnOn = false;
                    if (Main.lMain.SendMessage(0, 4, 0, "")) {
                        Main.lMain.SaveMessage(context, 0, 4, 0, "");
                    }
                    finish();
                }
            });

            final ProgressBar vTimer = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            vTimer.setIndeterminate(false);
            vTimer.setMax(50);
            vTimer.setProgress(0);
            ad.setView(vTimer);

            ad.setCancelable(false);
            final AlertDialog dialog = ad.show();

            new CountDownTimer(5000, 100) {
                @Override
                public void onTick(long millisUntilFinished) {
                    vTimer.setProgress(Math.round(millisUntilFinished / 100));
                    ad.setView(vTimer);
                }

                @Override
                public void onFinish() {
                    if(bTurnOn) {
                        BA.enable();
                        dialog.dismiss();
                        String sBsid = BluetoothAdapter.getDefaultAdapter().getAddress();

                        SharedPreferences prefs = context.getSharedPreferences(Main.class.getSimpleName(), Context.MODE_PRIVATE);
                        if (prefs != null && sBsid != null) {
                            prefs.edit().putString(Main.PROPERTY_BSSID, sBsid).apply();
                        }
                    }
                    finish();
                }
            }.start();
        } else {
            Intent btIntent = new Intent(context, Main.class);
            context.startActivity(btIntent);

            this.finish();
        }
    }
}
