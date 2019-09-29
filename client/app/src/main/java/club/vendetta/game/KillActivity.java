package club.vendetta.game;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import club.vendetta.game.misc.PingSend;
import club.vendetta.game.misc.PlayerAdapter;

public class KillActivity extends ActionBarActivity {

    PlayerAdapter paGames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kill);

        paGames = new PlayerAdapter(this, R.layout.player_item);
        ((ListView) findViewById(R.id.playerList)).setAdapter(paGames);
        paGames.clear();

        Location lMe = PingSend.GiveMe(getApplicationContext());

        int iGame = getIntent().getIntExtra("game", 0);
        int iPlayer = getIntent().getIntExtra("player", 0);

        Main.lMain.SetMessages(getIntent().getIntExtra("mid", 0));
        Main.lMain.KillList(getApplicationContext(), paGames, iGame, iPlayer, lMe);
        paGames.delete(iPlayer);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.all, menu);
        Main.menuNumbers(menu, getResources().getDisplayMetrics().scaledDensity, getApplicationContext());
        return true;
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
}
