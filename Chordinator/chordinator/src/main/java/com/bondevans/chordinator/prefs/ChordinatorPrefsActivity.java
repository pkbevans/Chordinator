package com.bondevans.chordinator.prefs;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.bondevans.chordinator.ColourScheme;
import com.bondevans.chordinator.Log;
import com.bondevans.chordinator.R;
import com.bondevans.chordinator.utils.Ute;

public class ChordinatorPrefsActivity extends AppCompatActivity {
    private static final String TAG = "ChordinatorPrefs";
    private static final int EXPORTSETTINGS_ID = Menu.FIRST + 1;
    private static final int IMPORTSETTINGS_ID = Menu.FIRST + 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
		int colourScheme;
		colourScheme = Ute.getColourScheme(this);
		setTheme(colourScheme == ColourScheme.LIGHT? R.style.Chordinator_Light_Theme_Theme: R.style.Chordinator_Dark_Theme_Theme);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.prefs_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar); // Attaching the layout to the toolbar object
        setSupportActionBar(toolbar);                   // Setting toolbar as the ActionBar with setSupportActionBar() call

        getSupportActionBar();
//      getSupportActionBar().setLogo(/*colourScheme == ColourScheme.DARK ? */R.drawable.chordinator_aug_logo_dark_bkgrnd /*: R.drawable.chordinator_aug_logo_light_bkgrnd*/);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");
        switch (item.getItemId()) {
            case android.R.id.home:
                Log.d(TAG, "HELLO - HOME PRESSED");
                finish();
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
     * @see android.app.SherlockPreferenceActivity#onCreateOptionsMenu(com.actionbarsherlock.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Add import/export settings here
        menu.add(0, EXPORTSETTINGS_ID, 0, getString(R.string.export_settings));
        menu.add(0, IMPORTSETTINGS_ID, 0, getString(R.string.import_settings));
        return super.onCreateOptionsMenu(menu);
    }
}

