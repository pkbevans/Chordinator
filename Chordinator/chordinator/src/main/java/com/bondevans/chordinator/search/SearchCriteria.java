package com.bondevans.chordinator.search;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.bondevans.chordinator.ColourScheme;
import com.bondevans.chordinator.Log;
import com.bondevans.chordinator.R;
import com.bondevans.chordinator.prefs.SongPrefs;
import com.bondevans.chordinator.utils.Ute;

public class SearchCriteria extends AppCompatActivity {
	public static final String TAG = "SearchCriteria";
	private CheckBox chordieOnly;
	private EditText keywords;
	private String 	mDefaultKeywords = "chords chopro";
	private int mColourScheme;

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mColourScheme = Ute.getColourScheme(this);
		setTheme(mColourScheme == ColourScheme.LIGHT? R.style.Chordinator_Light_Theme_Theme: R.style.Chordinator_Dark_Theme_Theme);
		super.onCreate(savedInstanceState);
		Log.d(TAG, "HELLO onCreate");
		setContentView(R.layout.search_criteria_layout);
		chordieOnly = (CheckBox) findViewById(R.id.chordie_only);
		keywords = (EditText) findViewById(R.id.keywords);
		keywords.setText(getKeywords());

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar); // Attaching the layout to the toolbar object
        setSupportActionBar(toolbar);                   // Setting toolbar as the ActionBar with setSupportActionBar() call
//      getSupportActionBar().setLogo(mColourScheme == ColourScheme.DARK? R.drawable.chordinator_aug_logo_dark_bkgrnd: R.drawable.chordinator_aug_logo_light_bkgrnd);
		getSupportActionBar().setTitle(R.string.search_title);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return super.onKeyDown(keyCode, event);
	}

	public void searchButtonClicked(View v){
		// Do something
		EditText criteria = (EditText) findViewById(R.id.search_criteria);
		String searchString = criteria.getText().toString();
		// Add in some other key words
		searchString = searchString.concat(" " + keywords.getText().toString());
		Log.d(TAG, "HELLO - searchButtonClicked ["+searchString+"]");
		if(chordieOnly != null && chordieOnly.isChecked()){
			searchString = searchString.concat(" &sitesearch=chordie.com");
		}

		Intent search = new Intent(this, SearchActivity.class);
		search.putExtra(SearchActivity.SEARCH_CRITERIA, searchString);
		startActivity(search);
		// Store keywords - user may have changed them
		setKeywords(keywords.getText().toString());
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

	private String getKeywords(){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		mDefaultKeywords = settings.getString(SongPrefs.PREF_KEY_KEYWORDS, mDefaultKeywords);
		return mDefaultKeywords;
	}
	private String setKeywords(String keywords){
		Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
		editor.putString(SongPrefs.PREF_KEY_KEYWORDS, keywords);
		editor.commit();
		return mDefaultKeywords;
	}
}
