package com.bondevans.chordinator;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bondevans.chordinator.chunking.SongHTMLFormatter;
import com.bondevans.chordinator.chunking.SongTextFormatter;
import com.bondevans.chordinator.chunking.TextSongHTMLFormatter;
import com.bondevans.chordinator.db.DBUtils;
import com.bondevans.chordinator.dialogs.PromotePaidAppDialog;
import com.bondevans.chordinator.grids.ChordShapeProvider;
import com.bondevans.chordinator.prefs.SongPrefs;
import com.bondevans.chordinator.trial.Trial;

import java.io.File;
import java.io.FileDescriptor;
import java.lang.ref.WeakReference;

public class SongViewerFragment extends Fragment implements OnClickListener{
    private static final int MINSLEEP_DEFAULT = 10;
//    private static final int MINSLEEP_TURBO = 1;
    private View mContentView=null;

	private final static String TAG = "SongViewerFragment";
	private final static int BACKGROUND_ALPHA = 125;
	public SongCanvas3 mSongCanvas=null;
	public SongFile mSf = null;
	public static final String KEY_SCROLLSPEED = "SPEED";
	private static final String KEY_AUTOSCROLL = "AUTOSCROLL";
	private static final String KEY_TEXTSIZE = "TEXTSIZE";
	private static final String KEY_TRANSPOSE = "TRANSPOSE";
	private boolean showGrids=false;
	private int mGridInstrument=ChordShapeProvider.INSTRUMENT_GUITAR;
	public int	mScrollSpeed=-1;
	private boolean mInSetList=false;
	public SongPrefs mPrefs = new SongPrefs();
	ImageButton biggerButton;
	ImageButton smallerButton;
	ImageButton trupButton;
	ImageButton trdownButton;
	Button minusButton;
	Button plusButton;
	Button scrollButton;
	ImageButton prevButton;
	ImageButton nextButton;
	ScrollView scroller;
	TextView scrollSpeedLabel;
	//	AUTOSCROLL
	public boolean mAutoScroll = false;
	private int scrollSleep;
	public final static int MAXSPEED = 20;
	private final static int MINSPEED = 0;
	private final static int SCROLL_INC = 10; //Milliseconds
	private static final int MAXLINE_LEN = 80;
//    private int mMinSleep=MINSLEEP_DEFAULT;

	// ChordinatorPlus
	long mSongId=0;
	public int mScrollDelay=0;		// Number of seconds to delay before scrolling
	private int mSavedScrollSpeed=-1;
	private String [] mTextSizes = null;
	private int mCurrentTextSizeIndex=0;
	private int mDefaultTextSize;	// The text size set up in the preferences

	SongViewerListener songViewerListener;

	private int mRequestCode;

	private final static int NO_TRANSPOSE=99;
	private int mTranspose= NO_TRANSPOSE;
	private boolean mScrollPreference;
	private boolean mReadOnly;

	public interface SongViewerListener{
		void onNewFileCreated();
		void nextSong();
		void prevSong();
	}

