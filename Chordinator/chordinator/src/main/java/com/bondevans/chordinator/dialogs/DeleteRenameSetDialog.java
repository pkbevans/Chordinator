package com.bondevans.chordinator.dialogs;

import com.bondevans.chordinator.Log;
import com.bondevans.chordinator.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.InputType;

import android.widget.EditText;

public class DeleteRenameSetDialog extends DialogFragment{
	private static final String TAG = "DeleteRenameSetDialog";
	private static final String KEY_SETID = "dsfjhk";
	private static final String KEY_SETNAME = "89df7h";
	private static final String KEY_DIALOG_ID = "9sdfni";
	public static final int DIALOG_DELETE = 0;
	public static final int DIALOG_RENAME = 1;
	long setId;
	String setName;
	EditText setNameText;
	DeleteRenameSetListener deleteRenameSetListener;

	public interface DeleteRenameSetListener{
		void deleteSetFromDB(long setId, String setName);
		void renameSet(long setId, String fromSetName, String toSetName);
	}
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.DialogFragment#onAttach(android.app.Activity)
	 */
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			deleteRenameSetListener = (DeleteRenameSetListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement DeleteRenameSetListener");
		}
	}
	
	public static DeleteRenameSetDialog newInstance(int dialogId, long setId, String setName) {
		DeleteRenameSetDialog frag = new DeleteRenameSetDialog();
		Bundle args = new Bundle();
		args.putInt(KEY_DIALOG_ID, dialogId);
		args.putLong(KEY_SETID, setId);
		args.putString(KEY_SETNAME, setName);
		frag.setArguments(args);
		return frag;
	}
	/* (non-Javadoc)
	 * @see android.support.v4.app.DialogFragment#onCreateDialog(android.os.Bundle)
	 */
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		int dialogId = getArguments().getInt(KEY_DIALOG_ID);
		if( dialogId == DIALOG_DELETE){
			setId = getArguments().getLong(KEY_SETID);
			setName = getArguments().getString(KEY_SETNAME);

			return new AlertDialog.Builder(getActivity())
			.setTitle(setName)
			.setMessage(getString(R.string.are_you_sure_delete_set))
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					// User clicked OK so delete
					deleteRenameSetListener.deleteSetFromDB(setId, setName);
				}
			})
			.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					/* User clicked cancel so do nothing */
				}
			})
			.create();
		}
		else if( dialogId == DIALOG_RENAME){
			setId = getArguments().getLong(KEY_SETID);
			setName = getArguments().getString(KEY_SETNAME);

			setNameText = new EditText(getActivity());
			setNameText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS|InputType.TYPE_CLASS_TEXT);
			setNameText.setText(setName);
			return new AlertDialog.Builder(getActivity())
			.setTitle(getString(R.string.rename) +" "+ setName)
			.setMessage(getString(R.string.new_file_name))
			.setView(setNameText)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					// User clicked OK so get new name from setNameText
					Log.d(TAG, "[Rename to["+ setNameText.getText().toString()+"]");
					deleteRenameSetListener.renameSet(setId, setName,
							setNameText.getText().toString());
				}
			})
			.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					/* User clicked cancel so do nothing */
				}
			})
			.create();
		}

		return null;// WILL NEVER GET HERE
	}
}
