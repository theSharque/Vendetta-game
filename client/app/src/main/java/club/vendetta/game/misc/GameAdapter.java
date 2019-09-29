package club.vendetta.game.misc;

import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import club.vendetta.game.Main;
import club.vendetta.game.R;

/**
 * Created by sharque on 12.08.14.
 * <p/>
 * Custom adapter
 */
public class GameAdapter implements ListAdapter {
    private final int layoutResourceId;
    private final Context context;
    private final String[] sGameType;
    private final String[] sEndType;
    private List<GameItem> data;

    public GameAdapter(Context context, int layoutResourceId) {
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = new ArrayList<GameItem>();

        sGameType = new String[]{context.getString(R.string.gameTypeOpen), context.getString(R.string.gameTypeFriends)};
        sEndType = new String[]{context.getString(R.string.gameWinnerLast), context.getString(R.string.gameWinnerScore)};
    }

    public void clear() {
        this.data.clear();
    }

    public void add(int id, Bitmap bmPhoto, int iGameType, int iEndType, int iStartType, int iPlayer, int iInGame, int iVictims) {
        data.add(new GameItem(id, bmPhoto, iGameType, "", iEndType, iStartType, iPlayer, iInGame, iVictims));
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int i) {
        return this.data.size() > i && this.data.get(i).id != 0;
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
    public Object getItem(int i) {
        return data.get(i);
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
    public View getView(int position, View gameView, ViewGroup parent) {
        View row = gameView;
        GameHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new GameHolder();
            holder.game_photo = (ImageView) row.findViewById(R.id.contact_photo);
            holder.game_Type = (TextView) row.findViewById(R.id.gameType);
            holder.game_endType = (TextView) row.findViewById(R.id.endType);
            holder.game_players = (TextView) row.findViewById(R.id.playersCount);
            holder.game_victims = (TextView) row.findViewById(R.id.victimsCount);
            holder.game_ingame = (TextView) row.findViewById(R.id.ingame);

            row.setTag(holder);
        } else {
            holder = (GameHolder) row.getTag();
        }

        GameItem gameItem = data.get(position);
        holder.game_photo.setImageBitmap(gameItem.bmPhoto);
        holder.game_Type.setText(sGameType[gameItem.iGameType]);
        if (Main.iGameId == gameItem.id) {
            holder.game_ingame.setVisibility(View.VISIBLE);
        } else {
            holder.game_ingame.setVisibility(View.INVISIBLE);
        }

        if (gameItem.iStartType == 0) {
            holder.game_players.setText(Html.fromHtml(String.format(context.getString(R.string.gamePlayers), gameItem.iInGame, gameItem.iPlayer)));
        } else {
            holder.game_players.setText(Html.fromHtml(String.format(context.getString(R.string.gameHours), gameItem.iInGame, gameItem.iPlayer)));
        }
        holder.game_victims.setText(Html.fromHtml(String.format(context.getString(R.string.victims), gameItem.iVictims)));
        holder.game_endType.setText(Html.fromHtml(String.format(context.getString(R.string.endType), sEndType[gameItem.iEndType])));

        return row;
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

    static class GameHolder {
        public ImageView game_photo;
        public TextView game_Type;
        public TextView game_endType;
        public TextView game_players;
        public TextView game_victims;
        public TextView game_ingame;
    }
}
