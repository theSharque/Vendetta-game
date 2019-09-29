package club.vendetta.game;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import club.vendetta.game.misc.PingSend;
import club.vendetta.game.misc.PlayerAdapter;

public class ApproveSend extends ActionBarActivity {

    private String sClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approve_send);

        String sPost;

        SharedPreferences settings = getSharedPreferences(Main.PREFS_NAME, 0);
        int iCount = settings.getInt("contactsCount", 0);
        sClass = getIntent().getStringExtra("classname");

        if (iCount != 0) {
            findViewById(R.id.friendList).setVisibility(View.VISIBLE);
            findViewById(R.id.shareList).setVisibility(View.GONE);

            sPost = settings.getString("contactsPost", "");

            final PlayerAdapter pAdapter = new PlayerAdapter(this, R.layout.player_item);
            ((ListView) findViewById(R.id.contactList)).setAdapter(pAdapter);
            pAdapter.clear();

            final int iArg = getIntent().getIntExtra("iArg", 0);
            final int iGame = getIntent().getIntExtra("iGame", 0);

            Location lMe = PingSend.GiveMe(getApplicationContext());

            Main.lMain.Approver(getApplicationContext(), sPost, pAdapter, iGame, lMe);
            if (pAdapter.isEmpty()) {
                findViewById(R.id.contactList).setVisibility(View.GONE);
                findViewById(R.id.shareList).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.shareText)).setText(Html.fromHtml(getString(R.string.shareText)));
            }
            ((ListView) findViewById(R.id.contactList)).setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    Toast toast;

                    if (pAdapter.getItem(position).bGray) {
                        toast = Toast.makeText(getApplicationContext(), getString(R.string.needMoreGame), Toast.LENGTH_LONG);
                    } else {
                        if (!Main.lMain.SendMessage((int) id, iArg, iGame, "")) {
                            toast = Toast.makeText(getApplicationContext(), getString(R.string.network_problem), Toast.LENGTH_LONG);
                        } else {
                            if (iArg == 1) {
                                toast = Toast.makeText(getApplicationContext(), getString(R.string.approveSent), Toast.LENGTH_LONG);
                            } else {
                                toast = Toast.makeText(getApplicationContext(), getString(R.string.inviteSent), Toast.LENGTH_LONG);
                            }
                        }
                    }

                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            });
        } else {
            findViewById(R.id.friendList).setVisibility(View.GONE);
            findViewById(R.id.shareList).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.shareText)).setText(Html.fromHtml(getString(R.string.shareText)));
        }
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

    public void share(View view) {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");

        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.msgShareTitle));
        sharingIntent.putExtra(Intent.EXTRA_TEXT, String.format(getString(R.string.msgShareText), Main.sId));
        sharingIntent = Intent.createChooser(sharingIntent, getString(R.string.msgShareWindowTitle));
        sharingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(sharingIntent);
        this.finish();
    }

    public void back(View view) {
        try {
            startActivity(new Intent(getApplicationContext(), Class.forName(BuildConfig.APPLICATION_ID + "." + sClass)));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        this.finish();
    }
}
