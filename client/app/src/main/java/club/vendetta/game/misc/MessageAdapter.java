package club.vendetta.game.misc;

import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import club.vendetta.game.R;

/**
 * Created by Sharque on 06.10.14.
 * message incoming view adapter
 */
public class MessageAdapter implements ListAdapter {
    private final int layoutResourceId;
    private final Context context;
    private final View.OnClickListener oclOpen;
    private final View.OnClickListener oclRemove;
    private List<MessageItem> data;

    public MessageAdapter(Context context, int textViewResourceId, View.OnClickListener oclRemove, View.OnClickListener oclOpen) {
        this.layoutResourceId = textViewResourceId;
        this.context = context;
        this.oclRemove = oclRemove;
        this.oclOpen = oclOpen;

        data = new ArrayList<MessageItem>();
    }

    public void clear() {
        data.clear();
    }

    public void add(Integer id, Integer iType, String sLogin, Integer iArg1, String sArg2) {
        data.add(new MessageItem(id, iType, sLogin, iArg1, sArg2));
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int i) {
        return data.size() > 0 && data.size() > i && data.get(i).id != 0;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver dataSetObserver) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {

    }

    @Override
    public int getCount() {
        return data.size();
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
    public View getView(int i, View view, ViewGroup viewGroup) {
        MessageHolder holder;

        if (view == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            view = inflater.inflate(layoutResourceId, viewGroup, false);

            holder = new MessageHolder();
            holder.title = (TextView) view.findViewById(R.id.title);
            holder.text = (TextView) view.findViewById(R.id.text);
            holder.remove = (ImageButton) view.findViewById(R.id.remove);

            view.setTag(holder);
        } else {
            holder = (MessageHolder) view.getTag();
        }

        MessageItem messageItem = data.get(i);

        holder.pos = i;
        holder.id = messageItem.id;
        holder.remove.setOnClickListener(oclRemove);
        holder.title.setOnClickListener(oclOpen);

        switch (messageItem.iType) {
            case 1:  // Approve my photo
                holder.title.setText(context.getString(R.string.msgApproveTitle));
                holder.text.setText(context.getString(R.string.msgPleaseApprove));
                break;

            case 2:  // Approved
                holder.title.setText(context.getString(R.string.msgApproveTitle));
                if (messageItem.iArg1 == 1) {
                    holder.text.setText(context.getString(R.string.msgPhotoApproved));
                } else {
                    holder.text.setText(context.getString(R.string.msgPhotoDeclined));
                }
                break;

            case 3:  // Invite to game
                holder.title.setText(context.getString(R.string.msgInviteTitle));
                if (!messageItem.sArg2.equals("")) {
                    holder.text.setText(String.format(context.getString(R.string.msgInviteAsk), messageItem.sLogin));
                } else {
                    holder.text.setText(String.format(context.getString(R.string.msgInviteText), messageItem.sLogin));
                }
                break;

            case 4:  // Invite to game
                if (messageItem.sArg2.equals("")) {
                    holder.title.setText(context.getString(R.string.msgGameCloseTitle));
                    holder.text.setText(context.getString(R.string.msgGameCloseText));
                } else {
                    holder.title.setText(context.getString(R.string.msgUserLeaveTitle));
                    holder.text.setText(String.format(context.getString(R.string.msgUserLeaveText), messageItem.sArg2));
                }
                break;

            case 7:  // A lot of messages about game
                switch (messageItem.iArg1) {
                    case 0:
                        holder.title.setText(context.getString(R.string.msgTooFarTitle));
                        holder.text.setText(context.getString(R.string.msgTooFarText));
                        break;

                    case 1:
                        holder.title.setText(context.getString(R.string.msgGameStartedTitle));
                        holder.text.setText(context.getString(R.string.msgGameStartedText));
                        break;

                    case 2:
                        holder.title.setText(context.getString(R.string.msgGameStartTitle));
                        holder.text.setText(context.getString(R.string.msgGameStartText));
                        break;

                    case 3:
                        holder.title.setText(context.getString(R.string.msgGameCantTitle));
                        holder.text.setText(context.getString(R.string.msgGameCantText));
                        break;

                    case 4:
                        holder.title.setText(context.getString(R.string.msgAskDeclinedTitle));
                        holder.text.setText(context.getString(R.string.msgAskDeclinedText));
                        break;

                    case 5:
                        holder.title.setText(context.getString(R.string.msgWinTitle));
                        holder.text.setText(context.getString(R.string.msgWinText));
                        break;

                    case 6:
                        holder.title.setText(context.getString(R.string.msgNoWinTitle));
                        holder.text.setText(context.getString(R.string.msgNoWinText));
                        break;

                    case 7:
                        holder.title.setText(context.getString(R.string.msgDeleteTitle));
                        holder.text.setText(context.getString(R.string.msgDeleteText));
                        break;

                    case 8:
                        holder.title.setText(context.getString(R.string.msgWinnerTitle));
                        holder.text.setText(String.format(context.getString(R.string.msgWinnerText), messageItem.sArg2));
                        break;
                }
                break;

            case 9:
                holder.title.setText(context.getString(R.string.msgKilledTitle));
                holder.text.setText(String.format(context.getString(R.string.msgKilledText), messageItem.sLogin));
                break;

            case 10: // Start the game
                holder.title.setText(context.getString(R.string.msgNeedStartTitle));
                holder.text.setText(context.getString(R.string.msgNeedStartText));
                break;
        }

        return view;
    }

    @Override
    public int getItemViewType(int i) {
        return R.layout.message_item;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    public static class MessageHolder {
        public int id;
        public TextView title;
        public TextView text;
        public ImageButton remove;
        public int pos;
    }
}
