package com.bondevans.chordinator.asynctask;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;


import com.bondevans.chordinator.ChordinatorException;
import com.bondevans.chordinator.Log;
import com.bondevans.chordinator.R;
import com.bondevans.chordinator.db.DBUtils;

public class SortOutFilePathsTask extends AsyncTask<String, Void, String> {
	private static final String TAG = "SortOutFilePathsTask";
	private ProgressDialog progressDialog;
	private String errorMsg = "";
	private Context mContext;

	public SortOutFilePathsTask(Context context){
		mContext = context;
	}

	/* (non-Javadoc)
	 * @see android.os.AsyncTask#onPreExecute()
	 */
	@Override
	protected void onPreExecute() {
		progressDialog = ProgressDialog.show(mContext, "", "Updating File Paths", true, false);
		super.onPreExecute();
	}
	String encoding;
	@Override
	protected String doInBackground(String... args) {
		String response = "";

		sortOutFileNames(args[0]);
		return response;
	}

	void sortOutFileNames(String authority) {
		Log.d(TAG, "Updating song paths for [" + authority + "] to [" + Environment.getExternalStorageDirectory().getPath() + "]");
		try {
			DBUtils.updateSongPaths(mContext.getContentResolver(), 
					authority, 
					mContext.getString(R.string.sdcard),
					Environment.getExternalStorageDirectory().getPath());
		} catch (ChordinatorException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onPostExecute(String result) {
		progressDialog.cancel();
		if( errorMsg.equalsIgnoreCase("")){
			// No errors...
		}
		else{
			// something went wrong
		}
	}
}
