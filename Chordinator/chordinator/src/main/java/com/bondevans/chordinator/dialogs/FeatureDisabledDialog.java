package com.bondevans.chordinator.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;

import com.bondevans.chordinator.R;

public class FeatureDisabledDialog extends DialogFragment{

	public static final String TAG = "FeatureDisabledDialog";

	public static FeatureDisabledDialog newInstance() {
		FeatureDisabledDialog frag = new FeatureDisabledDialog();
		Bundle args = new Bundle();
		frag.setArguments(args);
		return frag;
	}
	/* (non-Javadoc)
	 * @see android.support.v4.app.DialogFragment#onCreateDialog(android.os.Bundle)
	 */
	@Override
	public Dialog onCreateDialog(Bundle args) {

		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		return new AlertDialog.Builder(getActivity())
		.setTitle(R.string.feature_disabled_title)
		.setView(inflater.inflate(R.layout.feature_disabled_layout, null, false))
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// User clicked OK so close dialog 
			}
		})
		.setNeutralButton(R.string.upgrade_now, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Go to Chordinator Augmented on google.play
				launchAppStore();
			}
		})
		.create();
	}
	/*
	 * Launch google.play.chordinator.aug
	 * 
	 */
	void launchAppStore(){
		try {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.paid_url))));
		}
		catch (Exception e) {
		}
	}
}
