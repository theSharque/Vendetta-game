package club.vendetta.game;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

public class LocationActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transparent);

        final Context context = getApplicationContext();

        final AlertDialog.Builder ad = new AlertDialog.Builder(context);
        ad.setTitle(getString(R.string.app_name)); // сообщение
        ad.setMessage(getString(R.string.msgLocationText)); // сообщение

        ad.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                finish();
            }
        });

        ad.setCancelable(false);
        ad.show();
    }
}
