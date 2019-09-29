package club.vendetta.game;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import club.vendetta.game.misc.GameAdapter;
import club.vendetta.game.misc.GameItem;

public class FindGame extends ActionBarActivity {

    private GameAdapter gAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_game);

        Main.lMain.SetMessages(getIntent().getIntExtra("mid", 0));

        gAdapter = new GameAdapter(this, R.layout.game_item);
        ((ListView) findViewById(R.id.gameList)).setAdapter(gAdapter);

        ((ListView) findViewById(R.id.gameList)).setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent iGameDetail = new Intent(getApplicationContext(), GameDetail.class);
                iGameDetail.putExtra("game", (int) id);
                iGameDetail.putExtra("gametype", ((GameItem) gAdapter.getItem(position)).iGameType);
                iGameDetail.putExtra("classname", "FindGame");
                startActivity(iGameDetail);
            }
        });
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
        gAdapter.clear();

        Main.lMain.login();
        Main.lMain.GameList(getApplicationContext(), gAdapter);
        ((ListView) findViewById(R.id.gameList)).invalidateViews();

        if (gAdapter.isEmpty()) {
            findViewById(R.id.findGame).setVisibility(View.GONE);
            ((TextView) findViewById(R.id.noGameText)).setText(Html.fromHtml(getString(R.string.noGameText)));
            findViewById(R.id.createOwn).setVisibility(View.VISIBLE);

            if (Main.bInGame) {
                findViewById(R.id.game_btn).setVisibility(View.GONE);
            } else {
                findViewById(R.id.game_btn).setVisibility(View.VISIBLE);
            }
        } else {
            findViewById(R.id.findGame).setVisibility(View.VISIBLE);
            findViewById(R.id.createOwn).setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.all, menu);
        Main.menuNumbers(menu, getResources().getDisplayMetrics().scaledDensity, getApplicationContext());
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

    public void createGame(View view) {
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
            Intent iNewGame = new Intent(getApplicationContext(), NewGame.class);
            iNewGame.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(iNewGame);
            this.finish();
        }
    }

    @Override
    public void onBackPressed() {
        back(null);
    }

    public void back(View view) {
        startActivity(new Intent(getApplicationContext(), DrumMenu.class));
        this.finish();
    }
}
