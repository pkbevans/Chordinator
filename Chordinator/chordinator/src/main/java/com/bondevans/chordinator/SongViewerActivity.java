package com.bondevans.chordinator;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.bondevans.chordinator.dialogs.HelpFragment;
import com.bondevans.chordinator.prefs.ChordinatorPrefsActivity;
import com.bondevans.chordinator.setlist.SetList;
import com.bondevans.chordinator.setlist.SetList2;
import com.bondevans.chordinator.setlist.SetSong;
import com.bondevans.chordinator.utils.Ute;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;

public class SongViewerActivity extends AppCompatActivity implements SongViewerFragment.SongViewerListener{
	private static final int SETOPTIONS_ID = Menu.FIRST + 6;
	private static final int SHOWHELP_ID = Menu.FIRST + 7; 
	private static final int TRANSPOSEUP_ID = Menu.FIRST + 8; 
	private static final int TRANSPOSEDOWN_ID = Menu.FIRST + 9; 
	private static final int SHARESONG_ID = Menu.FIRST + 10;
	private static final String TAG = "SongViewerActivity";
	public static final String INTENT_SONGID = "com.bondevans.chordinator.songId";
	public static final String INTENT_INSET = "com.bondevans.chordinator.inSetList";
	public static final String INTENT_SETID = "com.bondevans.chordinator.setId";
	private static final String KEY_SONGID = "dgsfgdcxc";
	private static final String KEY_FILENAME = "fdsxceke";
	SongViewerFragment mViewer; 
	private SetList mSetList1;
	private SetList2 mSetList2;
	private long mSongId=0;
	private String mFileName;
	private int mColourScheme;
	FileDescriptor mFileDescriptor;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		mColourScheme = Ute.getColourScheme(this);
		setTheme(mColourScheme == ColourScheme.LIGHT? R.style.Chordinator_Light_Theme_Theme: R.style.Chordinator_Dark_Theme_Theme);
		boolean inSetList;

		super.onCreate(savedInstanceState);
		setContentView(R.layout.songview_fragment);

        Intent myIntent = getIntent();
		// See if we are in a set list - if so set up the prev and next buttons
		inSetList = myIntent.getBooleanExtra(INTENT_INSET, false);
		long setId = myIntent.getLongExtra(INTENT_SETID, 0);
		// See if we got the rowID of the Song record
		mSongId = savedInstanceState == null?myIntent.getLongExtra(INTENT_SONGID, 0): savedInstanceState.getLong(KEY_SONGID);
        Log.d(TAG, "INTENT data:" + myIntent.getData().toString());
        Log.d(TAG, "INTENT path:" + myIntent.getData().getPath());
        Log.d(TAG, "INTENT host:" + myIntent.getData().getHost());
        Log.d(TAG, "INTENT scheme:" + myIntent.getData().getScheme());
        Log.d(TAG, "INTENT port:" + myIntent.getData().getPort());
        Log.d(TAG, "INTENT authority:" + myIntent.getData().getAuthority());
        Log.d(TAG, "INTENT ssp:" + myIntent.getData().getSchemeSpecificPart());
        Log.d(TAG, "INTENT fragment:" + myIntent.getData().getFragment());
		if(myIntent.getScheme().equalsIgnoreCase("content")){
			Log.d(TAG, "CONTENT");
			// Get the URI for the content
			Uri uri = myIntent.getData();
			Log.d(TAG, "CONTENT - GOT uri"+uri.toString());
			ParcelFileDescriptor parcelFileDescriptor = null;
			try{
				// Get a parcelFileDescriptor from the URI
				parcelFileDescriptor = getContentResolver().openFileDescriptor(uri, "r");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				Log.d(TAG, "File not found: "+e.getMessage());
				Toast.makeText(this, R.string.cant_open_file, Toast.LENGTH_LONG).show();
				finish();
			}
			Log.d(TAG, "CONTENT - GOT ParcelFileDescriptor");
			// Get a regular file descriptor
			mFileDescriptor = parcelFileDescriptor.getFileDescriptor();
			Log.d(TAG, "CONTENT - GOT FileDescriptor");
			mFileName = "";
			Toast.makeText(this, R.string.read_only_file, Toast.LENGTH_LONG).show();
		}
		else{
			mFileName = savedInstanceState == null?myIntent.getData().getPath(): savedInstanceState.getString(KEY_FILENAME);
		}
		// If we have got a New-style set then load up a SetList2 and set the current position in it
		if( setId > 0){
			inSetList=true;
			mSetList2 = new SetList2(getContentResolver(), getString(R.string.authority), setId);
			// get song position 
			mSetList2.setCurrentSong(mSongId);
		}
		else if(mFileName.contains("/"+SetList.SETLIST_PREFIX)){
			Log.d(TAG, "HELLO - Old-style SET");
			// Old-style set list opened from Browser
			try {
				mSetList1 = new SetList(null, mFileName);
				mFileName = mSetList1.getFirstSong().getPath();
				Log.d(TAG, "HELLO - Old-style SET: ["+mFileName+"]");
				inSetList=true;
			} catch (Exception e) {
				SongUtils.toast(this, e.getMessage());
				e.printStackTrace();
			} catch (Throwable e) {
				// Auto-generated catch block
				e.printStackTrace();
			}
		}

