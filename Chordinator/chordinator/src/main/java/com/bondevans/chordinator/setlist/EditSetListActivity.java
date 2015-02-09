package com.bondevans.chordinator.setlist;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import android.view.View;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.bondevans.chordinator.ColourScheme;
import com.bondevans.chordinator.Log;
import com.bondevans.chordinator.R;
import com.bondevans.chordinator.SongUtils;
import com.bondevans.chordinator.dialogs.HelpFragment;
import com.bondevans.chordinator.utils.Ute;

public class EditSetListActivity extends SherlockFragmentActivity {
	private static final int	SHOWHELP_ID = Menu.FIRST + 1;
	private static final int 	ADDSONGS_ID = Menu.FIRST + 7;
	private final static String TAG = "EditSetListActivity";
	public final static String 	SET_ID = "set_id";
	public final static String 	SET_NAME = "set_name";
	private static final int 	REQUEST_ADDSONGS = 0;
	private boolean 			loaded = false;
	private long 				mSetId;
	private String				mSetName;
	private EditSetListFragment mEditSetListFragment;
	private int mColourScheme;

	@Override
	public void onCreate(Bundle savedInstance) {
		Log.d(TAG, "HELLO onCreate - loaded["+(loaded?"TRUE":"FALSE")+"]");
		mColourScheme = Ute.getColourScheme(this);
		setTheme(mColourScheme == ColourScheme.LIGHT? R.style.Theme_Sherlock_Light: R.style.Theme_Sherlock);
		super.onCreate(savedInstance);

		mSetId = getIntent().getLongExtra(SET_ID, 0);
		mSetName = getIntent().getStringExtra(SET_NAME);

		setContentView(R.layout.edit_set_fragment);

		mEditSetListFragment = (EditSetListFragment) getSupportFragmentManager()
				.findFragmentById(R.id.editsetlist_fragment);
		mEditSetListFragment.setSet(mSetId, mSetName);

		getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.edit_setlist)+": "+mSetName);
		// enable home as up if not actually at home
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
	}

	/* (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockListActivity#onCreateOptionsMenu(com.actionbarsherlock.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// If we are showing a SET, we need an Add Songs item and a delete SET item
		menu.add(0,ADDSONGS_ID, 0, getString(R.string.add_songs))
		.setIcon(mColourScheme == ColourScheme.DARK ? R.drawable.ic_add_songs_dark : R.drawable.ic_add_songs_light)
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS|MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		menu.add(0, SHOWHELP_ID, 0, getString(R.string.help))
		.setIcon(R.drawable.ic_menu_help);

		return super.onCreateOptionsMenu(menu);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onConfigurationChanged(android.content.res.Configuration)
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.d(TAG, "onConfigurationChanged");
		loaded = true;
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d(TAG, "onOptionsItemSelected");
		switch (item.getItemId()) {
		case android.R.id.home:
			// Back home
			finish();
			break;
		case ADDSONGS_ID:
			addSongs(mSetId, mSetName);
			break;
		case SHOWHELP_ID:
			showHelp();
			break;
		}
		return true;
	}

	public void addSongs(long setId, String setName) {
		Intent myIntent = new Intent(this, AddSongsToSetActivity.class);
		try {
			// Put the SET iD in the intent
			myIntent.putExtra(AddSongsToSetActivity.KEY_SETID, setId);
			myIntent.putExtra(AddSongsToSetActivity.KEY_SETNAME, setName);
			startActivityForResult(myIntent, REQUEST_ADDSONGS);
		}
		catch (ActivityNotFoundException e) {
			SongUtils.toast( this,e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "HELLO onActivityResult["+requestCode+"]["+resultCode+"]");
		if(resultCode == RESULT_OK){
			if(requestCode == REQUEST_ADDSONGS){
				// Reload songs
				mEditSetListFragment.loadSongs();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	public void doneEditing( View v){
		Log.d(TAG, "HELLO doneEditing setId=["+mSetId+"]");
		// re-write set out to DB
		mEditSetListFragment.setList.writeSetList(this.getContentResolver());
		this.setResult(RESULT_OK);
		finish();
	}
	private void showHelp() {
		Log.d(TAG, "showHelp");
		HelpFragment newFragment = HelpFragment.newInstance(HelpFragment.HELP_PAGE_EDITSET);
		newFragment.show(getSupportFragmentManager(), "dialog");
	}
}
