package club.vendetta.game.misc;

/**
 * Created by sharque on 10.08.14.
 * <p/>
 * Collapse class for message
 */

public class ServerMessage {
    public int iId;
    public int iType;
    public String sSender;
    public int iArg1;
    public String sArg2;

    public ServerMessage(int iId, int iType, String sSender, int iArgument, String sArg2) {
        this.iId = iId;
        this.iType = iType;
        this.sSender = sSender;
        this.iArg1 = iArgument;
        this.sArg2 = sArg2;
    }
}
