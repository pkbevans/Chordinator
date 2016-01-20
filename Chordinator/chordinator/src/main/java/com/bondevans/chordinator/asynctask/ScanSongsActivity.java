package com.bondevans.chordinator.asynctask;


import android.content.Intent;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bondevans.chordinator.ColourScheme;
import com.bondevans.chordinator.Log;
import com.bondevans.chordinator.R;
import com.bondevans.chordinator.utils.SdCardFactory;
import com.bondevans.chordinator.utils.Ute;

/**
 * MainActivity displays the screen's UI and starts a TaskFragment which will
 * execute an asynchronous task and will retain itself when configuration
 * changes occur.
 */
public class ScanSongsActivity extends FragmentActivity implements ScanSongsFragment.TaskCallbacks {
	private static final String TAG = ScanSongsActivity.class.getSimpleName();

	public static final String INTENT_DOSETS = "ScanSongsDoSets";
	private static final String KEY_FILES = "FilesScanned";
	private static final String KEY_SONGS = "SongsFound";
	private static final String KEY_FILE = "fileFound";
	private static final String KEY_FOLDER = "folder";
	private static final String TAG_TASK_FRAGMENT = "task_fragment";

	private ScanSongsFragment mTaskFragment;
	private TextView mFolderText;
	private TextView mFileText;
	private TextView mFileCountText;
	private TextView mSongsFoundText;
	private Button mButton;
	private Boolean mDoSets;
	private SdCardFactory mSdcards;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate(Bundle)");
        int colourScheme = Ute.getColourScheme(this);
        setTheme(colourScheme == ColourScheme.LIGHT ? R.style.Chordinator_Light_Theme_Theme : R.style.Chordinator_Dark_Theme_Theme);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.scan_songs_layout);

		Intent myIntent = getIntent();
		// See if we need to import SETLISTS
		mDoSets = myIntent.getBooleanExtra(INTENT_DOSETS, false);
		// Get a list of the disks
		mSdcards = new SdCardFactory(this);

		// Initialize views.
		mFolderText = (TextView) findViewById(R.id.folder);
		mFileText = (TextView) findViewById(R.id.file);
		mFileCountText = (TextView) findViewById(R.id.files_scanned_text);
		mSongsFoundText = (TextView) findViewById(R.id.songs_found_text);
		mButton = (Button) findViewById(R.id.task_button);
		mButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mTaskFragment.isRunning()) {
					mTaskFragment.cancel();
				}
			}
		});

		// Restore saved state.
		if (savedInstanceState != null) {
			mFolderText.setText(savedInstanceState.getString(KEY_FOLDER));
			mFileText.setText(savedInstanceState.getString(KEY_FILE));
			mFileCountText.setText(savedInstanceState.getString(KEY_FILES));
			mSongsFoundText.setText(savedInstanceState.getString(KEY_SONGS));
		}

		FragmentManager fm = getSupportFragmentManager();
		mTaskFragment = (ScanSongsFragment) fm.findFragmentByTag(TAG_TASK_FRAGMENT);

		// If the Fragment is non-null, then it is being retained
		// over a configuration change.
		if (mTaskFragment == null) {
			mTaskFragment = new ScanSongsFragment();
			fm.beginTransaction().add(mTaskFragment, TAG_TASK_FRAGMENT).commit();
		}

		if (mTaskFragment.isRunning()) {
			mButton.setText(getString(R.string.cancel));
		} else {
			mButton.setText(getString(R.string.start));
		}
	}
	@Override
	public void onSaveInstanceState(Bundle outState) {
		Log.d(TAG, "onSaveInstanceState(Bundle)");
		super.onSaveInstanceState(outState);
		outState.putString(KEY_FILE, mFileText.getText().toString());
		outState.putString(KEY_FOLDER, mFolderText.getText().toString());
		outState.putString(KEY_FILES, mFileCountText.getText().toString());
		outState.putString(KEY_SONGS, mSongsFoundText.getText().toString());
	}

	/*********************************/
	/***** TASK CALLBACK METHODS *****/
	/*********************************/

	@Override
	public void onPreExecute() {
		Log.d(TAG, "onPreExecute()");
		mButton.setText(getString(R.string.cancel));
	}

	@Override
	public void onProgressUpdate(String folder, String fileFound, String fileCount, String found) {
		Log.d(TAG, "onProgressUpdate");
		if(!folder.isEmpty()){
			mFolderText.setText(mSdcards.toDisplay(folder));
		}
		if(!fileFound.isEmpty()){
			mFileText.setText(fileFound);
		}
		mFileCountText.setText(String.format(getString(R.string.files_scanned), fileCount));
		mSongsFoundText.setText(String.format(getString(R.string.songs_found), found));
	}

	@Override
	public void onCancelled() {
		Log.d(TAG, "onCancelled()");
		Toast.makeText(this, R.string.task_cancelled_msg, Toast.LENGTH_SHORT).show();
		// Get outta here
		finish();
	}

	@Override
	public void onPostExecute(int found) {
		Log.d(TAG, "onPostExecute()");
		
		if(found>0){
			Toast.makeText(this, String.format(getString(R.string.songs_found), found), Toast.LENGTH_SHORT).show();
		}
		else{
			Toast.makeText(this, R.string.no_songs_found, Toast.LENGTH_SHORT).show();
		}
		// Get outta here
		finish();
	}

	/************************/
	/***** LOGS & STUFF *****/
	/************************/

	@Override
	protected void onStart() {
		Log.d(TAG, "onStart()");
		super.onStart();
	}

	@Override
	protected void onResume() {
		Log.d(TAG, "onResume()");
		super.onResume();
		// For the first time - just get one with it without waiting for user to press the button.
		// Make sure we don't restart after the user has pressed stop and then changed orientation
		if(!mTaskFragment.isRunning()) {
			mTaskFragment.start(true, mDoSets, mSdcards.getSdCards());
		}
	}

	@Override
	protected void onPause() {
		Log.d(TAG, "onPause()");
		super.onPause();
	}

	@Override
	protected void onStop() {
		Log.d(TAG, "onStop()");
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy()");
		super.onDestroy();
	}
}
