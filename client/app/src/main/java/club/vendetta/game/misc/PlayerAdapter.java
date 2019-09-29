package club.vendetta.game.misc;

import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.location.Location;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import club.vendetta.game.R;

/**
 * Created by sharque on 12.08.14.
 * <p/>
 * Show player in game with all information about them
 */
public class PlayerAdapter implements ListAdapter {
    private final int layoutResourceId;
    private final Context context;
    public int iGameType = 1;
    public int iGameStatus = 1;
    public boolean bSelectable = true;
    private List<PlayerItem> data;
    private Location lMe;
    private int iDeleted = 0;
    private int iPos = 0;

    public PlayerAdapter(Context context, int textViewResourceId) {
        this.layoutResourceId = textViewResourceId;
        this.context = context;
        this.data = new ArrayList<PlayerItem>();
    }

    public void clear() {
        this.data.clear();
    }

    public void add(int iPhoto, String sLogin) {
        data.add(new PlayerItem(0, BitmapFactory.decodeResource(this.context.getResources(), iPhoto), sLogin, 0, "", "", "", "", "", true, 0, "", false));
        iPos = data.size();
    }

    public void add(int id, Bitmap bmPhoto, String sLogin, int iAdmin, Location lMe, String sLocation, String sBsid, String sPass, String sWins, String sKills, int iStars, String sKilledBy, boolean bGray) {
        this.lMe = lMe;
        data.add(new PlayerItem(id, bmPhoto, sLogin, iAdmin, sLocation, sBsid, sPass, sWins, sKills, true, iStars, sKilledBy, bGray));
        iPos = data.size();
    }

    public void add(int id, Bitmap bmPhoto, String sLogin, int iAdmin, Location lMe, String sLocation, String sBsid, String sPass, String sWins, String sKills, int iLive, int iStars, String sKilledBy) {
        this.lMe = lMe;
        data.add(new PlayerItem(id, bmPhoto, sLogin, iAdmin, sLocation, sBsid, sPass, sWins, sKills, iLive == 1, iStars, sKilledBy, false));
        iPos = data.size();
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int i) {
        return data.size() > i && iDeleted <= 0 && data.get(i).id != 0 && bSelectable;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver dataSetObserver) {
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
    }

    @Override
    public int getCount() {
        return this.data.size();
    }

    @Override
    public PlayerItem getItem(int i) {
        return i < data.size() ? data.get(i) : null;
    }

    @Override
    public long getItemId(int i) {
        return data.get(i).id;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int position, View vItem, ViewGroup parent) {
        PlayerHolder holder;
        Location lUser = new Location("gamer");
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);
        ColorMatrixColorFilter bwFilter = new ColorMatrixColorFilter(matrix);

        if (vItem == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            vItem = inflater.inflate(layoutResourceId, parent, false);

            holder = new PlayerHolder();
            holder.player_photo = (ImageView) vItem.findViewById(R.id.contact_photo);
            holder.player_killed = (ImageView) vItem.findViewById(R.id.contact_killed);
            holder.player_login = (TextView) vItem.findViewById(R.id.login);
            holder.player_rank = (TextView) vItem.findViewById(R.id.rank);
            holder.player_admin = (TextView) vItem.findViewById(R.id.admin);
            holder.player_distance = (TextView) vItem.findViewById(R.id.distance);
            holder.player_wins = (TextView) vItem.findViewById(R.id.wins);
            holder.player_kills = (TextView) vItem.findViewById(R.id.kills);
            holder.player_stars_count = (TextView) vItem.findViewById(R.id.starCount);

            holder.player_stars[0] = (ImageView) vItem.findViewById(R.id.star0);
            holder.player_stars[1] = (ImageView) vItem.findViewById(R.id.star1);
            holder.player_stars[2] = (ImageView) vItem.findViewById(R.id.star2);
            holder.player_stars[3] = (ImageView) vItem.findViewById(R.id.star3);
            holder.player_stars[4] = (ImageView) vItem.findViewById(R.id.star4);
            holder.player_stars[5] = (ImageView) vItem.findViewById(R.id.star5);
            holder.player_stars[6] = (ImageView) vItem.findViewById(R.id.star6);
            holder.player_stars[7] = (ImageView) vItem.findViewById(R.id.star7);
            holder.player_stars[8] = (ImageView) vItem.findViewById(R.id.star8);
            holder.player_stars[9] = (ImageView) vItem.findViewById(R.id.star9);

            vItem.setTag(holder);
        } else {
            holder = (PlayerHolder) vItem.getTag();
        }

