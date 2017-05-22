package com.bondevans.chordinator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;

import static android.content.Intent.ACTION_MEDIA_BUTTON;

public class MyFlicReceiver extends BroadcastReceiver{
    private static final String TAG = "MyFlicReceiver";
    public static final String FLIC_MESSAGE = "FLICMSG";
    public static final String FLIC_INTENT = "FLIC_INTENT";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Display details of event:
        Log.d(TAG, "HELLO FLIC: ACTION="+intent.getAction());
        if (ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
            KeyEvent Xevent = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            // e will get a KEY_DOWN and a KEY_UP message - only do something on the UP
            if ((KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE == Xevent.getKeyCode() ||
//                    KeyEvent.KEYCODE_MEDIA_FAST_FORWARD == Xevent.getKeyCode() ||
//                    KeyEvent.KEYCODE_MEDIA_REWIND == Xevent.getKeyCode() ||
                    KeyEvent.KEYCODE_MEDIA_NEXT == Xevent.getKeyCode() ||
                    KeyEvent.KEYCODE_MEDIA_PREVIOUS == Xevent.getKeyCode()) &&
                    (Xevent.getAction() == KeyEvent.ACTION_UP)) {
                Log.d(TAG, "HELLO FLIC PLAY-PAUSE/NEXT/PREVIOUS");
                sendMessage(context, Xevent.getKeyCode());
            }
            else {
                Log.d(TAG, "HELLO FLIC IGNORING EVENT: "+ Xevent.getKeyCode());
            }
        }
    }
    // Send an Intent with an action named "custom-event-name". The Intent sent should
// be received by the ReceiverActivity.
    private void sendMessage(Context context, int message) {
        Log.d(TAG, "Broadcasting message");
        Intent intent = new Intent(FLIC_INTENT);
        // You can also include some extra data.
        intent.putExtra(FLIC_MESSAGE, message);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
