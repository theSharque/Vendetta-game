package club.vendetta.game;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.CheckBox;

public class Agreement extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agreement);
    }

    public void confirm(View view) {
        if (((CheckBox) findViewById(R.id.agreed)).isChecked()) {
            SharedPreferences prefs = getSharedPreferences(Main.class.getSimpleName(), Context.MODE_PRIVATE);
            prefs.edit().putBoolean(Main.AGGREEMENT, true).apply();

            startActivity(new Intent(getApplicationContext(), Main.class));
            this.finish();
        }
    }

    public void showTerm(View view) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(getString(R.string.web_terms)));
        startActivity(i);
    }

    public void check(View view) {
        if (((CheckBox) findViewById(R.id.agreed)).isChecked()) {
            findViewById(R.id.agreement_button).setEnabled(true);
        } else {
            findViewById(R.id.agreement_button).setEnabled(false);
        }
    }
}
