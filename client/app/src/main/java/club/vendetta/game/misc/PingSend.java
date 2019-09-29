package club.vendetta.game.misc;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;

import club.vendetta.game.LocationActivity;
import club.vendetta.game.Main;

public class PingSend {
    private static final int TIMEOUT_IN_MS = 6000;
    private static LocationClient lcPinger;
    private static LocationManager locManager;
    private static int bOld;
    private static int bNew;
    private static boolean bSent = false;
    private static Location location;

    public static void ping(final Context context) {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setAltitudeRequired(false);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        bOld = 1;
        bNew = 1;
        bSent = false;
        final SharedPreferences prefs = context.getSharedPreferences(Main.class.getSimpleName(), Context.MODE_PRIVATE);

        // Old standart
        Looper locLooper = Looper.myLooper();
        locManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        final LocationListener locListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location loc) {
                location = loc;
                if (location != null) {
                    bOld = 2;
                    if (!bSent) {
                        bSent = true;
                        prefs.edit().putLong("lastPing", System.currentTimeMillis()).apply();
                        if (!Main.lMain.SendMessage(0, 6, 0, location.getLatitude() + ":" + location.getLongitude())) {
                            Main.lMain.SaveMessage(context, 0, 6, 0, location.getLatitude() + ":" + location.getLongitude());
                        }
                    }
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
            }

            @Override
            public void onProviderEnabled(String s) {
            }

            @Override
            public void onProviderDisabled(String s) {
            }
        };

        try {
            locManager.requestSingleUpdate(criteria, locListener, locLooper);
        } catch ( IllegalArgumentException e ) {
            e.printStackTrace();
        }

        final Handler myHandler = new Handler(locLooper);
        myHandler.postDelayed(new Runnable() {
            public void run() {
                if (bNew == 0) {
                    showAttention(context);
                }
                bOld = 0;
                locManager.removeUpdates(locListener);
            }
        }, TIMEOUT_IN_MS);

        // New standart
        GooglePlayServicesClient.ConnectionCallbacks lcCallBacks = new GooglePlayServicesClient.ConnectionCallbacks() {
            @Override
            public void onConnected(Bundle bundle) {
                int iTry = 10;
                if (lcPinger.isConnected()) {
                    location = lcPinger.getLastLocation();
                    while (location == null && iTry > 0 && bOld == 1) {
                        try {
                            Thread.sleep(TIMEOUT_IN_MS);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        try {
                            location = lcPinger.getLastLocation();
                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                        }

                        iTry--;
                    }

                    if (location == null) {
                        if (bOld == 0) {
                            showAttention(context);
                        }
                        bNew = 0;
                    } else {
                        bNew = 2;
                        if (!bSent) {
                            bSent = true;
                            prefs.edit().putLong("lastPing", System.currentTimeMillis()).apply();
                            if (!Main.lMain.SendMessage(0, 6, 0, location.getLatitude() + ":" + location.getLongitude())) {
                                Main.lMain.SaveMessage(context, 0, 6, 0, location.getLatitude() + ":" + location.getLongitude());
                            }
                        }
                    }
                }
            }

            @Override
            public void onDisconnected() {

            }
        };

        GooglePlayServicesClient.OnConnectionFailedListener lcFiledListner = new GooglePlayServicesClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(ConnectionResult connectionResult) {
                if (bOld == 0) {
                    showAttention(context);
                }
                bNew = 0;
            }
        };

        lcPinger = new LocationClient(context, lcCallBacks, lcFiledListner);
        lcPinger.connect();
    }

    public static Location GiveMe(Context context) {
        if( location == null ) {
            locManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            locManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
                        @Override
                        public void onStatusChanged(String provider, int status, Bundle extras) {
                        }

                        @Override
                        public void onProviderEnabled(String provider) {
                        }

                        @Override
                        public void onProviderDisabled(String provider) {
                        }

                        @Override
                        public void onLocationChanged(final Location location) {
                        }
                    }
            );

            location = locManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        }

        return location;
    }

    public static void showAttention(Context con) {
        Intent btIntent = new Intent(con, LocationActivity.class);
        btIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        con.startActivity(btIntent);
    }
}
