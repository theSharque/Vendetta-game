package club.vendetta.game.misc;

public class MessageItem {
    public int id;
    public int iType;
    public String sLogin;
    public int iArg1;
    public String sArg2;

    public MessageItem(int id, int iType, String sLogin, int iArg1, String sArg2) {
        this.id = id;
        this.iType = iType;
        this.sLogin = sLogin;
        this.iArg1 = iArg1;
        this.sArg2 = sArg2;
    }
}