	@Override
	public void onAttach(Activity activity) {
		Log.d(TAG, "HELLO onAttach");
		super.onAttach(activity);
		try {
			songViewerListener = (SongViewerListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement SongViewerListener");
		}
	}

	/** Called when the Fragment is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "HELLO onCreate");
		// If this is the first time - display 
		// Load up the users preferences
		loadPreferences();
		getCurrentTextSizeIndex();
		// Remember the default text size
		mDefaultTextSize=mPrefs.getTextSize();
		// Override defaults if orientation changed
		if(savedInstanceState != null){
			mSavedScrollSpeed = savedInstanceState.getInt(KEY_SCROLLSPEED);
			Log.d(TAG, "savedInstanceState NOT null scrollSpeed=["+mSavedScrollSpeed+"]");
			mAutoScroll = savedInstanceState.getBoolean(KEY_AUTOSCROLL);
			mCurrentTextSizeIndex = savedInstanceState.getInt(KEY_TEXTSIZE);
			setTextSizeIndex(mCurrentTextSizeIndex);
			// re-apply transpose settings
			mTranspose = savedInstanceState.getInt(KEY_TRANSPOSE, NO_TRANSPOSE);
		}
		// Set up scroll handler
		mFragment=new WeakReference<>(this);
		mRedrawHandler = new RefreshHandler(mFragment.get());

		super.onCreate(savedInstanceState);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "HELLO onCreatView");
		mContentView = inflater.inflate(R.layout.songviewer_layout, container, false);

		minusButton = (Button) mContentView.findViewById(R.id.minusButton);
		minusButton.setOnClickListener(this);
		minusButton.getBackground().setAlpha(BACKGROUND_ALPHA);
		plusButton = (Button) mContentView.findViewById(R.id.plusButton);
		plusButton.setOnClickListener(this);
		plusButton.getBackground().setAlpha(BACKGROUND_ALPHA);
		scrollButton = (Button) mContentView.findViewById(R.id.scrollButton);
		scrollButton.setOnClickListener(this);
		scrollButton.getBackground().setAlpha(BACKGROUND_ALPHA);

		prevButton = (ImageButton) mContentView.findViewById(R.id.prevButton);
		prevButton.setOnClickListener(this);
		nextButton = (ImageButton) mContentView.findViewById(R.id.nextButton);
		nextButton.setOnClickListener(this);
		scrollSpeedLabel = (TextView) mContentView.findViewById(R.id.scroll_speed);

		scroller = (ScrollView) mContentView.findViewById(R.id.scroller);
		mSongCanvas = (SongCanvas3) mContentView.findViewById(R.id.song_canvas);
		registerForContextMenu(mSongCanvas);

		biggerButton = (ImageButton) mContentView.findViewById(R.id.biggerButton);
		biggerButton.setOnClickListener(this);
		biggerButton.setEnabled((mCurrentTextSizeIndex != mTextSizes.length - 1));

		smallerButton = (ImageButton) mContentView.findViewById(R.id.smallerButton);
		smallerButton.setOnClickListener(this);
		smallerButton.setEnabled((mCurrentTextSizeIndex != 0));

		trupButton = (ImageButton) mContentView.findViewById(R.id.trupButton);
		trupButton.setOnClickListener(this);

		trdownButton = (ImageButton) mContentView.findViewById(R.id.trdownButton);
		trdownButton.setOnClickListener(this);
		return mContentView;
	}

	public void savePreviousSongSettings(){
		Log.d(TAG, "HELLO savePreviousSongSettings1");
		if(mSf != null){
			Log.d(TAG, "HELLO savePreviousSongSettings2:"+mSf.getTitle());
			// If the current song has been transposed - save this in the preferences
			if(mSongCanvas.getTranspose() !=0 ){
				setPreference(mSf.getTitle() + SongPrefs.PREF_KEY_TRANSPOSE, mSongCanvas.getTranspose());
			}
			else{
				// If its set to the default value then remove the preference (ignore any errors)
				removePreference(mSf.getTitle() + SongPrefs.PREF_KEY_TRANSPOSE);
			}
			// If the current song has been autoscrolled - save this in the preferences
			if(mScrollSpeed != SongPrefs.DEFAULTSPEED ){
				setPreference(mSf.getTitle() + SongPrefs.PREF_KEY_SCROLLSPEED, mScrollSpeed);
			}
			else{
				// If its set to the default value then remove the preference (ignore any errors)
				removePreference(mSf.getTitle() + SongPrefs.PREF_KEY_SCROLLSPEED);
			}
		}
	}

	public void setSong(boolean inSetList, long songId, String fileName, FileDescriptor mFileDescriptor){
		// Readonly if opening content from FileDescriptor
		mReadOnly = mFileDescriptor != null;
		// Save any settings for previous song, if appropriate
		savePreviousSongSettings();

		// See if we are in a set list - if so set up the prev and next buttons
		mInSetList = inSetList;
		// See if we got the rowID of the Song record
		mSongId = songId;
		// Get the full file path to the chosen song from the Intent
		try {
			// Create a new SongFile - this loads up the contents of the file into the Song class
			Log.d(TAG, "HELLO setSong ["+fileName+"]");
			if(SongUtils.isBannedFileType(fileName)){
				Log.d(TAG, "HELLO onCreate BANNED");
				throw new ChordinatorException(getString(R.string.banned_file_type));
			}
			mSf = new SongFile(fileName, mFileDescriptor, mPrefs.getDefaultEncoding());
		} catch (ChordinatorException e) {
			errMsgToast(e.getMessage());
			//			this.finish();
			return;
		}

		if(isDim() && Trial.maxViewsReached(getActivity(), SongPrefs.PREF_KEY_FREEVIEWCOUNT, true)){
			// Show Pester Dialog in Diminished
			DialogFragment newFragment = PromotePaidAppDialog.newInstance();
			newFragment.show(getFragmentManager(), "dialog");

			// Reset conter
			Trial.resetViewCount(getActivity());
		}
		setPrevNextButtons();
		scrollButton.setVisibility(View.VISIBLE);
		biggerButton.setVisibility(View.VISIBLE);
		smallerButton.setVisibility(View.VISIBLE);
		if(mSf.hasTitle){
			// Can't transpose a text file
			trupButton.setVisibility(View.VISIBLE);
			trdownButton.setVisibility(View.VISIBLE);
		}
		// Check whether song is in the Db and add if not (dont bother if its just an orientation change (or if we are opening from fileDescriptor)
		if( mSavedScrollSpeed == -1 && mFileDescriptor == null){// TODO Shouldnt be using this flag
			UpdateSongDB();
		}
		// Create a new Song Canvas and load up the chosen song
		mSongCanvas.setupSong(mSf.getSong(), mPrefs);
		// Support TAB in Chordinator_aug
		mSongCanvas.setTabSupported(true);
		mSongCanvas.setIsChopro(mSf.hasTitle);// Use Fixed-width font for non-chopro files

		// See if we're previously viewed and transposed or set an auto-scroll timer for this song
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());

		// ...and load up into a SongPrefs object
		if(mTranspose != NO_TRANSPOSE){
			mSongCanvas.setTranspose(mTranspose);
		}
		else{
			mSongCanvas.setTranspose(settings.getInt(mSf.getTitle()+ SongPrefs.PREF_KEY_TRANSPOSE, 0));
		}

		if( mSavedScrollSpeed != -1 ){
			setScrollSpeed(mSavedScrollSpeed);
		}
		else{
			setScrollSpeed(settings.getInt(mSf.getTitle()+ SongPrefs.PREF_KEY_SCROLLSPEED, SongPrefs.DEFAULTSPEED));
		}
		setAutoScroll(mScrollPreference, mScrollDelay);// Make sure scroll delay kicks in when in a set list, but also make sure we only autoscroll if selected in preferences

		// Keep the screen on...
		mSongCanvas.setKeepScreenOn(true);
		mSongCanvas.doFullRedraw();
		scroller.fullScroll(ScrollView.FOCUS_UP);
		settingsBasedOnPreferences();

		Log.d(TAG, "HELLO END OF onCreateView");
	}

	void settingsBasedOnPreferences(){
		Log.d(TAG, "HELLO settingsBasedOnPreferences");
		// set View background color
		if(mContentView != null){
			ColourScheme col = new ColourScheme(mPrefs.getColourScheme());
			mContentView.setBackgroundColor(col.background());
		}
		// Set instrument for chord grids
		mSongCanvas.setChordGridInstrument(mGridInstrument);

	}
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu, android.view.View, android.view.ContextMenu.ContextMenuInfo)
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		// Don't display context menu if no song shown
		if(mSf!=null){
			super.onCreateContextMenu(menu, v, menuInfo);
			MenuInflater inflater = getActivity().getMenuInflater();
			inflater.inflate(R.menu.song_view_context, menu);
			menu.setHeaderTitle("Options");
			// If show grids is ON - only show grids OFF and vice versa
			if(showGrids){
				menu.removeItem(R.id.grids_on);
			}
			else{
				menu.removeItem(R.id.grids_off);
			}
			// Remove "Save Transposed" and "Revert to Original key" options if song not transposed
			if(mSongCanvas.getTranspose()==0){
				menu.removeItem(R.id.save_trans);
				menu.removeItem(R.id.orig_trans);
			}
			// Remove "save to text", option if text file
			if(!mSf.hasTitle){
				menu.removeItem(R.id.save_text);
			}
			if((Build.VERSION.SDK_INT<Build.VERSION_CODES.KITKAT)) {
				// Remove printing option if version < 19 (Kitkat)
				menu.removeItem(R.id.print);
			}
			if(mReadOnly){
				// Remove Edit/Save to Text/Save to Html, etc if mReadonly
				menu.removeItem(R.id.save_text);
				menu.removeItem(R.id.save_html);
				menu.removeItem(R.id.edit);
				menu.removeItem(R.id.save_trans);
				menu.removeItem(R.id.print);
				menu.removeItem(R.id.share_song);
			}
		}
		else{
			Log.d(TAG,"HELLO onCreateContextMenu- NOT now dear");
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		Log.d(TAG,"HELLO onContextItemSelected");
		if(item.getGroupId()!= R.id.context_group_song ){
			Log.d(TAG,"HELLO onContextItemSelected - NOT now dear");

			return super.onContextItemSelected(item);
		}
		Log.d(TAG,"HELLO onContextItemSelected");
		if (item.getItemId() == R.id.edit) {
			editSong(mSf.getSongFilePath());
			return true;
		} else if (item.getItemId() == R.id.save_trans) {
			saveTransposedSong(mSf.getSongFilePath());
			return true;
		} else if (item.getItemId() == R.id.orig_trans) {
			revertToOriginalKey();
			return true;
		} else if (item.getItemId() == R.id.save_text) {
			saveSongToText(mSf.getSongFilePath());
			return true;
		} else if (item.getItemId() == R.id.save_html) {
			saveSongToHTML(mSf.getSongFilePath());
			return true;
		} else if (item.getItemId() == R.id.print) {
			printSong(mSf.getSongFilePath(), mSf.hasTitle?mSf.getTitle():mSf.getSongFile());
			return true;
		} else if (item.getItemId() == R.id.grids_on) {
			showGrids = true;
			mSongCanvas.setShowGrids(true);
			return true;
		} else if (item.getItemId() == R.id.grids_off) {
			showGrids = false;
			mSongCanvas.setShowGrids(false);
			return true;
		} else if (item.getItemId() == R.id.share_song) {
			shareSong();
			return true;
		} else {
			Log.d(TAG,"HELLO UNKNOWN ID["+item.getItemId()+"]");
			return super.onContextItemSelected(item);
		}
	}


	@Override
	public void onSaveInstanceState(Bundle outState) {
		Log.d(TAG, "HELLO on SaveInstanceState scrollSpeed=["+mScrollSpeed+"]");
		outState.putInt(KEY_SCROLLSPEED, mScrollSpeed);
		outState.putBoolean(KEY_AUTOSCROLL, mAutoScroll);
		outState.putInt(KEY_TEXTSIZE, mCurrentTextSizeIndex);
		if( mSongCanvas != null){
			outState.putInt(KEY_TRANSPOSE, mSongCanvas.getTranspose());
			Log.d(TAG, "HELLO PUTTING TRANSPOSE="+mSongCanvas.getTranspose());
		}
		super.onSaveInstanceState(outState);
	}

	public void setScrollButtons(){
		if(mAutoScroll){
			plusButton.setVisibility(View.VISIBLE);
			minusButton.setVisibility(View.VISIBLE);
			scrollSpeedLabel.setVisibility(View.VISIBLE);
			scrollSpeedLabel.setText(mScrollSpeed+"");
			scrollButton.setText(getString(R.string.stop_scroll));
		}
		else{
			plusButton.setVisibility(View.INVISIBLE);
			minusButton.setVisibility(View.INVISIBLE);
			scrollSpeedLabel.setVisibility(View.INVISIBLE);
			scrollButton.setText(getString(R.string.auto_scroll));
		}
	}
	private void setPrevNextButtons(){
		if(mInSetList){
			prevButton.setVisibility(View.VISIBLE);
			nextButton.setVisibility(View.VISIBLE);
		}
		else{
			prevButton.setVisibility(View.INVISIBLE);
			nextButton.setVisibility(View.INVISIBLE);
		}
	}
	private void loadPreferences() {
		Log.d(TAG, "HELLO loadPreferences");
		// Restore preferences....
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
		// ...and load up into a SongPrefs object
		mPrefs.setTextSize(Integer.parseInt(settings.getString(SongPrefs.PREF_KEY_TEXTSIZE, "22")));
		mPrefs.setShowGrids(settings.getBoolean(SongPrefs.PREF_KEY_SHOWGRIDS, false));
		showGrids = mPrefs.isShowGridsOn();
		mGridInstrument=Integer.parseInt(settings.getString(SongPrefs.PREF_KEY_GRID_INSTRUMENT, "1"));
		mPrefs.setAutoScroll(settings.getBoolean(SongPrefs.PREF_KEY_AUTOSCROLL, false));
//		mPrefs.setTurboScroll(settings.getBoolean(SongPrefs.PREF_KEY_TURBOSCROLL, false));
        mPrefs.setScrollSpeedMultiplier(Integer.parseInt(settings.getString(SongPrefs.PREF_KEY_SCROLL_SPEED_FACTOR, "1")));
		mScrollDelay = Integer.parseInt(settings.getString(SongPrefs.PREF_KEY_SCROLL_DELAY, "0"));
		mScrollPreference = mAutoScroll = mPrefs.isAutoScrollOn();
		Log.d(TAG, "HELLO AUTOSCROLL=["+(mAutoScroll?"ON":"OFF")+"]");
//		mMinSleep = mPrefs.isTurboScrollOn()?MINSLEEP_TURBO:MINSLEEP_DEFAULT;
		mScrollBy = SCROLLBY_DEFAULT* mPrefs.getScrollSpeedMultiplier();
		//		mPrefs.setFullScreen(settings.getBoolean(SongPrefs.pref_key_fullscreen), false));
		mPrefs.setColourScheme(Integer.parseInt(settings.getString(SongPrefs.PREF_KEY_COLOURSCHEME, "1")));
		mPrefs.setMode(Integer.parseInt(settings.getString(SongPrefs.PREF_KEY_CHORDLYRIC_MODE, "0")));
		mPrefs.setDefaultEncoding(settings.getString(SongPrefs.PREF_KEY_DEFAULT_ENCODING, ""));
		mPrefs.setAddDashes(settings.getBoolean(SongPrefs.PREF_KEY_ADDDASHES, false));
		mPrefs.setHonourLFs(settings.getBoolean(SongPrefs.PREF_KEY_HONOURLFS, false));
		mPrefs.setInlineMode(Integer.parseInt(settings.getString(SongPrefs.PREF_KEY_INLINEMODE, "0")));

		loadTextSizes();
	}

	private void loadTextSizes(){
		// Work out where we are in text size list so that we can inc/dec text size
		if(mTextSizes == null){
			Log.d(TAG, "HELLO - getting textSizes");
			mTextSizes = this.getResources().getStringArray(R.array.text_size_values);
		}
	}

	private void getCurrentTextSizeIndex(){
		Log.d(TAG, "HELLO - setCurrentTextSizeIndex");
		// Get current position
		for(int i=0;i<mTextSizes.length;i++){
			if(mPrefs.getTextSize() == Integer.parseInt(mTextSizes[i])){
				mCurrentTextSizeIndex = i;
				Log.d(TAG, "HELLO - setCurrentTextSize ["+i+"]");
				break;
			}
		}
	}
	private void setTextSizeIndex(int currentTextSizeIndex){
		Log.d(TAG, "HELLO - setTextSizeIndex ["+currentTextSizeIndex+"]");
		// If current text size != mPrefs version then override mPrefs
		if(mPrefs.getTextSize() != Integer.parseInt(mTextSizes[currentTextSizeIndex])){
			mPrefs.setTextSize(Integer.parseInt(mTextSizes[currentTextSizeIndex]));
		}

	}
	/**
	 * Edit the current Song
	 */
	private void editSong(String songPath) {
		// Open the file with the EditSong Activity
		Intent myIntent = new Intent(getActivity(),	EditSong.class);
		try {
			// Put the path to the song file in the intent
			myIntent.putExtra(getString(R.string.song_path), songPath);
			mRequestCode = SongUtils.EDITSONG_REQUEST;// DIRTY HACK
			startActivityForResult(myIntent, mRequestCode);
		}
		catch (ActivityNotFoundException e) {
			errMsgToast(e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "HELLO onActivityResult 1["+requestCode+"]");
		if (requestCode == SongUtils.SETOPTIONS_REQUEST) {
			// If something has changed, re-load the preferences (which
			// SongOptions will have updated)
			reloadPreferences();
		}
		else if (mRequestCode == SongUtils.EDITSONG_REQUEST) {// DIRTY HACK
			if (resultCode == Activity.RESULT_OK) {
				Log.d(TAG, "HELLO onActivityResult 2");
				//The song has been saved so re-load the songfile
				try {
					mSf.reloadSong(mPrefs.getDefaultEncoding());
					mSongCanvas.setSong(mSf.getSong());
					//					setTitle(mSf.getTitleAndArtist());// We may have changed the name
					Log.d(TAG, "HELLO onActivityResult 3");
				} catch (ChordinatorException e) {
					Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
				}
			}
		}
	}


	public void shareSong(){
		if(mSf != null){
			File aFile = new File(mSf.getSongFilePath());
			Intent theIntent = new Intent(Intent.ACTION_SEND);
			theIntent.setType("text/plain");
			// TODO - need more intelligence about whether we want to share the raw chopro or
			// the formatted text.
			theIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(aFile));
			theIntent.putExtra(Intent.EXTRA_TEXT, mSf.getSong().getSongText());
			//next line specific to email attachments
			theIntent.putExtra(Intent.EXTRA_SUBJECT, "Sending " + aFile.getName());
			try {
				startActivity(Intent.createChooser(theIntent, "Share With...."));
			}
			catch (Exception e) {
                Log.d(TAG, "Oops");
			}
		}
	}

	private void saveTransposedSong(final String filePath){
		//		new Thread(new Runnable(){
		//			public void run(){
		// load up file and transpose the whole lot
		String newSong;
		try {
			newSong = Transpose.song(SongUtils.loadFile(filePath, null, mPrefs.getDefaultEncoding()),
					mSongCanvas.getTranspose());
			// then save it back to same file name
			SongUtils.writeFile(filePath, newSong);
			// and reload the song
			mSf.reloadSong(mPrefs.getDefaultEncoding());
			mSongCanvas.setSong(mSf.getSong());
			mSongCanvas.setTranspose(0);
			// Remove the preference (ignore any errors)
			removePreference(mSf.getTitle() + SongPrefs.PREF_KEY_TRANSPOSE);
		} catch (ChordinatorException e) {
			errMsgToast(e.getMessage());
		}
		//			}
		//		}).start();
	}

	private void revertToOriginalKey(){
		mSongCanvas.setTranspose(0);
		mSongCanvas.doInvalidate();
	}
	private void saveSongToText(final String filePath){
		boolean isnew=true;
		if(isnew){
			Log.d(TAG, "HELLO - new asynk save as text");
			SaveSongAsTextTask task = new SaveSongAsTextTask();
			task.execute(new String[] {filePath,mSongCanvas.getTranspose()+""});
		}
		else{
			// load up file and transpose the whole lot
			try {
				SongTextFormatter stf = new SongTextFormatter(mSf.getSong(), mSongCanvas.getTranspose());
				stf.formatSong(MAXLINE_LEN);
				// then save it back to same file name but with extension swapped for .txt
				String newFileName = SongUtils.swapSuffix(filePath, ".txt");

				SongUtils.writeFile(newFileName, stf.getFormattedSong());
				errMsgToast("Song " + mSf.getSong().getTitle()+" saved to:\n" + newFileName);
			} catch (ChordinatorException e) {
				errMsgToast(e.getMessage());
			}
		}
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// Go immersive if we are in a Set
		if(mInSetList){
			setFullScreen();
		}
	}

	private class SaveSongAsTextTask extends AsyncTask<String, Void, String> {
		private ProgressDialog progressDialog;
		String newFileName;
		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.save), true, false);
			super.onPreExecute();
		}
		@Override
		protected String doInBackground(String... args) {
			String response = "FAIL";

			String state = Environment.getExternalStorageState();
			if(state.equals(Environment.MEDIA_MOUNTED) ||
					state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)){
				response = doSaveAsText(args[0], Integer.parseInt(args[1]));
			}
			else{
				Log.d(TAG,"No Media Mounted!");
			}

			return response;
		}

		String doSaveAsText(String filePath, int transposeBy){
			String ret = "OK";
			try {
				SongTextFormatter stf = new SongTextFormatter(mSf.getSong(), mSongCanvas.getTranspose());
				stf.formatSong(MAXLINE_LEN);
				// then save it back to same file name but with extension swapped for .txt
				newFileName = SongUtils.swapSuffix(filePath, ".txt");

				SongUtils.writeFile(newFileName, stf.getFormattedSong());
			} catch (ChordinatorException e) {
				ret = e.getMessage();
			}
			return ret;
		}

		@Override
		protected void onPostExecute(String result) {
			if( result.equalsIgnoreCase("OK")){
				errMsgToast("Song " + mSf.getSong().getTitle()+" saved to:\n" + newFileName);
				// Update the browser listing if we are in dual pane mode
				songViewerListener.onNewFileCreated();
			}
			else{
				errMsgToast(result);
			}
			progressDialog.cancel();
		}
	}

	@SuppressWarnings("unused")
	private WebView mWebView;	// This is intentional
	private String mHtmlDocument;
	private void printSong(String filePath, final String title) {
		Log.d(TAG, "HELLO Printing: "+title);
	    // Create a WebView object specifically for printing
	    WebView webView = new WebView(getActivity());
	    webView.setWebViewClient(new WebViewClient() {

	            public boolean shouldOverrideUrlLoading(WebView view, String url) {
	                return false;
	            }

	            @Override
	            public void onPageFinished(WebView view, String url) {
	                createWebPrintJob(view, title);
	                mWebView = null;
	            }
	    });

	    // Generate an HTML document on the fly:
	    mHtmlDocument = convertSongToHtml(filePath);
	    Log.d(TAG, "HELLO HTML: ["+mHtmlDocument+"]");
	    webView.loadDataWithBaseURL(null, mHtmlDocument, "text/HTML", "UTF-8", null);

	    // Keep a reference to WebView object until you pass the PrintDocumentAdapter
	    // to the PrintManager
	    mWebView = webView;
	}

	private String convertSongToHtml(String filePath){
		SongTextFormatter stf;

		// load up file and transpose the whole lot
		if(!mSf.hasTitle){
			// If the song is a text file then needs different handling - no title so use file name
			stf = new TextSongHTMLFormatter(mSf.getSong(), mSongCanvas.getTranspose(), mSf.getSongFile());
		}
		else{
			stf = new SongHTMLFormatter(mSf.getSong(), mSongCanvas.getTranspose());
		}
		stf.formatSong(MAXLINE_LEN);
		// return html
		return stf.getFormattedSong();
	}

	@TargetApi(Build.VERSION_CODES.KITKAT)
	private void createWebPrintJob(WebView webView, String title) {

	    // Get a PrintManager instance
	    PrintManager printManager = (PrintManager) getActivity()
	            .getSystemService(Context.PRINT_SERVICE);

	    // Get a print adapter instance
	    PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter();

	    // Create a print job with name and adapter instance
	    String jobName = title;
	    PrintJob printJob = printManager.print(jobName, printAdapter,
	            new PrintAttributes.Builder().build());

	    // Save the job object for later status checking
//	    mPrintJobs.add(printJob); // Don't need this (probably)
	}
	private void saveSongToHTML(final String filePath){
		try {
			// Save converted song back to same file name +.html
			SongUtils.writeFile(SongUtils.swapSuffix(filePath, ".html"), convertSongToHtml(filePath));
			errMsgToast("Song " + mSf.getSong().getTitle()+" saved to:\n" + SongUtils.swapSuffix(filePath, ".html"));
		} catch (ChordinatorException e) {
			errMsgToast(e.getMessage());
		}
	}

	private void errMsgToast(String msg){
		Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
	}


	public void setPreference( String key, int value){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());

		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(key, value);
		// Commit the edits!
		editor.commit();
	}

	public void removePreference( String key ){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());

		SharedPreferences.Editor editor = settings.edit();
		editor.remove(key);
		// Commit the edits!
		editor.commit();
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.minusButton) {
			Log.d(TAG, "HELLO - minus button");
			updateScrollSpeed(false);
		}
		else if (view.getId() == R.id.plusButton) {
			updateScrollSpeed(true);
		}
		else if (view.getId() == R.id.scrollButton) {
			toggleScroll();
		}
		else if (view.getId() == R.id.nextButton) {
			songViewerListener.nextSong();
		}
		else if (view.getId() == R.id.prevButton) {
			songViewerListener.prevSong();
		}
		else if (view.getId() == R.id.biggerButton) {
			biggerText();
		}
		else if (view.getId() == R.id.smallerButton) {
			smallerText();
		}
		else if (view.getId() == R.id.trupButton) {
			mSongCanvas.transposeSong(true);
		}
		else if (view.getId() == R.id.trdownButton) {
			mSongCanvas.transposeSong(false);
		}
	}
	private void biggerText() {
		Log.d(TAG, "HELLO biggerText1");
		if( mCurrentTextSizeIndex<mTextSizes.length-1){
			mPrefs.setTextSize(Integer.parseInt(mTextSizes[++mCurrentTextSizeIndex]));
			Log.d(TAG, "HELLO biggerText2");
			mSongCanvas.setPreferences(mPrefs);
			Log.d(TAG, "HELLO biggerText3");
			smallerButton.setEnabled(true);//avoid buttons staying disabled
		}
		biggerButton.setEnabled((mCurrentTextSizeIndex==mTextSizes.length-1)?false:true);
		Log.d(TAG, "HELLO biggerText4");
	}

	private void smallerText() {
		Log.d(TAG, "HELLO smallerText");
		if( mCurrentTextSizeIndex>0){
			mPrefs.setTextSize(Integer.parseInt(mTextSizes[--mCurrentTextSizeIndex]));
			mSongCanvas.setPreferences(mPrefs);
			biggerButton.setEnabled(true);//avoid buttons staying disabled
		}
		smallerButton.setEnabled((mCurrentTextSizeIndex==0)?false:true);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	public void onPause() {
		Log.d(TAG,"HELLO onPause");
		if(mAutoScroll){
			setAutoScroll(false,0);
		}
		super.onPause();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	public void onResume() {
		Log.d(TAG,"HELLO onResume");
		if(mAutoScroll){
			setAutoScroll(true, mScrollDelay);
		}
		super.onResume();
	}

	@Override
	public void onStop() {
		Log.d(TAG,"HELLO onStop");
		if(mAutoScroll){
			setAutoScroll(false, 0);
		}
		super.onStop();
	}
	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	public void onDestroy() {
		Log.d(TAG,"HELLO onDestroy");
		super.onDestroy();
	}

	/**
	 * Create a simple handler that we can use to cause animation to happen.  We
	 * set ourselves as a target and we can use the sleep()
	 * function to cause an update/invalidate to occur at a later date.
	 */
	private RefreshHandler mRedrawHandler;
	private WeakReference<SongViewerFragment> mFragment;

	private final int SCROLLBY_DEFAULT = 1;
//	private final int SCROLLBY_TURBO = 2;
	private int mScrollBy=SCROLLBY_DEFAULT;

	static class RefreshHandler extends Handler {
		int scrollDelayLeft=0;
		SongViewerFragment myFrag;

		public RefreshHandler(SongViewerFragment songViewerFragment) {
			myFrag = songViewerFragment;
		}

		@Override
		public void handleMessage(Message msg) {
			myFrag.updateScroll(scrollDelayLeft);
		}

		public void sleep(long delayMillis) {
			this.removeMessages(0);
			sendMessageDelayed(obtainMessage(0), delayMillis);
		}
		/**
		 * Use this when counting down the delay before scrolling
		 * @param delayMillis
		 * @param scrollDelaySecs
		 */
		public void sleep(long delayMillis, int scrollDelaySecs) {
			scrollDelayLeft = scrollDelaySecs;
			this.removeMessages(0);
			// Wait for 1 second if we are on a scroll delay count down
			sendMessageDelayed(obtainMessage(0), delayMillis);
		}
	}
	/**
	 * Handles the basic autoScroll loop, checking to see if we are scrolling
	 * and setting the timer for the next scroll.
	 */
//	long lastUptime=0;
	private void updateScroll(int scrollDelay) {
//		long thisUptime=0;
		if (mAutoScroll) {
			// If scrollDelay specified then have a pause before 1st scroll
			if(scrollDelay==0){
				scrollSpeedLabel.setText(mScrollSpeed+"");
//				thisUptime=android.os.SystemClock.uptimeMillis();
//				Log.d(TAG, "HELLO scrollSleep=["+scrollSleep+"] mScrollBy=["+mScrollBy+"] DIFF=["+(thisUptime-lastUptime)+"] uptime=["+thisUptime+"]");
//				lastUptime=thisUptime;
				scroller.smoothScrollBy(0,mScrollBy);
				mRedrawHandler.sleep(scrollSleep);
			}
			else{
				scrollSpeedLabel.setText("-"+scrollDelay);
				mRedrawHandler.sleep(1000, scrollDelay-1);
			}
		}
	}
	/**
	 * Stops or starts autoscrolling
	 * @param as
	 * @param scrollDelay
	 */
	public void setAutoScroll(boolean as, int scrollDelay){
		mAutoScroll = as;
		if( mAutoScroll ){
			setScrollSleep();
			updateScroll(scrollDelay);
		}
		setScrollButtons();
	}
	private void setScrollSleep(){
		scrollSleep = MINSLEEP_DEFAULT +(MAXSPEED - mScrollSpeed)*SCROLL_INC;
	}

	private void setScrollSpeed(int speed){
		mScrollSpeed = speed;
		setScrollSleep();
	}
	public void updateScrollSpeed(boolean up){
		if(up){
			if(mScrollSpeed < MAXSPEED)mScrollSpeed+=1;
		}
		else{
			if(mScrollSpeed > MINSPEED)mScrollSpeed-=1;
		}
		setScrollSleep();
		scrollSpeedLabel.setText(mScrollSpeed+"");
	}
	private class UpdateSongDBTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... args) {
			String response = "";
			if (mSongId != 0 || 
					(mSongId = DBUtils.getSongIdFromPath(getActivity().getContentResolver(), getString(R.string.authority),
							mSf.getSongPath(),mSf.getSongFile())) != 0) {
				DBUtils.updateSong(getActivity().getContentResolver(), getString(R.string.authority), mSongId, mSf.getTitle(), mSf.getArtist(), mSf.getComposer());
			}
			else{
				//Must have been launched by the browser screen
				// Only add to the DB if there is a {title} tag
				if(mSf.hasTitle){
					// This song isn't in the DB yet so add it in
					DBUtils.addSong(getActivity().getContentResolver(), getString(R.string.authority), mSf.getSongPath(),
							mSf.getSongFile(), mSf.getTitle(), mSf.getArtist(), mSf.getComposer());
				}
				else{
					Log.d(TAG, "HELLO NO TITLE");
				}
			}
			return response;
		}

		@Override
		protected void onPostExecute(String result) {
			//			textView.setText(result);
		}
	}


	private void UpdateSongDB() {
		Log.d(TAG, "HELLO UpdateSongDB");
		UpdateSongDBTask task = new UpdateSongDBTask();
		task.execute(new String[] {});
	}

	public void reloadPreferences(){
		loadPreferences();
		///
		Log.d(TAG, "HELLO current size = ["+mTextSizes[mCurrentTextSizeIndex]+"]");
		Log.d(TAG, "HELLO - default text size B4=["+mDefaultTextSize+"] AFTER=["+mPrefs.getTextSize()+"]");
		// IF we changed the text size (i.e. in SetOptions screen) then don't try and restore the
		// text size from the zoom size
		if(mPrefs.getTextSize() == mDefaultTextSize){
			// We didn't change the default Text Size so OK to override with current size
			Log.d(TAG, "HELLO setting Text Size");
			setTextSizeIndex(mCurrentTextSizeIndex);
			Log.d(TAG, "HELLO current size = ["+mTextSizes[mCurrentTextSizeIndex]+"]");
		}
		else{
			mDefaultTextSize = mPrefs.getTextSize();
			getCurrentTextSizeIndex();
			// May need to re-enable/disable text size buttons.
			biggerButton.setEnabled((mCurrentTextSizeIndex==mTextSizes.length-1)?false:true);
			smallerButton.setEnabled((mCurrentTextSizeIndex==0)?false:true);
		}

		//
		setScrollButtons();
		settingsBasedOnPreferences();
		// .. and then reload the new preferences into the canvas and
		// force a redraw
		mSongCanvas.setPreferences(mPrefs);

	}

    @SuppressLint("InlinedApi")
	private void setFullScreen(){
    	Log.d(TAG, "HELLO setFullScreen BUILD=["+Build.VERSION.SDK_INT+"}");
    	if (Build.VERSION.SDK_INT >= 18) {
    		int uiOptions = getActivity().getWindow().getDecorView().getSystemUiVisibility();
    		uiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
    		uiOptions |= View.SYSTEM_UI_FLAG_FULLSCREEN;
    		uiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
    		getActivity().getWindow().getDecorView().setSystemUiVisibility(uiOptions);
    	}
    }
	private boolean isDim(){
		return(getString(R.string.app_version).equalsIgnoreCase("dim")?true:false);
	}
	public void toggleScroll(){
		mAutoScroll = (mAutoScroll?false:true);// Toggle Autoscroll
		setAutoScroll(mAutoScroll, mScrollDelay);
	}
}