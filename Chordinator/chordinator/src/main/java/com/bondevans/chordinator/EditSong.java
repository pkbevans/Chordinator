package com.bondevans.chordinator;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;

import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.bondevans.chordinator.prefs.SongPrefs;
import com.bondevans.chordinator.utils.Ute;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class EditSong extends AppCompatActivity {
	private final static String TAG = "EditSong";
	//	private static final int DIALOG_ID_SAVE = 1;
	private static final int ADDTAG_ID = Menu.FIRST+1;
	private static final int ADDCHORD_ID = Menu.FIRST+2;
	private static final int ADDCOMMENT_ID = Menu.FIRST+3;
	private static final int ADDSOC_ID = Menu.FIRST+4;
	private static final int ADDEOC_ID = Menu.FIRST+5;
	private static final int ADDRC_ID = Menu.FIRST+6;
	private static final int ADDSOT_ID = Menu.FIRST+7;
	private static final int ADDEOT_ID = Menu.FIRST+8;
	public static final int ADDTITLE_ID = Menu.FIRST+9;
	public static final int ADDSUBTITLE_ID = Menu.FIRST+10;

	private static final String SAVE_FILE = "saveFile";

	CABEditText mSongText; //KITKAT WORKAROUND
	String mFilePath;
	private boolean mTextChanged = false;
	private TextWatcher textWatcher = new myTextWatcher();

	@TargetApi(11)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(Ute.getColourScheme(this) == ColourScheme.LIGHT? R.style.Chordinator_Light_Theme_Theme: R.style.Chordinator_Dark_Theme_Theme);
		super.onCreate(savedInstanceState);

		setContentView(R.layout.edit_song);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar); // Attaching the layout to the toolbar object
        setSupportActionBar(toolbar);                   // Setting toolbar as the ActionBar with setSupportActionBar() call

        getSupportActionBar().setTitle(R.string.edit_song);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSongText = (CABEditText) findViewById(R.id.song_text);
		if(isDim()){
			// Diminished Only  - Load an ad
	        // Gets the ad view defined in layout/ad_fragment.xml with ad unit ID set in
	        // values/strings.xml.
			AdView mAdView = (AdView) findViewById(R.id.adView);
	        if(mAdView != null){
		        // Start loading the ad in the background.
		        mAdView.loadAd(new AdRequest.Builder().build());
	        }
		}
		// Get the full file path to the chosen song from the Intent
		mFilePath = getIntent().getStringExtra(getString(R.string.song_path));
		// Load up song and put it in the EditText
		try {
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
			// ...and load up into a SongPrefs object, replacing any DOS-style CR/LF pairs with a single UNIX-style LF
			mSongText.setText(SongUtils.loadFile(mFilePath, null, settings.getString(SongPrefs.PREF_KEY_DEFAULT_ENCODING, "")).replaceAll("\r\n", "\n"));
		} catch (ChordinatorException e) {
			errMsgToast(e.getMessage());
			this.finish();
		}

		mSongText.setEnabled(true);
		mSongText.setClickable(true);

		mSongText.addTextChangedListener(textWatcher);
		if((Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB)) {
				mSongText.setCustomSelectionActionModeCallback(new EditActionCallback());
		}
	}
	private void errMsgToast(String msg){
		Toast.makeText(EditSong.this, msg, Toast.LENGTH_LONG).show();
	}

	public void saveSong(){
		writeSong();
		this.setResult(RESULT_OK);
		this.finish();
	}

	private void writeSong(){
		//Write out to file in a separate thread
		try {
			Log.d(TAG,"saving file:"+mFilePath);
			SongUtils.writeFile(mFilePath, mSongText.getText().toString());
		} catch (Exception e) {
			errMsgToast(e.getMessage());
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && mTextChanged) {
			Log.d(TAG, "HELLO - BACK PRESSED and SONG CHANGED");
			showSaveFileDialog();
			return true;
		}
		Log.d(TAG, "HELLO - BACK PRESSED2");
		return super.onKeyDown(keyCode, event);
	}

	private class myTextWatcher implements TextWatcher{
		@Override
		public void afterTextChanged(Editable s) {
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,int count) {
			mTextChanged = true;
		}
	}
	@TargetApi(Build.VERSION_CODES.HONEYCOMB) @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d(TAG, "onOptionsItemSelected");
		switch (item.getItemId()) {
		case android.R.id.home:
			// Back to Songs
			if (mTextChanged) {
				Log.d(TAG, "HELLO - HOME PRESSED and SONG CHANGED");
				showSaveFileDialog();
				return true;
			}
			else{
				finish();
			}
			break;
		case ADDTAG_ID:
			if((Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB)) {
				startActionMode(new TagActionMode());
			}
			break;
		case ADDCHORD_ID:
			insertChord();
			break;
		case ADDCOMMENT_ID:
			insertComment();
			break;
		}
		return false;
	}

	@TargetApi(11)
	private class EditActionCallback implements ActionMode.Callback{
		@Override
		public boolean onPrepareActionMode(ActionMode mode, android.view.Menu menu) {
			mSongText.setWindowFocusWait(true);	//KITKAT WORKAROUND
			return false;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mSongText.setWindowFocusWait(false); //KITKAT WORKAROUND
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, android.view.Menu menu) {
			// These options will be displayed when the user has selected some text
			menu.add(0, ADDCOMMENT_ID, 1, "{c:}")
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

			menu.add(0, ADDCHORD_ID, 1, "[]")
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

			return true;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode,
				android.view.MenuItem item) {
			if(item.getItemId() == ADDCHORD_ID){
				insertChord();

				return true;
			}
			else if(item.getItemId() == ADDCOMMENT_ID){
				insertComment();
				return true;
			}
			return false;
		}

	}
	private void showSaveFileDialog(){

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		Fragment prev = getSupportFragmentManager().findFragmentByTag(SAVE_FILE);
		if (prev != null) {
			ft.remove(prev);
		}
		SaveFileDialog newFragment = SaveFileDialog.newInstance();
		newFragment.show(ft, SAVE_FILE);
	}

	public static class SaveFileDialog extends DialogFragment{

		/**
		 * Empty constructor
		 */
		static SaveFileDialog newInstance(){
			SaveFileDialog frag = new SaveFileDialog();
			Bundle args = new Bundle();
			frag.setArguments(args);
			return frag;
		}
		/* (non-Javadoc)
		 * @see android.support.v4.app.DialogFragment#onCreateDialog(android.os.Bundle)
		 */
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return new AlertDialog.Builder(getActivity())
			.setMessage(R.string.save_file)
			.setCancelable(false)
			.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					((EditSong)getActivity()).saveSong();
				}
			})
			.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
					((EditSong)getActivity()).finish();
				}
			})
			.create();
		}
	}

	private void insertBrackets(String open, String close){
		// Get the position of the caret
		int start = mSongText.getSelectionStart();
		int end = mSongText.getSelectionEnd();
		Log.d(TAG, "insertBrackets ["+start+"]["+end+"]");
		// insert "[]"
		if(start>-1){
			mSongText.setText(mSongText.getText().insert(start, open).insert(end+open.length(), close));
			mSongText.setSelection(start+open.length());
		}
	}

	private void insertChord(){
		insertBrackets("[","]");
	}

	private void insertComment(){
		insertBrackets("{c:","}");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// This is the MENU button menu
		MenuItem menuItem;
		if((Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB)){
			menuItem = menu.add(0, ADDTAG_ID, 1, "Tags..");
			MenuItemCompat.setShowAsAction(menuItem, MenuItem.SHOW_AS_ACTION_ALWAYS);
		}

		menuItem = menu.add(0, ADDCOMMENT_ID, 1, "{c:}");
		MenuItemCompat.setShowAsAction(menuItem, MenuItem.SHOW_AS_ACTION_ALWAYS);

		menuItem = menu.add(0, ADDCHORD_ID, 2, "[]");
		MenuItemCompat.setShowAsAction(menuItem, MenuItem.SHOW_AS_ACTION_ALWAYS);
		return super.onCreateOptionsMenu(menu);
	}
	
	@TargetApi(11)
	private class TagActionMode implements ActionMode.Callback{

		@Override
		public boolean onActionItemClicked(ActionMode mode,
				android.view.MenuItem item) {

			if(item.getItemId() == ADDSOC_ID){
				insertBrackets("{soc}","");
				return true;
			}
			else if(item.getItemId() == ADDEOC_ID){
				insertBrackets("{eoc}","");
				return true;
			}
			else if(item.getItemId() == ADDRC_ID){
				insertBrackets("{rc}","");
				return true;
			}
			else if(item.getItemId() == ADDSOT_ID){
				insertBrackets("{sot}","");
				return true;
			}
			else if(item.getItemId() == ADDEOT_ID){
				insertBrackets("{eot}","");
				return true;
			}
			else if(item.getItemId() == ADDTITLE_ID){
				insertBrackets("{t:","}");
				return true;
			}
			else if(item.getItemId() == ADDSUBTITLE_ID){
				insertBrackets("{st:","}");
				return true;
			}
			return false;
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode,
				android.view.Menu menu) {
			// {soc}, {eoc}, {rc}, {sot}, {eot}, {t:}, {st:}

			menu.add(0, ADDSOC_ID, Menu.NONE, "{soc}")
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

			menu.add(0, ADDEOC_ID, Menu.NONE, "{eoc}")
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

			menu.add(0, ADDRC_ID, Menu.NONE, "{rc}")
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

			menu.add(0, ADDSOT_ID, Menu.NONE, "{sot}")
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

			menu.add(0, ADDEOT_ID, Menu.NONE, "{eot}")
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

			menu.add(0, ADDTITLE_ID, Menu.NONE, "{t:}")
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

			menu.add(0, ADDSUBTITLE_ID, Menu.NONE, "{st:}")
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode,
				android.view.Menu menu) {
			return false;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
		}
	}
	private boolean isDim(){
		return(getString(R.string.app_version).equalsIgnoreCase("dim")?true:false);
	}
}

