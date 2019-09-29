package club.vendetta.game;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import club.vendetta.game.misc.PingSend;

public class DrumMenu extends ActionBarActivity {
    public static ImageView ivDrum;
    public static Context context;
    public static int iMenuPosition;
    public static int iFirstPosition;
    public static ImageView ivDown, ivUp;
    public static boolean bShowArrows = true;
    static Animation[] anUp = new Animation[9];
    static Animation[] anDown = new Animation[9];
    private static TextView[] tvMenu = new TextView[9];
    private static int iMenuCount;

    public static void loadMenu() {
        iMenuCount = 5;

        tvMenu[0].startAnimation(AnimationUtils.loadAnimation(context, R.anim.btn0));
        tvMenu[1].startAnimation(AnimationUtils.loadAnimation(context, R.anim.btn1));
        tvMenu[2].startAnimation(AnimationUtils.loadAnimation(context, R.anim.btn2));
        tvMenu[3].startAnimation(AnimationUtils.loadAnimation(context, R.anim.btn3));
        tvMenu[4].startAnimation(AnimationUtils.loadAnimation(context, R.anim.btn4));
        tvMenu[5].startAnimation(AnimationUtils.loadAnimation(context, R.anim.btn5));
        tvMenu[6].startAnimation(AnimationUtils.loadAnimation(context, R.anim.btn6));
        tvMenu[7].startAnimation(AnimationUtils.loadAnimation(context, R.anim.btn7));
        tvMenu[8].startAnimation(AnimationUtils.loadAnimation(context, R.anim.btn8));

        anUp[1] = AnimationUtils.loadAnimation(context, R.anim.btn1up);
        anUp[2] = AnimationUtils.loadAnimation(context, R.anim.btn2up);
        anUp[3] = AnimationUtils.loadAnimation(context, R.anim.btn3up);
        anUp[4] = AnimationUtils.loadAnimation(context, R.anim.btn4up);
        anUp[5] = AnimationUtils.loadAnimation(context, R.anim.btn5up);
        anUp[6] = AnimationUtils.loadAnimation(context, R.anim.btn6up);
        anUp[7] = AnimationUtils.loadAnimation(context, R.anim.btn7up);
        anUp[8] = AnimationUtils.loadAnimation(context, R.anim.btn8up);

        anDown[0] = AnimationUtils.loadAnimation(context, R.anim.btn0down);
        anDown[1] = AnimationUtils.loadAnimation(context, R.anim.btn1down);
        anDown[2] = AnimationUtils.loadAnimation(context, R.anim.btn2down);
        anDown[3] = AnimationUtils.loadAnimation(context, R.anim.btn3down);
        anDown[4] = AnimationUtils.loadAnimation(context, R.anim.btn4down);
        anDown[5] = AnimationUtils.loadAnimation(context, R.anim.btn5down);
        anDown[6] = AnimationUtils.loadAnimation(context, R.anim.btn6down);
        anDown[7] = AnimationUtils.loadAnimation(context, R.anim.btn7down);

        setupMenu();
    }

    public static void setupMenu() {
        for (TextView tvItem : tvMenu) {
            tvItem.setText("");
        }

        if (iFirstPosition >= 0) {
            tvMenu[iFirstPosition].setText(context.getString(R.string.find_game));
        }

        if (Main.bInGame) {
            tvMenu[iFirstPosition + 1].setText(context.getString(R.string.my_game));
        } else {
            tvMenu[iFirstPosition + 1].setText(context.getString(R.string.create_game));
        }
        tvMenu[iFirstPosition + 2].setText(context.getString(R.string.player_info));
        tvMenu[iFirstPosition + 3].setText(context.getString(R.string.share));

        if (iFirstPosition < 5) {
            tvMenu[iFirstPosition + 4].setText(context.getString(R.string.about));
        }
    }

