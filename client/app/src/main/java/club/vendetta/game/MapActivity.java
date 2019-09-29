package club.vendetta.game;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends FragmentActivity {
    private GoogleMap mMap;
    private String sLogin;
    private Bitmap bmMarker;
    private Bitmap bmVictim;
    private String sLocation;
    private String sBsid;
    private String sPass;
    private String sKills;
    private String sWins;
    private int iId;
    private final Handler hShow = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                case 3:
                    Main.lMain.updateUser(iId);
                    if (Main.lMain.GetValue("err").equals("0") && Main.lMain.GetValue("l").contains(":")) {
                        sLocation = Main.lMain.GetValue("l");
                        if (Main.lMain.GetValue("d").equals("1")) {
                            back(null);
                        }

                        String[] sLoc = sLocation.split(":");
                        LatLng lVictim = new LatLng(Double.parseDouble(sLoc[0]), Double.parseDouble(sLoc[1]));

                        mVictim.setPosition(lVictim);
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lVictim, 16.0f));
                    }

                    hShow.sendEmptyMessageDelayed(3, 60000);
            }
        }
    };
    private Marker mVictim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Intent intent = getIntent();
        iId = intent.getIntExtra("id", 0);
        sLogin = intent.getStringExtra("Login");
        bmVictim = intent.getParcelableExtra("photo");
        sLocation = intent.getStringExtra("location");
        sBsid = intent.getStringExtra("bsid");
        sPass = intent.getStringExtra("password");
        sKills = intent.getStringExtra("kills");
        sWins = intent.getStringExtra("wins");

        bmMarker = BitmapFactory.decodeResource(getResources(), R.drawable.marker).copy(Bitmap.Config.ARGB_8888, true);

        Canvas caPhoto = new Canvas(bmMarker);
        float fDiff = getResources().getDisplayMetrics().scaledDensity;
        caPhoto.drawBitmap(Bitmap.createScaledBitmap(bmVictim, (int) (bmMarker.getWidth() - 58.0f * fDiff), (int) (bmMarker.getWidth() - 58.0f * fDiff), false), 26 * fDiff, 7 * fDiff, null);
        setUpMapIfNeeded();

        hShow.sendEmptyMessage(3);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        supportInvalidateOptionsMenu();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        mMap.setMyLocationEnabled(true);
        mMap.setBuildingsEnabled(true);
        mMap.setIndoorEnabled(true);

        String[] sLoc = sLocation.split(":");

        MarkerOptions moVictim = new MarkerOptions();
        LatLng lVictim = new LatLng(Double.parseDouble(sLoc[0]), Double.parseDouble(sLoc[1]));
        moVictim.position(lVictim);
        moVictim.draggable(false);
        moVictim.title(sLogin);
        moVictim.flat(false);
        moVictim.icon(BitmapDescriptorFactory.fromBitmap(bmMarker));

        mVictim = mMap.addMarker(moVictim);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lVictim, 16.0f));
    }

    public void back(View view) {
        Intent iVictim = new Intent(getApplicationContext(), VictimDetail.class);

        iVictim.putExtra("location", sLocation);
        iVictim.putExtra("id", iId);
        iVictim.putExtra("login", sLogin);
        iVictim.putExtra("photo", bmVictim);
        iVictim.putExtra("bsid", sBsid);
        iVictim.putExtra("password", sPass);
        iVictim.putExtra("kills", sKills);
        iVictim.putExtra("wins", sWins);

        startActivity(iVictim);
        this.finish();
    }
}
