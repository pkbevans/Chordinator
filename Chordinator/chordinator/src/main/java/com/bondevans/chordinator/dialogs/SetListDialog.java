package com.bondevans.chordinator.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;


import com.bondevans.chordinator.db.DBUtils;
import com.bondevans.chordinator.db.SongDB;
import com.bondevans.chordinator.Log;
import com.bondevans.chordinator.R;

public class SetListDialog extends DialogFragment {
	private static OnSetSelectedListener setSelectedListener;

	
	public interface OnSetSelectedListener{
		void addNewSet(long songId, String songName);
		void setNameClicked(long setId, long songId, String setName, String songName);
	}
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

	private final static String TAG="SetListDialog";
	Cursor setCursor=null; 
	/* (non-Javadoc)
	 * @see android.support.v4.app.DialogFragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle arg0) {
		Log.d(TAG, "HELLO onActivityCreated");
		super.onActivityCreated(arg0);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.DialogFragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "HELLO onCreate");
		super.onCreate(savedInstanceState);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.DialogFragment#onStart()
	 */
	@Override
	public void onStart() {
		Log.d(TAG, "HELLO onStart");
		super.onStart();
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.DialogFragment#show(android.support.v4.app.FragmentManager, java.lang.String)
	 */
	@Override
	public void show(FragmentManager manager, String tag) {
		Log.d(TAG, "HELLO show");
		super.show(manager, tag);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.DialogFragment#show(android.support.v4.app.FragmentTransaction, java.lang.String)
	 */
	@Override
	public int show(FragmentTransaction transaction, String tag) {
		Log.d(TAG, "HELLO show2");
		return super.show(transaction, tag);
	}

	public static SetListDialog newInstance(long songId, String songName) {
		Log.d(TAG, "HELLO newInstance");
		SetListDialog frag = new SetListDialog();
		Bundle args = new Bundle();
		args.putLong("id", songId);
		args.putString("song", songName);
		frag.setArguments(args);
		frag.setRetainInstance(true);
		return frag;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Log.d(TAG, "HELLO onCreateDialog");
		final long songId = getArguments().getLong("id");
		final String songName = getArguments().getString("song");
		String [] projection = {SongDB.COLUMN_ID, SongDB.COLUMN_SET_NAME};

		setCursor = getActivity().getContentResolver().query(
				DBUtils.SET(getString(R.string.authority)), projection, null, null, SongDB.COLUMN_SET_NAME + " ASC");

		if( setCursor.getCount()>0){
			return new AlertDialog.Builder(getActivity())
			.setTitle(getString(R.string.set_name))
			.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					// User clicked Add so show the Add Set dialog
					setSelectedListener.addNewSet(songId, songName);
				}
			})
			.setCursor(setCursor, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					Log.d(TAG, "HELLO button=["+whichButton+"]");
					setCursor.moveToPosition(whichButton);
					long set_id = Long.parseLong(setCursor.getString(0));//Set id
					setSelectedListener.setNameClicked(set_id, songId, setCursor.getString(1), songName);
				}
			}
			, SongDB.COLUMN_SET_NAME)
			.create();
		}
		else{
			return new AlertDialog.Builder(getActivity())
			.setTitle(getString(R.string.set_name))
			.setMessage(getString(R.string.no_sets))
			.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					// User clicked Add so show the Add Set dialog
					setSelectedListener.addNewSet(songId, songName);
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

	/* (non-Javadoc)
	 * @see android.support.v4.app.DialogFragment#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	public void onSaveInstanceState(Bundle arg0) {
		Log.d(TAG, "HELLO onSaveInstanceState");
		super.onSaveInstanceState(arg0);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.DialogFragment#onCancel(android.content.DialogInterface)
	 */
	@Override
	public void onCancel(DialogInterface dialog) {
		Log.d(TAG, "HELLO onCancel");
		setCursor.close();
		super.onCancel(dialog);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.DialogFragment#onDestroyView()
	 */
	@Override
	public void onDestroyView() {
		Log.d(TAG, "HELLO onDestroyView");
		setCursor.close();
		super.onDestroyView();
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.DialogFragment#onDismiss(android.content.DialogInterface)
	 */
	@Override
	public void onDismiss(DialogInterface dialog) {
		Log.d(TAG, "HELLO onDismiss");
		setCursor.close();
		super.onDismiss(dialog);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.DialogFragment#onStop()
	 */
	@Override
	public void onStop() {
		Log.d(TAG, "HELLO onStop");
		setCursor.close();
		super.onStop();
	}
}
