package com.bondevans.chordinator;

import java.io.File;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bondevans.chordinator.conversion.SongConverter;
import com.bondevans.chordinator.db.DBUtils;
import com.bondevans.chordinator.dialogs.AddSetDialog;
import com.bondevans.chordinator.dialogs.BrowserSetListDialog;
import com.bondevans.chordinator.dialogs.HelpFragment;
import com.bondevans.chordinator.dialogs.LatestFragment;
import com.bondevans.chordinator.prefs.ChordinatorPrefsActivity;
import com.bondevans.chordinator.prefs.SongPrefs;
import com.bondevans.chordinator.search.SearchCriteria;
import com.bondevans.chordinator.setlist.SetList;
import com.bondevans.chordinator.utils.Ute;

public class SongBrowserActivity extends AppCompatActivity
implements SongViewerFragment.SongViewerListener,
SongBrowserFragment.OnSongFileSelectedListener,
BrowserSetListDialog.OnSetSelectedListener,
AddSetDialog.CreateSetListener
{
	private static final String TAG = "SongBrowserActivity";
	private static final int SHOWHELP_ID = Menu.FIRST + 1;
	private static final int PREFERENCES_ID = Menu.FIRST + 4;
	private static final int SHARESONG_ID = Menu.FIRST + 10;
	private static final int REFRESH_ID = Menu.FIRST + 16;
	private static final int UP_ID = Menu.FIRST + 17;
	private static final int SEARCH_INTERNET_ID = Menu.FIRST + 18;
	private static final int ABOUT_ID = Menu.FIRST + 19;
	private static final int DOWNLOAD_ID = Menu.FIRST + 24;
	private static final int SEARCH_LOCAL_ID = Menu.FIRST + 25;

	private static int mColourScheme;
	private static int LIGHT=ColourScheme.LIGHT;
	public static final String TAG_SONGBROWSER = "TAG_SONGBROWSER";
	public static final String TAG_SONGVIEWER = "TAG_SONGVIEWER";
	SongViewerFragment	songViewerFragment;
	private boolean mSongInView=false;
	private SongBrowserFragment songBrowserFragment=null;
	private boolean mUpEnabled=false;
	private SearchView searchFileView ;
	protected boolean mInSearch=false;
	private Menu mMenu=null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		LinearLayout songFrame=null;
		Log.d(TAG, "HELLO onCreate");
		mColourScheme = Ute.getColourScheme(this);
		setTheme(mColourScheme == ColourScheme.LIGHT? R.style.Chordinator_Light_Theme_Theme: R.style.Chordinator_Dark_Theme_Theme);

		super.onCreate(savedInstanceState);
		// See if they want split screen mode in Landscape
		int listPaneSize=0;
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
			setContentView(R.layout.songlist_fragment_nosplit);// This is the xml with all the different frags
		}
		FragmentManager fm = getSupportFragmentManager();
		if(null==(songBrowserFragment = (SongBrowserFragment) fm.findFragmentByTag(TAG_SONGBROWSER))){
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			songBrowserFragment = new SongBrowserFragment();
			ft.add(R.id.list_container, songBrowserFragment, TAG_SONGBROWSER);
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

		searchFileView = new SearchView(getSupportActionBar().getThemedContext());
		searchFileView.setQueryHint(getString(R.string.search_hint));
		searchFileView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String query) {
				Log.d(TAG, "HELLO - onQueryTextSubmit2 ["+query+"]");
				searchFileView.clearFocus();
				return false;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				mInSearch=true;
				Log.d(TAG, "HELLO - onQueryTextChange2 ["+newText+"]");
				if(songBrowserFragment != null){	
					songBrowserFragment.refresh(newText);
				}
				return false;
			}
		});

		setupActionBar();// This will be called many times
		// Check to see if we are showing the browser fragment (e.g. after orientation change)
		browseFiles();
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
	/* (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockFragmentActivity#onCreateOptionsMenu(com.actionbarsherlock.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		mMenu = menu;
		MenuItem menuItem;

		if(mUpEnabled){
			menuItem = menu.add(0, UP_ID, 1, getString(R.string.up))
					.setIcon(R.drawable.ic_up_sel);
			MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
		}

		menuItem = menu.add(0,SEARCH_LOCAL_ID, 0, getString(R.string.search_songs))
		.setIcon(mColourScheme == LIGHT ? R.drawable.ai_search_light:R.drawable.ai_search_dark);

		MenuItemCompat.setActionView(menuItem, searchFileView);
		MenuItemCompat.setOnActionExpandListener(menuItem, new MenuItemCompat.OnActionExpandListener() {
			@Override
			public boolean onMenuItemActionCollapse(MenuItem item) {
				Log.d(TAG, "HELLO onMenuItemActionCollapse");
				songBrowserFragment.refresh("");
				return true;  // Return true to collapse action view
			}

			@Override
			public boolean onMenuItemActionExpand(MenuItem item) {
				return true;  // Return true to expand action view
			}
		});
		MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS | MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

		menuItem = menu.add(0, REFRESH_ID, 0, getString(R.string.refresh))
		.setIcon(R.drawable.ai_refresh);
		MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);

		menuItem = menu.add(0,SEARCH_INTERNET_ID, 0, getString(R.string.search_songs))
		.setIcon(mColourScheme == LIGHT ? R.drawable.ic_download_light:R.drawable.ic_download_dark);
		MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);

		menu.add(0, DOWNLOAD_ID, 0, getString(R.string.make_download_dir));

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
			finish();
			break;
		case SHOWHELP_ID:
			showHelp();
			break;
		case PREFERENCES_ID:
			setPreferences();
			break;
		case REFRESH_ID:
			songBrowserFragment.refresh();
			break;
		case DOWNLOAD_ID:
			songBrowserFragment.makeDownloadDir();
			break;
		case SHARESONG_ID:
			songViewerFragment.shareSong();
			break;
		case UP_ID:
			upOneLevel(null);
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
		// Instantiate a new instance of SongBrowserFragment or replace existing one if already in place
		FragmentManager fm = getSupportFragmentManager();
		if(null==(songBrowserFragment = (SongBrowserFragment) fm.findFragmentByTag(TAG_SONGBROWSER))){
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			songBrowserFragment = new SongBrowserFragment();
			// Add the fragment to the activity, pushing this transaction on to the back stack.
			ft.replace(R.id.list_container, songBrowserFragment, TAG_SONGBROWSER);
			ft.setTransition(FragmentTransaction.TRANSIT_NONE);
			ft.addToBackStack(null);
			ft.commit();
		}
		else{
			// Do nothing
			Log.d(TAG, "HELLO - found SongBrowserFragement");
		}
		setupActionBar();
		// enable home as up if not actually at home
		//		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	private void showHelp() {
		Log.d(TAG, "showHelp");
		int page = HelpFragment.HELP_PAGE_FILEBROWSER;

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

	@Override
	public void onNewFileCreated() {
		Log.d(TAG, "HELLO newFileCreated");
		// refresh file browser fragment if there is one.
		if( songBrowserFragment != null){
			Log.d(TAG, "HELLO newFileCreated refreshing");
			songBrowserFragment.refresh();
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

	private void setupActionBar(){
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(mColourScheme == LIGHT? R.drawable.chordinator_aug_logo_light_bkgrnd: R.drawable.chordinator_aug_logo_dark_bkgrnd);
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
		Log.d(TAG, "HELLO - onKeyDown");
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			Log.d(TAG, "HELLO - BACK PRESSED");
			finish();
			return true;
		}
		return false;
	}

	@Override
	public void onSongFileSelected(boolean inSet, File songFile) {
		SongViewerFragment viewer = (SongViewerFragment) getSupportFragmentManager()
				.findFragmentByTag(TAG_SONGVIEWER);

		if (viewer == null || !viewer.isVisible()) {
			// Open the file with the SongViewerActivity
			Intent showSong = new Intent(this, SongViewerActivity.class);

			showSong.putExtra("com.chordinator.SongPath", songFile.toString());
			showSong.putExtra("com.bondevans.chordinator.inSetList", inSet);
			showSong.setData(Uri.fromFile(songFile));

			try {
				startActivityForResult(showSong, SongUtils.SONGACTIVITY_REQUEST);
			}
			catch (ActivityNotFoundException e) {
				Log.e(TAG, "NO ACTIVITY FOUND: SongViewerActivity");
			}
		}
		else {
			viewer.setSong(inSet, 0, songFile.getPath(), null);
			if(!mSongInView){
				addShareButton();
			}
		}
	}

	public void upOneLevel(View v){
		Log.d(TAG, "HELLO upOneLevel");
		songBrowserFragment.upOneLevel();
	}
	/**
	 *
	 * Delete the given file from disk
	 * @param filePath
	 */
	public void doDelete(String filePath) {
		File myFile = new File (filePath);
		if(myFile.delete()){
			songBrowserFragment.refresh();
			SongUtils.toast(SongBrowserActivity.this, myFile.getName()+" deleted");
		}
		else{
			SongUtils.toast(SongBrowserActivity.this, "Unable to delete file: "+filePath);
		}
		// Delete song from DB for Chordinator Aug
		DBUtils.deleteSongByFile(getContentResolver(), getString(R.string.authority), myFile.getParent(), myFile.getName());
	}

	private static final String DELETEOLDFILE = "DELETE";
	private static final long BROWSER_SET = 0;
	public void copyFile(String oldPath, String newPath, boolean deleteOldFile) {
		Log.d(TAG, "HELLO save_as ["+oldPath+" to ["+newPath+"]");
		if( new File(newPath).exists()){
			SongUtils.toast(this, getString(R.string.file_exists));
		}
		else{
			SaveAsTask task = new SaveAsTask();
			task.execute(new String[] {oldPath, newPath, deleteOldFile?DELETEOLDFILE:""});
			// Delete old file
			//			if(deleteOldFile){
			//				doDelete(oldPath);
			//			}
		}
	}

	private class SaveAsTask extends AsyncTask<String, Void, String> {
		private ProgressDialog progressDialog;
		private String oldFilePath = "";
		private String newFilePath = "";
		private boolean deleteOldFile=false;
		private String errorMsg = "";
		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(SongBrowserActivity.this, "", getString(R.string.copying), true, false);
			super.onPreExecute();
		}
		String encoding;
		@Override
		protected String doInBackground(String... args) {
			oldFilePath = args[0];
			newFilePath = args[1];
			deleteOldFile = args[2].compareToIgnoreCase(DELETEOLDFILE)==0?true:false;
			String response = "";
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(SongBrowserActivity.this);
			encoding = settings.getString(SongPrefs.PREF_KEY_DEFAULT_ENCODING, "");

			String state = Environment.getExternalStorageState();
			if(state.equals(Environment.MEDIA_MOUNTED) ||
					state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)){
				doSaveAs(oldFilePath, newFilePath, encoding);
			}
			else{
				errorMsg = "No Media mounted!";
				Log.d(TAG,errorMsg);
			}

			return response;
		}

		void doSaveAs(String oldPath, String newPath, String encoding){
			Log.d(TAG, "HELLO doSaveAs ["+oldPath+" to ["+newPath+"]");
			try {
				SongUtils.writeFile(newPath, SongUtils.loadFile(oldPath, null, encoding));
			} catch (ChordinatorException e) {
				e.printStackTrace();
				errorMsg = getString(R.string.copy_failed);
			}
		}

		@Override
		protected void onPostExecute(String result) {
			progressDialog.cancel();
			if( errorMsg.equalsIgnoreCase("")){
				// No errors...
				if(deleteOldFile){
					// this is a "rename" - not a "save as"
					doDelete(oldFilePath);
				}
			}
			else{
				// something went wrong
				SongUtils.toast(SongBrowserActivity.this, getString(R.string.copy_failed));
			}
			songBrowserFragment.refresh();
		}
	}

	@Override
	public void convertChoPro(SongConverter mSc, String newFileName) {
		try {
			File x = new File(newFileName);
			SongUtils.writeFile(newFileName, mSc.createCSF().trim());
			songBrowserFragment.refresh();
			SongUtils.toast( this, x.getName()+ " "+ getString(R.string.saved));
		} catch (ChordinatorException e) {
			SongUtils.toast( this, e.getMessage());
		}
	}

	@Override
	public void enableUp(boolean enable) {
		if(mMenu != null){
			// If not already enabled, enable the UP button
			if(enable && !mUpEnabled){
				MenuItem menuItem = mMenu.add(0, UP_ID, 1, getString(R.string.up))
				.setIcon(R.drawable.ic_up_sel);
                MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
			}
			//Remove the button if we are disabling it
			else if (!enable){
				mMenu.removeItem(UP_ID);
			}
		}
		mUpEnabled = enable;
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
			// TODO force re-draw somehow if colour scheme has changed
			Log.d(TAG, "HELLO onActivityResult3");
		}
		else /*if(requestCode == SongUtils.SONGACTIVITY_REQUEST)*/{
			Log.d(TAG, "HELLO onActivityResult");
			songBrowserFragment.onActivityResult(requestCode, resultCode, data);
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
	public void addNewSet(String songPath) {
		Log.d(TAG, "HELLO: addNewSet ["+songPath+"]");
		// Pop up a dialog asking for file name
		DialogFragment newFragment = AddSetDialog.newInstance(false, BROWSER_SET, songPath);
		newFragment.show(getSupportFragmentManager(), "dialog");
	}

	@Override
	public void setNameClicked(String setName, String songPath) {
		addSongToSETLIST(this, setName, songPath);
	}

	@Override
	public void createSet(String setName, long songId, String songName) {
		// Not used in SongBrowser
	}

	/**
	 * Create a new SETLIST file and add the song to it.
	 */
	@Override
	public void createBrowserSet(String setName, String songName) {
		writeNewSETLIST(this, setName, songName);
		if( songBrowserFragment!= null){
			songBrowserFragment.refresh("");
		}
	}
	
	/**
	 * 
	 * @param context
	 * @param setName
	 * @param songName
	 */
	public static void writeNewSETLIST(Context context, String setName, String songName) {
		String newSet = Statics.CHORDINATOR_DIR + setName;
		// Write new [empty] file in default directory
		try {
			Log.d(TAG,"HELLO saving file:" + newSet);
			SongUtils.writeFile(newSet, "");
			SetList mySetList = new SetList ("", new File(newSet));
			mySetList.addSong(songName);
			// Add new song to end of set list
			mySetList.writeSetList();

		} catch (Exception e) {
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
		}
    	File file = new File(newSet);
    	Log.d(TAG, "HELLO exists: "+(file.exists()?"TRUE":"FALSE"));
	}
	
	public static void addSongToSETLIST(Context context, String setName, String songPath) {
		SetList mySetList;
		Log.d(TAG, "HELLO: set="+setName);
		// Get specified SETLIST
		try {
			mySetList = new SetList ("", new File(Statics.CHORDINATOR_DIR+setName));
			mySetList.addSong(songPath);
			// Add new song to end of set list
			mySetList.writeSetList();
		} catch (Throwable e) {
			Toast.makeText(context,	"Can't write set list: "+setName, Toast.LENGTH_LONG).show();
			return;
		}
		File song = new File(songPath);
		SongUtils.toast(context, "Added: " + song.getName() + " to : "+ setName);
	}

}