    public static void menuUp() {
        if (iMenuPosition < iMenuCount) {
            setupMenu();

            for (int i = iFirstPosition; i < iFirstPosition + iMenuCount; ++i) {
                if (i >= 1 && i <= 8) {
                    tvMenu[i].startAnimation(anUp[i]);
                }
            }

            iMenuPosition++;
            iFirstPosition--;
        }
    }

    public static void menuDown() {
        if (iMenuPosition > 1) {
            setupMenu();

            for (int i = iFirstPosition; i < iFirstPosition + iMenuCount; ++i) {
                if (i >= 0 && i <= 7) {
                    tvMenu[i].startAnimation(anDown[i]);
                }
            }

            iMenuPosition--;
            iFirstPosition++;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drum_menu);

        context = getApplicationContext();

        iMenuPosition = 2;
        iFirstPosition = 3;

        MainReceiver.resendMessage(context);
        PingSend.ping(context);

        ivDrum = (ImageView) findViewById(R.id.imgDrum);
        ivDown = (ImageView) findViewById(R.id.arDown);
        ivUp = (ImageView) findViewById(R.id.arUp);
        RelativeLayout.LayoutParams lpDrum = (RelativeLayout.LayoutParams) ivDrum.getLayoutParams();

        Rect rect = new Rect();
        Window win = getWindow();  // Get the Window
        win.getDecorView().getWindowVisibleDisplayFrame(rect);

        lpDrum.width = rect.width();
        lpDrum.height = rect.width();
        lpDrum.rightMargin = -rect.width() / 2;
        lpDrum.bottomMargin = -rect.width() / 3;

        ivDrum.setLayoutParams(lpDrum);
        ivDrum.setOnTouchListener(new DrumSlider(this));

        ivDown.setLayoutParams(lpDrum);
        ivUp.setLayoutParams(lpDrum);
        final Animation anArDown = AnimationUtils.loadAnimation(this, R.anim.ar_down);
        final Animation anArUp = AnimationUtils.loadAnimation(this, R.anim.ar_up);

        anArDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (bShowArrows) {
                    ivUp.startAnimation(anArUp);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        anArUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (bShowArrows) {
                    ivDown.startAnimation(anArDown);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        ivUp.startAnimation(anArUp);

        findViewById(R.id.mainMenu).setOnClickListener(new MenuListener(this));

        TextView tvSoundText = (TextView) findViewById(R.id.soundText);
        TextView tvSound = (TextView) findViewById(R.id.sound);

        SoundListener clSound = new SoundListener(this, tvSound);
        tvSoundText.setOnClickListener(clSound);
        tvSound.setOnClickListener(clSound);
        if (Main.bSound) {
            tvSound.setText(R.string.on);
        } else {
            tvSound.setText(R.string.off);
        }

        tvMenu[0] = (TextView) findViewById(R.id.btn0);
        tvMenu[1] = (TextView) findViewById(R.id.btn1);
        tvMenu[2] = (TextView) findViewById(R.id.btn2);
        tvMenu[3] = (TextView) findViewById(R.id.btn3);
        tvMenu[4] = (TextView) findViewById(R.id.btn4);
        tvMenu[5] = (TextView) findViewById(R.id.btn5);
        tvMenu[6] = (TextView) findViewById(R.id.btn6);
        tvMenu[7] = (TextView) findViewById(R.id.btn7);
        tvMenu[8] = (TextView) findViewById(R.id.btn8);

        for (int i = 0; i < 9; ++i) {
            tvMenu[i].setTextSize(TypedValue.COMPLEX_UNIT_PX, rect.width() / 12);
        }

        loadMenu();
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
        if( Main.lMain == null ) {
            Main.lMain = new Loader(getApplicationContext());
        }

        Main.lMain.login();
        loadMenu();

        Animation aDrum = ivDrum.getAnimation();
        if (aDrum != null) {
            DrumMenu.ivDrum.startAnimation(AnimationUtils.loadAnimation(DrumMenu.context, R.anim.drumclean));
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
}
