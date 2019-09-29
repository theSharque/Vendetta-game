package club.vendetta.game;

import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.appsflyer.AppsFlyerLib;

public class Main extends ActionBarActivity {
    public static final String MESSAGE_GATE = "vendetta.club";
    public static final String BASE_URL = "http://vendetta.club:2000/";
    public static final String BASE_SEC_URL = "https://vendetta.club:2001/";
    public static final String PREFS_NAME = "MainPrefs";
    public static final String LOCAL_CACHE = "/photos/";
    public static final String AGGREEMENT = "Agreement";
    public final static String FILTER_NAME = "vendetta";
    public static final IntentFilter ifMessager = new IntentFilter(Main.FILTER_NAME);
    public static final String PROPERTY_BSSID = "MY_BSSID";
    public static final BroadcastReceiver brMessager = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancelAll();
            ((ActionBarActivity) context).supportInvalidateOptionsMenu();
        }
    };
    public volatile static boolean bSound;
    public volatile static boolean bLogedIn;
    public volatile static boolean bInGame;
    public volatile static boolean bNeedSign;
    public volatile static boolean bNeedApprove;
    public volatile static int iPlayedGames;
    public volatile static int iGameId;
    public volatile static int iScores = 0;
    public volatile static int iWins = 0;
    public volatile static int iGameState = 1;
    public volatile static String sId;
    public volatile static String sLogin;
    public volatile static String sVictimPass;
    public volatile static String sVictimAnswer;
    public volatile static Loader lMain;
    public volatile static boolean bDie;
    public volatile static String sMsgCount;
    public volatile static String sPhotoUrl = "";

    public synchronized static void btOn(Context con) {
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            Intent btIntent = new Intent(con, BlueActivity.class);
            btIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            con.startActivity(btIntent);
        }
    }

    public synchronized static void menuNumbers(Menu menu, float fDiff, Context context) {
        if (Main.lMain == null) {
            Main.lMain = new Loader(context);
        }
        Main.lMain.login();

        MenuItem item = menu.findItem(R.id.action_message);
        Drawable drMail = item.getIcon();

        if (Main.sMsgCount == null || Main.sMsgCount.equals("0")) {
            item.setEnabled(false);
            drMail.setAlpha(64);
            item.setIcon(drMail);
        } else {
            item.setEnabled(true);
            drMail.setAlpha(255);

            Bitmap.Config conf = Bitmap.Config.ARGB_8888;
            Bitmap bmMail = Bitmap.createBitmap((int) (drMail.getIntrinsicWidth() * fDiff) + 40, (int) (drMail.getIntrinsicHeight() * fDiff), conf);

            Canvas cvMail = new Canvas(bmMail);
            Paint paint = new Paint();
            paint.setColor(0xBBFFFFFF);
            paint.setTextAlign(Paint.Align.RIGHT);
            paint.setTextSize(16 * fDiff);
            paint.setFakeBoldText(true);

            drMail.setBounds(40, 0, (int) (drMail.getIntrinsicWidth() * fDiff) + 40, (int) (drMail.getIntrinsicHeight() * fDiff));
            drMail.draw(cvMail);
            cvMail.drawText(Main.sMsgCount, 40, cvMail.getHeight() * 4 / 5, paint);

            item.setIcon(new BitmapDrawable(bmMail));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = getSharedPreferences(Main.class.getSimpleName(), Context.MODE_PRIVATE);

        if (!prefs.getBoolean(AGGREEMENT, false)) {
            startActivity(new Intent(getApplicationContext(), Agreement.class));
            this.finish();
            return;
        }

        AppsFlyerLib.setAppsFlyerKey("b9ZjZUnDEdkA5dFwFPSxch");
        AppsFlyerLib.sendTracking(getApplicationContext());

        lMain = new Loader(getApplicationContext());

        Main.lMain.login();
        sendBroadcast(new Intent("club.vendetta.game.RESTART_SOCKET"));

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        bSound = settings.getBoolean("sound", true);

        if (!bLogedIn) {
            lMain.login();
        }

        Main.lMain.SetMessages(getIntent().getIntExtra("mid", 0));
        DrumMenu.context = getApplicationContext();

        if (bLogedIn && !bNeedApprove && !sPhotoUrl.equals("")) {
            if (bInGame) {
                Intent iScreen = new Intent(getApplicationContext(), GameDetail.class);
                iScreen.putExtra("game", iGameId);
                iScreen.putExtra("classname", "DrumMenu");
                startActivity(iScreen);
                this.finish();
            } else {
                startActivity(new Intent(getApplicationContext(), DrumMenu.class));
                this.finish();
            }
        } else {
            startActivity(new Intent(getApplicationContext(), UserInfo.class));
            this.finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
