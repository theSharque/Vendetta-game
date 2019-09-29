package club.vendetta.game;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import club.vendetta.game.misc.PingSend;
import club.vendetta.game.misc.PlayerAdapter;
import club.vendetta.game.misc.PlayerItem;

public class Victims extends ActionBarActivity {

    private PlayerAdapter pAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_victims);

        pAdapter = new PlayerAdapter(this, R.layout.player_item);
        ((ListView) findViewById(R.id.victimsList)).setAdapter(pAdapter);

        ((ListView) findViewById(R.id.victimsList)).setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent iLookVictim = new Intent(getApplicationContext(), VictimDetail.class);

                PlayerItem pItem = pAdapter.getItem(position);

                iLookVictim.putExtra("photo", pItem.bmPhoto);
                iLookVictim.putExtra("id", pItem.id);
                iLookVictim.putExtra("login", pItem.sLogin);
                iLookVictim.putExtra("location", pItem.sLocation);
                iLookVictim.putExtra("bsid", pItem.sBsid);
                iLookVictim.putExtra("password", pItem.sPass);
                iLookVictim.putExtra("kills", pItem.sKills);
                iLookVictim.putExtra("wins", pItem.sWins);
                iLookVictim.putExtra("classname", "Victims");

                startActivity(iLookVictim);
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

//        supportInvalidateOptionsMenu();
        pAdapter.clear();

        Location lMe = PingSend.GiveMe(getApplicationContext());

        Main.lMain.VictimList(getApplicationContext(), pAdapter, lMe);
        if (pAdapter.isEmpty()) {
            back(null);
        }
        ((ListView) findViewById(R.id.victimsList)).invalidateViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.all, menu);
        Main.menuNumbers(menu, getResources().getDisplayMetrics().scaledDensity, getApplicationContext());

        if (this.findViewById(R.id.victimsList) != null) {
            onResume();
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

    @Override
    public void onBackPressed() {
        back(null);
    }

    public void back(View view) {
        Intent iGameDetail = new Intent(getApplicationContext(), GameDetail.class);
        iGameDetail.putExtra("classname", "DrumMenu");
        iGameDetail.putExtra("game", Main.iGameId);
        startActivity(iGameDetail);
        this.finish();
    }
}
