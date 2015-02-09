package com.bondevans.chordinator.prefs;

import java.nio.charset.Charset;
import java.util.SortedMap;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.bondevans.chordinator.R;
import com.bondevans.chordinator.dialogs.HelpFragment;

public class EncodingPrefs extends FragmentActivity {
	private static final int SHOWHELP_ID = Menu.FIRST; 
	private Spinner def_encoding;
	private static final String DEFAULT = "<DEFAULT>";
	boolean mConverted=false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.preferences_encoding);
		// Get preferences
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		
//		this.setTitle("Set Preferences");
		// SELECT ENCODING FROM DROP DOWN LIST
		final SortedMap<String, Charset> map = Charset.availableCharsets();
		String [] encodings = new String[map.size()+1];
		int i=0;
		encodings[i++]=DEFAULT;
		for(String charsetName : map.keySet()){
			final Charset charset = Charset.forName(charsetName);
			encodings[i++] = charset.name();
		}

		int position = 0;
		def_encoding = (Spinner) findViewById(R.id.def_encoding);
		ArrayAdapter<String> encodingAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, encodings);
		encodingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		def_encoding.setAdapter(encodingAdapter);
		def_encoding.setPrompt(getString(R.string.pref_tit_encoding));
		position = 0;
		String currentSetting = settings.getString(SongPrefs.PREF_KEY_DEFAULT_ENCODING, "");
		if( currentSetting.equalsIgnoreCase("")){
			currentSetting = DEFAULT;
		}
		while( position < encodings.length && !encodings[position].equalsIgnoreCase(currentSetting)){
			position++;
		}
		def_encoding.setSelection(position);	
	}

	public void okClicked(View v){
		// Update preferences
		updatePrefs();
		setResult(RESULT_OK);
		finish();
	}

	public void cancelClicked(View v){
		// Do nothing just finish
		this.setResult(RESULT_CANCELED);
		finish();
	}
	
	private void updatePrefs(){
		// Save user preferences. We need an Editor object to
		// make changes. All objects are from android.context.Context
		// ALWAYS UPDATE NEW STYLE SharedPreferences
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = settings.edit();

		String encoding = (String) def_encoding.getSelectedItem();
		if( encoding.equalsIgnoreCase(DEFAULT)){
			encoding = "";
		}

		editor.putString(SongPrefs.PREF_KEY_DEFAULT_ENCODING, encoding);

		// Commit the edits
		editor.commit();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// This is the MENU button menu
		MenuItem x = menu.add(0, SHOWHELP_ID, 8, getString(R.string.help));
		x.setIcon(R.drawable.ic_menu_help);
		return super.onCreateOptionsMenu(menu);
	}

	// MENU Button pressed and option selected
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean handled=false;

		switch (item.getItemId()) {
		case SHOWHELP_ID:
			showHelp();
			handled = true;
			break;
		default:
			break;
		}
		return handled;
	}
	private void showHelp() {
		//		Log.d(TAG,"showHelp");
		HelpFragment newFragment = HelpFragment.newInstance(HelpFragment.HELP_PAGE_SETOPTIONS);
		newFragment.show(getSupportFragmentManager(), "dialog");
	}
}
