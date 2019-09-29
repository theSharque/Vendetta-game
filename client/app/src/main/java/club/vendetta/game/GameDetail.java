package club.vendetta.game;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.appsflyer.AppsFlyerLib;

import club.vendetta.game.misc.PingSend;
import club.vendetta.game.misc.PlayerAdapter;
import club.vendetta.game.misc.PlayerItem;

public class GameDetail extends ActionBarActivity implements View.OnTouchListener {

    public static int iVictimCount;
    public static int iVictimId;
    public static int iTotal;
    public static int iReady;
    public static int iStartType;
    PlayerAdapter paGames;
    boolean bAdmin;
    private int iGame;
    private Location lMe;
    private ListView lvPlayers;
    private float mDownX;
    private View mDownView;
    private Animation anMove;
    private boolean bInvite;
    private String sClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_detail);

        paGames = new PlayerAdapter(this, R.layout.player_item);
        lvPlayers = (ListView) findViewById(R.id.playerList);
        lvPlayers.setAdapter(paGames);
        lvPlayers.setOnTouchListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(Main.brMessager);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(Main.brMessager, Main.ifMessager);
        supportInvalidateOptionsMenu();

        bInvite = getIntent().getBooleanExtra("invite", false);
        iGame = getIntent().getIntExtra("game", Main.iGameId);
        int iGameType = getIntent().getIntExtra("gametype", 1);
        sClass = getIntent().getStringExtra("classname");

        Main.lMain.login();
        Main.lMain.SetMessages(getIntent().getIntExtra("mid", 0));

        lMe = PingSend.GiveMe(getApplicationContext());

        paGames.clear();
        paGames.bSelectable = false;
        bAdmin = Main.lMain.PlayerList(getApplicationContext(), paGames, iGame, lMe);

        View btnYourVictim = findViewById(R.id.victims);
        if (bInvite) {
            findViewById(R.id.inviter).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.inviter).setVisibility(View.GONE);
            if (Main.iGameState == 1 || Main.iGameState == 0) {
                if (iStartType == 0) {
                    ((TextView) findViewById(R.id.resolution)).setText(Html.fromHtml(String.format(getString(R.string.gamePlayers), paGames.getCount(), iTotal)));
                } else {
                    ((TextView) findViewById(R.id.resolution)).setText(Html.fromHtml(String.format(getString(R.string.gameHours), iReady, iTotal)));
                }
                if (Main.iGameId == iGame) {
                    findViewById(R.id.invite).setVisibility(View.VISIBLE);
                    findViewById(R.id.leave).setVisibility(View.VISIBLE);
                    findViewById(R.id.request).setVisibility(View.GONE);
                    findViewById(R.id.join).setVisibility(View.GONE);
                    if (bAdmin && paGames.getCount() > 1) {
                        findViewById(R.id.start).setVisibility(View.VISIBLE);
                    }
                } else {
                    findViewById(R.id.invite).setVisibility(View.GONE);
                    findViewById(R.id.leave).setVisibility(View.GONE);
                    if (iGameType == 0) {
                        findViewById(R.id.join).setVisibility(View.VISIBLE);
                        findViewById(R.id.request).setVisibility(View.GONE);
                    } else {
                        findViewById(R.id.join).setVisibility(View.GONE);
                        findViewById(R.id.request).setVisibility(View.VISIBLE);
                    }
                }
                btnYourVictim.setVisibility(View.GONE);
            } else {
                findViewById(R.id.invite).setVisibility(View.GONE);
                findViewById(R.id.leave).setVisibility(View.VISIBLE);
                findViewById(R.id.start).setVisibility(View.GONE);
                if (iVictimCount > 0) {
                    btnYourVictim.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.all, menu);
        Main.menuNumbers(menu, getResources().getDisplayMetrics().scaledDensity, getApplicationContext());

        if (lvPlayers != null) {
            paGames.clear();
            paGames.bSelectable = false;
            bAdmin = Main.lMain.PlayerList(getApplicationContext(), paGames, iGame, lMe);
            lvPlayers.invalidateViews();

            if (iStartType == 0) {
                ((TextView) findViewById(R.id.resolution)).setText(Html.fromHtml(String.format(getString(R.string.gamePlayers), paGames.getCount(), iTotal)));
            } else {
                ((TextView) findViewById(R.id.resolution)).setText(Html.fromHtml(String.format(getString(R.string.gameHours), iReady, iTotal)));
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_message:
                Intent iMsg = new Intent(getApplicationContext(), Messages.class);
                startActivity(iMsg);
                return true;

            case R.id.action_help:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void InvitePlayer(View view) {
        if (paGames.iGameType == 1 || Main.iGameId == iGame) {
            Intent iSendApprove = new Intent(getApplicationContext(), ApproveSend.class);
            if (Main.iGameId == iGame) {
                iSendApprove.putExtra("iArg", 3);
                iSendApprove.putExtra("classname", "GameDetail");
                startActivity(iSendApprove);
            }
        } else {
            if (!Main.lMain.SendMessage(0, 5, 1, String.valueOf(iGame))) {
                Toast toast;
                toast = Toast.makeText(getApplicationContext(), getString(R.string.network_problem), Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        }
    }

    private void LeaveGame(final float x) {
        AlertDialog.Builder ad = new AlertDialog.Builder(this);
        ad.setTitle(getString(R.string.msgLeaveGameTitle)); // сообщение
        ad.setMessage(getString(R.string.msgLeaveGameText)); // сообщение
        ad.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                if (Main.lMain.SendMessage(0, 4, 0, "")) {
                    setResult(RESULT_OK);
                    Main.lMain.login();
                    startActivity(new Intent(getApplicationContext(), DrumMenu.class));
                    finish();
                } else {
                    Toast toast;
                    toast = Toast.makeText(getApplicationContext(), getString(R.string.network_problem), Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }
        });

        ad.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                if (mDownView != null && x > 0) {
                    anMove = new TranslateAnimation(x, 0, 0, 0);
                    anMove.setStartOffset(0);
                    anMove.setDuration(200);
                    anMove.setFillAfter(true);
                    mDownView.startAnimation(anMove);
                }
            }
        });

        ad.setCancelable(true);
        ad.show();
    }

    private void DeletePlayer(final int uid, final float x) {
        AlertDialog.Builder ad = new AlertDialog.Builder(this);
        ad.setTitle(getString(R.string.msgDeleteAskTitle)); // сообщение
        ad.setMessage(getString(R.string.msgDeleteAskText)); // сообщение
        ad.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                if (Main.lMain.SendMessage(uid, 7, 7, "")) {
                    setResult(RESULT_OK);
                    Main.lMain.login();
                    paGames.remove(uid);

                    anMove = new TranslateAnimation(0, 0, 0, 0);
                    anMove.setStartOffset(0);
                    anMove.setDuration(0);
                    anMove.setFillAfter(true);
                    mDownView.startAnimation(anMove);
                    lvPlayers.invalidateViews();

                    if (iStartType == 0) {
                        ((TextView) findViewById(R.id.resolution)).setText(Html.fromHtml(String.format(getString(R.string.gamePlayers), paGames.getCount(), iTotal)));
                    } else {
                        ((TextView) findViewById(R.id.resolution)).setText(Html.fromHtml(String.format(getString(R.string.gameHours), iReady, iTotal)));
                    }
                } else {
                    Toast toast;
                    toast = Toast.makeText(getApplicationContext(), getString(R.string.network_problem), Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }
        });

        ad.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                anMove = new TranslateAnimation(x, 0, 0, 0);
                anMove.setStartOffset(0);
                anMove.setDuration(200);
                anMove.setFillAfter(true);
                mDownView.startAnimation(anMove);
            }
        });

        ad.setCancelable(true);
        ad.show();
    }

    public void Accept(View view) {
        if (Main.bNeedApprove) {
            AlertDialog.Builder ad = new AlertDialog.Builder(this);
            ad.setTitle(this.getString(R.string.msgYouNeedAppproveTitle));
            ad.setMessage(this.getString(R.string.msgYouNeedAppproveText));
            ad.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg1) {
                }
            });
            ad.setCancelable(true);
            ad.show();
        } else {
            if (Main.lMain.SendMessage(0, 5, 1, String.valueOf(iGame))) {
                paGames.clear();
                Main.lMain.PlayerList(getApplicationContext(), paGames, iGame, lMe);

                Intent iMyGame = getIntent();
                iMyGame.putExtra("invite", false);
                iMyGame.putExtra("game", iGame);
                bInvite = false;

                onResume();
/*
                startActivity(iMyGame);
                finish();
*/
            } else {
                Toast toast;
                toast = Toast.makeText(getApplicationContext(), getString(R.string.network_problem), Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        }
    }

    public void Decline(View view) {
        AlertDialog.Builder ad = new AlertDialog.Builder(this);
        ad.setTitle(getString(R.string.msgDeclineTitle)); // сообщение
        ad.setMessage(getString(R.string.msgDeclineText)); // сообщение
        ad.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                if (Main.lMain.SendMessage(0, 5, 0, String.valueOf(iGame))) {
                    finish();
                } else {
                    Toast toast;
                    toast = Toast.makeText(getApplicationContext(), getString(R.string.network_problem), Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }
        });

        ad.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
            }
        });

        ad.setCancelable(true);
        ad.show();
    }

    public void victims(View view) {
        Intent iVictims;
        if (iVictimCount == 1) {
            PlayerAdapter pAdapter = new PlayerAdapter(this, R.layout.player_item);

            Location lMe = PingSend.GiveMe(getApplicationContext());
            Main.lMain.VictimList(getApplicationContext(), pAdapter, lMe);

            PlayerItem pItem = pAdapter.getItem(0);

            iVictims = new Intent(getApplicationContext(), VictimDetail.class);

            iVictims.putExtra("photo", pItem.bmPhoto);
            iVictims.putExtra("id", pItem.id);
            iVictims.putExtra("login", pItem.sLogin);
            iVictims.putExtra("location", pItem.sLocation);
            iVictims.putExtra("bsid", pItem.sBsid);
            iVictims.putExtra("password", pItem.sPass);
            iVictims.putExtra("kills", pItem.sKills);
            iVictims.putExtra("wins", pItem.sWins);
            iVictims.putExtra("classname", "GameDetail");
        } else {
            iVictims = new Intent(getApplicationContext(), Victims.class);
        }

        startActivity(iVictims);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = motionEvent.getX();

                // Find the child view that was touched (perform a hit test)
                Rect rect = new Rect();
                int childCount = lvPlayers.getChildCount();
                int[] listViewCoords = new int[2];
                lvPlayers.getLocationOnScreen(listViewCoords);
                int x = (int) motionEvent.getRawX() - listViewCoords[0];
                int y = (int) motionEvent.getRawY() - listViewCoords[1];
                mDownView = null;

                View child;
                for (int i = 0; i < childCount; i++) {
                    child = lvPlayers.getChildAt(i);
                    child.getHitRect(rect);
                    if (rect.contains(x, y)) {
                        mDownView = child; // This is your down view
                        break;
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (mDownView != null && motionEvent.getX() - mDownX > 0) {
                    PlayerAdapter.PlayerHolder player = (PlayerAdapter.PlayerHolder) mDownView.getTag();

                    if (((Main.iGameState == 1 && bAdmin) || player.id == Integer.valueOf(Main.sId))) {
                        anMove = new TranslateAnimation(motionEvent.getX() - mDownX, motionEvent.getX() - mDownX, 0, 0);
                        anMove.setStartOffset(0);
                        anMove.setDuration(0);
                        anMove.setFillAfter(true);
                        mDownView.startAnimation(anMove);
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                if (mDownView != null) {
                    PlayerAdapter.PlayerHolder player = (PlayerAdapter.PlayerHolder) mDownView.getTag();

                    if (motionEvent.getX() - mDownX < view.getWidth() / 10) {
                        if (mDownX < 90 * getApplicationContext().getResources().getDisplayMetrics().density) {

                            Intent iLookVictim = new Intent(getApplicationContext(), PhotoPreview.class);

                            iLookVictim.putExtra("login", player.player_login.getText());
                            iLookVictim.putExtra("photo", player.bmPhoto);

                            startActivity(iLookVictim);
                        } else if (player.player_admin.getVisibility() == View.VISIBLE) {
                            Toast toast;
                            toast = Toast.makeText(getApplicationContext(), getString(R.string.admin), Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                        }
                    }

                    if (motionEvent.getX() - mDownX > view.getWidth() / 2) {
                        if (player.id == Integer.valueOf(Main.sId)) {
                            LeaveGame(motionEvent.getX() - mDownX);
                            return true;
                        } else if (Main.iGameState == 1 && bAdmin) {
                            DeletePlayer(player.id, motionEvent.getX() - mDownX);
                        }
                    } else {
                        if (((Main.iGameState == 1 && bAdmin) || player.id == Integer.valueOf(Main.sId)) && motionEvent.getX() - mDownX > 0) {
                            anMove = new TranslateAnimation(motionEvent.getX() - mDownX, 0, 0, 0);
                            anMove.setStartOffset(0);
                            anMove.setDuration(200);
                            anMove.setFillAfter(true);
                            mDownView.startAnimation(anMove);
                        }
                    }
                }
                break;

            default:
                return false;
        }
        return false;
    }

    public void StartGame(View view) {
        AlertDialog.Builder ad = new AlertDialog.Builder(this);
        ad.setTitle(getString(R.string.msgStartGameTitle)); // сообщение
        ad.setMessage(getString(R.string.msgStartGameText)); // сообщение
        ad.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                AppsFlyerLib.sendTrackingWithEvent(getApplicationContext(), "Play in game", "");
                if (Main.lMain.SendMessage(0, 10, 0, "")) {
                    onResume();
                } else {
                    Toast toast;
                    toast = Toast.makeText(getApplicationContext(), getString(R.string.network_problem), Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }
        });

        ad.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
            }
        });

        ad.setCancelable(true);
        ad.show();
    }

    public void requestGame(View view) {
        if (Main.bNeedApprove) {
            AlertDialog.Builder ad = new AlertDialog.Builder(this);
            ad.setTitle(getApplicationContext().getString(R.string.msgYouNeedAppproveTitle));
            ad.setMessage(getApplicationContext().getString(R.string.msgYouNeedAppproveText));
            ad.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg1) {
                }
            });
            ad.setCancelable(true);
            ad.show();
        } else {
            Intent iSendApprove = new Intent(getApplicationContext(), ApproveSend.class);

            iSendApprove.putExtra("iGame", iGame);
            iSendApprove.putExtra("iArg", 3);
            iSendApprove.putExtra("classname", "GameDetail");
            startActivity(iSendApprove);
        }
    }

    @Override
    public void onBackPressed() {
        back(null);
    }

    public void back(View view) {
        try {
            startActivity(new Intent(getApplicationContext(), Class.forName(BuildConfig.APPLICATION_ID + "." + sClass)));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        this.finish();
    }

    public void leave(View view) {
        LeaveGame(0);
    }

    public void JoinGame(View view) {
        if (Main.iGameId != iGame) {
            AlertDialog.Builder ad = new AlertDialog.Builder(this);
            ad.setTitle(getString(R.string.msgTitleAllreadyInGame));
            ad.setMessage(getString(R.string.msgTextAllreadyInGame));
            ad.setPositiveButton(R.string.join, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg1) {
                    if (Main.lMain.SendMessage(0, 5, 1, String.valueOf(iGame))) {
                        Main.lMain.login();

                        sClass = "DrumMenu";
                        findViewById(R.id.join).setVisibility(View.GONE);
                        findViewById(R.id.invite).setVisibility(View.VISIBLE);
                        findViewById(R.id.leave).setVisibility(View.VISIBLE);

                        paGames.clear();
                        bAdmin = Main.lMain.PlayerList(getApplicationContext(), paGames, iGame, lMe);
                        ((ListView) findViewById(R.id.playerList)).invalidateViews();
                    } else {
                        Toast toast;
                        toast = Toast.makeText(getApplicationContext(), getString(R.string.network_problem), Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }
                }
            });

            ad.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg1) {
                }
            });

            ad.setCancelable(true);
            ad.show();
        } else {
            if (Main.lMain.SendMessage(0, 5, 1, String.valueOf(iGame))) {
                Main.lMain.login();

                sClass = "DrumMenu";
                findViewById(R.id.join).setVisibility(View.GONE);
                findViewById(R.id.invite).setVisibility(View.VISIBLE);
                findViewById(R.id.leave).setVisibility(View.VISIBLE);

                paGames.clear();
                bAdmin = Main.lMain.PlayerList(getApplicationContext(), paGames, iGame, lMe);
                ((ListView) findViewById(R.id.playerList)).invalidateViews();
            } else {
                Toast toast;
                toast = Toast.makeText(getApplicationContext(), getString(R.string.network_problem), Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        }
    }
}
