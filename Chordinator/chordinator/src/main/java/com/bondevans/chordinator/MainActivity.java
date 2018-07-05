package com.bondevans.chordinator;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bondevans.chordinator.asynctask.ScanSongsActivity;
import com.bondevans.chordinator.asynctask.SortOutFilePathsTask;
import com.bondevans.chordinator.db.DBUtils;
import com.bondevans.chordinator.dialogs.AddSetDialog;
import com.bondevans.chordinator.dialogs.FirstRunFragment;
import com.bondevans.chordinator.dialogs.HelpFragment;
import com.bondevans.chordinator.dialogs.LatestFragment;
import com.bondevans.chordinator.dialogs.SetListDialog;
import com.bondevans.chordinator.prefs.ChordinatorPrefsActivity;
import com.bondevans.chordinator.prefs.SongPrefs;
import com.bondevans.chordinator.search.SearchCriteria;
import com.bondevans.chordinator.setlist.SetSongListActivity;
import com.bondevans.chordinator.songlist.SongListFragment;
import com.bondevans.chordinator.utils.Ute;

import java.io.File;

import static android.support.v4.content.PermissionChecker.PERMISSION_DENIED;

public class MainActivity extends AppCompatActivity
implements SongListFragment.OnSongSelectedListener,
SongViewerFragment.SongViewerListener,
SetListDialog.OnSetSelectedListener,
AddSetDialog.CreateSetListener
{
	private static final String TAG = "MainActivity";
	private static final int SHOWHELP_ID = Menu.FIRST + 1;
	private static final int SCANSONGS_ID = Menu.FIRST + 2;
	private static final int DELALL_ID = Menu.FIRST + 3;
	private static final int PREFERENCES_ID = Menu.FIRST + 4;
	private static final int SELECTSET_ID = Menu.FIRST + 5;
	private static final int SORTSONGS_ID = Menu.FIRST + 9;
	private static final int SHARESONG_ID = Menu.FIRST + 10;
	private static final int RECENT_ID = Menu.FIRST + 11;
	private static final int FAVOURITES_ID = Menu.FIRST + 12;
	private static final int TITLE_ID = Menu.FIRST + 13;
	private static final int ARTIST_ID = Menu.FIRST + 14;
	private static final int BROWSE_ID = Menu.FIRST + 15;
	private static final int SEARCH_INTERNET_ID = Menu.FIRST + 18;
	private static final int ABOUT_ID = Menu.FIRST + 19;
	private static final int SEARCH_LOCAL_ID = Menu.FIRST + 25;
    private static final int REQUEST_CODE_READ_STORAGE_PERMISSION = 4523;
	private static final int OK = 0;
	private static final int FAILED = -1;

    private static int mColourScheme;
	private static int LIGHT=ColourScheme.LIGHT;
	private static final String TAG_SONGLIST = "TAG_SONGLIST";
	public static final String TAG_SONGVIEWER = "TAG_SONGVIEWER";
	SongListFragment 	songListFragment;
	SongViewerFragment	songViewerFragment;
	private boolean mSongInView=false;
	private int mSortOrder = SongListFragment.LIST_MODE_TITLE;
    private SearchView searchDBView ;
	private Menu mMenu = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
        LinearLayout songFrame=null;
		Log.d(TAG, "HELLO onCreate");
		mColourScheme = Ute.getColourScheme(this);
		setTheme(mColourScheme == ColourScheme.LIGHT? R.style.Chordinator_Light_Theme_Theme: R.style.Chordinator_Dark_Theme_Theme);

		logOsDetails();
		super.onCreate(savedInstanceState);
        checkFileAccessPermission();
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
			setContentView(R.layout.songlist_fragment_nosplit);// This is the xml without all the different frags
		}

		// Get the sort order, so that we can pass it in to the song list frag
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		mSortOrder=settings.getInt(SongPrefs.PREF_KEY_SORTORDER, SongListFragment.LIST_MODE_TITLE);

		FragmentManager fm = getSupportFragmentManager();
		if(null==(songListFragment = (SongListFragment) fm.findFragmentByTag(TAG_SONGLIST))){
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			songListFragment = SongListFragment.newInstance(mSortOrder,
					settings.getInt(SongPrefs.PREF_KEY_SORTDIRECTION, SongListFragment.LIST_MODE_TITLE));
			ft.add(R.id.list_container, songListFragment, TAG_SONGLIST);
			ft.commit();
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

		searchDBView = new SearchView(getSupportActionBar().getThemedContext());
        searchDBView.setQueryHint(getString(R.string.search_hint));
        searchDBView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String query) {
				Log.d(TAG, "HELLO - onQueryTextSubmit1 ["+query+"]");
				searchDBView.clearFocus();
				return false;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				Log.d(TAG, "HELLO - onQueryTextChange1 ["+newText+"]");
				songListFragment.setFilter(newText);
				return false;
			}
		});

		setupActionBar();// This will be called many times
