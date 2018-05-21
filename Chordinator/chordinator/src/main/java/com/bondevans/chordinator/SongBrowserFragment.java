package com.bondevans.chordinator;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.ListFragment;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bondevans.chordinator.conversion.SongConverter;
import com.bondevans.chordinator.dialogs.BrowserSetListDialog;
import com.bondevans.chordinator.prefs.SongPrefs;
import com.bondevans.chordinator.setlist.EditSetList;
import com.bondevans.chordinator.setlist.SetList;
import com.bondevans.chordinator.utils.SdCardFactory;
import com.bondevans.chordinator.utils.SdCardFactory.SdCard;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class SongBrowserFragment extends ListFragment 
{
	private final static String TAG = "SongBrowserFragment";
	private static final String KEY_TITLE = "KEY1";
	private static final String KEY_ARTIST = "KEY2";
	private static final String KEY_COMPOSER = "KEY3";
	public static final String KEY_FILENAME = "KEY4";
	public static final String KEY_FILEPATH = "KEY5";
	public static final String KEY_FOLDER = "KEY6";
	private static final String KEY_CURDIR = "KEY7";
	public static final String KEY_RENAMEFLAG = "KEY7";
	private List<String> directoryEntries = new ArrayList<>();
	public File mCurrentDirectory;
    // Song Conversion
	private static SongConverter mSc;
	private static String mFilePath;
	private int mPosition=0;
	private OnSongFileSelectedListener songFileSelectedListener;
    private TextView	mCurrentFolder;
	private SongArrayAdapter songArrayAdapter;
	private String mSdCardRoot;
	private String mFilter="";
	private AdView mAdView;
	private SdCardFactory mCards;

	public interface OnSongFileSelectedListener{
		void onSongFileSelected(boolean inSet, File songFile);
		void convertChoPro(SongConverter mSc, String newFileName);
		void doDelete(String filePath);
		void copyFile(String oldPath, String newPath, boolean deleteOldFile);
		void upOneLevel(View v);
		void enableUp(boolean enabled);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			songFileSelectedListener = (OnSongFileSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnSongFileSelectedListener");
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// Check to see if we have a frame in which to embed the details
		// fragment directly in the containing UI.
		View songFrame = getActivity().findViewById(R.id.songview_fragment);
        boolean dualPane = songFrame != null && songFrame.getVisibility() == View.VISIBLE;

		if (savedInstanceState != null) {
			// Restore last state for checked position.
			mPosition = savedInstanceState.getInt("curChoice", 0);
		}

		if (dualPane) {
			Log.d(TAG, "HELLO - dual pane");
			// In dual-pane mode, the list view highlights the selected item.
			getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		}
		
		if(isDim()){
			// Diminished Only  - Load an ad
	        // Gets the ad view defined in layout/ad_fragment.xml with ad unit ID set in
	        // values/strings.xml.
	        mAdView = (AdView) getView().findViewById(R.id.adView);
	        if(mAdView != null){
		        // Start loading the ad in the background.
		        mAdView.loadAd(new AdRequest.Builder().build());
	        }
		}
	}

	public static SongBrowserFragment newInstance() {
		SongBrowserFragment f = new SongBrowserFragment();
		Bundle b = new Bundle();
		f.setArguments(b);
		Log.d(TAG, "HELLO newInstance");
		return f;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "HELLO onCreate1");
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
		// Default is /sdcard/chordinator
		Log.d(TAG, "HELLO onCreate2");

		// See whether we've changed directory before changing orientation
		String curDir="";
		if( savedInstanceState != null ){
			curDir=savedInstanceState.getString(KEY_CURDIR);
		}
		else{
			curDir = settings.getString(SongPrefs.PREF_KEY_SONGDIR, Statics.CHORDINATOR_DIR);
		}

		mCurrentDirectory = new File (curDir);
		Log.d(TAG, "HELLO onCreate3["+mCurrentDirectory+"]");
		// Only all access to sdcard on Chordinator Aug
		mSdCardRoot = Environment.getExternalStorageDirectory().getPath();

		Log.d(TAG, "HELLO onCreate4");
	}


	/* (non-Javadoc)
	 * @see android.support.v4.app.ListFragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.songbrowser_layout, container, false);
		mCurrentFolder = (TextView) contentView.findViewById(R.id.currentFolder);
		Log.d(TAG, "HELLO onCreatView");
		return contentView;
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.ListFragment#onViewCreated(android.view.View, android.os.Bundle)
	 */
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		Log.d(TAG, "HELLO onViewCreated");
		final ListView lv = getListView();
		registerForContextMenu(lv);

		lv.setTextFilterEnabled(true);
		lv.setItemsCanFocus(false);
		lv.setLongClickable(true);
		lv.setFastScrollEnabled(true);
		Log.d(TAG, "HELLO onViewCreated2");
		mCards = new SdCardFactory(getActivity());
		mCards.log();
		browseFolder(mCurrentDirectory);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "HELLO onActivityResult-fragment");
		// If Exit pressed on sub activity, then exit the whole application
		if( requestCode == SongUtils.SONGACTIVITY_REQUEST ){
			System.gc();
			switch (resultCode){
			case Activity.RESULT_OK:
				browseFolder(mCurrentDirectory); 	// Refresh
				setSelection(mPosition);
				break;
			default:
				super.onActivityResult(requestCode, resultCode, data);
			}
		}
		else{
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	/**
	 * This function browses up one level 
	 * according to the field: currentDirectory
	 */
	public void upOneLevel(){
		Log.d(TAG, "HELLO path5=["+mCurrentDirectory.getParentFile().getPath()+"]");
		if(mCurrentDirectory.getParent() != null){
			Log.d(TAG, "HELLO path6=["+mCurrentDirectory.getParentFile().getPath()+"]");
			browseFolder(mCurrentDirectory.getParentFile());
		}
	}

	public void browseFolder(final File aFile){
		Log.d(TAG, "HELLO browseFolder ["+aFile.getName()+"] filter ["+mFilter+"]");
		// On relative we display the full path in the title.
		if(!aFile.exists()){
			// If file doesn't exist (for some bizarre reason) just default to /sdcard
			Log.d(TAG, "HELLO file doesn't exist");
			mCurrentDirectory = new File(mSdCardRoot);
			listFilesinFolder(mCurrentDirectory, mFilter);
		}
		else if (aFile.isDirectory()){
			mCurrentDirectory = aFile;// Remember current folder
			listFilesinFolder(aFile, mFilter);
		}
		else{
			// Save file path in preferences so we come back here next time
			saveCurrentDir();
			// Start an intent to View the file, that was clicked...
			openFile(aFile);
		}
	}

	private void openFile(File theFile){
		// Don't open any files with "banned" suffixes
		if(SongUtils.isBanned(theFile)){
			SongUtils.toast(getActivity(), getString(R.string.banned_file));
			return;
		}
		songFileSelectedListener.onSongFileSelected(false, theFile);
	}

	/**
	 * fill
	 * lists all files and folders in a given directory
	 * @param folder
	 * @param nameFilter
	 */
	private void listFilesinFolder(File folder, String nameFilter) {
		directoryEntries.clear();
		ArrayList<String> songFiles = new ArrayList<String>();
		Log.d(TAG, "HELLO SDCARDROOT=["+mSdCardRoot+"] CURRENT FOLDER=["+folder.getPath()+"]");
		// If we have gone UP from an sdcard root then simply list the sdcard and any other external sdcards - not the actual folders.
		// Also disable the up button at this point
		boolean root=false;
		if(mCards.isPathUpFromRoot(folder)){
			songFileSelectedListener.enableUp(false);
			root=true;
			for( SdCard sdcard: mCards.getSdCards()){
				directoryEntries.add(sdcard.name+File.separator);
			}
			mCurrentFolder.setText("/");
		}
		else{
			songFileSelectedListener.enableUp(true);
			mCurrentFolder.setText(doPath(folder.getPath()));
		}

		if(!root){
			if(nameFilter.equalsIgnoreCase("")){
				// Add all the SETLISTs at the top - unless we are searching - in which case don't bother with SETLISTS
				SetListFilter filter = new SetListFilter(SetList.SETLIST_PREFIX);

				if(folder.listFiles(filter)!=null){
					for (File currentFile : folder.listFiles(filter)){
						//				Log.d(TAG, "HELLO adding "+songName(currentFile));
						directoryEntries.add(songName(currentFile));
					}
				}
			}
			// Now add the rest of the files
			FilenameFilter fnf = new MyFilenameFilter(nameFilter);
			if(folder.listFiles(fnf)!=null){
				for (File currentFile : folder.listFiles(fnf)){
					if (currentFile.getName().startsWith(".")){
						// Don't add anything starting with a . (i.e. hidden folders and files)
					}
					else if (currentFile.isDirectory()) {
						// Add folder name - with slash on the end, so that the folder image is shown
						songFiles.add(currentFile.getName()+File.separator);
					}
					else if (currentFile.getName().startsWith(SetList.SETLIST_PREFIX)){
						// Ignore set lists here
						//				Log.d(TAG, "HELLO ignoring "+songName(currentFile));
					}
					else{
						//				Log.d(TAG, "HELLO Adding FILENAME: ["+fileName+"]");
						songFiles.add(songName(currentFile));
					}
				}
			}
		}
		// Sort the song files,
		Collections.sort(songFiles, new FileNameComparator());
		//then add them to the directory listing
		Iterator<String> e;
		e = songFiles.iterator();
		while (e.hasNext()) {
			directoryEntries.add(e.next());
		}
		Log.d(TAG, "HELLO B4 setListAdapter2");
		e = directoryEntries.iterator();
		String [] myfiles = new String [directoryEntries.size()];

		int x=0;
		while(e.hasNext()){
			myfiles[x++]=e.next();
		}
		songArrayAdapter = new SongArrayAdapter(getActivity(), myfiles);
		setListAdapter(songArrayAdapter);
		final ListView lv = getListView();
		registerForContextMenu(lv);

		lv.setTextFilterEnabled(true);
		lv.setItemsCanFocus(false);
		lv.setLongClickable(true);
	}

	/**
	 * Returns song name given a song file
	 * @param file
	 * @return
	 */
	private String songName( File file ){
		return file.getName();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		//		Log.d(TAG, "HELLO onListItemClick");
		// Get file+icon object from the list
		String selectedFile = (String) getListView().getItemAtPosition(position);
		openItem(selectedFile);
		mPosition = position;
	}

	/**
	 * openItem - Opens the selected file
	 * @param selectedItem
	 */
	private void openItem(String selectedItem){
		Log.L(TAG, "Item", selectedItem);
		if(mCards.isRoot(selectedItem)){
			browseFolder(new File(mCards.toPath(selectedItem)));
		}
		else{
			browseFolder(new File(addDir(mCards.toPath(selectedItem))));
		}
	}

	public void refresh(){
		browseFolder(mCurrentDirectory);
	}

	public void refresh(String filter){
		mFilter = filter;
		if( mCurrentDirectory != null){
			browseFolder(mCurrentDirectory);
		}
	}

	public void makeDownloadDir(){
		// Make the current folder the song download directory
		// Save file path in preferences so we come back here next time
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());

		SharedPreferences.Editor editor = settings.edit();
		editor.putString(SongPrefs.PREF_KEY_DOWNLOADDIR, this.mCurrentDirectory.getPath()+"/");
		// Don't forget to commit your edits!!!
		editor.commit();
	}
	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	public void onPause() {
		//		Log.d(TAG,"HELLO Pausing");
		//
		super.onPause();
	}


	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	public void onResume() {
		Log.d(TAG,"HELLO Resuming");
		super.onResume();
	}

	@Override
	public void onStop() {
		//		Log.d(TAG,"HELLO stopping");
		super.onStop();
	}
	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	public void onDestroy() {
		//		Log.d(TAG,"HELLO Being Destroyed");
		super.onDestroy();
	}
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu, android.view.View, android.view.ContextMenu.ContextMenuInfo)
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		// CHORDINATOR - AUGMENTED
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		String selectedItem = (String) getListView().getItemAtPosition(info.position);
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.file_browser_context, menu);
		menu.setHeaderTitle("Options");
