package club.vendetta.game;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.appsflyer.AppsFlyerLib;

import java.io.IOException;

import club.vendetta.game.misc.CameraView;

public class UserInfo extends ActionBarActivity implements Camera.PictureCallback {
    public static String sPhoneNumber;
    public static Bitmap bmAvatar = null;
    private static Camera camera = null;
    private static int iCameraId;
    private ImageButton ibAvatar;
    private ColorMatrixColorFilter bwFilter;
    private CameraView cvUser;
    private Camera.CameraInfo info;
    private Camera.Parameters param;
    private SoundPool soundPool;
    private int soundId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        soundPool = new SoundPool(1, AudioManager.STREAM_SYSTEM, 0);
        soundId = soundPool.load("/system/media/audio/ui/camera_click.ogg", 1);

        Rect rect = new Rect();
        Window win = getWindow();  // Get the Window
        win.getDecorView().getWindowVisibleDisplayFrame(rect);

        RelativeLayout.LayoutParams lpSelfie = (RelativeLayout.LayoutParams) findViewById(R.id.user_click).getLayoutParams();
        lpSelfie.topMargin = rect.width() / 2;
        findViewById(R.id.user_click).setLayoutParams(lpSelfie);
        ((TextView) findViewById(R.id.user_click)).setText(Html.fromHtml(getString(R.string.user_click)));
        findViewById(R.id.user_approve).setLayoutParams(lpSelfie);
        ((TextView) findViewById(R.id.user_approve)).setText(Html.fromHtml(getString(R.string.user_approve)));

        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);
        bwFilter = new ColorMatrixColorFilter(matrix);

        ibAvatar = ((ImageButton) findViewById(R.id.avatar));
        LinearLayout.LayoutParams lpAvatar = (LinearLayout.LayoutParams) ibAvatar.getLayoutParams();
        int iMinH = rect.height() * 45 / 100;
        int iMinW = rect.width() * 90 / 100;

        if (iMinH < iMinW) {
            lpAvatar.height = iMinH;
            lpAvatar.width = iMinH;
        } else {
            lpAvatar.height = iMinW;
            lpAvatar.width = iMinW;
        }

        ibAvatar.setLayoutParams(lpAvatar);

        iCameraId = 0;
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                iCameraId = i;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(Main.brMessager);
        if (camera != null) {
            cvUser.mHolder.removeCallback(cvUser);
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (camera != null) {
            camera = Camera.open(iCameraId);
            param = camera.getParameters();
            param.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            param.setJpegQuality(100);
            camera.setParameters(param);
            camera.setDisplayOrientation(0);
            cvUser = new CameraView(this, camera, ibAvatar, info);
            ((FrameLayout) findViewById(R.id.preview)).addView(cvUser);

            try {
                camera.setPreviewDisplay(cvUser.getHolder());
            } catch (IOException e) {
                e.printStackTrace();
            }
            camera.startPreview();
        }

        registerReceiver(Main.brMessager, Main.ifMessager);

        if (Main.lMain == null) {
            Main.lMain = new Loader(getApplicationContext());
        }

        Main.lMain.login();
        if (!Main.sPhotoUrl.equals("")) {
            if (bmAvatar == null) {
                Main.lMain.DownloadBitmap(Main.sPhotoUrl);
                if (Main.lMain.bitmap != null) {
                    bmAvatar = Main.lMain.bitmap.copy(Main.lMain.bitmap.getConfig(), true);
                }
            }
            ibAvatar.setImageBitmap(bmAvatar);
            if (Main.bNeedApprove) {
                ibAvatar.setColorFilter(bwFilter);
            } else {
                ibAvatar.clearColorFilter();
            }
        } else {
            if (bmAvatar != null) {
                bmAvatar.recycle();
                bmAvatar = null;
            }
            ibAvatar.setImageResource(R.drawable.nobody);
        }

        if (Main.bLogedIn) {
            Main.lMain.SetMessages(getIntent().getIntExtra("mid", 0));

            findViewById(R.id.login).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.login)).setText(Main.sLogin);

            findViewById(R.id.loginname).setVisibility(View.GONE);
            findViewById(R.id.btnSignIn).setVisibility(View.GONE);

            if (Main.bNeedApprove || Main.sPhotoUrl.equals("")) {
                findViewById(R.id.back).setVisibility(View.GONE);
                findViewById(R.id.rank).setVisibility(View.GONE);
                findViewById(R.id.fame).setVisibility(View.GONE);

                if (Main.sPhotoUrl.equals("") || bmAvatar == null) {
                    findViewById(R.id.user_click).setVisibility(View.VISIBLE);
                    findViewById(R.id.user_approve).setVisibility(View.GONE);
                    findViewById(R.id.approveBtn).setVisibility(View.GONE);
                } else {
                    findViewById(R.id.user_click).setVisibility(View.GONE);
                    findViewById(R.id.user_approve).setVisibility(View.VISIBLE);
                    findViewById(R.id.approveBtn).setVisibility(View.VISIBLE);
                }

                findViewById(R.id.logo).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.user_click).setVisibility(View.GONE);
                findViewById(R.id.user_approve).setVisibility(View.GONE);
                findViewById(R.id.back).setVisibility(View.VISIBLE);
                findViewById(R.id.rank).setVisibility(View.VISIBLE);
                findViewById(R.id.fame).setVisibility(View.VISIBLE);
                findViewById(R.id.approveBtn).setVisibility(View.GONE);
                ((TextView) findViewById(R.id.scores)).setText(Html.fromHtml(String.format(getString(R.string.scoresItem), Main.iScores)));
                ((TextView) findViewById(R.id.wins)).setText(Html.fromHtml(String.format(getString(R.string.winsItem), Main.iWins)));
                findViewById(R.id.logo).setVisibility(View.GONE);
            }

            final AlertDialog.Builder ad = new AlertDialog.Builder(this);
            ad.setTitle(R.string.changeNameTitle); // сообщение
            ad.setMessage(R.string.changeNameMsg); // сообщение
            ad.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg1) {
                    findViewById(R.id.login).setVisibility(View.GONE);

                    findViewById(R.id.btnSignIn).setVisibility(View.VISIBLE);
                    findViewById(R.id.rank).setVisibility(View.GONE);
                    findViewById(R.id.fame).setVisibility(View.GONE);
                    findViewById(R.id.approveBtn).setVisibility(View.GONE);
                    findViewById(R.id.user_approve).setVisibility(View.GONE);

                    EditText etLogin = (EditText) findViewById(R.id.loginname);
                    etLogin.setVisibility(View.VISIBLE);
                    if (!Main.sLogin.equals("")) {
                        etLogin.setText(Main.sLogin);
                    }
                    etLogin.requestFocus();
                }
            });

            ad.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg1) {
                }
            });

            ad.setCancelable(true);

            findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Main.bInGame) {
                        Toast toast;
                        toast = Toast.makeText(getApplicationContext(), getString(R.string.msgYouAreInGameNick), Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    } else {
                        ad.show();
                    }
                }
            });

            ibAvatar.setEnabled(true);
        } else if (Main.lMain.sImei == null) {
            ((TextView) findViewById(R.id.login)).setText(getString(R.string.needBlueTooth));
            ibAvatar.setEnabled(false);
        } else if (Main.bNeedSign) {
            findViewById(R.id.login).setVisibility(View.GONE);
            findViewById(R.id.user_click).setVisibility(View.GONE);

            ibAvatar.setEnabled(false);
            findViewById(R.id.loginname).setVisibility(View.VISIBLE);
            findViewById(R.id.btnSignIn).setVisibility(View.VISIBLE);
            findViewById(R.id.rank).setVisibility(View.GONE);
            findViewById(R.id.fame).setVisibility(View.GONE);
        } else {
            ((TextView) findViewById(R.id.login)).setText(getString(R.string.network_problem));
            ibAvatar.setEnabled(false);
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

    public void SignIn(View view) {
        findViewById(R.id.loginname).setEnabled(false);
        final SharedPreferences sPref = getPreferences(MODE_PRIVATE);

        if (sPhoneNumber == null || sPhoneNumber.equals("")) {
            sPhoneNumber = sPref.getString("phone", "");

            if (sPhoneNumber.equals("")) {
                sPhoneNumber = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getLine1Number();
                if (sPhoneNumber == null || sPhoneNumber.equals("")) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(this);

                    alert.setTitle(getString(R.string.msgYourPhoneTitle));
                    alert.setMessage(getString(R.string.msgYourPhoneText));

                    final EditText input = new EditText(this);
                    input.setInputType(InputType.TYPE_CLASS_NUMBER);
                    alert.setView(input);

                    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            sPhoneNumber = input.getText().toString();
                            if (!sPhoneNumber.equals("")) {
                                if (sPhoneNumber != null && !sPhoneNumber.equals("")) {
                                    SharedPreferences.Editor edit = sPref.edit();
                                    edit.putString("phone", sPhoneNumber);
                                    edit.apply();
                                    Main.lMain.SignIn(((EditText) findViewById(R.id.loginname)).getText().toString(), sPhoneNumber);
                                }
                            }
                        }
                    });

                    alert.setCancelable(false);
                    alert.show();
                } else {
                    sPref.edit().putString("phone", sPhoneNumber);
                }
            }
        }

        if (sPhoneNumber != null && !sPhoneNumber.equals("")) {
            if (Main.sLogin == null || Main.sLogin.equals("")) {
                AppsFlyerLib.sendTrackingWithEvent(getApplicationContext(), "registration", "");
            }

            Main.sLogin = ((EditText) findViewById(R.id.loginname)).getText().toString();
            Main.lMain.SignIn(Main.sLogin, sPhoneNumber);
        }

        ((TextView) findViewById(R.id.login)).setText(Main.sLogin);
        findViewById(R.id.login).setVisibility(View.VISIBLE);
        findViewById(R.id.loginname).setVisibility(View.GONE);
        findViewById(R.id.btnSignIn).setVisibility(View.GONE);
        if (Main.sPhotoUrl.equals("") || bmAvatar == null) {
            findViewById(R.id.user_click).setVisibility(View.VISIBLE);
        }

        ibAvatar.setEnabled(true);

        if (Main.bNeedApprove) {
            findViewById(R.id.rank).setVisibility(View.GONE);
            findViewById(R.id.fame).setVisibility(View.GONE);
            findViewById(R.id.approveBtn).setVisibility(View.VISIBLE);
            findViewById(R.id.user_click).setVisibility(View.GONE);
            findViewById(R.id.user_approve).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.back).setVisibility(View.VISIBLE);
            findViewById(R.id.rank).setVisibility(View.VISIBLE);
            findViewById(R.id.fame).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.scores)).setText(Html.fromHtml(String.format(getString(R.string.scoresItem), Main.iScores)));
            ((TextView) findViewById(R.id.wins)).setText(Html.fromHtml(String.format(getString(R.string.winsItem), Main.iWins)));
            findViewById(R.id.logo).setVisibility(View.GONE);
        }
    }

    public void ChangeImage(View view) {
        if (Main.bInGame) {
            Toast toast;
            toast = Toast.makeText(getApplicationContext(), getString(R.string.msgYouAreInGameText), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        } else {
            findViewById(R.id.approveBtn).setVisibility(View.GONE);
            findViewById(R.id.user_approve).setVisibility(View.GONE);
            findViewById(R.id.user_click).setVisibility(View.GONE);
            findViewById(R.id.rank).setVisibility(View.GONE);
            findViewById(R.id.fame).setVisibility(View.GONE);
            findViewById(R.id.btnPhoto).setVisibility(View.VISIBLE);
            ibAvatar.clearColorFilter();

            if (camera != null) {
                return;
            }

            info = new Camera.CameraInfo();
            Camera.getCameraInfo(iCameraId, info);
            camera = Camera.open(iCameraId);

            param = camera.getParameters();
            param.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            param.setJpegQuality(100);
            camera.setParameters(param);
            camera.setDisplayOrientation(0);
            cvUser = new CameraView(this, camera, ibAvatar, info);
            ((FrameLayout) findViewById(R.id.preview)).addView(cvUser);

            try {
                camera.setPreviewDisplay(cvUser.getHolder());
            } catch (IOException e) {
                e.printStackTrace();
            }
            camera.startPreview();
        }
    }

    public void Approve(View view) {
        Intent iSendApprove = new Intent(getApplicationContext(), ApproveSend.class);
        iSendApprove.putExtra("iArg", 1);
        iSendApprove.putExtra("classname", "UserInfo");
        startActivity(iSendApprove);
    }

    public void back(View view) {
        if (camera != null) {
            cvUser.mHolder.removeCallback(cvUser);
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
        startActivity(new Intent(getApplicationContext(), DrumMenu.class));
        this.finish();
    }

    public void TakePhoto(View view) {
        camera.takePicture(null, null, this);
    }

    @Override
    public void onPictureTaken(byte[] bytes, Camera cam) {
        findViewById(R.id.btnPhoto).setVisibility(View.GONE);

        soundPool.play(soundId, 1, 1, 1, 0, 1f);

        if (cam != null) {
            cvUser.mHolder.removeCallback(cvUser);
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }

        Matrix matrix = new Matrix();
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            matrix.preRotate(270);
            matrix.postScale(-1.0f, 1.0f);
        } else {
            matrix.preRotate(90);
        }

        int iHeight = param.getPictureSize().height;
        Bitmap bmPhoto = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        if (bmAvatar != null) {
            bmAvatar.recycle();
            bmAvatar = null;
        }
        bmAvatar = Bitmap.createBitmap(bmPhoto, 0, 0, iHeight, iHeight, matrix, true);

        String sReferal = getSharedPreferences(Main.PREFS_NAME, MODE_PRIVATE)
                .getString("referal", "");
        if (sReferal.equals("")) {
            Main.lMain.UploadBitmap(Main.BASE_URL + "photoin.php?b=" + Main.lMain.sImei, bmAvatar);
        } else {
            Main.lMain.UploadBitmap(Main.BASE_URL + "photoin.php?b=" + Main.lMain.sImei + "&r=" + sReferal, bmAvatar);
        }

        this.onResume();
    }
}
