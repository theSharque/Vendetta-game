package club.vendetta.game;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class About extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ((TextView) findViewById(R.id.version)).setText(Html.fromHtml(String.format(getString(R.string.aboutText), BuildConfig.VERSION_NAME)));

        ((TextView) findViewById(R.id.mail)).setText(Html.fromHtml(String.format(getString(R.string.aboutMail))));
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

    @Override
    public void onBackPressed() {
        back(null);
    }

    public void back(View view) {
        startActivity(new Intent(getApplicationContext(), DrumMenu.class));
        this.finish();
    }

    public void openSite(View view) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(getString(R.string.aboutLink)));
        startActivity(i);
    }
}
