package club.vendetta.game;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import club.vendetta.game.misc.GameAdapter;
import club.vendetta.game.misc.MessageAdapter;
import club.vendetta.game.misc.PlayerAdapter;
import club.vendetta.game.misc.ServerMessage;

/**
 * Created by sharque on 05.08.14.
 * Loader - all HTTP action is here.
 */
public class Loader {
    private static final int TIMEOUT = 2000;
    public volatile static boolean isOnline;
    public volatile Map<String, String> mValues = new HashMap<String, String>();
    public volatile Bitmap bitmap = null;
    public volatile long oldTime;
    public volatile String sImei = "";

    public Loader(Context context) {
        if (sImei == null || sImei.length() == 0) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (tm != null) {
                sImei = tm.getDeviceId();
            }
            if (sImei == null || sImei.length() == 0) {
                sImei = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            }
        }
        NetworkInfo netInfo = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        isOnline = (netInfo != null && netInfo.isConnectedOrConnecting());

        if( !isOnline ) {
            Main.bLogedIn = false;
        }
    }

    public boolean login() {
        long newTime = System.currentTimeMillis();
        if (isOnline && !Main.bLogedIn || oldTime < newTime - TIMEOUT) {

            synchronized (this) {
                Main.bLogedIn = false;
                if (DownloadPage(Main.BASE_URL + "login.php?b=" + sImei)) {
                    try {
                        if (GetValue("err").equals("0")) {
                            Main.sLogin = GetValue("login");
                            Main.sId = GetValue("id");
                            Main.iGameId = Integer.valueOf(GetValue("ig"));
                            Main.bInGame = Main.iGameId > 0;
                            Main.sVictimPass = GetValue("vp");
                            Main.sVictimAnswer = GetValue("va");
                            Main.iPlayedGames = Integer.valueOf(GetValue("gm"));
                            Main.iScores = Integer.valueOf(GetValue("kl"));
                            Main.iWins = Integer.valueOf(GetValue("wn"));
                            Main.bDie = GetValue("die").equals("1");
                            Main.sMsgCount = GetValue("msg");

                            if (Main.bInGame && GetValue("gs").matches("[0-9]")) {
                                Main.iGameState = Integer.valueOf(GetValue("gs"));
                            } else {
                                Main.iGameState = 0;
                            }

                            Main.bLogedIn = true;
                            Main.sPhotoUrl = GetValue("photo");
                            Main.bNeedApprove = GetValue("na").equals("1");
                        } else if (GetValue("err").equals("1")) {
                            Main.bNeedSign = true;
                        }
                    } catch ( NumberFormatException e ) {
                        e.printStackTrace();
                    }
                }
            }
        }

        oldTime = newTime;

        return Main.bLogedIn;
    }

    public boolean SignIn(String sLogin, String sPhoneNumber) {
        if (isOnline) {
            Main.bLogedIn = false;
            String sTemp = null;
            try {
                sTemp = URLEncoder.encode(sLogin, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            synchronized (this) {
                if (DownloadPage(Main.BASE_URL + "signin.php?b=" + sImei + "&l=" + sTemp + "&p=" + sPhoneNumber)) {
                    if (GetValue("err").equals("0")) {
                        Main.sLogin = sLogin;
                        Main.sId = GetValue("id");
                        Main.bLogedIn = true;
                    } else if (GetValue("err").equals("1")) {
                        Main.bNeedSign = false;
                    }
                }

                return Main.bLogedIn;
            }
        } else {
            return false;
        }
    }

    public  boolean GameList(Context con, GameAdapter gaGames) {
        String sInGame = "";

        if (isOnline) {
            synchronized(this) {
                if (DownloadPage(Main.BASE_URL + "games.php?b=" + sImei)) {
                    if (GetValue("err").equals("0")) {
                        if (!GetValue("cnt").equals("0")) {
                            sInGame = GetValue("ig");
                            Main.iGameId = Integer.valueOf(sInGame);
                            if (Main.iGameId > 0 && GetValue("gs").matches("[0-9]")) {
                                Main.iGameState = Integer.valueOf(GetValue("gs"));
                            }

                            for (int i = 0; i < Integer.valueOf(GetValue("cnt")); ++i) {
                                Bitmap bmPhoto;
                                if (!GetValue("p" + i).equals("") && DownloadBitmap(GetValue("p" + i))) {
                                    bmPhoto = bitmap;
                                } else {
                                    bmPhoto = BitmapFactory.decodeResource(con.getResources(), android.R.drawable.ic_dialog_alert);
                                }

                                try {
                                    gaGames.add(
                                            Integer.valueOf(GetValue("id" + i)),
                                            bmPhoto,
                                            Integer.valueOf(GetValue("gt" + i)),
                                            Integer.valueOf(GetValue("et" + i)),
                                            Integer.valueOf(GetValue("st" + i)),
                                            Integer.valueOf(GetValue("sc" + i)),
                                            Integer.valueOf(GetValue("ig" + i)),
                                            Integer.valueOf(GetValue("vc" + i))
                                    );
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } else if (GetValue("err").equals("1")) {
                        Main.bLogedIn = false;
                        Main.bNeedSign = true;
                    }
                }

                return !sInGame.equals("0") && !sInGame.equals("");
            }
        } else {
            return false;
        }
    }

    public synchronized boolean SaveGame(int iGameType, int iStartType, int iStartCount, int iVictimsCount, int iEndType) {
        return DownloadPage(Main.BASE_URL + "savegame.php?b=" + sImei + "&gt=" + iGameType + "&st=" + iStartType + "&sc=" + iStartCount + "&vc=" + iVictimsCount + "&et=" + iEndType) && GetValue("err").equals("0");
    }

    public synchronized boolean Approver(Context con, String sPost, PlayerAdapter paContacts, int iGame, Location lMe) {
        if (UploadString(Main.BASE_SEC_URL + "contacts.php?b=" + sImei + "&g=" + iGame, sPost)) {
            if (!GetValue("cnt").equals("0") && !GetValue("cnt").equals("")) {
                for (int i = 0; i < Integer.valueOf(GetValue("cnt")); ++i) {
                    Bitmap bmPhoto;
                    if (!GetValue("p" + i).equals("") && DownloadBitmap(GetValue("p" + i))) {
                        bmPhoto = bitmap;
                    } else {
                        bmPhoto = BitmapFactory.decodeResource(con.getResources(), android.R.drawable.ic_dialog_alert);
                    }

                    paContacts.add(
                            Integer.valueOf(GetValue("u" + i)),
                            bmPhoto,
                            GetValue("n" + i),
                            0,
                            lMe,
                            GetValue("g" + i),
                            "",
                            "",
                            GetValue("w" + i),
                            GetValue("k" + i),
                            0,
                            "",
                            GetValue("r" + i).equals("1")
                    );
                }
            }

            return true;
        } else {
            return false;
        }
    }

    public synchronized boolean GetMessages(List<ServerMessage> smTemp) {
        long newTime = System.currentTimeMillis();
        if (!Main.bLogedIn || oldTime < newTime - TIMEOUT) {
            DownloadPage(Main.BASE_URL + "getmsg.php?b=" + sImei + "&s=1");
            if (GetValue("err").equals("0") && !GetValue("cnt").equals("")) {
                for (int i = 0; i < Integer.valueOf(GetValue("cnt")); ++i) {
                    smTemp.add(new ServerMessage(
                                    Integer.valueOf(GetValue("i" + i)),
                                    Integer.valueOf(GetValue("t" + i)),
                                    GetValue("s" + i),
                                    Integer.valueOf(GetValue("a" + i)),
                                    GetValue("b" + i))
                    );
                }
            } else {
                return false;
            }
        }
        oldTime = newTime;
        return true;
    }

    public boolean Messages(MessageAdapter maMessages) {
        DownloadPage(Main.BASE_URL + "getmsg.php?b=" + sImei + "&s=2");
        if (GetValue("err").equals("0")) {
            for (int i = 0; i < Integer.valueOf(GetValue("cnt")); ++i) {
                maMessages.add(Integer.valueOf(GetValue("i" + i)),
                        Integer.valueOf(GetValue("t" + i)),
                        GetValue("s" + i),
                        Integer.valueOf(GetValue("a" + i)),
                        GetValue("b" + i)
                );
            }

            return true;
        } else {
            return false;
        }
    }

    public synchronized boolean SendMessage(int iTo, int iType, int iArg, String sArg2) {
        return DownloadPage(Main.BASE_URL + "message.php?b=" + sImei + "&a=" + iArg + "&t=" + iType + "&u=" + iTo + "&a2=" + sArg2) && GetValue("err").equals("0");
    }

    public synchronized void SaveMessage(Context con, int iTo, int iType, int iArg, String sArg2) {
        SharedPreferences spLoader = con.getSharedPreferences(Main.class.getSimpleName() + "msg", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = spLoader.edit();
        int iMax = spLoader.getInt("iMax", 0);

        editor.putString("msg" + iMax, iTo + ":" + iType + ":" + iArg + ":" + sArg2);

        ++iMax;
        editor.putInt("iMax", iMax);
        editor.apply();
    }

    public synchronized boolean SetMessages(int iId) {
        return iId <= 0 || DownloadPage(Main.BASE_URL + "setmsg.php?b=" + sImei + "&id=" + iId, false);
    }

    public synchronized boolean PlayerList(Context con, PlayerAdapter paPlayers, int sGame, Location lMe) {
        String sInGame = "";

        if (DownloadPage(Main.BASE_URL + "players.php?b=" + sImei + "&g=" + sGame)) {
            if (GetValue("err").equals("0") && !GetValue("cnt").equals("0")) {
                sInGame = GetValue("ga");
                GameDetail.iVictimCount = Integer.parseInt(GetValue("vc"));
                GameDetail.iVictimId = Integer.parseInt(GetValue("vi"));
                GameDetail.iStartType = Integer.parseInt(GetValue("st"));
                GameDetail.iTotal = Integer.parseInt(GetValue("pt"));
                GameDetail.iReady = Integer.parseInt(GetValue("pr"));

                paPlayers.iGameType = Integer.valueOf(GetValue("gt"));
                paPlayers.iGameStatus = Integer.valueOf(GetValue("gs"));
                for (int i = 0; i < Integer.valueOf(GetValue("cnt")); ++i) {
                    Bitmap bmPhoto;
                    if (!GetValue("p" + i).equals("") && DownloadBitmap(GetValue("p" + i))) {
                        bmPhoto = bitmap;
                    } else {
                        bmPhoto = BitmapFactory.decodeResource(con.getResources(), android.R.drawable.ic_dialog_alert);
                    }

                    paPlayers.add(
                            Integer.valueOf(GetValue("id" + i)),
                            bmPhoto,
                            GetValue("l" + i),
                            Integer.valueOf(GetValue("a" + i)),
                            lMe,
                            GetValue("g" + i),
                            "",
                            "",
                            GetValue("w" + i),
                            GetValue("k" + i),
                            Integer.valueOf(GetValue("d" + i)) + 1,
                            Integer.valueOf(GetValue("s" + i)),
                            GetValue("kb" + i)
                    );
                }
            } else if (GetValue("err").equals("1")) {
                Main.bLogedIn = false;
                Main.bNeedSign = true;
            }
        } else {
            paPlayers.add(android.R.drawable.ic_dialog_alert, con.getString(R.string.noVictims));
        }

        return !sInGame.equals("0") && !sInGame.equals("");
    }

    public synchronized boolean VictimList(Context con, PlayerAdapter pAdapter, Location lMe) {
        String sInGame = "";

        if (DownloadPage(Main.BASE_URL + "victims.php?b=" + sImei)) {
            if (GetValue("err").equals("0") && !GetValue("cnt").equals("0")) {
                sInGame = GetValue("ig");

                for (int i = 0; i < Integer.valueOf(GetValue("cnt")); ++i) {
                    Bitmap bmPhoto;
                    if (!GetValue("p" + i).equals("") && DownloadBitmap(GetValue("p" + i))) {
                        bmPhoto = bitmap;
                    } else {
                        bmPhoto = BitmapFactory.decodeResource(con.getResources(), android.R.drawable.ic_dialog_alert);
                    }

                    pAdapter.add(
                            Integer.valueOf(GetValue("id" + i)),
                            bmPhoto,
                            GetValue("l" + i),
                            0,
                            lMe,
                            GetValue("g" + i),
                            GetValue("b" + i),
                            GetValue("s" + i),
                            GetValue("w" + i),
                            GetValue("k" + i),
                            1,
                            Integer.valueOf(GetValue("t" + i)),
                            ""
                    );
                }
            } else if (GetValue("err").equals("1")) {
                Main.bLogedIn = false;
                Main.bNeedSign = true;
            }
        } else {
            pAdapter.add(android.R.drawable.ic_dialog_alert, con.getString(R.string.noVictims));
        }

        return !sInGame.equals("0") && !sInGame.equals("");
    }

    public synchronized boolean KillList(Context con, PlayerAdapter paGames, int sGame, int iPlayer, Location lMe) {
        String sInGame = "";

        if (DownloadPage(Main.BASE_URL + "playkill.php?b=" + sImei + "&g=" + sGame + "&p=" + iPlayer)) {
            if (GetValue("err").equals("0") && !GetValue("cnt").equals("0")) {
                sInGame = GetValue("ga");
                for (int i = 0; i < Integer.valueOf(GetValue("cnt")); ++i) {
                    Bitmap bmPhoto;
                    if (!GetValue("p" + i).equals("") && DownloadBitmap(GetValue("p" + i))) {
                        bmPhoto = bitmap;
                    } else {
                        bmPhoto = BitmapFactory.decodeResource(con.getResources(), android.R.drawable.ic_dialog_alert);
                    }

                    paGames.add(
                            Integer.valueOf(GetValue("id" + i)),
                            bmPhoto,
                            GetValue("l" + i),
                            Integer.valueOf(GetValue("a" + i)),
                            lMe,
                            GetValue("g" + i),
                            "",
                            "",
                            GetValue("w" + i),
                            GetValue("k" + i),
                            Integer.valueOf(GetValue("d" + i)),
                            Integer.valueOf(GetValue("s" + i)),
                            GetValue("kb" + i)
                    );
                }
            } else if (GetValue("err").equals("1")) {
                Main.bLogedIn = false;
                Main.bNeedSign = true;
            }
        } else {
            paGames.add(android.R.drawable.ic_dialog_alert, con.getString(R.string.noVictims));
        }
        paGames.delete(iPlayer);

        return !sInGame.equals("0") && !sInGame.equals("");
    }

    public synchronized boolean updateUser(int iId) {
        return DownloadPage(Main.BASE_URL + "updater.php?b=" + sImei + "&u=" + iId);
    }

    public synchronized boolean UploadString(final String pageURL, final String sData) {
        if (isOnline) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String page = "";
                        HttpClient httpClient = new DefaultHttpClient();
                        HttpContext localContext = new BasicHttpContext();
                        HttpPost httpPost = new HttpPost(pageURL);

                        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                        nameValuePairs.add(new BasicNameValuePair("data", sData));
                        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                        HttpResponse response = httpClient.execute(httpPost, localContext);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(
                                response.getEntity().getContent()));

                        String line;
                        while ((line = reader.readLine()) != null) {
                            page += line + "\n";
                        }

                        ParsePage(page.split("&"));
                    } catch (ClientProtocolException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            t.start();
            try {
                Log.d("VENDETTA", pageURL);
                t.join(5000);
                return true;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    public synchronized boolean UploadBitmap(final String pageURL, Bitmap bm) {
        if (isOnline) {
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, 100, bao);

            final byte[] ba = bao.toByteArray();

            final HttpResponse[] response = new HttpResponse[1];
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        HttpClient httpClient = new DefaultHttpClient();
                        HttpContext localContext = new BasicHttpContext();
                        HttpPost httpPost = new HttpPost(pageURL);
                        httpPost.setEntity(new ByteArrayEntity(ba));
                        response[0] = httpClient.execute(httpPost, localContext);
                    } catch (ClientProtocolException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            t.start();
            try {
                Log.d("VENDETTA", pageURL);
                t.join(15000);
                if (response[0].getStatusLine().getStatusCode() == 200) {
                    return true;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    public synchronized boolean DownloadPage(final String pageURL) {
        return DownloadPage(pageURL, true);
    }

    public synchronized boolean DownloadPage(final String pageURL, final boolean bWait) {
        if (isOnline) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String page = "";
                        BufferedReader reader = null;
                        try {
                            URL url = new URL(pageURL);
                            URLConnection urlConnection = url.openConnection();
                            urlConnection.setConnectTimeout(2000);
                            urlConnection.setReadTimeout(2000);
                            reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if (bWait) {
                            String line;
                            while (reader != null && (line = reader.readLine()) != null) {
                                page += line + "\n";
                            }

                            ParsePage(page.split("&"));
                        }
                    } catch (ClientProtocolException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            t.start();
            try {
                Log.d("VENDETTA", pageURL);
                if (bWait) {
                    t.join(5000);
                }
            } catch (InterruptedException e) {
                return false;
            }

            return true;
        } else {
            return false;
        }
    }

    public synchronized boolean DownloadBitmap(final String imageURL) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(Main.BASE_URL + "photos/" + imageURL + ".jpg");
                    URLConnection urlConnection = url.openConnection();
                    urlConnection.setConnectTimeout(2000);
                    urlConnection.setReadTimeout(2000);
                    bitmap = BitmapFactory.decodeStream(urlConnection.getInputStream());
                    cacheBitmap(imageURL);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        if ((bitmap = getCache(imageURL)) == null) {
            if (isOnline) {
                t.start();
                try {
                    Log.d("VENDETTA", imageURL);
                    t.join(5000);
                } catch (InterruptedException e) {
                    return false;
                }

                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public void ParsePage(String[] sLastPage) {
        mValues.clear();
        String[] sVals;
        if (sLastPage != null) {
            for (String aSLastPage : sLastPage) {
                sVals = aSLastPage.split("=");
                if (sVals.length > 1) {
                    mValues.put(sVals[0], sVals[1]);
                } else {
                    mValues.put(sVals[0], "");
                }
            }
        }
    }

    public String GetValue(String sIndex) {
        if (mValues.containsKey(sIndex)) {
            return mValues.get(sIndex);
        } else {
            return "";
        }
    }

    private Bitmap getCache(String imageURL) {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/" + BuildConfig.APPLICATION_ID;

        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        bitmap = BitmapFactory.decodeFile(path + Main.LOCAL_CACHE + imageURL, options);

        return bitmap;
    }

    private void cacheBitmap(String imageURL) {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/" + BuildConfig.APPLICATION_ID;

        File mediaStorageDir = new File(path + Main.LOCAL_CACHE);
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return;
            }
        }

        try {
            FileOutputStream fos = new FileOutputStream(mediaStorageDir + "/" + imageURL);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
