package com.bondevans.chordinator.dialogs;

import com.bondevans.chordinator.Log;
import com.bondevans.chordinator.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;



public class SongDetailsDialog extends DialogFragment{

	public static final String TAG = "SongDetailsDialog";
	public static final String SONG_DETAILS = "songdetails";
	private static final String KEY_FILEPATH = "JHK";
	private static String mFilePath;

	public static SongDetailsDialog newInstance(String filePath) {
		Log.d(TAG, "HELLO filePath=["+filePath+"]");
		SongDetailsDialog frag = new SongDetailsDialog();
		mFilePath=filePath;
		Bundle args = new Bundle();
		args.putString(KEY_FILEPATH, filePath);
		frag.setArguments(args);
		return frag;
	}
	/* (non-Javadoc)
	 * @see android.support.v4.app.DialogFragment#onCreateDialog(android.os.Bundle)
	 */
	@Override
	public Dialog onCreateDialog(Bundle args) {
	
		return new AlertDialog.Builder(getActivity())
		.setTitle(R.string.song_details)
		.setMessage(mFilePath)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// User clicked OK so close dialog 
			}
		})
		.create();
	}
}
