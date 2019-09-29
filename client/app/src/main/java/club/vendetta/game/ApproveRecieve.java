package club.vendetta.game;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ApproveRecieve extends Activity {

    private int iUser;
    private String sPhoto;
    private boolean bInvite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_approve_recieve);

        Intent i = getIntent();
        sPhoto = i.getStringExtra("photo");
        String sName = i.getStringExtra("name");
        iUser = i.getIntExtra("user", 0);
        bInvite = i.getBooleanExtra("invite", false);

        if (bInvite) {
            ((TextView) findViewById(R.id.approve_text)).setText(getString(R.string.approveInvite));
        } else {
            ((TextView) findViewById(R.id.approve_text)).setText(getString(R.string.approveAsc));
        }

        ((TextView) findViewById(R.id.login)).setText(sName);
        Rect rect = new Rect();
        Window win = getWindow();  // Get the Window
        win.getDecorView().getWindowVisibleDisplayFrame(rect);

        ImageButton ibAvatar = ((ImageButton) findViewById(R.id.avatar));
        LinearLayout.LayoutParams lpAvatar = (LinearLayout.LayoutParams) ibAvatar.getLayoutParams();

        int iMinH = rect.height() * 40 / 100;
        int iMinW = rect.width() * 80 / 100;

        if (iMinH < iMinW) {
            lpAvatar.height = iMinH;
            lpAvatar.width = iMinH;
        } else {
            lpAvatar.height = iMinW;
            lpAvatar.width = iMinW;
        }

        ibAvatar.setLayoutParams(lpAvatar);

        if (Main.lMain.DownloadBitmap(sPhoto)) {
            ibAvatar.setImageBitmap(Main.lMain.bitmap);
        } else {
            ibAvatar.setImageResource(android.R.drawable.ic_dialog_alert);
        }
    }

    /*
        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            getMenuInflater().inflate(R.menu.all, menu);
            Main.menuNumbers(menu, getResources().getDisplayMetrics().scaledDensity);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_message:
                    Intent iMsg = new Intent(this, Messages.class);
                    this.startActivity(iMsg);
                    return true;

                case R.id.action_help:
                    return true;
            }

            return super.onOptionsItemSelected(item);
        }
    */
    public void Accept(View view) {
        if (bInvite) {
            if (!Main.lMain.SendMessage(iUser, 3, 0, "")) {
                Main.lMain.SaveMessage(getApplicationContext(), iUser, 3, 0, "");
            }
        } else {
            if (!Main.lMain.SendMessage(0, 2, 1, sPhoto)) {
                Main.lMain.SaveMessage(getApplicationContext(), 0, 2, 1, sPhoto);
            }
        }

        finish();
    }

    public void Decline(View view) {
        if (bInvite) {
            if (!Main.lMain.SendMessage(iUser, 7, 4, "")) {
                Main.lMain.SaveMessage(getApplicationContext(), iUser, 7, 4, "");
            }
        } else {
            if (!Main.lMain.SendMessage(0, 2, 0, sPhoto)) {
                Main.lMain.SaveMessage(getApplicationContext(), 0, 2, 0, sPhoto);
            }
        }

        finish();
    }
}
