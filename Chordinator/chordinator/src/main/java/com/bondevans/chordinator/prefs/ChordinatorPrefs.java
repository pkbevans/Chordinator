package com.bondevans.chordinator.prefs;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.bondevans.chordinator.ColourScheme;
import com.bondevans.chordinator.Log;
import com.bondevans.chordinator.R;
import com.bondevans.chordinator.utils.Ute;

public class ChordinatorPrefs extends SherlockPreferenceActivity {
	private static final String TAG = "ChordinatorPrefs";
	private static final int EXPORTSETTINGS_ID = Menu.FIRST + 1;
	private static final int IMPORTSETTINGS_ID = Menu.FIRST + 2;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		int colourScheme;
		colourScheme = Ute.getColourScheme(this);
		setTheme(colourScheme == ColourScheme.LIGHT? R.style.Theme_Sherlock_Light: R.style.Theme_Sherlock);
		super.onCreate(savedInstanceState);

		// Load up fragment
		setContentView(R.layout.songprefs_fragment);

        getSupportActionBar().setLogo(colourScheme == ColourScheme.DARK ? R.drawable.chordinator_aug_logo_dark_bkgrnd : R.drawable.chordinator_aug_logo_light_bkgrnd);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d(TAG, "onOptionsItemSelected");
		switch (item.getItemId()) {
		case android.R.id.home:
			Log.d(TAG, "HELLO - HOME PRESSED");
			break;
		case EXPORTSETTINGS_ID:
			SongPrefs.exportSettings(this);
			break;
		case IMPORTSETTINGS_ID:
			SongPrefs.importSettings(this);
			break;
		}
		return false;
	}
	/* (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockPreferenceActivity#onCreateOptionsMenu(com.actionbarsherlock.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Add import/export settings here
		menu.add(0, EXPORTSETTINGS_ID, 0, getString(R.string.export_settings));
		menu.add(0, IMPORTSETTINGS_ID, 0, getString(R.string.import_settings));
		return super.onCreateOptionsMenu(menu);
	}
}
