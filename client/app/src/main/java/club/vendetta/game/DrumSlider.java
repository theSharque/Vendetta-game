package club.vendetta.game;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;

public class DrumSlider implements View.OnTouchListener {
    private static final float fBulletAngle = 60;
    private static final float fTime = 200;
    private static final float fMagnifer = 6;

    private final Context context;
    private final SoundPool sp;
    private final int soundClick;
    int iRepeatCout, iBullet;
    private Animation aRotate;
    private float fDownAngle, fMoveAngle, fStartAngle, fEndAngle;

    public DrumSlider(Context context) {
        this.context = context;
        fStartAngle = 0.0f;
        sp = new SoundPool(16, AudioManager.STREAM_MUSIC, 0);
        soundClick = sp.load(context, R.raw.click, 1);
    }

    @Override
    public boolean onTouch(final View view, MotionEvent event) {
        double x, y;

        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (DrumMenu.bShowArrows) {
                    DrumMenu.bShowArrows = false;
                    DrumMenu.ivDown.setVisibility(View.GONE);
                    DrumMenu.ivUp.setVisibility(View.GONE);
                    DrumMenu.ivDown.clearAnimation();
                    DrumMenu.ivUp.clearAnimation();
                }

                x = event.getX() - (view.getWidth() / 2d);
                y = view.getHeight() - event.getY() - (view.getHeight() / 2d);
                fMoveAngle = fDownAngle = (float) (180 - Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI);

                if (aRotate != null && !aRotate.hasEnded()) {
                    float time = (float) AnimationUtils.currentAnimationTimeMillis() - (float) aRotate.getStartTime();
                    float fPosition = 1.0f / ((float) aRotate.getDuration() / time);

                    float fCorrectPosition = aRotate.getInterpolator().getInterpolation(fPosition);
                    if (iRepeatCout > iBullet) {
                        if (fEndAngle > fStartAngle) {
                            fStartAngle -= fBulletAngle;
                            fStartAngle += fBulletAngle * fCorrectPosition;
                        } else {
                            fStartAngle += fBulletAngle;
                            fStartAngle -= fBulletAngle * fCorrectPosition;
                        }
                    } else {
                        fStartAngle += (fEndAngle - fStartAngle) * fCorrectPosition;
                    }

                    aRotate = new RotateAnimation(fStartAngle, fStartAngle, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

                    aRotate.setStartOffset(0);
                    aRotate.setDuration(0);
                    aRotate.setFillAfter(true);
                    aRotate.setInterpolator(context, android.R.anim.linear_interpolator);

                    view.startAnimation(aRotate);
                } else {
                    fStartAngle = fEndAngle;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                x = event.getX() - (view.getWidth() / 2d);
                y = view.getHeight() - event.getY() - (view.getHeight() / 2d);
                float fTempAngle = (float) (180 - Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI);
                fStartAngle += fMoveAngle - fTempAngle;
                fMoveAngle = fTempAngle;

                aRotate = new RotateAnimation(fStartAngle, fStartAngle, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

                aRotate.setStartOffset(0);
                aRotate.setDuration(0);
                aRotate.setFillAfter(true);
                aRotate.setInterpolator(context, android.R.anim.linear_interpolator);

                view.startAnimation(aRotate);
                break;

            case MotionEvent.ACTION_UP:
                x = event.getX() - (view.getWidth() / 2d);
                y = view.getHeight() - event.getY() - (view.getHeight() / 2d);
                float fUpAngle = (float) (180 - Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI);
                fEndAngle = fStartAngle + ((fDownAngle - fUpAngle) / (event.getEventTime() - event.getDownTime()) * fMagnifer) * fBulletAngle;
                fEndAngle -= fEndAngle % fBulletAngle;

                iRepeatCout = (int) (Math.abs(fStartAngle - fEndAngle) / fBulletAngle);

                float fCorrectPosition;
                if (iRepeatCout > 0) {
                    DecelerateInterpolator diCorrector = new DecelerateInterpolator();
                    fCorrectPosition = diCorrector.getInterpolation(1.0f / (float) iRepeatCout);
                } else {
                    if (Math.abs(fEndAngle - fStartAngle) > fBulletAngle / 3) {
                        if (fEndAngle > fStartAngle) {
                            DrumMenu.menuUp();
                        } else {
                            DrumMenu.menuDown();
                        }
                    }
                    fCorrectPosition = 1;
                }
                int iTime = (int) (fTime * fCorrectPosition);
                iBullet = 0;

                if (Math.abs(fEndAngle - fStartAngle) > fBulletAngle) {
                    if (fEndAngle > fStartAngle) {
                        DrumMenu.menuUp();
                        aRotate = new RotateAnimation(fStartAngle, fStartAngle + fBulletAngle, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                        fStartAngle += fBulletAngle;
                    } else {
                        DrumMenu.menuDown();
                        aRotate = new RotateAnimation(fStartAngle, fStartAngle - fBulletAngle, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                        fStartAngle -= fBulletAngle;
                    }
                } else {
                    aRotate = new RotateAnimation(fStartAngle, fEndAngle, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                }

                aRotate.setDuration(iTime);
                aRotate.setFillAfter(true);
                aRotate.setInterpolator(context, android.R.anim.linear_interpolator);
                aRotate.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        if (fEndAngle > fStartAngle) {
                            if (Main.bSound) {
                                sp.play(soundClick, 1, 1, 0, 0, 1);
                            }
                        } else if (fEndAngle < fStartAngle) {
                            if (Main.bSound) {
                                sp.play(soundClick, 1, 1, 0, 0, 1);
                            }
                        }
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if (iRepeatCout > iBullet) {
                            if (Math.abs(fEndAngle - fStartAngle) > fBulletAngle) {
                                if (fEndAngle > fStartAngle) {
                                    aRotate = new RotateAnimation(fStartAngle, fStartAngle + fBulletAngle, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                                    fStartAngle += fBulletAngle;
                                } else {
                                    aRotate = new RotateAnimation(fStartAngle, fStartAngle - fBulletAngle, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                                    fStartAngle -= fBulletAngle;
                                }
                            } else {
                                aRotate = new RotateAnimation(fStartAngle, fEndAngle, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                            }

                            DecelerateInterpolator diCorrector = new DecelerateInterpolator();
                            float fCorrectPosition = diCorrector.getInterpolation(1.0f / (float) (iRepeatCout - iBullet));
                            int iTime = (int) (fTime * fCorrectPosition);
                            iBullet++;
                            aRotate.setDuration(iTime);
                            aRotate.setFillAfter(true);
                            if (iRepeatCout == iBullet) {
                                aRotate.setInterpolator(context, android.R.anim.decelerate_interpolator);
                            } else {
                                aRotate.setInterpolator(context, android.R.anim.linear_interpolator);
                            }
                            aRotate.setAnimationListener(this);

                            view.startAnimation(aRotate);
                        } else {
                            fEndAngle = fEndAngle % 360.0f;
                            fStartAngle = fEndAngle;
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });

                view.startAnimation(aRotate);
                break;
        }

        return true;
    }
}
