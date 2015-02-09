package com.bondevans.chordinator.dialogs;

import java.io.File;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.bondevans.chordinator.Log;
import com.bondevans.chordinator.R;
import com.bondevans.chordinator.SongUtils;
import com.bondevans.chordinator.db.DBUtils;

public class DeleteSongDialog extends DialogFragment{

	public static final String TAG = "DeleteSongDialog";
	public static final String SONG_DETAILS = "songdetails";
	static String mFilePath;
	static String mTitle;
	static long mSongId;
	private static String mAuthority;
	
	public static DeleteSongDialog newInstance(String authority, long songId, String title, String filePath) {
		Log.d(TAG, "HELLO filePath=["+filePath+"]");
		DeleteSongDialog frag = new DeleteSongDialog();
		Bundle args = new Bundle();
		mAuthority = authority;
		mFilePath = filePath;
		mTitle = title;
		mSongId = songId;
		frag.setArguments(args);
		return frag;
	}
	/* (non-Javadoc)
	 * @see android.support.v4.app.DialogFragment#onCreateDialog(android.os.Bundle)
	 */
	@Override
	public Dialog onCreateDialog(Bundle args) {
		TextView songTitle;
		final CheckBox deleteFromDisk;

		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.song_delete_dialog, null);

		songTitle = (TextView) layout.findViewById(R.id.songTitle);
		songTitle.setText(mFilePath);
		deleteFromDisk = (CheckBox) layout.findViewById(R.id.deleteFromDisk);
		deleteFromDisk.setChecked(false);
		

		return new AlertDialog.Builder(getActivity())
		.setTitle(mTitle)
		.setView(layout)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Delete song from database
				doDBDelete(mSongId);
				// If requested, delete from disk as well
				if(deleteFromDisk.isChecked()){
					doDiskDelete(mFilePath);
				}
			}
		})
		.create();
	}
	void doDBDelete(long song_id){
		int rows = getActivity().getContentResolver().delete(
				Uri.withAppendedPath(DBUtils.SONG(mAuthority), String.valueOf(song_id)), 
				null, null);
		if(rows != 1){
			SongUtils.toast(getActivity(), "Failed to Delete - song_id="+song_id);
		}
	}
	private void doDiskDelete(String filePath) {
		File myFile = new File (filePath);
		if(myFile.delete()){
			SongUtils.toast(this.getActivity(), myFile.getName()+" deleted");
		}
		else{
			SongUtils.toast(this.getActivity(), "Unable to delete file: "+filePath);
		}
	}
}
