    package com.bondevans.chordinator.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.bondevans.chordinator.R;

public class GotSongDialog extends DialogFragment {
    public static final String KEY_SONGTEXT = "KEY1";
    public static final String KEY_SONGFILE = "KEY2";
    public static final String KEY_CHOPRO = "KEY3";
//    private static final String TAG = "GotSongDialog";
    private GotSongListener gotSongListener;

    public interface GotSongListener {
        void onGotSong(String fileName, String songText, boolean isChoPro, boolean convertChopro);
    }

    public void setGotSongListener(GotSongListener gotSongListener) {
        this.gotSongListener = gotSongListener;
    }

    /* (non-Javadoc)
     * @see android.support.v4.app.DialogFragment#onCreateDialog(android.os.Bundle)
     */
    @Override @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String songText= getArguments().getString(KEY_SONGTEXT);
        final boolean isChoPro = getArguments().getBoolean(KEY_CHOPRO);
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.song_found_dialog,
                (ViewGroup) getActivity().findViewById(R.id.layout_root));

        // Only show the "Convert to Chopro checkbox if the file is NOT already in chopro
        final CheckBox convertChopro = (CheckBox) layout.findViewById(R.id.convertChopro);
        convertChopro.setVisibility(isChoPro?View.INVISIBLE:View.VISIBLE);

        TextView mSongTextView = (TextView) layout.findViewById(R.id.song);
        final TextView mFileName = (TextView) layout.findViewById(R.id.file_name);

        mSongTextView.setText(songText);
        mFileName.setText(getArguments().getString(KEY_SONGFILE));

        return new AlertDialog.Builder(getActivity())
                .setView(layout)
                .setTitle(R.string.set_song_details)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String fileName = mFileName.getText().toString();

                        gotSongListener.onGotSong(fileName, songText, isChoPro, !isChoPro&&convertChopro.isChecked());
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Cancel pressed so do nothing
                    }
                }).create();
    }
}
