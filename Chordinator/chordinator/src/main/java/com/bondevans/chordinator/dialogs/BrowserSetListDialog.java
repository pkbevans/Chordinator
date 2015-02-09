package com.bondevans.chordinator.dialogs;

import java.io.File;
import java.io.FilenameFilter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import android.widget.ArrayAdapter;

import com.bondevans.chordinator.Log;
import com.bondevans.chordinator.R;
import com.bondevans.chordinator.SongFile;
import com.bondevans.chordinator.Statics;
import com.bondevans.chordinator.setlist.SetList;

public class BrowserSetListDialog extends DialogFragment {
	private static OnSetSelectedListener setSelectedListener;


	public interface OnSetSelectedListener{
		public void addNewSet(String songPath);
		public void setNameClicked(String setName, String songPath);
	}

	private File chordinatorDir;
	private String[] mSetNames;
	/* (non-Javadoc)
	 * @see android.support.v4.app.DialogFragment#onAttach(android.app.Activity)
	 */
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			setSelectedListener = (OnSetSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnSetSelectedListener");
		}
	}

	private final static String TAG="BroswerSetListDialog";

	public static BrowserSetListDialog newInstance(String songPath) {
		Log.d(TAG, "HELLO newInstance");
		BrowserSetListDialog frag = new BrowserSetListDialog();
		Bundle args = new Bundle();
		args.putString("song", songPath);
		frag.setArguments(args);
		frag.setRetainInstance(true);
		return frag;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Log.d(TAG, "HELLO onCreateDialog");
		final String songPath = getArguments().getString("song");

		chordinatorDir = new File (Statics.CHORDINATOR_DIR);
		// Only interested in SETLIST files
		SetListFileFilter filter = new SetListFileFilter(SetList.SETLIST_PREFIX);
		mSetNames = chordinatorDir.list(filter);

		if( mSetNames.length>0){
			return new AlertDialog.Builder(getActivity())
			.setItems(mSetNames, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					Log.d(TAG, "HELLO whichButton=["+whichButton+"]");
					// User selected the Set
					setSelectedListener.setNameClicked(mSetNames[whichButton], songPath);
				}
			})
			.setTitle(getString(R.string.set_name))
			.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					// User clicked Add so show the Add Set dialog
					setSelectedListener.addNewSet(songPath);
				}
			})
			.create();
		}
		else{
			return new AlertDialog.Builder(getActivity())
			.setTitle(getString(R.string.set_name))
			.setMessage(getString(R.string.no_sets))
			.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					// User clicked Add so show the Add Set dialog
					setSelectedListener.addNewSet(songPath);
				}
			})
			.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					/* User clicked cancel so do nothing */
				}
			})
			.create();
		}
	}
	private class SetListFileFilter implements FilenameFilter{
		private String prefix="";
		public SetListFileFilter(String prefix){
			this.prefix = prefix;
		}

		public boolean accept(File dir, String name){
			if (name.startsWith(prefix))
				return true;
			return false;
		}
	}
}
