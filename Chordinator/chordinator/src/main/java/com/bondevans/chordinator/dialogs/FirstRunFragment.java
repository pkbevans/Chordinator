package com.bondevans.chordinator.dialogs;

import com.bondevans.chordinator.Log;
import com.bondevans.chordinator.R;
import com.bondevans.chordinator.asynctask.ScanSongsActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;


public class FirstRunFragment extends DialogFragment{

	protected static final String TAG = "FirstRunFragment";

	public static FirstRunFragment newInstance() {
		Log.d(TAG, "HELLO newInstance");
		FirstRunFragment frag = new FirstRunFragment();
		Bundle args = new Bundle();
		frag.setArguments(args);
		return frag;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Log.d(TAG, "HELLO onCreateDialog");

		return new AlertDialog.Builder(getActivity())
		.setTitle(getString(R.string.chord_aug_firstrun_heading))
		.setMessage(R.string.chord_aug_firstrun_text)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int whichButton) {
				// Scan disk for songs
				Log.d(TAG, "HELLO scanForSongs");
				// Scan for songs and import sets at the same time
				scanForSongs();
			}
		})
		.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Cancel pressed - do nothing
			}
		})
		.create();
	}
	private void scanForSongs(){
		Log.d(TAG, "HELLO scanForSongs");
		Intent myIntent = new Intent(this.getActivity(), ScanSongsActivity.class);
		myIntent.putExtra(ScanSongsActivity.INTENT_DOSETS, true);// Do do sets
		try {
			startActivity(myIntent);
		}
		catch (ActivityNotFoundException e) {
			Log.d(TAG, e.getMessage());
		}
	}

}
