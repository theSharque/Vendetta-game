package club.vendetta.game;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import club.vendetta.game.misc.MessageAdapter;
import club.vendetta.game.misc.MessageItem;
import club.vendetta.game.misc.PingSend;


public class Messages extends ActionBarActivity {

    MessageAdapter maMessages;
    View.OnClickListener oclRemove = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            MessageAdapter.MessageHolder msg = (MessageAdapter.MessageHolder) ((View) view.getParent()).getTag();
            Main.lMain.SetMessages(msg.id);
            if (!Main.sMsgCount.equals("") && Integer.valueOf(Main.sMsgCount) > 0) {
                Main.sMsgCount = String.valueOf(Integer.valueOf(Main.sMsgCount) - 1);
            }

            maMessages.clear();
            Main.lMain.login();
            Main.lMain.Messages(maMessages);

            ((ListView) findViewById(R.id.messageList)).invalidateViews();
        }
    };
    private Context context;
    View.OnClickListener oclOpen = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            MessageAdapter.MessageHolder msg = (MessageAdapter.MessageHolder) ((View) ((View) view.getParent()).getParent()).getTag();

            MessageItem smMsg = (MessageItem) maMessages.getItem(msg.pos);
            Intent iMsg = null;

            Main.lMain.SetMessages(smMsg.id);

            switch (smMsg.iType) {
                case 1:
                    iMsg = new Intent(context, ApproveRecieve.class);
                    iMsg.putExtra("mid", smMsg.id);
                    iMsg.putExtra("name", smMsg.sLogin);
                    iMsg.putExtra("photo", smMsg.sArg2);
                    iMsg.putExtra("invite", false);
                    break;

                case 2:
                    iMsg = new Intent(context, UserInfo.class);
                    iMsg.putExtra("mid", smMsg.id);
                    break;

                case 3:
                    if (!smMsg.sArg2.equals("")) {
                        iMsg = new Intent(context, ApproveRecieve.class);

                        iMsg.putExtra("mid", smMsg.id);
                        iMsg.putExtra("name", smMsg.sLogin);
                        iMsg.putExtra("user", smMsg.iArg1);
                        iMsg.putExtra("photo", smMsg.sArg2);
                        iMsg.putExtra("invite", true);
                    } else {
                        iMsg = new Intent(context, GameDetail.class);

                        Location location = PingSend.GiveMe(getApplicationContext());

                        iMsg.putExtra("mid", smMsg.id);
                        iMsg.putExtra("game", smMsg.iArg1);
                        iMsg.putExtra("invite", true);
                        iMsg.putExtra("name", smMsg.sLogin);
                        iMsg.putExtra("location", location);
                        iMsg.putExtra("classname", "DrumMenu");
                    }
                    break;

                case 4:
                    iMsg = new Intent(context, Main.class);
                    iMsg.putExtra("mid", smMsg.id);
                    break;

                case 7:
                    Main.lMain.login();
                    switch (smMsg.iArg1) {
                        case 0:
                            iMsg = new Intent(context, FindGame.class);
                            break;

                        case 1:
                            iMsg = new Intent(context, GameDetail.class);
                            iMsg.putExtra("classname", "DrumMenu");
                            break;

                        case 2:
                            Main.btOn(context);
                            iMsg = new Intent(context, GameDetail.class);
                            iMsg.putExtra("classname", "DrumMenu");
                            break;

                        case 3:
                            iMsg = new Intent(context, FindGame.class);
                            break;

                        case 4:
                            iMsg = new Intent(context, FindGame.class);
                            break;

                        case 5:
                            iMsg = new Intent(context, GameDetail.class);
                            iMsg.putExtra("classname", "DrumMenu");
                            break;

                        case 6:
                            iMsg = new Intent(context, GameDetail.class);
                            iMsg.putExtra("classname", "DrumMenu");
                            break;

                        case 7:
                            iMsg = new Intent(context, FindGame.class);
                            break;

                        case 8:
                            iMsg = new Intent(context, GameDetail.class);
                            iMsg.putExtra("classname", "DrumMenu");
                            break;
                    }

                    if (iMsg != null) {
                        iMsg.putExtra("mid", smMsg.id);
                    }
                    break;

                case 9:
                    iMsg = new Intent(getApplicationContext(), KillActivity.class);

                    iMsg.putExtra("mid", smMsg.id);
                    iMsg.putExtra("game", smMsg.iArg1);
                    iMsg.putExtra("player", Integer.valueOf(smMsg.sArg2));
                    break;

                case 10:
                    iMsg = new Intent(context, GameDetail.class);

                    iMsg.putExtra("mid", smMsg.id);
                    iMsg.putExtra("game", smMsg.iArg1);
                    iMsg.putExtra("classname", "DrumMenu");
                    break;
            }

            if (iMsg != null) {
                startActivity(iMsg);
                finish();
            }
        }
    };
    private ListView lvMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        context = getApplicationContext();

        lvMessages = ((ListView) findViewById(R.id.messageList));
        maMessages = new MessageAdapter(this, R.layout.message_item, oclRemove, oclOpen);
        lvMessages.setAdapter(maMessages);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(Main.brMessager);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(Main.brMessager, Main.ifMessager);
        supportInvalidateOptionsMenu();

        maMessages.clear();
        Main.lMain.login();
        Main.lMain.Messages(maMessages);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.about, menu);

        if (lvMessages != null) {
            maMessages.clear();
            Main.lMain.login();
            Main.lMain.Messages(maMessages);
            lvMessages.invalidateViews();
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return item.getItemId() == R.id.action_help || super.onOptionsItemSelected(item);
    }

    public void back(View view) {
        this.finish();
    }
}
