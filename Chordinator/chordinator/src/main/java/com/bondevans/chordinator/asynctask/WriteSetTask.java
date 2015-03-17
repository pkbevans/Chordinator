package com.bondevans.chordinator.asynctask;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import com.bondevans.chordinator.Log;
import com.bondevans.chordinator.db.DBUtils;
import com.bondevans.chordinator.db.SongDB;
import com.bondevans.chordinator.setlist.SetSong;

import java.util.List;

/**
 * Writes SET to database in background.
 * (Currently not used)
 */
public class WriteSetTask extends AsyncTask<Void, Void, Void> {
	private static final String TAG = "WriteSetTask";
	private Context mContext;
	private String mAuthority;
	private long mSetId;
	private List<SetSong> mSongs;

	public WriteSetTask(Context context, String authority, long setId, List<SetSong> songs) {
		mContext = context;
		mAuthority = authority;
		mSetId = setId;
		mSongs = songs;
	}

	@Override
	protected Void doInBackground(Void... voids) {
		writeSet();
		return null;
	}

	void writeSet() {
		Log.d(TAG, "HELLO - deleting Set Items ["+mSetId+"]");
		// Delete all set items first
		mContext.getContentResolver().delete(Uri.withAppendedPath(
						DBUtils.SETITEM(mAuthority), String.valueOf(mSetId)),
				null, null);

		// then iterate thru new list and give each one a set_order
		int i = 0;
		while (i < mSongs.size()) {
			Log.d(TAG, "HELLO Adding song [" + mSongs.get(i).title + "] set_order=[" + i + "]");
			ContentValues values = new ContentValues();
			values.put(SongDB.COLUMN_SETLIST_ID, mSetId);
			values.put(SongDB.COLUMN_SONG_ID, mSongs.get(i).id);
			values.put(SongDB.COLUMN_SET_ORDER, i);

			mContext.getContentResolver().insert(DBUtils.SETITEM(mAuthority), values);
			++i;
		}
	}
}