        PlayerItem playerItem = data.get(position);

        holder.id = playerItem.id;
        holder.player_photo.setImageBitmap(playerItem.bmPhoto);
        holder.bmPhoto = playerItem.bmPhoto;

        if (playerItem.bLive) {
            if (playerItem.bGray) {
                holder.player_photo.setColorFilter(bwFilter);
                holder.player_killed.setVisibility(View.GONE);
                holder.player_rank.setText(context.getString(R.string.rank_0));
                holder.player_rank.setTextColor(Color.WHITE);
            } else {
                holder.player_killed.setVisibility(View.GONE);
                holder.player_rank.setText(context.getString(R.string.rank_0));
                holder.player_rank.setTextColor(Color.WHITE);
            }
        } else {
            holder.player_photo.setAlpha(190);
            holder.player_photo.setColorFilter(bwFilter);
            holder.player_killed.setVisibility(View.VISIBLE);
            if (playerItem.sKilledBy.equals("")) {
                holder.player_rank.setText(context.getString(R.string.disconected));
            } else {
                holder.player_rank.setText(String.format(context.getString(R.string.killedby), playerItem.sKilledBy));
            }
            holder.player_rank.setTextColor(context.getResources().getColor(R.color.red));
        }

        int iMaxStars = ((playerItem.iStars - 1) / 10) * 10;
        if (iMaxStars > 0) {
            holder.player_stars_count.setText(String.valueOf(iMaxStars) + " + ");
        }

        for (int i = 0; i < playerItem.iStars - iMaxStars; ++i) {
            holder.player_stars[i].setVisibility(View.VISIBLE);
        }

        holder.player_login.setText(playerItem.sLogin);
        if (playerItem.iAdmin == 1) {
            holder.player_admin.setVisibility(View.VISIBLE);
        } else {
            holder.player_admin.setVisibility(View.GONE);
        }

        String[] sLoc = playerItem.sLocation.split(":");
        if (sLoc.length == 2) {
            lUser.setLatitude(Double.parseDouble(sLoc[0]));
            lUser.setLongitude(Double.parseDouble(sLoc[1]));
        }

        lUser.setAltitude(0);
        if (lMe != null) {
            if (Math.round(lMe.distanceTo(lUser)) >= 1000) {
                holder.player_distance.setText(Html.fromHtml(String.format(context.getString(R.string.kilometers), Math.round(lMe.distanceTo(lUser) / 1000))));
            } else {
                holder.player_distance.setText(Html.fromHtml(String.format(context.getString(R.string.meters), Math.round(lMe.distanceTo(lUser)))));
            }
        }

        if (!playerItem.sWins.equals("")) {
            holder.player_wins.setText(" " + playerItem.sWins);
        } else {
            holder.player_wins.setText(" 0");
        }

        if (!playerItem.sKills.equals("")) {
            holder.player_kills.setText(" " + playerItem.sKills);
        } else {
            holder.player_kills.setText(" 0");
        }

        if (iDeleted > 0) {
            if (iDeleted == playerItem.id) {
                iPos = position;
                Animation aKill = AnimationUtils.loadAnimation(context, R.anim.kill_anim);

                aKill.setAnimationListener(new Animation.AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        iDeleted = 0;
                        data.remove(iPos);
                        ((ListView) ((Activity) context).findViewById(R.id.playerList)).invalidateViews();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });

                vItem.startAnimation(aKill);
            }

            if (position > iPos) {
                Animation aUp = AnimationUtils.loadAnimation(context, R.anim.up_anim);
                vItem.startAnimation(aUp);
            }
        }

        return vItem;
    }

    @Override
    public int getItemViewType(int i) {
        return R.layout.game_item;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    public void delete(int iId) {
        this.iDeleted = iId;
    }

    public void remove(int iId) {
        for (int i = 0; i < data.size(); ++i) {
            if (data.get(i).id == iId) {
                data.remove(i);

                break;
            }
        }
    }

    public static class PlayerHolder {
        public int id;
        public Bitmap bmPhoto;
        public ImageView player_photo;
        public ImageView player_killed;
        public TextView player_login;
        public TextView player_admin;
        public TextView player_distance;
        public TextView player_wins;
        public TextView player_kills;
        public TextView player_rank;

        public TextView player_stars_count;
        public ImageView[] player_stars = new ImageView[10];
    }
}
