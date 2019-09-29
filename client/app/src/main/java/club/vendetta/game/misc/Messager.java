package club.vendetta.game.misc;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.support.v4.app.NotificationCompat;

import java.util.ArrayList;
import java.util.List;

import club.vendetta.game.ApproveRecieve;
import club.vendetta.game.DieActivity;
import club.vendetta.game.FindGame;
import club.vendetta.game.GameDetail;
import club.vendetta.game.KillActivity;
import club.vendetta.game.Loader;
import club.vendetta.game.Main;
import club.vendetta.game.R;
import club.vendetta.game.UserInfo;
import club.vendetta.game.misc.PingSend;
import club.vendetta.game.misc.ServerMessage;

/**
 * Created by sharque on 10.08.14.
 * <p/>
 * Messaging service - download all message from server.
 */
public class Messager {
    public List<ServerMessage> smIncoming = new ArrayList<ServerMessage>();
    private Context context;

    public void doExchange(Context con) {
        boolean bNeedUpdate = false;
        this.context = con;

        exchangeMessages();
        for (ServerMessage saMessage : smIncoming) {
            switch (saMessage.iType) {
                case 1:  // Approve my photo
                case 2:  // Approved
                case 3:  // Invite to game
                case 4:  // Invite to game
                case 7:  // A lot of messages about game
                case 9:  // Kill animation
                case 10: // Start the game
                    sendNotification(saMessage);
                    bNeedUpdate = true;
                    break;

                case 6: // Ping
                    Main.lMain.SetMessages(saMessage.iId);
                    PingSend.ping(context);
                    break;

                case 8: // Die
                    Main.lMain.SetMessages(saMessage.iId);
                    Intent iDie = new Intent(context, DieActivity.class);
                    iDie.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(iDie);
                    break;

                case 11: // Update bluetooth ID
                    Main.lMain.SetMessages(saMessage.iId);

                    SharedPreferences prefs = context.getSharedPreferences(Main.class.getSimpleName(), Context.MODE_PRIVATE);
                    String sBsid = prefs.getString(Main.PROPERTY_BSSID, null);
                    if (sBsid == null) {
                        sBsid = BluetoothAdapter.getDefaultAdapter().getAddress();
                    }


                    if (sBsid != null) {
                        prefs.edit().putString(Main.PROPERTY_BSSID, sBsid).apply();
                        if (!Main.lMain.SendMessage(0, 11, 0, sBsid)) {
                            Main.lMain.SaveMessage(context, 0, 11, 0, sBsid);
                        }
                    }
                    break;

                default: // Unknown message
                    Main.lMain.SetMessages(saMessage.iId);
                    sendNotification(saMessage);
                    break;
            }
        }
        smIncoming.clear();
        if (bNeedUpdate) {
            Main.lMain.oldTime = 0;
            context.sendBroadcast(new Intent(Main.FILTER_NAME));
        }
    }

    public void exchangeMessages() {
        try {
            Main.lMain.GetMessages(smIncoming);
        } catch (NullPointerException e) {
            Main.lMain = new Loader(context);
            smIncoming.clear();
        }
    }

