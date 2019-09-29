package club.vendetta.game;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PhotoPreview extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_preview);

        Rect rect = new Rect();
        Window win = getWindow();  // Get the Window
        win.getDecorView().getWindowVisibleDisplayFrame(rect);

        Intent i = getIntent();
        Bitmap bmPhoto = i.getParcelableExtra("photo");
        String sName = i.getStringExtra("login");

        ImageButton ibAvatar = ((ImageButton) findViewById(R.id.avatar));
        ((TextView) findViewById(R.id.login)).setText(sName);
        ibAvatar.setImageBitmap(bmPhoto);

        LinearLayout.LayoutParams lpAvatar = (LinearLayout.LayoutParams) ibAvatar.getLayoutParams();
        int iMinH = rect.height() * 50 / 100;
        int iMinW = rect.width() * 90 / 100;

        if (iMinH < iMinW) {
            lpAvatar.height = iMinH;
            lpAvatar.width = iMinH;
        } else {
            lpAvatar.height = iMinW;
            lpAvatar.width = iMinW;
        }

        ibAvatar.setLayoutParams(lpAvatar);
    }

    public void back(View view) {
        this.finish();
    }
}
