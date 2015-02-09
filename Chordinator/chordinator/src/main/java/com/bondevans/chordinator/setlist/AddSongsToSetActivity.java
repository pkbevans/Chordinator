package com.bondevans.chordinator.setlist;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import android.view.View;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.bondevans.chordinator.ColourScheme;
import com.bondevans.chordinator.Log;
import com.bondevans.chordinator.R;
import com.bondevans.chordinator.utils.Ute;

public class AddSongsToSetActivity extends SherlockFragmentActivity {
	private static final String TAG = "AddSongsToSetActivity";
	public static final String KEY_SETID="dfgjkdu";
	public static final String KEY_SETNAME="gkskeh";
	private long mSetId;
	private String mSetName;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTheme(Ute.getColourScheme(this) == ColourScheme.LIGHT? R.style.Theme_Sherlock_Light: R.style.Theme_Sherlock);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_songs_to_set_fragment);// This is the xml with all the different frags
		// Get the setID that we are going to ad songs to from the Intent
		mSetId = getIntent().getLongExtra(KEY_SETID, -1);
		mSetName = getIntent().getStringExtra(KEY_SETNAME);
		//
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.add_songs_to)+" "+mSetName);
		// enable home as up if not actually at home
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
	}

	public void checkBoxClicked(View v){
		Log.d(TAG, "HELLO checkBoxClicked");
	}

	public void doneAddingSongs( View v){
		Log.d(TAG, "HELLO doneAddingSongs setId=["+mSetId+"]");
		FragmentManager fm = getSupportFragmentManager();
		AddSongsToSetFragment frag = (AddSongsToSetFragment) fm.findFragmentById(R.id.add_songs_to_set_fragment);
		// Actually add the new songs to the set
		frag.addSongsToSet(mSetId);
		this.setResult(RESULT_OK);
		finish();
	}
	
	public void cancelAddingSongs(View v){
		Log.d(TAG, "HELLO cancelAddingSongs");
		finish();
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d(TAG, "onOptionsItemSelected");
		switch (item.getItemId()) {
		case android.R.id.home:
			// Back to Songs
			finish();
			break;
		}
		return true;
	}
}