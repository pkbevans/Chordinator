package com.bondevans.chordinator.dialogs;

import com.bondevans.chordinator.Log;
import com.bondevans.chordinator.R;
import com.bondevans.chordinator.setlist.SetList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.InputType;

import android.widget.EditText;

public class AddSetDialog extends DialogFragment {
	private static final String TAG = "AddSetDialog";
	private static final String KEY_CALLEDBYMAIN = "hfdh";
	private static final String KEY_SONGID = "Sosdfl";
	private static final String KEY_SONGNAME = "sasdk";
	private EditText fileNameText;
	private CreateSetListener createSetListener;

	public interface CreateSetListener{
		void createSet(String setName, long songId, String songName);
		void createBrowserSet(String setName, String songName);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.DialogFragment#onAttach(android.app.Activity)
	 */
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			createSetListener = (CreateSetListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement CreateSetListener");
		}
	}

	public static AddSetDialog newInstance(boolean calledFromMainActivity, long songId, String songName) {
		AddSetDialog frag = new AddSetDialog();
		Bundle args = new Bundle();
		args.putBoolean(KEY_CALLEDBYMAIN, calledFromMainActivity);
		args.putLong(KEY_SONGID, songId);
		args.putString(KEY_SONGNAME, songName);
		frag.setArguments(args);
		return frag;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final long id = getArguments().getLong(KEY_SONGID);
		final String name = getArguments().getString(KEY_SONGNAME);
		final boolean dbSet = getArguments().getBoolean(KEY_CALLEDBYMAIN);
		fileNameText = new EditText(getActivity());
		fileNameText.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_WORDS);
		return new AlertDialog.Builder(getActivity())
		.setTitle(getString(R.string.add_new_set))
		.setView(fileNameText)
		.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				if( dbSet ){
					Log.d(TAG, "HELLO OK pressed - ID=["+id+"]");
					// If id>0 then we are adding a song to a DB set
					createSetListener.createSet(fileNameText.getText().toString(), id, name);
				}
				else{
					Log.d(TAG, "HELLO OK pressed - NO ID");
					// otherwise its a SETLIST on sdcard
					createSetListener.createBrowserSet(SetList.SETLIST_PREFIX+fileNameText.getText().toString(), name);
				}
			}
		}
				)
				.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						/* User clicked cancel so do nothing */
						Log.d(TAG, "HELLO Cancel pressed");
					}
				})
				.create();
	}
}
