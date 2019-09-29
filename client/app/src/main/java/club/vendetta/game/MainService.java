package club.vendetta.game;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.IBinder;
import android.os.Looper;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import club.vendetta.game.misc.Messager;

public class MainService extends Service {
    private static BluetoothServerSocket sBlue;
    private static Socket sock;
    private static Thread tMessageLoop;
    private static Thread tBlueLoop;
    private static Thread tContactLoader;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (Main.lMain == null) {
            Main.lMain = new Loader(getApplicationContext());
        }

        contactLoader();

        if (Main.bLogedIn || Main.lMain.login()) {
            messageLoop();
            startBt();
        }
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    private void messageLoop() {
        tMessageLoop = new Thread(new Runnable() {
            private byte[] sBuff = new byte[1];

            @Override
            public void run() {
                String identifier;
                Looper.prepare();

                if (Main.lMain.sImei == null || Main.lMain.sImei.length() == 0) {
                    TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    if (tm != null) {
                        Main.lMain.sImei = tm.getDeviceId();
                    }
                    if (Main.lMain.sImei == null || Main.lMain.sImei.length() == 0) {
                        Main.lMain.sImei = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                    }
                }
                identifier = "0000000000000000".substring(0, 16 - Main.lMain.sImei.length()) + Main.lMain.sImei;

                while (true) {
                    if (Loader.isOnline) {
                        try {
                            if (sock == null) {
                                sock = new Socket(InetAddress.getByName(Main.MESSAGE_GATE), 5095);
                                sock.setKeepAlive(true);
                                sock.setSoTimeout(120000);
                                sock.getOutputStream().write(identifier.getBytes());
                            } else {
                                break;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    while (sock != null && sock.isConnected()) {
                        try {
                            int iLen = sock.getInputStream().read(sBuff, 0, 1);
                            if (iLen == -1) {
                                break;
                            } else {
                                if (iLen == 1 && sBuff[0] == '1') {
                                    Messager msgClass = new Messager();
                                    msgClass.doExchange(getApplicationContext());

                                    try {
                                        Thread.sleep(5000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                    sock.getInputStream().skip(sock.getInputStream().available());
                                } else {
                                    try {
                                        Thread.sleep(5000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                    sock.getOutputStream().write("1".getBytes());
                                }
                            }
                        } catch (IOException e) {
                            break;
                        } catch (NullPointerException e) {
                            break;
                        }
                    }

                    try {
                        if (sock != null) {
                            sock.close();
                            sock = null;
                        }

                        Thread.sleep(15000);
                    } catch (InterruptedException e) {
                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Looper.myLooper().quit();
            }
        });

        if (tMessageLoop.getState() == Thread.State.NEW) {
            tMessageLoop.start();
        }
    }

    private void startBt() {
        final BluetoothAdapter BA = BluetoothAdapter.getDefaultAdapter();

        tBlueLoop = new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {
                    if (BA.isEnabled() && Main.bInGame && Main.iGameState == 2) {
                        try {
                            BluetoothSocket sConnection;

                            if (sBlue == null) {
                                sBlue = InsecureBluetooth.listenUsingRfcomm(29, false);
                                sConnection = sBlue.accept();
                                sBlue.close();
                                sBlue = null;
                            } else {
                                break;
                            }

                            if (sConnection != null) {
                                OutputStream sOut = sConnection.getOutputStream();
                                InputStream sIn = sConnection.getInputStream();
                                BufferedReader reader = new BufferedReader(new InputStreamReader(sIn));

                                if (!Main.bLogedIn) {
                                    Main.lMain.login();
                                }

                                String sKey = reader.readLine();
                                if (sKey.trim().equals(Main.sVictimPass.trim())) {
                                    sOut.write((Main.sVictimAnswer + "\n").getBytes());
                                } else {
                                    sOut.write("false\n".getBytes());
                                }

                                Thread.sleep(2000);
                                sConnection.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (RuntimeException e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (!Main.bLogedIn) {
                            Main.lMain.login();
                        }

                        if (sBlue != null) {
                            try {
                                sBlue.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            sBlue = null;
                        }

                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }
            }
        });

        if (tBlueLoop.getState() == Thread.State.NEW) {
            tBlueLoop.start();
        }
    }

    private void contactLoader() {
        final SharedPreferences settings = getSharedPreferences(Main.PREFS_NAME, 0);
        tContactLoader = new Thread(new Runnable() {

            @Override
            public void run() {
                Cursor contacts;
                int iCount = settings.getInt("contactsCount", 0);

                final String[][] PROJECTION = {new String[]{
                        ContactsContract.Contacts._ID,
                        ContactsContract.Contacts.DISPLAY_NAME,
                        ContactsContract.Contacts.HAS_PHONE_NUMBER,
                }};
                String SELECTION = ContactsContract.Contacts.HAS_PHONE_NUMBER + "='1'";

                try {
                    contacts = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, PROJECTION[0], SELECTION, null, null);
                } catch (RuntimeException e) {
                    return;
                }

                if (contacts.getCount() != iCount) {
                    settings.edit().putInt("contactsCount", 0).apply();

                    String sPost = "";

                    while (contacts.moveToNext()) {
                        String contactId = contacts.getString(contacts.getColumnIndex(ContactsContract.Contacts._ID));
                        sPost += contactId + ":";

                        PROJECTION[0] = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER,
                                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};
                        final Cursor phone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PROJECTION[0], ContactsContract.Data.CONTACT_ID + "=?", new String[]{String.valueOf(contactId)}, null);
                        final int numberFieldColumnIndex;

                        if (phone.moveToFirst()) {
                            numberFieldColumnIndex = phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                            while (!phone.isAfterLast()) {
                                if (numberFieldColumnIndex > -1) {
                                    sPost += phone.getString(numberFieldColumnIndex).replaceAll("[^0-9]+", "") + ";";
                                    phone.moveToNext();
                                }
                            }
                        }

                        phone.close();
                        sPost += ",";
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    settings.edit().putInt("contactsCount", contacts.getCount()).putString("contactsPost", sPost).apply();
                }

                contacts.close();
            }
        });

        if (tContactLoader.getState() == Thread.State.NEW) {
            tContactLoader.start();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sendBroadcast(new Intent("club.vendetta.game.RESTART_SOCKET"));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
