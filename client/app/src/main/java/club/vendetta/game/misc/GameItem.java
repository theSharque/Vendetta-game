package club.vendetta.game.misc;

import android.graphics.Bitmap;

/**
 * Created by sharque on 12.08.14.
 * One easy game item for adapter
 */
public class GameItem {
    public int id;
    public Bitmap bmPhoto;
    public int iGameType;
    public String sGameEmpty;
    public int iEndType;
    public int iStartType;
    public int iPlayer;
    public int iInGame;
    public int iVictims;

    public GameItem(int id, Bitmap bm, int iGameType, String sGameEmpty, int iEndType, int iStartType, int iPlayer, int iInGame, int iVictims) {
        super();

        this.id = id;
        this.bmPhoto = bm.copy(bm.getConfig(), true);
        bm.recycle();

        this.iGameType = iGameType;
        this.sGameEmpty = sGameEmpty;
        this.iEndType = iEndType;
        this.iStartType = iStartType;
        this.iPlayer = iPlayer;
        this.iInGame = iInGame;
        this.iVictims = iVictims;
    }
}
