package club.vendetta.game;

import android.content.Intent;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class NewGame extends ActionBarActivity implements View.OnTouchListener {
    private int iGameType = 1;
    private int iStartType = 0;
    private int iEndType = 0;
    private int iStartCount;
    TextWatcher twPlayersCounter = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            boolean bPlayers;

            if (!charSequence.toString().equals("")) {
                iStartCount = Integer.valueOf(charSequence.toString());
                bPlayers = iStartCount > 0 && iStartCount < 100;
            } else {
                bPlayers = false;
            }

            findViewById(R.id.create).setEnabled(bPlayers);
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    };
    private int iVictimCount;
    private ImageView[] ivRips = new ImageView[3];
    private float iOldX;
    private boolean bAllowFree;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_game);

        iVictimCount = 1;

        findViewById(R.id.gameTypeSwitch).setOnTouchListener(this);
        findViewById(R.id.gameStartSwitch).setOnTouchListener(this);
        findViewById(R.id.gameWinSwitch).setOnTouchListener(this);
        findViewById(R.id.gameVictimsSwitch).setOnTouchListener(this);

        ivRips[0] = (ImageView) findViewById(R.id.rip1);
        ivRips[1] = (ImageView) findViewById(R.id.rip2);
        ivRips[2] = (ImageView) findViewById(R.id.rip3);

        bAllowFree = Main.iPlayedGames > 4;

        ((EditText) findViewById(R.id.gameStartPlayers)).addTextChangedListener(twPlayersCounter);
        ((EditText) findViewById(R.id.gameStartHours)).addTextChangedListener(twPlayersCounter);
        Rect rect = new Rect();
        Window win = getWindow();  // Get the Window
        win.getDecorView().getWindowVisibleDisplayFrame(rect);

        ((TextView) findViewById(R.id.createGame)).setTextSize(TypedValue.COMPLEX_UNIT_PX, rect.width() / 11);

        ((TextView) findViewById(R.id.gameTypeLeftWord)).setTextSize(TypedValue.COMPLEX_UNIT_PX, rect.width() / 24);
        ((TextView) findViewById(R.id.gameTypeRightWord)).setTextSize(TypedValue.COMPLEX_UNIT_PX, rect.width() / 24);
        ((TextView) findViewById(R.id.gameStartLeftWord)).setTextSize(TypedValue.COMPLEX_UNIT_PX, rect.width() / 24);
        ((TextView) findViewById(R.id.gameStartRightWord)).setTextSize(TypedValue.COMPLEX_UNIT_PX, rect.width() / 24);
        ((TextView) findViewById(R.id.gameWinLeftWord)).setTextSize(TypedValue.COMPLEX_UNIT_PX, rect.width() / 24);
        ((TextView) findViewById(R.id.gameWinRightWord)).setTextSize(TypedValue.COMPLEX_UNIT_PX, rect.width() / 24);

        ((TextView) findViewById(R.id.gameTypeTitle)).setTextSize(TypedValue.COMPLEX_UNIT_PX, rect.width() / 18);
        ((TextView) findViewById(R.id.gameStartTitle)).setTextSize(TypedValue.COMPLEX_UNIT_PX, rect.width() / 18);
        ((TextView) findViewById(R.id.gameVictimsTitle)).setTextSize(TypedValue.COMPLEX_UNIT_PX, rect.width() / 18);
        ((TextView) findViewById(R.id.gameWinTitle)).setTextSize(TypedValue.COMPLEX_UNIT_PX, rect.width() / 18);

        ((ImageView) findViewById(R.id.gameTypePistol)).setMaxHeight(rect.height() / 12);
        if (!bAllowFree) {
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0);
            ((ImageView) findViewById(R.id.gameTypePistol)).setColorFilter(new ColorMatrixColorFilter(matrix));
        }

        ((ImageView) findViewById(R.id.gameStartPistol)).setMaxHeight(rect.height() / 12);
        ((ImageView) findViewById(R.id.gameWinPistol)).setMaxHeight(rect.height() / 12);
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

    public void SaveGame(View view) {
        if (Main.lMain.SaveGame(iGameType, iStartType, iStartCount, iVictimCount, iEndType)) {
            Main.lMain.login();

            Intent iGameDetail = new Intent(getApplicationContext(), GameDetail.class);
            iGameDetail.putExtra("game", Main.iGameId);
            iGameDetail.putExtra("classname", "DrumMenu");
            iGameDetail.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(iGameDetail);
            this.finish();
        } else {
            Toast toast;
            toast = Toast.makeText(getApplicationContext(), getString(R.string.network_problem), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
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

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        boolean bRight = false;

        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            iOldX = motionEvent.getX();
        }

        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            LinearLayout llAction = (LinearLayout) view;

            if (Math.abs(iOldX - motionEvent.getX()) > 50) {
                if (iOldX < motionEvent.getX()) {
                    bRight = true;
                }
            } else {
                if (motionEvent.getX() > llAction.getWidth() / 2 + 50) {
                    bRight = true;
                } else if (motionEvent.getX() < llAction.getWidth() / 2 - 50) {
                    bRight = false;
                } else {
                    switch (view.getId()) {
                        case R.id.gameTypeSwitch:
                            bRight = iGameType == 1;
                            break;

                        case R.id.gameStartSwitch:
                            bRight = iStartType == 0;
                            break;

                        case R.id.gameWinSwitch:
                            bRight = iEndType == 0;
                            break;
                    }
                }
            }
            Rect rPaddings;
            ImageView pistol;

            switch (view.getId()) {
                case R.id.gameTypeSwitch:
                    pistol = (ImageView) findViewById(R.id.gameTypePistol);
                    if (bRight) {
                        if (bAllowFree) {
                            iGameType = 0;
                            findViewById(R.id.gameTypeRightStar).setVisibility(View.VISIBLE);
                            ((TextView) findViewById(R.id.gameTypeRightWord)).setTextColor(getResources().getColor(R.color.white));

                            findViewById(R.id.gameTypeLeftStar).setVisibility(View.INVISIBLE);
                            ((TextView) findViewById(R.id.gameTypeLeftWord)).setTextColor(getResources().getColor(R.color.gray));

                            pistol.setImageResource(R.drawable.rgun);
                            rPaddings = new Rect(pistol.getPaddingLeft(), pistol.getPaddingTop(), pistol.getPaddingRight(), pistol.getPaddingBottom());
                            pistol.setBackgroundResource(R.drawable.rgunbg);
                            pistol.setPadding(rPaddings.left, rPaddings.top, rPaddings.right, rPaddings.bottom);
                        } else {
                            Toast toast;
                            toast = Toast.makeText(getApplicationContext(), getString(R.string.gameTypeNeed), Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.TOP, 0, 0);
                            toast.show();
                        }
                    } else {
                        iGameType = 1;
                        findViewById(R.id.gameTypeLeftStar).setVisibility(View.VISIBLE);
                        ((TextView) findViewById(R.id.gameTypeLeftWord)).setTextColor(getResources().getColor(R.color.white));

                        findViewById(R.id.gameTypeRightStar).setVisibility(View.INVISIBLE);
                        ((TextView) findViewById(R.id.gameTypeRightWord)).setTextColor(getResources().getColor(R.color.gray));

                        pistol.setImageResource(R.drawable.lgun);
                        rPaddings = new Rect(pistol.getPaddingLeft(), pistol.getPaddingTop(), pistol.getPaddingRight(), pistol.getPaddingBottom());
                        pistol.setBackgroundResource(R.drawable.lgunbg);
                        pistol.setPadding(rPaddings.left, rPaddings.top, rPaddings.right, rPaddings.bottom);
                    }
                    break;

                case R.id.gameStartSwitch:
                    pistol = (ImageView) findViewById(R.id.gameStartPistol);
                    if (bRight) {
                        iStartType = 1;
                        findViewById(R.id.gameStartRightStar).setVisibility(View.VISIBLE);
                        findViewById(R.id.gameStartRight).setVisibility(View.VISIBLE);
                        ((TextView) findViewById(R.id.gameStartRightWord)).setTextColor(getResources().getColor(R.color.white));

                        findViewById(R.id.gameStartLeftStar).setVisibility(View.INVISIBLE);
                        findViewById(R.id.gameStartLeft).setVisibility(View.GONE);
                        ((TextView) findViewById(R.id.gameStartLeftWord)).setTextColor(getResources().getColor(R.color.gray));

                        pistol.setImageResource(R.drawable.rgun);
                        rPaddings = new Rect(pistol.getPaddingLeft(), pistol.getPaddingTop(), pistol.getPaddingRight(), pistol.getPaddingBottom());
                        pistol.setBackgroundResource(R.drawable.rgunbg);
                        pistol.setPadding(rPaddings.left, rPaddings.top, rPaddings.right, rPaddings.bottom);
                    } else {
                        iStartType = 0;
                        findViewById(R.id.gameStartLeftStar).setVisibility(View.VISIBLE);
                        findViewById(R.id.gameStartLeft).setVisibility(View.VISIBLE);
                        ((TextView) findViewById(R.id.gameStartLeftWord)).setTextColor(getResources().getColor(R.color.white));

                        findViewById(R.id.gameStartRightStar).setVisibility(View.INVISIBLE);
                        findViewById(R.id.gameStartRight).setVisibility(View.GONE);
                        ((TextView) findViewById(R.id.gameStartRightWord)).setTextColor(getResources().getColor(R.color.gray));

                        pistol.setImageResource(R.drawable.lgun);
                        rPaddings = new Rect(pistol.getPaddingLeft(), pistol.getPaddingTop(), pistol.getPaddingRight(), pistol.getPaddingBottom());
                        pistol.setBackgroundResource(R.drawable.lgunbg);
                        pistol.setPadding(rPaddings.left, rPaddings.top, rPaddings.right, rPaddings.bottom);
                    }
                    break;

                case R.id.gameVictimsSwitch:
                    if (bRight) {
                        if (iVictimCount < 3) {
                            iVictimCount++;
                        }
                    } else {
                        if (iVictimCount > 1) {
                            iVictimCount--;
                        }
                    }

                    for (int i = 0; i < 3; ++i) {
                        if (i < iVictimCount) {
                            ivRips[i].setImageResource(R.drawable.ripon);
                        } else {
                            ivRips[i].setImageResource(R.drawable.ripoff);
                        }
                    }
                    break;

                case R.id.gameWinSwitch:
                    pistol = (ImageView) findViewById(R.id.gameWinPistol);
                    if (bRight) {
                        iEndType = 1;
                        findViewById(R.id.gameWinRightStar).setVisibility(View.VISIBLE);
                        ((TextView) findViewById(R.id.gameWinRightWord)).setTextColor(getResources().getColor(R.color.white));

                        findViewById(R.id.gameWinLeftStar).setVisibility(View.INVISIBLE);
                        ((TextView) findViewById(R.id.gameWinLeftWord)).setTextColor(getResources().getColor(R.color.gray));

                        pistol.setImageResource(R.drawable.rgun);
                        rPaddings = new Rect(pistol.getPaddingLeft(), pistol.getPaddingTop(), pistol.getPaddingRight(), pistol.getPaddingBottom());
                        pistol.setBackgroundResource(R.drawable.rgunbg);
                        pistol.setPadding(rPaddings.left, rPaddings.top, rPaddings.right, rPaddings.bottom);
                    } else {
                        iEndType = 0;
                        findViewById(R.id.gameWinLeftStar).setVisibility(View.VISIBLE);
                        ((TextView) findViewById(R.id.gameWinLeftWord)).setTextColor(getResources().getColor(R.color.white));

                        findViewById(R.id.gameWinRightStar).setVisibility(View.INVISIBLE);
                        ((TextView) findViewById(R.id.gameWinRightWord)).setTextColor(getResources().getColor(R.color.gray));

                        pistol.setImageResource(R.drawable.lgun);
                        rPaddings = new Rect(pistol.getPaddingLeft(), pistol.getPaddingTop(), pistol.getPaddingRight(), pistol.getPaddingBottom());
                        pistol.setBackgroundResource(R.drawable.lgunbg);
                        pistol.setPadding(rPaddings.left, rPaddings.top, rPaddings.right, rPaddings.bottom);
                    }
                    break;
            }
        }

        return true;
    }
}
