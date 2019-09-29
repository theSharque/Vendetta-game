package club.vendetta.game.misc;

import android.graphics.Bitmap;

/**
 * Created by sharque on 12.08.14.
 * Player item element
 */
public class PlayerItem {
    public int id;
    public Bitmap bmPhoto;
    public String sLogin;
    public int iAdmin;
    public String sLocation;
    public String sBsid;
    public String sPass;
    public String sWins;
    public String sKills;
    public boolean bLive;
    public int iStars;
    public String sKilledBy;
    public boolean bGray;

    public PlayerItem(int id, Bitmap bm, String sLogin, int iAdmin, String sLocation, String sBsid, String sPass, String sWins, String sKills, boolean bLive, int iStars, String sKilledBy, boolean bGray) {
        super();

        this.id = id;
        this.bmPhoto = bm.copy(bm.getConfig(), true);
        bm.recycle();

        this.sLogin = sLogin;
        this.iAdmin = iAdmin;
        this.sLocation = sLocation;
        this.sBsid = sBsid;
        this.sPass = sPass;
        this.sWins = sWins;
        this.sKills = sKills;
        this.bLive = bLive;
        this.iStars = iStars;
        this.sKilledBy = sKilledBy;
        this.bGray = bGray;
    }
}