    private void sendNotification(ServerMessage smMsg) {
        Intent iMain;

        NotificationManager mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        PendingIntent contentIntent;

        int iNotificationId;
        switch (smMsg.iType) {
            case 1:
                Intent iApprover = new Intent(context, ApproveRecieve.class);

                iApprover.putExtra("mid", smMsg.iId);
                iApprover.putExtra("name", smMsg.sSender);
                iApprover.putExtra("photo", smMsg.sArg2);
                iApprover.putExtra("invite", false);

                contentIntent = PendingIntent.getActivity(context, 0,
                        iApprover, PendingIntent.FLAG_ONE_SHOT);

                mBuilder.setSmallIcon(R.drawable.ic_launcher);
                mBuilder.setContentTitle(context.getString(R.string.msgApproveTitle));
                mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(context.getString(R.string.msgPleaseApprove)));
                mBuilder.setContentText(context.getString(R.string.msgPleaseApprove));
                mBuilder.setDefaults(Notification.DEFAULT_ALL);
                mBuilder.setContentIntent(contentIntent);
                mBuilder.setAutoCancel(true);
                iNotificationId = 1;
                break;

            case 2:
                iMain = new Intent(context, UserInfo.class);

                iMain.putExtra("mid", smMsg.iId);

                contentIntent = PendingIntent.getActivity(context, 0,
                        iMain, PendingIntent.FLAG_ONE_SHOT);

                mBuilder.setSmallIcon(R.drawable.ic_launcher);
                mBuilder.setContentTitle(context.getString(R.string.msgApproveTitle));
                if (smMsg.iArg1 == 1) {
                    mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(context.getString(R.string.msgPhotoApproved)));
                    mBuilder.setContentText(context.getString(R.string.msgPhotoApproved));
                } else {
                    mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(context.getString(R.string.msgPhotoDeclined)));
                    mBuilder.setContentText(context.getString(R.string.msgPhotoDeclined));
                }
                mBuilder.setAutoCancel(true);
                mBuilder.setDefaults(Notification.DEFAULT_ALL);
                mBuilder.setContentIntent(contentIntent);
                iNotificationId = 2;
                break;

            case 3:
                if (!smMsg.sArg2.equals("")) {
                    Intent iInvite = new Intent(context, ApproveRecieve.class);

                    iInvite.putExtra("mid", smMsg.iId);
                    iInvite.putExtra("name", smMsg.sSender);
                    iInvite.putExtra("user", smMsg.iArg1);
                    iInvite.putExtra("photo", smMsg.sArg2);
                    iInvite.putExtra("invite", true);

                    contentIntent = PendingIntent.getActivity(context, 0,
                            iInvite, PendingIntent.FLAG_ONE_SHOT);

                    mBuilder.setSmallIcon(R.drawable.ic_launcher);
                    mBuilder.setContentTitle(context.getString(R.string.msgInviteTitle));
                    mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(String.format(context.getString(R.string.msgInviteAsk), smMsg.sSender)));
                    mBuilder.setContentText(String.format(context.getString(R.string.msgInviteAsk), smMsg.sSender));
                    mBuilder.setDefaults(Notification.DEFAULT_ALL);
                    mBuilder.setContentIntent(contentIntent);
                    mBuilder.setAutoCancel(true);
                    iNotificationId = 3;
                } else {
                    Intent iInvite = new Intent(context, GameDetail.class);

                    Location lMe = PingSend.GiveMe(context);

                    iInvite.putExtra("mid", smMsg.iId);
                    iInvite.putExtra("game", smMsg.iArg1);
                    iInvite.putExtra("invite", true);
                    iInvite.putExtra("name", smMsg.sSender);
                    iInvite.putExtra("location", lMe);
                    iInvite.putExtra("classname", "DrumMenu");

                    contentIntent = PendingIntent.getActivity(context, 0,
                            iInvite, PendingIntent.FLAG_ONE_SHOT);

                    mBuilder.setSmallIcon(R.drawable.ic_launcher);
                    mBuilder.setContentTitle(context.getString(R.string.msgInviteTitle));
                    mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(String.format(context.getString(R.string.msgInviteText), smMsg.sSender)));
                    mBuilder.setContentText(String.format(context.getString(R.string.msgInviteText), smMsg.sSender));
                    mBuilder.setDefaults(Notification.DEFAULT_ALL);
                    mBuilder.setContentIntent(contentIntent);
                    mBuilder.setAutoCancel(true);
                    iNotificationId = 3;
                }
                break;

            case 4:
                iMain = new Intent(context, Main.class);

                iMain.putExtra("mid", smMsg.iId);

                contentIntent = PendingIntent.getActivity(context, 0,
                        iMain, PendingIntent.FLAG_ONE_SHOT);

                mBuilder.setSmallIcon(R.drawable.ic_launcher);
                if (smMsg.sArg2.equals("")) {
                    mBuilder.setContentTitle(context.getString(R.string.msgGameCloseTitle));
                    mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(context.getString(R.string.msgGameCloseText)));
                    mBuilder.setContentText(context.getString(R.string.msgGameCloseText));
                } else {
                    mBuilder.setContentTitle(context.getString(R.string.msgUserLeaveTitle));
                    mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(String.format(context.getString(R.string.msgUserLeaveText), smMsg.sArg2)));
                    mBuilder.setContentText(String.format(context.getString(R.string.msgUserLeaveText), smMsg.sArg2));
                }
                mBuilder.setAutoCancel(true);
                mBuilder.setDefaults(Notification.DEFAULT_ALL);
                mBuilder.setContentIntent(contentIntent);
                iNotificationId = 4;
                break;

            case 7:
                Main.lMain.login();

                switch (smMsg.iArg1) {
                    case 0:
                        iMain = new Intent(context, FindGame.class);

                        mBuilder.setContentTitle(context.getString(R.string.msgTooFarTitle));
                        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(context.getString(R.string.msgTooFarText)));
                        mBuilder.setContentText(context.getString(R.string.msgTooFarText));
                        break;

                    case 1:
                        Main.bLogedIn = false;
                        Main.lMain.login();
                        iMain = new Intent(context, GameDetail.class);
                        iMain.putExtra("classname", "DrumMenu");

                        mBuilder.setContentTitle(context.getString(R.string.msgGameStartedTitle));
                        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(context.getString(R.string.msgGameStartedText)));
                        mBuilder.setContentText(context.getString(R.string.msgGameStartedText));
                        break;

                    case 2:
                        iMain = new Intent(context, GameDetail.class);
                        iMain.putExtra("classname", "DrumMenu");

                        Main.btOn(context);
                        mBuilder.setContentTitle(context.getString(R.string.msgGameStartTitle));
                        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(context.getString(R.string.msgGameStartText)));
                        mBuilder.setContentText(context.getString(R.string.msgGameStartText));
                        break;

                    case 3:
                        iMain = new Intent(context, FindGame.class);

                        mBuilder.setContentTitle(context.getString(R.string.msgGameCantTitle));
                        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(context.getString(R.string.msgGameCantText)));
                        mBuilder.setContentText(context.getString(R.string.msgGameCantText));
                        break;

                    case 4:
                        iMain = new Intent(context, FindGame.class);

                        mBuilder.setContentTitle(context.getString(R.string.msgAskDeclinedTitle));
                        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(context.getString(R.string.msgAskDeclinedText)));
                        mBuilder.setContentText(context.getString(R.string.msgAskDeclinedText));
                        break;

                    case 5:
                        iMain = new Intent(context, GameDetail.class);
                        iMain.putExtra("classname", "DrumMenu");

                        mBuilder.setContentTitle(context.getString(R.string.msgWinTitle));
                        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(context.getString(R.string.msgWinText)));
                        mBuilder.setContentText(context.getString(R.string.msgWinText));
                        break;

                    case 6:
                        iMain = new Intent(context, GameDetail.class);
                        iMain.putExtra("classname", "DrumMenu");

                        mBuilder.setContentTitle(context.getString(R.string.msgNoWinTitle));
                        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(context.getString(R.string.msgNoWinText)));
                        mBuilder.setContentText(context.getString(R.string.msgNoWinText));
                        break;

                    case 7:
                        iMain = new Intent(context, FindGame.class);

                        mBuilder.setContentTitle(context.getString(R.string.msgDeleteTitle));
                        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(context.getString(R.string.msgDeleteText)));
                        mBuilder.setContentText(context.getString(R.string.msgDeleteText));
                        break;

                    case 8:
                        Main.bLogedIn = false;
                        Main.lMain.login();
                        iMain = new Intent(context, GameDetail.class);
                        iMain.putExtra("classname", "DrumMenu");

                        mBuilder.setContentTitle(context.getString(R.string.msgWinnerTitle));
                        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(String.format(context.getString(R.string.msgWinnerText), smMsg.sArg2)));
                        mBuilder.setContentText(String.format(context.getString(R.string.msgWinnerText), smMsg.sArg2));
                        break;

                    default:
                        iMain = new Intent(context, Main.class);
                        mBuilder.setContentTitle("Unknown error");
                        mBuilder.setContentText("Check type 7 arg:" + smMsg.iArg1);
                        break;
                }

                iMain.putExtra("mid", smMsg.iId);

                contentIntent = PendingIntent.getActivity(context, 0,
                        iMain, PendingIntent.FLAG_ONE_SHOT);

                mBuilder.setSmallIcon(R.drawable.ic_launcher);
                mBuilder.setAutoCancel(true);
                mBuilder.setDefaults(Notification.DEFAULT_ALL);
                mBuilder.setContentIntent(contentIntent);
                iNotificationId = 7;
                break;

            case 9:
                Main.bLogedIn = false;
                Main.lMain.login();
                Intent iSendApprove = new Intent(context, KillActivity.class);

                iSendApprove.putExtra("mid", smMsg.iId);
                iSendApprove.putExtra("game", smMsg.iArg1);
                iSendApprove.putExtra("player", Integer.valueOf(smMsg.sArg2));

                contentIntent = PendingIntent.getActivity(context, 0,
                        iSendApprove, PendingIntent.FLAG_ONE_SHOT);

                mBuilder.setSmallIcon(R.drawable.ic_launcher);

                mBuilder.setContentTitle(context.getString(R.string.msgKilledTitle));
                mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(String.format(context.getString(R.string.msgKilledText), smMsg.sSender)));
                mBuilder.setContentText(String.format(context.getString(R.string.msgKilledText), smMsg.sSender));

                mBuilder.setAutoCancel(true);
                mBuilder.setDefaults(Notification.DEFAULT_ALL);
                mBuilder.setContentIntent(contentIntent);
                iNotificationId = 9;
                break;

            case 10:
                Main.bLogedIn = false;
                Main.lMain.login();
                Intent iGame = new Intent(context, GameDetail.class);

                iGame.putExtra("mid", smMsg.iId);
                iGame.putExtra("game", smMsg.iArg1);
                iGame.putExtra("classname", "DrumMenu");

                contentIntent = PendingIntent.getActivity(context, 0,
                        iGame, PendingIntent.FLAG_ONE_SHOT);

                mBuilder.setSmallIcon(R.drawable.ic_launcher);
                mBuilder.setContentTitle(context.getString(R.string.msgNeedStartTitle));
                mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(context.getString(R.string.msgNeedStartText)));
                mBuilder.setContentText(context.getString(R.string.msgNeedStartText));
                mBuilder.setDefaults(Notification.DEFAULT_ALL);
                mBuilder.setContentIntent(contentIntent);
                mBuilder.setAutoCancel(true);
                iNotificationId = 10;
                break;

            default:
                mBuilder.setSmallIcon(R.drawable.ic_launcher);
                mBuilder.setContentTitle("Unknown error");
                mBuilder.setContentText("Check type:" + smMsg.iType);
                mBuilder.setDefaults(Notification.DEFAULT_ALL);
                iNotificationId = 999;
                break;
        }

        mNotificationManager.notify(iNotificationId, mBuilder.build());
    }
}
