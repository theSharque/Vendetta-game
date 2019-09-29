package club.vendetta.game;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Rect;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import club.vendetta.game.misc.PingSend;

public class VictimDetail extends ActionBarActivity {
    private AlertDialog.Builder ad;
    private String sVictimBsid;
    private String sVictimPass;
    private Location lUser = new Location("gamer");
    private boolean bResult;
    private Bitmap bmPhoto;
    private int iId;
    private String sLogin;
    private boolean bKilled;
    private String sLocation;
    private ColorMatrixColorFilter bwFilter;

    private final Handler hShow = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                case 1:
                    findViewById(R.id.kill_pic).setVisibility(View.VISIBLE);
                    ((ImageButton) findViewById(R.id.avatar)).setColorFilter(bwFilter);
                    bKilled = true;
                    break;

                case 2:
                    ad.show();
                    break;

                case 3:
                    Main.lMain.updateUser(iId);
                    if (Main.lMain.GetValue("err").equals("0") && Main.lMain.GetValue("l").contains(":")) {
                        sLocation = Main.lMain.GetValue("l");

                        if (Main.lMain.GetValue("d").equals("1")) {
                            hShow.sendEmptyMessage(1);
                        }
                        String[] sLoc = sLocation.split(":");
                        lUser.setLatitude(Double.parseDouble(sLoc[0]));
                        lUser.setLongitude(Double.parseDouble(sLoc[1]));
                        lUser.setAltitude(0);

                        Location lMe = PingSend.GiveMe(getApplicationContext());

                        if (Math.round(lMe.distanceTo(lUser)) < 1000) {
                            ((TextView) findViewById(R.id.distance)).setText(Html.fromHtml(String.format(
                                            getString(R.string.meters),
                                            Math.round(lMe.distanceTo(lUser))))
                            );
                        } else {
                            ((TextView) findViewById(R.id.distance)).setText(Html.fromHtml(String.format(
                                            getString(R.string.kilometers),
                                            Math.round(lMe.distanceTo(lUser) / 1000)))
                            );
                        }
                    }
                    this.sendEmptyMessageDelayed(3, 60000);
                    break;
            }
        }
    };
    private Integer iKills, iWins;
    private String sClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_victim_detail);

        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);
        bwFilter = new ColorMatrixColorFilter(matrix);

        ad = new AlertDialog.Builder(this);
        ad.setTitle(getString(R.string.msgMissed)); // сообщение
        ad.setMessage(getString(R.string.msgMissed)); // сообщение
        ad.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
            }
        });
        ad.setCancelable(false);

        bKilled = false;

        Rect rect = new Rect();
        Window win = getWindow();  // Get the Window
        win.getDecorView().getWindowVisibleDisplayFrame(rect);

        Intent intent = getIntent();
        sClass = intent.getStringExtra("classname");

        iId = intent.getIntExtra("id", 0);
        sLocation = intent.getStringExtra("location");
        sLogin = intent.getStringExtra("login");
        bmPhoto = intent.getParcelableExtra("photo");
        sVictimBsid = intent.getStringExtra("bsid");
        sVictimPass = intent.getStringExtra("password");

        iKills = Integer.valueOf(intent.getStringExtra("kills"));
        iWins = Integer.valueOf(intent.getStringExtra("wins"));

        ((TextView) findViewById(R.id.scores)).setText(Html.fromHtml(String.format(getString(R.string.scoresItem), iKills)));
        ((TextView) findViewById(R.id.wins)).setText(Html.fromHtml(String.format(getString(R.string.winsItem), iWins)));

        ((TextView) findViewById(R.id.login)).setText(sLogin);
        ((ImageButton) findViewById(R.id.avatar)).setImageBitmap(bmPhoto);

        ImageButton ibAvatar = ((ImageButton) findViewById(R.id.avatar));
        LinearLayout.LayoutParams lpAvatar = (LinearLayout.LayoutParams) ibAvatar.getLayoutParams();

        int iMinH = rect.height() * 45 / 100;
        int iMinW = rect.width() * 90 / 100;

        ImageView ivGlobe = (ImageView) findViewById(R.id.globe);
        LinearLayout.LayoutParams llGlobe = (LinearLayout.LayoutParams) ivGlobe.getLayoutParams();
        llGlobe.width = rect.width() / 9;
        llGlobe.height = rect.width() / 9;
        ivGlobe.setLayoutParams(llGlobe);

        LinearLayout llDistance = (LinearLayout) findViewById(R.id.dist_layout);
        RelativeLayout.LayoutParams rlDist = (RelativeLayout.LayoutParams) llDistance.getLayoutParams();

        ImageView ivKillPis = (ImageView) findViewById(R.id.kill_pic);
        RelativeLayout.LayoutParams rlKill = (RelativeLayout.LayoutParams) ivKillPis.getLayoutParams();

        if (iMinH < iMinW) {
            lpAvatar.height = iMinH;
            lpAvatar.width = iMinH;
            rlKill.width = iMinH;
            rlKill.height = iMinH;
            rlDist.topMargin = iMinH - llGlobe.height * 6 / 10;
        } else {
            lpAvatar.height = iMinW;
            lpAvatar.width = iMinW;
            rlKill.width = iMinW;
            rlKill.height = iMinW;
            rlDist.topMargin = iMinW - llGlobe.height * 6 / 10;
        }

        ibAvatar.setLayoutParams(lpAvatar);
        llDistance.setLayoutParams(rlDist);

        ivKillPis.setLayoutParams(rlKill);

        hShow.sendEmptyMessage(3);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(Main.brMessager);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(Main.brMessager, Main.ifMessager);

        supportInvalidateOptionsMenu();
        Main.lMain.login();
        if (Main.bDie) {
            this.finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.all, menu);
        Main.menuNumbers(menu, getResources().getDisplayMetrics().scaledDensity, getApplicationContext());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_message:
                Intent iMsg = new Intent(getApplicationContext(), Messages.class);
                startActivity(iMsg);
                return true;

            case R.id.action_help:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void fire(View view) {
        if (bKilled)
            return;

        final ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle(getString(R.string.msgFireTitle));
        pd.setMessage(getString(R.string.msgFireText));
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setIndeterminate(true);

        pd.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (bResult) {
                    hShow.sendEmptyMessage(1);
                } else {
                    hShow.sendEmptyMessage(2);
                }
            }
        });

        final Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                BluetoothAdapter BA = BluetoothAdapter.getDefaultAdapter();
                BluetoothSocket sConnection;
                String sKey = "";
                OutputStream sOut;
                InputStream sIn;
                BA.cancelDiscovery();

                BluetoothDevice BD = BA.getRemoteDevice(sVictimBsid);
                boolean bConnected = false;
                try {
//                    sConnection = BD.createInsecureRfcommSocketToServiceRecord(uuid);
                    sConnection = InsecureBluetooth.createRfcommSocket(BD, 29, false);
                } catch (IOException e) {
                    return;
                }

                try {
                    sConnection.connect();
                    bConnected = true;
                } catch (IOException e) {
                    try {
                        sConnection.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    e.printStackTrace();
                }

                try {
                    if (bConnected) {
                        sIn = sConnection.getInputStream();
                        sOut = sConnection.getOutputStream();
                        sOut.write((sVictimPass + "\n").getBytes());
                        BufferedReader reader = new BufferedReader(new InputStreamReader(sIn));
                        sKey = reader.readLine();
                        sIn.close();
                        sOut.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (!sKey.trim().equals("false") && !sKey.trim().equals("")) {
                    if (!Main.lMain.SendMessage(0, 8, 0, sKey)) {
                        Toast toast;
                        toast = Toast.makeText(getApplicationContext(), getString(R.string.network_problem), Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }

                    bResult = true;
                } else {
                    bResult = false;
                }

                try {
                    BA.cancelDiscovery();
                    sConnection.close();
                    Thread.sleep(1000);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                pd.dismiss();
            }
        });

        pd.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                t.interrupt();
            }
        });
        pd.show();

        t.start();
    }

    public void OpenMap(View view) {
        Intent iMap = new Intent(getApplicationContext(), MapActivity.class);
        iMap.putExtra("id", iId);
        iMap.putExtra("Login", sLogin);
        iMap.putExtra("photo", bmPhoto);
        iMap.putExtra("location", sLocation);
        iMap.putExtra("bsid", sVictimBsid);
        iMap.putExtra("password", sVictimPass);
        iMap.putExtra("kills", String.valueOf(iKills));
        iMap.putExtra("wins", String.valueOf(iWins));

        startActivity(iMap);
    }

    @Override
    public void onBackPressed() {
        back(null);
    }

    public void back(View view) {
        Intent iBack = null;
        try {
            iBack = new Intent(getApplicationContext(), Class.forName(BuildConfig.APPLICATION_ID + "." + sClass));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if (sClass.equals("GameDetail") && iBack != null) {
            iBack.putExtra("classname", "DrumMenu");
            iBack.putExtra("game", Main.iGameId);
            iBack.putExtra("invite", false);
        }

        startActivity(iBack);
        this.finish();
    }
}
