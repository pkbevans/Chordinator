package com.bondevans.chordinator.setlist;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.bondevans.chordinator.ChordinatorException;
import com.bondevans.chordinator.ColourScheme;
import com.bondevans.chordinator.Log;
import com.bondevans.chordinator.R;
import com.bondevans.chordinator.SongUtils;
import com.bondevans.chordinator.SongViewerActivity;
import com.bondevans.chordinator.SongViewerFragment;
import com.bondevans.chordinator.db.DBUtils;
import com.bondevans.chordinator.db.SongDB;
import com.bondevans.chordinator.dialogs.AddSetDialog;
import com.bondevans.chordinator.dialogs.DeleteRenameSetDialog;
import com.bondevans.chordinator.dialogs.HelpFragment;
import com.bondevans.chordinator.dialogs.LatestFragment;
import com.bondevans.chordinator.dialogs.SetListDialog;
import com.bondevans.chordinator.prefs.ChordinatorPrefsActivity;
import com.bondevans.chordinator.prefs.SongPrefs;
import com.bondevans.chordinator.songlist.SongListFragment;
import com.bondevans.chordinator.utils.Ute;

import java.io.File;

public class SetSongListActivity extends AppCompatActivity
implements SongListFragment.OnSongSelectedListener, 
SongViewerFragment.SongViewerListener, 
SetListDialog.OnSetSelectedListener,
AddSetDialog.CreateSetListener,
DeleteRenameSetDialog.DeleteRenameSetListener,
AddSongsToSetFragment.OnSongsAddedListener
{
	private static final String TAG = "SetSongListActivity";
	private static final int SHOWHELP_ID = Menu.FIRST + 1;
	private static final int PREFERENCES_ID = Menu.FIRST + 4;
	private static final int SELECTSET_ID = Menu.FIRST + 5;
	private static final int ADDSONGS_ID = Menu.FIRST + 7;
	private static final int DELETESET_ID = Menu.FIRST + 8;
	private static final int SHARESONG_ID = Menu.FIRST + 10;
	private static final int ABOUT_ID = Menu.FIRST + 19;
	private static final int EXPORTSET_ID = Menu.FIRST + 20;
	private static final int RENAMESET_ID = Menu.FIRST + 23;
	private static final int SEARCH_LOCAL_ID = Menu.FIRST + 25;
	public static final String INTENT_SETID = "com.bondevans.chordinator.setId";
	public static final String INTENT_SETNAME = "com.bondevans.chordinator.setName";

	private static int mColourScheme;
	private static int LIGHT= ColourScheme.LIGHT;
	private static final String TAG_SETSONGLIST = "TAG_SETSONGLIST";
	private static final String TAG_ADDSETSONGLIST = "TAG_ADDSETSONGLIST";
	public static final String TAG_SONGVIEWER = "TAG_SONGVIEWER";
	public static final String KEY_SETID = "jsfrsth";
	public static final String KEY_SETNAME = "ksfyggyh";
	EditSetListFragment mEditSetListFragment;
	SongViewerFragment	songViewerFragment;
	private long mSetId;
	private String mSetName;
	private boolean mSongInView=false;
	private SearchView searchSetView ;
	private Menu mMenu=null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		LinearLayout songFrame=null;
		Log.d(TAG, "HELLO onCreate");
		mColourScheme = Ute.getColourScheme(this);
		setTheme(mColourScheme == LIGHT? R.style.Chordinator_Light_Theme_Theme: R.style.Chordinator_Dark_Theme_Theme);

		super.onCreate(savedInstanceState);

		Intent myIntent = getIntent();
		// Get the Set ID and Set Name from the Intent - unless savidInstanceState is not null
		mSetId = savedInstanceState == null?myIntent.getLongExtra(INTENT_SETID, 0): savedInstanceState.getLong(KEY_SETID);
		mSetName = savedInstanceState == null?myIntent.getStringExtra(INTENT_SETNAME): savedInstanceState.getString(KEY_SETNAME);

		// See if they want split screen mode in Landscape
		int listPaneSize;
		if((listPaneSize = useSplitScreenMode())>0){
			Log.d(TAG, "HELLO SPLIT SCREEN");
			setContentView(R.layout.songlist_fragment);// This is the xml with all the different frags
			// Configurable Dual pane Layout
			songFrame = (LinearLayout)this.findViewById(R.id.songview_container);
			LinearLayout list = (LinearLayout)this.findViewById(R.id.list_container);
			if(songFrame != null){
				// If song container is null we are not in landscape
				LinearLayout.LayoutParams listParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, listPaneSize);
				LinearLayout.LayoutParams songParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 100-listPaneSize);
				list.setLayoutParams(listParams);
				songFrame.setLayoutParams(songParams);
			}
			// Configurable Dual pane Layout
		}
		else{
			Log.d(TAG, "HELLO SPLIT SCREEN - NOT");
			setContentView(R.layout.songlist_fragment_nosplit);// This is the xml with a single fragment
		}
		FragmentManager fm = getSupportFragmentManager();
		if(null==(mEditSetListFragment = (EditSetListFragment) fm.findFragmentByTag(TAG_SETSONGLIST))){
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			mEditSetListFragment = EditSetListFragment.newInstance(mSetId, mSetName);
			ft.add(R.id.list_container, mEditSetListFragment, TAG_SETSONGLIST);
			ft.commit();
		}
		else{
			mEditSetListFragment.setSetId(mSetId, mSetName);
		}
		// Check to see if we have a frame in which to embed the songView
		// fragment directly in the containing UI.
		boolean mDualPane = songFrame != null && songFrame.getVisibility() == View.VISIBLE;
		if(mDualPane){
			if(null==(songViewerFragment = (SongViewerFragment) fm.findFragmentByTag(TAG_SONGVIEWER))){
				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				songViewerFragment = new SongViewerFragment();
				ft.add(R.id.songview_container, songViewerFragment, TAG_SONGVIEWER);
				ft.commit();
			}
		}

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar); // Attaching the layout to the toolbar object
        setSupportActionBar(toolbar);                   // Setting toolbar as the ActionBar with setSupportActionBar() call

        searchSetView = new SearchView(getSupportActionBar().getThemedContext());
		searchSetView.setQueryHint("Search songs");
		searchSetView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String query) {
				Log.d(TAG, "HELLO - onQueryTextSubmit1 ["+query+"]");
				searchSetView.clearFocus();
				return false;
			}

			@Override
			public boolean onQueryTextChange(String filter) {
				Log.d(TAG, "HELLO - onQueryTextChange1 ["+filter+"]");
				AddSongsToSetFragment frag;
				if((frag=(AddSongsToSetFragment)getSupportFragmentManager().findFragmentByTag(TAG_ADDSETSONGLIST)) != null) {
					frag.setFilter(filter);
				}
				else {
					mEditSetListFragment.setFilter(filter);
				}
				return false;
			}                                                  
		});

		setupActionBar();// This will be called many times
	}

	private int useSplitScreenMode(){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		if( settings.getBoolean(SongPrefs.PREF_KEY_SPLIT_SCREEN, false)){
			return Integer.parseInt(settings.getString(SongPrefs.PREF_KEY_SPLIT_PROPORTION, "45"));
		}
		else{
			return 0;
		}
	}

	/**
	 * Adds song to SET selected in Dialog
	 * @param setId SET Id
	 * @param songId SET name
	 */
	public void setNameClicked(long setId, long songId, String setName, String songName){
		if(songId>0){
			// Long Press on a song in the songlist to add song to a set
			Log.d(TAG, "HELLO Add song ["+songName+"] to: "+setName+"");
			try {
				DBUtils.addSongToSet(getContentResolver(), getString(R.string.authority), setId, songId);
				SongUtils.toast(this, songName+" added to: "+setName);
			} catch (ChordinatorException e) {
				// Song already exists
				SongUtils.toast(this, songName+ " "+ getString(R.string.already_in_set));
			}
		}
		else{
			// Select Set actionItem clicked to select Set to view
			mSetId=setId;
			mSetName=setName;
			// Instantiate a new instance of SetSongListFragment or replace existing one if already in place
			FragmentManager fm = getSupportFragmentManager();
			if(null==(mEditSetListFragment = (EditSetListFragment) fm.findFragmentByTag(TAG_SETSONGLIST))){
				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				mEditSetListFragment = EditSetListFragment.newInstance(setId, setName);
				// Add the fragment to the activity, pushing this transaction on to the back stack.
				ft.replace(R.id.list_container, mEditSetListFragment, TAG_SETSONGLIST);
				ft.setTransition(FragmentTransaction.TRANSIT_NONE);
				ft.addToBackStack(null);
				ft.commit();
			}
			else{
				mEditSetListFragment.setSetId(setId, setName);
				mEditSetListFragment.updateList();
			}
			setupActionBar();
		}
	}

	@Override
	public void onSongSelected(long songId, String songPath) {
		Log.d(TAG, "HELLO onSongSelected songPath=["+songPath+"]");
		songViewerFragment = (SongViewerFragment) getSupportFragmentManager()
				.findFragmentByTag(TAG_SONGVIEWER);
		if (songViewerFragment == null || !songViewerFragment.isVisible()) {
			Log.d(TAG, "HELLO onSongSelected - launching SongViewerActivity");
			// Open the file with the SongViewerActivity
			Intent showSong = new Intent(this, SongViewerActivity.class);

			showSong.setData(Uri.fromFile(new File(songPath)));
			showSong.putExtra(SongViewerActivity.INTENT_SONGID, songId);
			showSong.putExtra(SongViewerActivity.INTENT_SETID, mSetId);
			showSong.putExtra(SongViewerActivity.INTENT_INSET, true);

			startActivity(showSong);
		}
		else {
			Log.d(TAG, "HELLO onSongSelected - found the fragment");
			songViewerFragment.setSong(false, songId, songPath, null);
			if(!mSongInView){
				addShareButton();
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockFragmentActivity#onCreateOptionsMenu(com.actionbarsherlock.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		mMenu = menu;
		MenuItem menuItem;

		menuItem = menu.add(0,SELECTSET_ID, 0, getString(R.string.tabname_sets))
		.setIcon(mColourScheme == LIGHT ? R.drawable.ic_setlists_light : R.drawable.ic_setlists_dark);
		MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);//|MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		menuItem = menu.add(0,SEARCH_LOCAL_ID, 0, getString(R.string.search_songs))
		.setIcon(mColourScheme == LIGHT ? R.drawable.ai_search_light : R.drawable.ai_search_dark);
		MenuItemCompat.setActionView(menuItem, searchSetView);
		MenuItemCompat.setOnActionExpandListener(menuItem, new MenuItemCompat.OnActionExpandListener() {
			@Override
			public boolean onMenuItemActionCollapse(MenuItem item) {
				// Do something when collapsed
				Log.d(TAG, "HELLO onMenuItemActionCollapse");
				AddSongsToSetFragment frag;
				if ((frag = (AddSongsToSetFragment) getSupportFragmentManager().findFragmentByTag(TAG_ADDSETSONGLIST)) != null) {
					frag.setFilter("");
				} else {
					mEditSetListFragment.setFilter("");
				}
				return true;  // Return true to collapse action view
			}

			@Override
			public boolean onMenuItemActionExpand(MenuItem item) {
				// Do something when expanded
				return true;  // Return true to expand action view
			}
		});
		MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS | MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

		menuItem = menu.add(0,ADDSONGS_ID, 0, getString(R.string.add_songs))
		.setIcon(mColourScheme == LIGHT ? R.drawable.ic_add_songs_light : R.drawable.ic_add_songs_dark);
		MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);//|MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		menuItem = menu.add(0,DELETESET_ID, 0, getString(R.string.menu_delete_set))
		.setIcon(mColourScheme == LIGHT ? R.drawable.ic_delete_set_light : R.drawable.ic_delete_set_dark);
		MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);//|MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		menu.add(0,EXPORTSET_ID, 0, getString(R.string.export_set));

		menu.add(0,RENAMESET_ID, 0, getString(R.string.rename_set));

		menu.add(0, PREFERENCES_ID, 0, getString(R.string.set_options))
		.setIcon(R.drawable.ic_menu_preferences);

		menu.add(0, SHOWHELP_ID, 0, getString(R.string.help))
		.setIcon(R.drawable.ic_menu_help);

		menu.add(0, ABOUT_ID, 0, getString(R.string.about));

		return super.onCreateOptionsMenu(menu);
	}

	void addShareButton(){
		MenuItem menuItem = mMenu.add(0, SHARESONG_ID, 0, getString(R.string.share_song))
		.setIcon(R.drawable.ic_menu_share);
		MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
		mSongInView=true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d(TAG, "onOptionsItemSelected");
		switch (item.getItemId()) {
		case android.R.id.home:
			// Back to Songs - unless we are showing AddSongsToSetFragment
			if(!removeAddSongsIfShowing()){
				finish();
			}
			break;
		case SHOWHELP_ID:
			showHelp();
			break;
		case PREFERENCES_ID:
			setPreferences();
			break;
		case SELECTSET_ID:
			DialogFragment newFragment1 = SetListDialog.newInstance(0, "");
			newFragment1.show(getSupportFragmentManager(), "dialog");
			break;
		case ADDSONGS_ID:
			addSongs(mSetId, mSetName);
			break;
		case DELETESET_ID:
			deleteSet(mSetId, mSetName);
			break;
		case SHARESONG_ID:
			songViewerFragment.shareSong();
			break;
		case ABOUT_ID:
			showAbout();
			break;
		case EXPORTSET_ID:
			exportSet(mSetId, mSetName);
			break;
		case RENAMESET_ID:
			DialogFragment newFragment2 = DeleteRenameSetDialog.newInstance(DeleteRenameSetDialog.DIALOG_RENAME, mSetId, mSetName);
			newFragment2.show(getSupportFragmentManager(), "dialog");
			break;
		}
		return true;
	}

	/**
	 * Check if the AddSongsToSet Fragment is showing - close if it is
	 * @return true if the fragment was found and removed, otherwise false
	 */
	private boolean removeAddSongsIfShowing() {
		if(getSupportFragmentManager().findFragmentByTag(TAG_ADDSETSONGLIST) != null){
			// Remove it
			getSupportFragmentManager().popBackStack();
			return true;
		}
		return false;
	}

	private void exportSet(long setId, String setName) {
		SetList mySet = new SetList(getContentResolver(), getString(R.string.authority), setId, setName);
		mySet.writeSetList();
		SongUtils.toast(this, getString(R.string.exported_to)+" "+ mySet.getSetListPath());
	}

	private void deleteSet(long setId, String setName){
		DialogFragment newFragment = DeleteRenameSetDialog.newInstance(DeleteRenameSetDialog.DIALOG_DELETE, setId, setName);
		newFragment.show(getSupportFragmentManager(), "dialog");
	}
	private void showHelp() {
		Log.d(TAG, "showHelp");
		int page = HelpFragment.HELP_PAGE_SETLIST;

		HelpFragment newFragment = HelpFragment.newInstance(page);
		newFragment.show(getSupportFragmentManager(), "dialog");
	}
	private void showAbout() {
		Log.d(TAG, "showAbout");
		String vers="";
		try {
			vers = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		LatestFragment newFragment = LatestFragment.newInstance(vers);
		newFragment.show(getSupportFragmentManager(), "dialog");
	}

	private void setPreferences(){
		Intent myIntent = new Intent(this, ChordinatorPrefsActivity.class);
		try {
			//  Need to know when set prefs is finished so start for result
			startActivityForResult(myIntent, SongUtils.SETOPTIONS_REQUEST);
		} catch (ActivityNotFoundException e) {
			SongUtils.toast(this, "ChordinatorPrefsActivity not found");
		}
	}
	public void addNewSet(long songId, String songName){
		// Pop up a dialog asking for file name
		Log.d(TAG, "Adding new Set");
		DialogFragment newFragment = AddSetDialog.newInstance(true, songId, songName);
		newFragment.show(getSupportFragmentManager(), "dialog");
	}

	public void createSet(String setName, long songId, String songName){
		Log.d(TAG, "HELLO setName=["+setName+"]");
		long setId = DBUtils.createSet(getContentResolver(), getString(R.string.authority), setName);
		if(setId != 0){
			SongUtils.toast(this, "Created: "+setName);
			// Add the song to the new set - if there is one (songId>0)
			if(songId>0){
				setNameClicked(setId, songId, setName, songName);
			}
		}
	}

	/**
	 * Start new fragment to add songs to the set
	 * @param setId SET Id
	 * @param setName SET name
	 */
	public void addSongs(long setId, String setName){
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		AddSongsToSetFragment addSongsToSetFragment = AddSongsToSetFragment.newInstance(setId, setName);
		// Add the fragment to the activity, pushing this transaction on to the back stack.
		ft.replace(R.id.list_container, addSongsToSetFragment, TAG_ADDSETSONGLIST);
		ft.setTransition(FragmentTransaction.TRANSIT_NONE);
		ft.addToBackStack(null);
		ft.commit();
	}

	public void deleteSetFromDB(long set_id, String setName) {
		// Delete all SET ITEMS for given set first
		getContentResolver().delete(
				Uri.withAppendedPath(DBUtils.SETITEM(getString(R.string.authority)), String.valueOf(set_id)),
				null, null);
		// Now delete the SET record
		int rows = getContentResolver().delete(
				Uri.withAppendedPath(DBUtils.SET(getString(R.string.authority)), String.valueOf(set_id)),
				null, null);
		if(rows != 1){
			SongUtils.toast(this, getString(R.string.failed_to_delete)+setName);
		}
		else{
			SongUtils.toast(this, getString(R.string.set_deleted)+": "+setName);
		}
		// If we've just deleted the set that we were looking at we need go back home.
		finish();
	}


	private void setupActionBar(){
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setTitle(getString(R.string.set) + ":" + mSetName);
	}
	public void renameSet(long setId, String oldName, String newName) {
		// rename set
		ContentValues values = new ContentValues();
		values.put(SongDB.COLUMN_SET_NAME, newName);
		int rows = getContentResolver().update(
				Uri.withAppendedPath(DBUtils.SET(getString(R.string.authority)), String.valueOf(setId)),
				values, null, null);
		if(rows!=1){
			SongUtils.toast(this, getString(R.string.rename_failed)+" "+oldName);
		}
		else{
			// Use the new name
			mSetName = newName;
			getSupportActionBar().setTitle(getString(R.string.set)+":"+mSetName);
		}

	}
	@Override
	public void onSaveInstanceState(Bundle outState) {
		Log.d(TAG, "HELLO on SaveInstanceState");

		outState.putLong(KEY_SETID, mSetId);
		outState.putString(KEY_SETNAME, mSetName);
		super.onSaveInstanceState(outState);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "HELLO onActivityResult-activity request=["+requestCode+"]result=["+resultCode+"]");
		// If in dual pane mode, need to update the SongViewer
		if( requestCode == SongUtils.SETOPTIONS_REQUEST){
			Log.d(TAG, "HELLO onActivityResult2 ["+(mSongInView?"SONG":"NO SONG"));
			if( songViewerFragment != null && mSongInView){
				Log.d(TAG, "HELLO onActivityResult2");
				songViewerFragment.reloadPreferences();
			}
			// TODO force re-draw somehow if colour scheme has changed
			Log.d(TAG, "HELLO onActivityResult3");
		}
		else /*if(requestCode == SongUtils.SONGACTIVITY_REQUEST)*/{
			Log.d(TAG, "HELLO onActivityResult");
		}
	}

	@Override
	public void nextSong() {
		// NOT applicable in Landscape
	}
	@Override
	public void prevSong() {
		// NOT applicable in Landscape
	}

	@Override
	public void onNewFileCreated() {
	}

	@Override
	public void browseFiles() {
	}

	@Override
	public void createBrowserSet(String setName, String songName) {
		// Not used.
	}

	@Override
	public void songsAdded() {
		Log.d(TAG, "songsAdded");
		if(mEditSetListFragment == null){
			Log.d(TAG, "songsAdded2");
			mEditSetListFragment = (EditSetListFragment) getSupportFragmentManager().findFragmentByTag(TAG_SETSONGLIST);
		}
		if (mEditSetListFragment!= null ){
			Log.d(TAG, "songsAdded3");
			mEditSetListFragment.setLoaded(false);
		}
	}
}