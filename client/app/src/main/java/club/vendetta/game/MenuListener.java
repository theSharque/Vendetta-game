package club.vendetta.game;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class MenuListener implements View.OnClickListener, Animation.AnimationListener {
    Context context;

    public MenuListener(Context context) {
        this.context = context;
    }

    @Override
    public void onClick(View view) {
        if (DrumMenu.bShowArrows) {
            DrumMenu.bShowArrows = false;
            DrumMenu.ivDown.setVisibility(View.GONE);
            DrumMenu.ivUp.setVisibility(View.GONE);
            DrumMenu.ivDown.clearAnimation();
            DrumMenu.ivUp.clearAnimation();
        }

        Animation aRollOut = AnimationUtils.loadAnimation(DrumMenu.context, R.anim.drumout);
        aRollOut.setAnimationListener(this);
        DrumMenu.ivDrum.startAnimation(aRollOut);
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        Intent iDrumMenu = null;

        switch (DrumMenu.iMenuPosition) {
            case 1:
                iDrumMenu = new Intent(context, FindGame.class);
                break;

            case 2:
                if (Main.bInGame) {
                    iDrumMenu = new Intent(context, GameDetail.class);
                    iDrumMenu.putExtra("game", Main.iGameId);
                    iDrumMenu.putExtra("classname", "DrumMenu");
                } else {
                    if (Main.bNeedApprove) {
                        AlertDialog.Builder ad = new AlertDialog.Builder(context);
                        ad.setTitle(context.getString(R.string.msgYouNeedAppproveTitle));
                        ad.setMessage(context.getString(R.string.msgYouNeedAppproveText));
                        ad.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int arg1) {
                            }
                        });
                        ad.setCancelable(true);
                        ad.show();
                    } else {
                        iDrumMenu = new Intent(context, NewGame.class);
                    }
                }
                break;

            case 3:
                iDrumMenu = new Intent(context, UserInfo.class);
                break;

            case 4:
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, context.getString(R.string.msgShareTitle));
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, String.format(context.getString(R.string.msgShareText), Main.sId));
                iDrumMenu = Intent.createChooser(sharingIntent, context.getString(R.string.msgShareWindowTitle));
                break;

            case 5:
                iDrumMenu = new Intent(context, About.class);
                break;
        }

        if (iDrumMenu != null) {
            iDrumMenu.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(iDrumMenu);
        }
    }

    @Override
    public void onAnimationStart(Animation animation) {
    }

    @Override
    public void onAnimationRepeat(Animation animation) {
    }
}