//		onFirstRun();
	}

	@SuppressLint("NewApi")
	@TargetApi(8)
	private void logOsDetails() {
		String version="";
		try {
			version = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		Log.d(TAG, "App Version:["+version+"]");
		Log.d(TAG, "Device:["+Build.DEVICE+"]");
		Log.d(TAG, "OS Version:["+Build.VERSION.SDK_INT+"]");
		if(Build.VERSION.SDK_INT>=9){
			Log.d(TAG, "ExternalStorageDirectory:["+Environment.getExternalStorageDirectory().getPath()+"]");
		}
		if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.FROYO){
			Log.d(TAG, "Hardware:["+Build.HARDWARE+"]");
		}
		Log.d(TAG, "Manufacturer/Model:["+Build.MANUFACTURER+"/"+Build.MODEL+"]");
	}

	private void onFirstRun() {
		// See if we need to display the Help Screen - Only do this the first time.
		// After that the help will be available from the Menu
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = settings.edit();
		if( settings.getBoolean(SongPrefs.PREF_KEY_FIRSTRUN, true)){
			// If its the first time - create /chordinator directory and copy the sample songs into it
			if(FAILED == copySamples()){
				return;	// Get outta here - don't bother with the rest
			}

			// Show First Run Welcome Screen
			FirstRunFragment newFragment = FirstRunFragment.newInstance();
			newFragment.show(getSupportFragmentManager(), "dialog");
		}
		// 28/10/14
		// Is this the first run of this version of the app? If so do anything necessary here
		int thisVersion=0;
		try {
			thisVersion = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		int prevVersion = settings.getInt(SongPrefs.PREF_KEY_FIRSTRUN_VERSION, 0);
		Log.d(TAG, "HELLO This version[" + thisVersion + "] Previous Version[" + prevVersion + "]");
		if( prevVersion < thisVersion){
			if( thisVersion >= 23 && prevVersion < 23){	// 2.4.0. 
				Log.d(TAG, "HELLO Upating song file paths");
				// Introduced new fileName handling throughout the app.
				// Need to update all Song path entries - replace /sdcard with Environment.getExternalStorageDirectory()
				SortOutFilePathsTask filePathsTask = new SortOutFilePathsTask(this);// Do this in background
				filePathsTask.execute(new String[] {getString(R.string.authority)});

				// Need to make sure that the current folder for the browser is not a naughty one...
				editor.putString(SongPrefs.PREF_KEY_SONGDIR, Statics.CHORDINATOR_DIR);
			}
		}
		
		editor.putInt(SongPrefs.PREF_KEY_FIRSTRUN_VERSION, thisVersion);
		editor.putBoolean(SongPrefs.PREF_KEY_FIRSTRUN, false);
		editor.apply(); // Use apply rather than commit to do it in background
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
	private int copySamples(){
		String sample1 = "{title:Sample Song}{composer:Paul Evans}{artist:Paul Evans}{c:Verse}My [Bm7]love has"+
				" [A/C#]gone and [Bm7]kicked me where the sun once [A/C#]shone. [Bm7]Something grips my [A/C#]brain and all I [Bm7]see is endless [A/C#]pain and miser-ee-ee-[D7]ey... [C#]"+
				"{c:Chorus}Cant e - [F#m]rase the [C#m]things we [Bm7]said (oh oh oh oh) [D7]But I'm still [C#]here an I'm [F#m]alive  [C#m] [Bm7] [D7]Its only [C#]Pride that gets me [F#m]up and [C#m]out my [Bm7]bed (oh oh oh oh) [D7]And I'm still [C#]here and I'll sur-[F#m]viiiiiiiiiiii[C#m]-i-i-i-i-[Bm7]ive.....";
		File dir = new File(Statics.CHORDINATOR_DIR);
		if( !dir.exists()){
			if(	!dir.mkdir()){
				Toast.makeText(this,
						"Can't create dir: "+dir.getAbsolutePath(), Toast.LENGTH_LONG).show();
				return FAILED;
			}
			try {
				SongUtils.writeFile(Statics.CHORDINATOR_DIR+"Sample Song.txt", sample1);
			} catch (ChordinatorException e) {
				Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
				return FAILED;
			}
		}
		return OK;
	}

	/**
	 * Adds song to SET selected in Dialog
	 * @param setId Set ID
	 * @param songId Song ID
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
			Log.d(TAG, "HELLO Launching SetSongListActivity");
			Intent myIntent = new Intent(this, SetSongListActivity.class);
			try {
				// Put the SET iD and SET Name in the intent
				myIntent.putExtra(SetSongListActivity.INTENT_SETID, setId);
				myIntent.putExtra(SetSongListActivity.INTENT_SETNAME, setName);
				startActivity(myIntent);
			}
			catch (ActivityNotFoundException e) {
				SongUtils.toast( this,e.getMessage());
			}
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

			long setId=0;
			showSong.setData(Uri.fromFile(new File(songPath)));
			showSong.putExtra(SongViewerActivity.INTENT_SONGID, songId);
			showSong.putExtra(SongViewerActivity.INTENT_SETID, setId); //Make sure its a Long
			showSong.putExtra(SongViewerActivity.INTENT_INSET, false);

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
		mMenu  = menu;
		MenuItem menuItem;

		menuItem = menu.add(0, SORTSONGS_ID, 0,"Sort By")
		.setIcon(mColourScheme == LIGHT ? R.drawable.ic_sort_light : R.drawable.ic_sort_dark);
		MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);

		menuItem = menu.add(0,BROWSE_ID, 0, getString(R.string.tabname_browse))
		.setIcon(mColourScheme == LIGHT ? R.drawable.ai_browser_light : R.drawable.ai_browser_dark);
		MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);

		menuItem = menu.add(0,SELECTSET_ID, 0, getString(R.string.tabname_sets))
		.setIcon(mColourScheme == LIGHT ? R.drawable.ic_setlists_light : R.drawable.ic_setlists_dark);
		MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);

		menuItem = menu.add(0,SEARCH_LOCAL_ID, 0, getString(R.string.search_songs))
		.setIcon(mColourScheme == LIGHT ? R.drawable.ai_search_light:R.drawable.ai_search_dark);
		MenuItemCompat.setActionView(menuItem, searchDBView);
		MenuItemCompat.setOnActionExpandListener(menuItem, new MenuItemCompat.OnActionExpandListener() {
			@Override
			public boolean onMenuItemActionCollapse(MenuItem item) {
				// Do something when collapsed
				Log.d(TAG, "HELLO onMenuItemActionCollapse");
				songListFragment.setFilter("");
				return true;  // Return true to collapse action view
			}

			@Override
			public boolean onMenuItemActionExpand(MenuItem item) {
				// Do something when expanded
				return true;  // Return true to expand action view
			}
		});
		MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

		menuItem = menu.add(0,SEARCH_INTERNET_ID, 0, getString(R.string.search_songs))
		.setIcon(mColourScheme == LIGHT ? R.drawable.ic_download_light:R.drawable.ic_download_dark);
		MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);

		menu.add(0, SCANSONGS_ID, 0, getString(R.string.scan_for_songs))
		.setIcon(R.drawable.ic_menu_scan);

		menu.add(0, DELALL_ID, 0, getString(R.string.delete_all))
		.setIcon(R.drawable.ic_menu_delall);

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
		mSongInView = true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d(TAG, "onOptionsItemSelected");
		switch (item.getItemId()) {
		case android.R.id.home:
			// Back to Songs
			Log.d(TAG, "HELLO - HOME PRESSED!!! - WHAT DO WE DO?");
			break;
		case SHOWHELP_ID:
			showHelp();
			break;
		case SCANSONGS_ID:
			scanForSongs();
			break;
		case DELALL_ID:
			deleteSongs();
			break;
		case PREFERENCES_ID:
			setPreferences();
			break;
		case SELECTSET_ID:
			DialogFragment newFragment1 = SetListDialog.newInstance(0, "");
			newFragment1.show(getSupportFragmentManager(), "dialog");
			break;
		case SORTSONGS_ID:
			startSupportActionMode(new SongActionMode());
			break;
		case BROWSE_ID:
			browseFiles();
			break;
		case SHARESONG_ID:
			songViewerFragment.shareSong();
			break;
		case SEARCH_INTERNET_ID:
			searchInternetForSongs();
			break;
		case ABOUT_ID:
			showAbout();
			break;
		}
		return true;
	}

	public void browseFiles() {
		Log.d(TAG, "HELLO launch SongBrowserActivity");
		Intent myIntent = new Intent(this, SongBrowserActivity.class);
		try {
			// Put the SET iD in the intent
			startActivity(myIntent);
		}
		catch (ActivityNotFoundException e) {
			SongUtils.toast( this,e.getMessage());
		}
	}
	private void deleteSongs() {
		Log.d(TAG, "HELLO deleteSongs");
		AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
		alertDialog.setTitle("You are about to remove all songs from the listing");
		alertDialog.setMessage("Are you sure?\n(No files will be deleted)\nHave you backed up your SETs?");
		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.yes), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Delete all Sets
				getContentResolver().delete(DBUtils.SET(getString(R.string.authority)), null, null);
				// Delete all Songs
				int rows = getContentResolver().delete(DBUtils.SONG(getString(R.string.authority)), null, null);
				SongUtils.toast(MainActivity.this, rows+" Songs Deleted");
			}
		});
		alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Do nothing
			}
		});
		alertDialog.show();
	}
	private void showHelp() {
		Log.d(TAG, "showHelp");
		int page = HelpFragment.HELP_PAGE_MYSONGS;
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

    public void scanForSongs(){
		Log.d(TAG, "HELLO scanForSongs");
		Intent myIntent = new Intent(this, ScanSongsActivity.class);
        myIntent.putExtra(ScanSongsActivity.INTENT_DOSETS, false);// Don't do sets
		try {
			// Put the SET iD in the intent
			startActivity(myIntent);
		}
		catch (ActivityNotFoundException e) {
			SongUtils.toast( this,e.getMessage());
		}
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

	private void setupActionBar(){
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setLogo(mColourScheme == LIGHT ? R.drawable.chordinator_aug_logo_light_bkgrnd : R.drawable.chordinator_aug_logo_dark_bkgrnd);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		Log.d(TAG, "HELLO on SaveInstanceState");
		super.onSaveInstanceState(outState);
	}

	@Override
	/*
	 * Handle non-menu keyboard events
	 */
	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		Log.d(TAG, "HELLO - onKeyDown");
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			Log.d(TAG, "HELLO - BACK PRESSED");
			finish();
			return true;
		}
		return false;
	}

	private final class SongActionMode implements ActionMode.Callback{
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			Log.d(TAG, "HELLO - onCreateActionMode");
			MenuItem menuItem;
			menuItem = menu.add(0,TITLE_ID, 0, getString(R.string.title))
					.setIcon(mSortOrder == SongListFragment.LIST_MODE_TITLE?R.drawable.title_icon_sel:R.drawable.title_icon);
			MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);

			menuItem = menu.add(0,ARTIST_ID, 0, getString(R.string.artist))
					.setIcon(mSortOrder == SongListFragment.LIST_MODE_ARTIST?R.drawable.artist_icon_sel:R.drawable.artist_icon);
			MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);

			menuItem = menu.add(0,RECENT_ID, 0, getString(R.string.recent))
					.setIcon(mSortOrder == SongListFragment.LIST_MODE_RECENT?R.drawable.recent_icon_sel:R.drawable.recent_icon);
			MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);

			menuItem = menu.add(0,FAVOURITES_ID, 0, getString(R.string.favourites))
					.setIcon(mSortOrder == SongListFragment.LIST_MODE_FAVS?R.drawable.fav_icon_sel:R.drawable.fav_icon);
			MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			Log.d(TAG, "HELLO - onPrepareActionMode");
			return true;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			Log.d(TAG, "HELLO - onActionItemClicked");
			if(item.getItemId() == TITLE_ID){
				sortByTitle();
			}
			else if(item.getItemId() == ARTIST_ID){
				sortByArtist();
			}
			else if(item.getItemId() == RECENT_ID){
				sortByRecent();
			}
			else if(item.getItemId() == FAVOURITES_ID){
				sortByFavs();
			}
			mode.finish();
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			//			mActionMode = null;
		}
	}

	private void searchInternetForSongs(){
		Log.d(TAG, "SearchForSongs");
		Intent myIntent = new Intent(this, SearchCriteria.class);
		try {
			startActivity(myIntent);
		} catch (ActivityNotFoundException e) {
			SongUtils.toast(this, "SearchCriteria not found");
		}
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
			// TODO force re-draw somehow if color scheme has changed
			Log.d(TAG, "HELLO onActivityResult3");
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

	public void sortByFavs(){
		Log.d(TAG, "HELLO sortbyfavs");
		// Reorder the songlist by favourites
		mSortOrder = SongListFragment.LIST_MODE_FAVS;
		songListFragment.changeSortOrder(mSortOrder);
	}
	public void sortByRecent(){
		Log.d(TAG, "HELLO sortbyrecent");
		// Reorder the songlist by favourites
		mSortOrder = SongListFragment.LIST_MODE_RECENT;
		songListFragment.changeSortOrder(mSortOrder);
	}
	public void sortByTitle(){
		Log.d(TAG, "HELLO sortbytitle");
		// reorder the songlist by favourites
		mSortOrder = SongListFragment.LIST_MODE_TITLE;
		songListFragment.changeSortOrder(mSortOrder);
	}
	public void sortByArtist(){
		Log.d(TAG, "HELLO sortbyArtist");
		// reorder the songlist by favourites
		mSortOrder = SongListFragment.LIST_MODE_ARTIST;
		songListFragment.changeSortOrder(mSortOrder);
	}

	@Override
	public void onNewFileCreated() {
		// SHOULD NEVER BE CALLED
		Log.d(TAG, "HELLO onNewFileCreated - WHY HAS THIS BEEN CALLED");
	}

	@Override
	public void createBrowserSet(String setName, String songName) {
		// SHOULD NEVER BE CALLED
		Log.d(TAG, "HELLO createBrowserSet - WHY HAS THIS BEEN CALLED");
	}
    private void checkFileAccessPermission() {
        // Required for API version 23 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.util.Log.d(TAG, "checkFileAccessPermission 1");
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                android.util.Log.d(TAG, "checkFileAccessPermission 2");
                // Need to request permission from the user
                String[] perms = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
                requestPermissions(perms, REQUEST_CODE_READ_STORAGE_PERMISSION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if( requestCode == REQUEST_CODE_READ_STORAGE_PERMISSION && grantResults[0] == PERMISSION_DENIED){
			// Handle user not allowing access.
			Toast.makeText(this, getString(R.string.permission_required), Toast.LENGTH_SHORT).show();
		}
		else{
			// Only run this if they have given consent
			onFirstRun();
		}
        Log.d(TAG, "onRequestPermissionsResult");
    }

}