//		menu.removeItem(R.id.add_to_set);// ALWAYS FOR AUG
		if(selectedItem.endsWith(File.separator) || selectedItem.equals("..")){
			// Directory, so can't edit or delete or convert or save_as or add to set
			menu.removeItem(R.id.edit);
			menu.removeItem(R.id.edit_as_txt);
			menu.removeItem(R.id.delete);
			menu.removeItem(R.id.convert);
			menu.removeItem(R.id.save_as);
			menu.removeItem(R.id.add_to_set);
			menu.removeItem(R.id.rename);
			menu.removeItem(R.id.import_set);//2.4.0
			menu.removeItem(R.id.share);
		}
		else if(selectedItem.startsWith(SetList.SETLIST_PREFIX)){
			// Old-style Set list, so can't convert or add to set
			menu.removeItem(R.id.convert);
			menu.removeItem(R.id.add_to_set);
		}
		else if(SongUtils.isBannedFileType(selectedItem)){
			// On banned list so can't convert
			menu.removeItem(R.id.convert);
			menu.removeItem(R.id.edit);
			menu.removeItem(R.id.edit_as_txt);
			menu.removeItem(R.id.open);
			menu.removeItem(R.id.import_set);//2.4.0
		}
		else{
			// Anything else (normal song file)
			menu.removeItem(R.id.edit_as_txt);//2.4.0
			menu.removeItem(R.id.import_set);//2.4.0
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if(item.getGroupId()!= R.id.context_group_browser){
			Log.d(TAG,"HELLO onContextItemSelected - sonvView Context Menu");

			return super.onContextItemSelected(item);
		}
		Log.d(TAG,"HELLO onContextItemSelected");
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        String selectedItem = (String) getListView().getItemAtPosition(info.position);
		Log.d(TAG, "HELLO selectedItem=["+ selectedItem +"]");
		mPosition = info.position;
		if (item.getItemId() == R.id.edit) {
			//				Log.d(TAG,"HELLO edit Selected");
			if( selectedItem.startsWith(SetList.SETLIST_PREFIX)){
				editSetList(selectedItem);
			}
			else{
				editSong(selectedItem);
			}
			return true;
		}else if(item.getItemId() == R.id.edit_as_txt){
            editSong(selectedItem); // Only for SETLIST files
            return true;
        }else if (item.getItemId() == R.id.share) {
			shareSong(addDir(selectedItem));
			return true;
		} else if (item.getItemId() == R.id.convert) {
			convertTextToChopro(addDir(selectedItem));
			return true;
		} else if (item.getItemId() == R.id.delete) {
			showDoDeleteDialog(addDir(selectedItem), selectedItem);
			return true;
		} else if (item.getItemId() == R.id.save_as) {
			showSaveAsDialog(mCurrentDirectory.getPath(), selectedItem);
			return true;
		}
		else if (item.getItemId() == R.id.rename) {
			showRenameDialog(mCurrentDirectory.getPath(), selectedItem);
			return true;
		} else if (item.getItemId() == R.id.open) {
			openItem(selectedItem);
			return true;
		} else if( item.getItemId() == R.id.import_set){importSet(selectedItem);
			return true;
		} else if (item.getItemId() == R.id.add_to_set) {
			showSetListDialog(addDir(selectedItem));
			return true;
		} else {
			return super.onContextItemSelected(item);
		}
	}
	private void importSet(String fileName) {
		try {
			// Load up into SetList
			SetList mySet = new SetList(getString(R.string.authority), addDir(fileName));
			// and then create SET in DB from it
			mySet.importSet(getActivity().getContentResolver());
			SongUtils.toast(getActivity(), fileName+" "+ getString(R.string.imported));
		} catch (Throwable e) {
			SongUtils.toast(getActivity(), e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Adds current directory to an item selected from the list
	 * @param name
	 * @return
	 */
	private String addDir(String name){
		return mCurrentDirectory.getPath() + File.separator + name;
	}

	public void showSetListDialog(String songName) {
		DialogFragment newFragment = BrowserSetListDialog.newInstance(songName);
		newFragment.show(getFragmentManager(), "dialog");
	}

	/**
	 * Edit the current Song 
	 */
	private void editSong(String songPath) {
		// Open the file with the EditSong Activity
		Intent myIntent = new Intent(getActivity(),	EditSong.class);
		try {
			// Put the path to the song file in the intent
			myIntent.putExtra(getString(R.string.song_path), addDir(songPath));
			//			startActivityForResult(myIntent, SongUtils.EDITSONG_REQUEST);
			startActivity(myIntent);
		}
		catch (ActivityNotFoundException e) {
			SongUtils.toast( getActivity(),e.getMessage());
		}
	}

	public void shareSong(String fileName){
		File aFile = new File(fileName);
		Intent theIntent = new Intent(Intent.ACTION_SEND);
		theIntent.setType("text/plain");
		// the formatted text.
		theIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(aFile));
//		theIntent.putExtra(Intent.EXTRA_TEXT, mSf.getSong().getSongText());
		//next line specific to email attachments
		theIntent.putExtra(Intent.EXTRA_SUBJECT, "Sending " + aFile.getName());
		try {
			startActivity(Intent.createChooser(theIntent, "Share With...."));
		}
		catch (Exception e) {
		}
	}

	private void convertTextToChopro(String filePath){
		SongFile sf;
		mSc = new SongConverter();
		mFilePath=filePath;
		// Read in contents of file
		//		SharedPreferences settings = getSharedPreferences(SongPrefs.PREFS_NAME, MODE_PRIVATE);
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
		// Get the full file path to the chosen song from the Intent
		try {
			// Create a new SongFile - this loads up the contents of the file into the Song class
			sf = new SongFile(mFilePath, null, settings.getString(SongPrefs.PREF_KEY_DEFAULT_ENCODING, ""));
		} catch (ChordinatorException e) {
			SongUtils.toast(getActivity(), e.getMessage());
			return;
		}
		if(sf.hasTitle){
			SongUtils.toast(getActivity(), getString(R.string.already_chopro));
		}
		else{
			// Convert to intermediate format....
			mSc.convertToIntermediateFormat(sf.getSong().getSongText());
			// The converter has had a guess at the title, so show this in a dialog box with Artist + composer
			// then set the title/artist/composer tags
			showGetSongTitleDialog(mSc.getSong().getTitle(), mSc.getSong().getArtist(), mSc.getSong().getComposer());
		}
	}

	void showGetSongTitleDialog(String title, String artist, String composer) {
		DialogFragment newFragment = GetSongTitleDialog.newInstance(title, artist, composer);
		newFragment.show(getFragmentManager(), "dialog");
		browseFolder(mCurrentDirectory); 	// Refresh
		setSelection(mPosition);
	}
	public static class GetSongTitleDialog extends DialogFragment{
		private static EditText titleText;
		private static EditText artistText;
		private static EditText composerText;

		static GetSongTitleDialog newInstance(String title, String artist, String composer) {
			GetSongTitleDialog frag = new GetSongTitleDialog();
			Bundle args = new Bundle();
			args.putString(KEY_TITLE, title);
			args.putString(KEY_ARTIST, artist);
			args.putString(KEY_COMPOSER, composer);
			frag.setArguments(args);
			return frag;
		}
		/* (non-Javadoc)
		 * @see android.support.v4.app.DialogFragment#onCreateDialog(android.os.Bundle)
		 */
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {

			LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.song_title_dialog, null);

			titleText = (EditText) layout.findViewById(R.id.title);
			titleText.setText(getArguments().getString(KEY_TITLE));
			artistText = (EditText) layout.findViewById(R.id.artist);
			artistText.setText(getArguments().getString(KEY_ARTIST));
			composerText = (EditText) layout.findViewById(R.id.composer);
			composerText.setText(getArguments().getString(KEY_COMPOSER));

			return new AlertDialog.Builder(getActivity())
			.setView(layout)
			.setTitle(R.string.set_song_details)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					// User clicked OK so get title/artist/composer from dialog
					mSc.setTitle(titleText.getText().toString());
					mSc.setComposer(composerText.getText().toString());
					mSc.setArtist(artistText.getText().toString());
					// then convert intermediate to CHOPRO format...
					String newFileName = SongUtils.swapSuffix(mFilePath, Statics.SONGFILEEXT);
					((SongBrowserActivity)getActivity()).convertChoPro(mSc, newFileName);
				}
			})
			.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// Cancel pressed so do nothing
				}
			}).create();
		}
	}

	void showDoDeleteDialog(String filePath, String fileName) {
		DialogFragment newFragment = DoDeleteDialog.newInstance(filePath, fileName);
		newFragment.show(getFragmentManager(), "dialog");
	}

	public static class DoDeleteDialog extends DialogFragment{
		String fileName;
		String filePath;

		static DoDeleteDialog newInstance(String filePath, String fileName) {
			DoDeleteDialog frag = new DoDeleteDialog();
			Bundle args = new Bundle();
			args.putString(KEY_FILENAME, fileName);
			args.putString(KEY_FILEPATH, filePath);
			frag.setArguments(args);
			return frag;
		}
		/* (non-Javadoc)
		 * @see android.support.v4.app.DialogFragment#onCreateDialog(android.os.Bundle)
		 */
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			fileName = getArguments().getString(KEY_FILENAME);
			filePath = getArguments().getString(KEY_FILEPATH);

			return new AlertDialog.Builder(getActivity())
			.setTitle(fileName)
			.setMessage(getString(R.string.are_you_sure_delete))
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					// User clicked OK so delete
					((SongBrowserActivity)getActivity()).doDelete(filePath);
				}
			})
			.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					/* User clicked cancel so do nothing */
				}
			})
			.create();
		}
	}

	void showRenameDialog(String folder, String fileName) {
		Log.d(TAG, "HELLO Rename folder=["+folder+"] file=["+fileName+"]");
		DialogFragment newFragment = SaveAsDialog.newInstance(folder, fileName, true);
		newFragment.show(getFragmentManager(), "dialog");
	}
	void showSaveAsDialog(String folder, String fileName) {
		Log.d(TAG, "HELLO SaveAs folder=["+folder+"] file=["+fileName+"]");
		DialogFragment newFragment = SaveAsDialog.newInstance(folder, fileName, false);
		newFragment.show(getFragmentManager(), "dialog");
	}

	/**
	 * SaveAsDialog - actually also used to Rename
	 * @author Paul
	 *
	 */
	public static class SaveAsDialog extends DialogFragment {
		EditText fileText;
		String fileName;
		String folder;

		static SaveAsDialog newInstance(String folder, String fileName, boolean rename) {
			SaveAsDialog frag = new SaveAsDialog();
			Bundle args = new Bundle();
			args.putString(KEY_FILENAME, fileName);
			args.putString(KEY_FOLDER, folder);
			args.putBoolean(KEY_RENAMEFLAG, rename);
			frag.setArguments(args);
			return frag;
		}

		/* (non-Javadoc)
		 * @see android.support.v4.app.DialogFragment#onCreateDialog(android.os.Bundle)
		 */
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			fileName = getArguments().getString(KEY_FILENAME);
			folder = getArguments().getString(KEY_FOLDER);
			final boolean rename = getArguments().getBoolean(KEY_RENAMEFLAG);

			fileText = new EditText(getActivity());
			fileText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS|InputType.TYPE_CLASS_TEXT);
			fileText.setText("Copy of "+fileName);
			return new AlertDialog.Builder(getActivity())
			.setTitle(rename?R.string.rename:R.string.save_as)
			.setMessage(getString(R.string.new_file_name))
			.setView(fileText)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					// User clicked OK so get name from fileName
					Log.d(TAG, "HELLO Copy/Rename ["+ fileText.getText().toString()+"]");
					((SongBrowserActivity)getActivity()).copyFile(folder+File.separator+fileName, 
							folder+File.separator+fileText.getText().toString(), 
							rename);
				}
			})
			.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					/* User clicked cancel so do nothing */
				}
			})
			.create();
		}
	}

	/**
	 * Edit the current Song
	 */
	private void editSetList(String setListPath) {
		// Open the file with the EditSong Activity
		Intent myIntent = new Intent(getActivity(), EditSetList.class);
		try {
			// Put the path to the set list file in the intent
			myIntent.putExtra(getString(R.string.song_path), addDir(setListPath));
			startActivity(myIntent);
		}
		catch (ActivityNotFoundException e) {
			SongUtils.toast( getActivity(),e.getMessage());
		}
	}

	public class SetListFilter implements FilenameFilter{
		private String prefix="";
		public SetListFilter(String prefix){
			this.prefix = prefix;
		}

		public boolean accept(File dir, String name){
			if (name.startsWith(prefix))
				return true;
			return false;
		}
	}

	@SuppressLint("DefaultLocale")
	public class MyFilenameFilter implements FilenameFilter{
		private String filter="";
		public MyFilenameFilter(String filter){
			this.filter = filter.toLowerCase();
		}
		@Override
		public boolean accept(File dir, String filename) {
			if(filename.toLowerCase().contains(filter))
				return true;
			return false;
		}

	}
	/* (non-Javadoc)
	 * @see android.app.Activity#onConfigurationChanged(android.content.res.Configuration)
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	public class SongArrayAdapter extends ArrayAdapter<String> {
		private final Context context;
		private final String[] fileNames;

		public SongArrayAdapter(Context context, String[] values) {
			super(context, R.layout.songbrowser_item, values);
			this.context = context;
			this.fileNames = values;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			//			Log.d(TAG, "HELLO - getView");
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = inflater.inflate(R.layout.songbrowser_item, parent, false);
			TextView textView = (TextView) rowView.findViewById(R.id.file_name);
			ImageView imageView = (ImageView) rowView.findViewById(R.id.file_icon);
			// Change the icon for Folder, Up one level, song file, banned file, set list
			String fileNameText = fileNames[position];
			if (fileNameText.endsWith(File.separator)) {
				// Folder
				imageView.setImageResource(R.drawable.folder_icon);
			}
			else if(SongUtils.isBannedFileType(fileNameText)|| SongUtils.isBanned(fileNameText)){
				// Banned file type
				imageView.setImageResource(R.drawable.banned_file_icon);
			}
			else if( fileNameText.startsWith(SetList.SETLIST_PREFIX)){
				// Old style Set List
				imageView.setImageResource(R.drawable.setlist_icon);
			}
			else{
				// Possible song file
				imageView.setImageResource(R.drawable.song_file_icon);
			}
			textView.setText(fileNameText);

			return rowView;
		}
	}
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.d(TAG, "HELLO PUTTING curDir=["+mCurrentDirectory.getPath()+"]");
		outState.putString(KEY_CURDIR, mCurrentDirectory.getPath());
	}
	/* (non-Javadoc)
	 * @see android.support.v4.app.ListFragment#onDestroyView()
	 */
	@Override
	public void onDestroyView() {
		// Save current Directory before leaving
		saveCurrentDir();
		super.onDestroyView();
	}
	void saveCurrentDir(){
		// Save file path in preferences so we come back here next time
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());

		SharedPreferences.Editor editor = settings.edit();
		editor.putString(SongPrefs.PREF_KEY_SONGDIR, this.mCurrentDirectory.getPath());
		// Don't forget to commit your edits!!!
		editor.commit();
	}

	/**
	 * doPath pretties up the folder path for display
	 * @param path
	 * @return
	 */
	private String doPath(String path){
		// remove any slashes on the end of the path
		Log.d(TAG, "HELLO doPath in["+path+"]");
		if( path.endsWith("/")){
			path = path.substring(0, path.length()-1);
		}
		// Replace Environment.getExternalStorageDirectory() with /sdcard for display purposes only
		path = mCards.toDisplay(path);

		Log.d(TAG, "HELLO dopath out["+path+"]");
		return path;
	}
	private boolean isDim(){
		return(getString(R.string.app_version).equalsIgnoreCase("dim")?true:false);
	}
}