		mViewer = (SongViewerFragment) getSupportFragmentManager()
				.findFragmentById(R.id.songview_fragment);

		mViewer.setSong(inSetList, mSongId, mFileName, mFileDescriptor);

		// Hide the action bar - unless we are on a large tablet with no physical buttons
		Configuration conf = getResources().getConfiguration();
		int layout = conf.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
		Log.d(TAG, "HELLO - layout = ["+layout+"]");
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar); // Attaching the layout to the toolbar object
        if(toolbar != null){
            setSupportActionBar(toolbar);                   // Setting toolbar as the ActionBar with setSupportActionBar() call
			// Set up the ActionBar correctly
			setUpActionBar();
		}
    }
	public void setUpActionBar(){
		getSupportActionBar().setLogo(mColourScheme == ColourScheme.DARK? R.drawable.chordinator_aug_logo_dark_bkgrnd: R.drawable.chordinator_aug_logo_light_bkgrnd);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// This is the MENU button menu
		menu.add(0, SHARESONG_ID, 7, getString(R.string.share_song))
		.setIcon(R.drawable.ic_menu_share);

		menu.add(0, SHOWHELP_ID, 8, getString(R.string.help))
		.setIcon(R.drawable.ic_menu_help);

		menu.add(0, SETOPTIONS_ID, 9, getString(R.string.set_options))
		.setIcon(R.drawable.ic_menu_preferences);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// This is the MENU button menu
		return super.onPrepareOptionsMenu(menu);
	}

	// MENU Button pressed and option selected
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean handled=false;

		switch (item.getItemId()) {
		case android.R.id.home:
			exitSong(RESULT_OK);
			break;
		case SHARESONG_ID: 
			mViewer.shareSong();
			handled = true;
			break;
		case SHOWHELP_ID:
			showHelp();
			handled = true;
			break;
		case SETOPTIONS_ID:
			setPreferences();
			handled = true;
			break;
		case TRANSPOSEUP_ID:
			mViewer.mSongCanvas.transposeSong(true);
			handled = true;
			break;
		case TRANSPOSEDOWN_ID:
			mViewer.mSongCanvas.transposeSong(false);
			handled = true;
			break;
		default:
			break;
		}
		return handled;
	}
	private void showHelp() {
		HelpFragment newFragment = HelpFragment.newInstance(HelpFragment.HELP_PAGE_SONGVIEWER);
		newFragment.show(getSupportFragmentManager(), "dialog");
	}
	private void setPreferences(){
		Intent myIntent = new Intent(this, ChordinatorPrefsActivity.class);
		try {
			startActivityForResult(myIntent, SongUtils.SETOPTIONS_REQUEST);
		} catch (ActivityNotFoundException e) {
			SongUtils.toast(this, "ChordinatorPrefsActivity not found");
		}
	}


	@Override
	/*
	 * Handle non-menu keyboard events
	 */
	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		Log.d(TAG, "HELLO onKeyDown ["+keyCode+"]");
		switch (keyCode) {
		case KeyEvent.KEYCODE_PAGE_DOWN:
			Log.d(TAG, "HELLO PAGEDOWN");
			break;
		case KeyEvent.KEYCODE_PAGE_UP:
			Log.d(TAG, "HELLO PAGEUP");
			break;
		case KeyEvent.KEYCODE_DPAD_DOWN:
			Log.d(TAG, "HELLO DOWN");
			break;
		case KeyEvent.KEYCODE_DPAD_UP:
			Log.d(TAG, "HELLO UP");
			break;
		case KeyEvent.KEYCODE_DPAD_LEFT:
			Log.d(TAG, "HELLO LEFT");
			// Left key
			//			mViewer.mSongCanvas.transposeSong(false);
			prevSong();
			break;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			Log.d(TAG, "HELLO RIGHT");
			// Right key - transpose up
			//			mViewer.mSongCanvas.transposeSong(true);
			nextSong();
			break;
		case KeyEvent.KEYCODE_BACK:
			Log.d(TAG, "HELLO - BACK PRESSED");
			exitSong(RESULT_OK);
			super.onKeyDown(keyCode, event);
			return true;
		}
		return false;
	}

	private void exitSong(int result) {
		Log.d(TAG, "HELLO exitSong");
		// Check whether there is actually a song being viewed before saving song settings
		if(mViewer.mSf != null){
			mViewer.savePreviousSongSettings();
		}
		//Switch off the autoScroll timer whatever happens
		mViewer.setAutoScroll(false,0);
		// Finish this activity, which will take us back to the song
		// browser.
		this.setResult(result);
//		this.finish();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "HELLO onActivityResult 1["+requestCode+"]");
		mViewer.onActivityResult(requestCode, resultCode, data);
    }
	/* (non-Javadoc)
	 * @see android.app.Activity#onRestart()
	 */
	@Override
	protected void onRestart() {
		if(mViewer.mAutoScroll){
			Log.d(TAG,"HELLO restarting");
			mViewer.setAutoScroll(true, mViewer.mScrollDelay);
		}
		super.onRestart();
	}
	@Override
	public void onNewFileCreated() {
		// Do nothing if SongViewer in its own activity
	}

	public void nextSong(){
		Log.d(TAG, "HELLO nextSong");
		if(mSetList1 != null){
			mFileName=mSetList1.getNextSong().getPath();
			Log.d(TAG, "HELLO nextSong ["+mFileName+"]");
			mViewer.setSong(true, 0, mFileName, mFileDescriptor);
		}
		else if(mSetList2 != null){
			// Get next Song
			try {
				SetSong song = mSetList2.getNextSong();
				mSongId=song.id;
				mFileName=song.filePath;
				Log.d(TAG, "HELLO nextSong ["+song.title+"]");
				mViewer.setSong(true, song.id, song.filePath, mFileDescriptor);
			} catch (ChordinatorException e) {
				e.printStackTrace();
			}
		}
	}

	public void prevSong(){
		Log.d(TAG, "HELLO prevSong");
		if(mSetList1 != null){
			mFileName=mSetList1.getPrevSong().getPath();
			Log.d(TAG, "HELLO prevSong ["+mFileName+"]");
			mViewer.setSong(true, 0, mFileName, mFileDescriptor);
		}
		else if(mSetList2 != null){
			try {
				SetSong song = mSetList2.getPrevSong();
				mSongId=song.id;
				mFileName=song.filePath;
				Log.d(TAG, "HELLO prevSong ["+song.title+"]");
				mViewer.setSong(true, song.id, song.filePath, mFileDescriptor);
			} catch (ChordinatorException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		Log.d(TAG, "HELLO on SaveInstanceState");
		outState.putLong(KEY_SONGID, mSongId);
		outState.putString(KEY_FILENAME, mFileName);
		super.onSaveInstanceState(outState);
	}

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "HELLO FLIC onResume");
        // Register to receive messages.
        // We are registering an observer (mFlicReceiver) to receive Intents
        // with actions named "custom-event-name".
        LocalBroadcastManager.getInstance(this).registerReceiver(mFlicReceiver,
                new IntentFilter(MyFlicReceiver.FLIC_INTENT));
    }

    public void onPause() {
        Log.d(TAG, "HELLO FLIC onPause");
        if(mFlicReceiver != null) try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mFlicReceiver);
        } catch (Exception e){
            Log.d(TAG, "HELLO FLIC - CAUGHT ERROR: "+e.getMessage());
        }
        super.onPause();
    }
    // Our handler for received Intents. This will be called whenever an Intent
    // with an action named "custom-event-name" is broadcasted.
    private BroadcastReceiver mFlicReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            int event = intent.getIntExtra(MyFlicReceiver.FLIC_MESSAGE, 0);
            Log.d(TAG, "HELLO FLIC Got event: " + event);
            mViewer = (SongViewerFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.songview_fragment);

            if(mViewer != null){
                if(event == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
					mViewer.toggleScroll();
                }
                else if(event == KeyEvent.KEYCODE_MEDIA_NEXT) {
                    nextSong();
                }
                else if(event == KeyEvent.KEYCODE_MEDIA_PREVIOUS) {
                    prevSong();
                }
//				else if(event == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD) {
//				}
//				else if(event == KeyEvent.KEYCODE_MEDIA_REWIND) {
//				}
                else{
                    Log.d(TAG, "HELLO FLIC - IGNORING UNKNOWN MESSAGE: "+event);
                }
            }
            else{
                Log.d(TAG,"HELLO FLIC - mViewer null");
            }
        }
    };
}
