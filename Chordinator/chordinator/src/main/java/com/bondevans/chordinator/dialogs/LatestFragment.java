package com.bondevans.chordinator.dialogs;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.ArrayAdapter;

import com.bondevans.chordinator.Log;
import com.bondevans.chordinator.R;
import com.bondevans.chordinator.SongUtils;

import java.io.File;

public class LatestFragment extends DialogFragment{
	private static final String TAG = "LatestFragment";
	private static String mVersion = "";
	private String[] mStrings = {
			"v6.2.1 - Fixed - First run creates /chordinator directory.",
			"v6.2.1 - Fixed - Chordie.com downloads fixed.",
			"v6.1.5 - Fixed - Preview from DropBox app now works again (read only)",
			"v6.1.5 - Improved - performance/memory fixes",
			"v6.1.4 - New - Added support for FLIC buttons (Thanks Derek!!) - Aug Only",
			"v6.1.4 - Fixed - Auto-scrolls while in Sets only if set in Preferences",
			"v6.1.3 - Fixed - checks whether permission granted to access storage",
            "v6.1.2 - Fixed - Bug fix in Print/Save to html (missing {eoc}",
            "v6.1.1 - Fixed - Added Scroll Speed Factor option to preferences for Large Screens",
            "v6.1 - Improved - Exported SETLISTs now saved as.txt",
            "v6.1 - Improved - text size in listing corresponds to Android Font-size setting",
            "v6.1 - Fixed - Favourites sort icon now shows up correctly",
            "v6.1 - Fixed - Moved favourite button to LEFT",
            "v6.1 - Improved - Added Scroll Speed Multiplier in Preferences",
            "v6.1 - Improved - Added larger text sizes for high density screens",
            "v6.1 - New - Added ability to sync SETLISTS via DropBox",
            "v6.0.1 - Fixed - Corrected various chord shapes",
            "v6.0.1 - Fixed - Crash on adding songs to Set on some devices",
            "v6.0.1 - New - Inline chord mode - switch on for all songs from Preferences OR with {inline} tag in your song file",
            "v6 - Removed dependency on ActionBarSherlock",
            "v5.4.3 - Fixed - Sort options not displayed correctly",
            "v5.4.3 - Improved - Sets ordered alphabetically",
            "v5.4.3 - Improved - Prev/Next buttons bigger and scaled for different screen sizes",
			"v5.4.2 - New - Added Tenor (C-G-D-A) Chord shapes",
			"v5.4.1 - New - Added Portugese translations - obrigado Jorge :)",
			"v5.4.1 - Fixed - Preferences on Android vs<11",
			"v5.4 - Swipe to remove from Set disabled, following feedback",
			"v5.4 - New. Now allows app to be installed/moved to sd card",
			"v5.3 - New. Swipe to remove song from SET in SET listing",
			"v5.3 - Improved - Reordering of SETs. Now on main SET listing and works much better",
			"v5.3 - Improved. Performance improved on lyric-only songs",
			"v5.3 - Improved - German Translation updated - thanks to Roland :)",
			"v5.2 - New - Migrated to Android Studio",
			"v5.2 - Fixed - Problem with Adding new Set",
			"v5.2 - Improved - On Songlist screen - current sort highlighted and reverse order now possible",
			"v5.2 - Fixed - Surrounding selected text with chords/tags",
			"v5.1 - New - Supports printing from Long press on Song View screen (requires Kit Kat)",
			"v5.1 - Fixed - Improved Save to Html feature",
			"v5.1 - Fixed - Download from UG",
			"v5.0.2 New - Now scans external cards for Songs",
//			"v5.0.2 Fixed - Colour scheme problems",
			"v5.0.2 New - Full screen in Set mode",
			"v5.0.2 Fixed - Performance improvements",
			"v5.0.2 Fixed - Added share option to Song Viewer Long press menu", 
			"v5.0.1 Fixed - Crash on Scan in Aug", 
			"v5.0 New - Add Share option to File Browser long press menu", 
			"v5.0 Fixed - Crash during Scan due to screen orientation change",
			"v5.0 New - Changed version numbering scheme. Unified Dim/Aug code",
/*			"v4.0 New - Free app now has all Aug functionality",
			"v2.4.0 Fixed - Fixed problems caused by hardcoded /sdcard",
			"v2.4.0 Fixed - App only offers to open .csf .txt and .chopro files",
			"v2.3.2 New - Added additional text sizes",
			"v2.3.1 Fixed - ERROR on file open on some devices",
			"v2.3 New - Add Search/Filter functionality",
			"v2.3 New - Faster scroll speeds option - see Preferences",
			"v2.3 Fixed - Adding songs to new Sets",
			"v2.3 Fixed - Duplicate song listings on Scan for Songs on OS v4.4",
			"v2.3 Fixed - Add-dashes defaults to OFF",
			"v2.3 Fixed - Import/Export settings",
			"v2.2.4 Fixed - Scroll delay now works in SETs",
			"v2.2.4 New - Scroll delay now up to 20 seconds",
			"v2.2.2 Fixed - RENAME file feature",
			"v2.2 New - Supports old-style SETLISTs",
			"v2.2 Fixed - Bug with using {comment:} tag",
			"v2.2 Fixed - Internet search in Landscape mode",
			"v2.2 New - Ability to change Song download folder -see File Browser menu",
			"v2.2 New - Option to always display empty lines - see Preferences",
			"v2.2 New - Dashes added to words with chords in the middle now optional - see Preferences",
			"v2.2 New - Updated Mandolin Chord Shapes",
			"v2.2 New - Added RENAME SET feature to Set list menu",
			"v2.2 New - Added RENAME file feature to File Browser Long Press menu",
			"v2.2 New - Option to automatically convert downloaded txt files to chopro",
			"v2.2 Fixed chordie search criteria",
			"v2.2 New - Made adding dashes to words with embedded chords optional in preferences",
			"v2.1.1 - ActionBar Library Updated",
			"v2.1 New - Added hyphens to words with multiple chords",
			"v2.0 New - Options to view/browse to File Location from song list Screen (Long Press)",
			"v2.0 New - Split screen proportions can now be set in Preferences",
			"v2.0 New - Improved Internet search - No longer just chordie.com",
			"v2.0 New - Improved Song Editing. Insert chopro tags easily (Requires Android v3.0)",
			"v2.0 New - Larger Text sizes now available", 
			"V2.0 New - Small UI tweaks",
			"v2.0 New - Song titles and artists displayed in consistent Title Case",
			"v2.0 Fixed - FC on Default Encoding in preferences",
			"v2.0 Fixed - Duplicate song entries on scans and downloads",
			"v2.0 Fixed - FCs due to badly formatted songs",
			"v2.0 Fixed - Current song retained in setlist on re-orientation",
			"v2.0 Fixed - Displays tab correctly if at top of song", 
			"v2.0 Fixed - Chord grids disappear on text size change, if not on by default", 
			"v2.0 Fixed - Invisible text in some dialog boxes",
			"v1.1 Fixed - Problem with large font sizes or large songs on Jelly Bean",
			"v1.1 Fixed - Text size buttons disabled if already on highest/lowest setting",
			"v1.1 Fixed - Now correctly opens UNICODE files (UTF16)",
			"v1.1 New - Split screen mode now optional in Landscape on Tablets- see Preferences",
			"v1.1 New - Send Debug Logs from About screen",
			"v1.1 New - Send Feedback from About screen",
			"v1.1 Fixed - duplicate library entries when opening same song twice from dropbox",
			"v1.0 - Brand new app" */
			// DONE Add dialog for delete from song list and add option to delete from disk at the same time
            // DONE - Rename SET action item
			// DONE - Corrected "Done" button colour on AddSongToSet screen
			// DONE - Correctly setting default values in Preferences - also default colour scheme now Dark",
			// DONE - Sort out consistent /sdcard/chordinator vs /mnt/sdcard/chordinator issues
			// DONE - Added import/export of SETs
			// DONE - gap between top of the screen and transpose buttons
			// DONE - re-ordered action items from main activity
			// DONE - moved import/export settings to preferences
			// DONE - refresh song after edit in landscape mode
			// DONE - Removed "puny" message
			// DONE - Bug fixes in import/export settings
			// DONE - Auto-scroll speed is zero when re-orienting from portrait to landscape
			// DONE - Set name not displayed in Set mode after re-orientation
			// DONE - Made transpose text size buttons more visible in dark colour scheme
			// DONE - Made prev/next buttons similar to transpose buttons
			// DONE - .prefix files now have banned file icon in file browser
			// DONE - song and banned file icons in browser made more visible in dark colour scheme
			// DONE - Improved autoscroll delay feature - removed dialog, increased possible delay to 10secs
			// "v0.3 - Added support for Brazilian Portugese - Big thanks to Herbert",
			// "v0.3 - Added Americanized spelling for US English",
			// "v0.3 - Added support for PREV/NEXT buttons for SETs (n/a in split screen mode)",
			// "v0.3 - Added ability to edit sets",
			// "v0.3 - Improved fast scrolling when sorting on Artist",
			// "v0.3 - Added Import/Export settings",
			// "v0.2 - Updated German translations - Big Thanks to Wilhelm",
			// "v0.2 - Sort Order remembered on app exit",
			// "v0.2 - Entire theme changes when colour scheme changes",
			// "v0.2 - Fixed: FC on delete songfile with orientation change before OK button",
			// "v0.2 - Fixed: Edit Song screen - black text on black background on some screens",
			// "v0.2 - Fixed: FC on Internet search",
			// "v0.2 - Fixed: Prefs change didn't take effect straight away on Tablet in split screen mode",
			// "v0.2 - Fixed: current Directory on File Browser not retained on orientation change",
			// "v0.2 - Fixed: /chordinator folder and sample file not created on first run",
			// "v0.1 - Beta Test version"
    		// DONE - Disable/invisible transpose buttons for non-chopro songs
            // DONE - Remove toggle favourite from long press menus
            // DONE - long press on Songlist - "Add Songs to SET" should be Add Song to SET
    		// DONE - Added Transpose buttons to SongViewer screen
    		// DONE - Inc Scroll speed button on landscape
    		// DONE - Chord-only and lyric-only mode
    		// DONE - set options from songviewer.
    		// DONE - slower autoscroll button doesnt work
    		// DONE - FC on orientation change when auto-scroll delay is on
    		// DONE - sort out addsongstoset layout in landscape
    		// DONE - FC on AddSongs... button (sometimes)
    		// DONE - Fastscroll on addsongtoset screen
    		// DONE - FC if you use space bar on Add Song to set screen to select a song
    		// DONE - Add AddSongs to SetSongLIstFragment
    		// DONE - Confirm dialog before delete set
    		// DONE- set list fragment gets added multiple times on set list screen - switching orientation
            };

	public static LatestFragment newInstance(String version) {
		mVersion  = version;
		LatestFragment frag = new LatestFragment();
		Bundle args = new Bundle();
		frag.setArguments(args);
		return frag;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
        .setAdapter(new ArrayAdapter<>(getActivity(),
				R.layout.latest_item, mStrings), null)
                
        .setTitle(getString(R.string.whats_new) + " - "+mVersion)
        .setPositiveButton(R.string.sendlog, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				sendLog();
			}
		})
        .setNeutralButton(R.string.feedback, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				sendFeedback(null);
			}
		})
        .create();
	}
	public void sendFeedback(View v){
		Intent theIntent = new Intent(Intent.ACTION_SEND);
		theIntent.setType("text/plain");
		// the formatted text.
		String [] emailAddress = {"feedback@bondevans.com", ""};
		theIntent.putExtra(Intent.EXTRA_EMAIL, emailAddress);
		//next line specific to email attachments
		theIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback));
		try {
			startActivity(Intent.createChooser(theIntent, "Send Feedback With...."));
		} 
		catch (Exception e) {
			Log.d(TAG,"Something went wrong: "+e.getMessage());
		}
	}

	/**
	 * Sends logcat output to Chordinator developer
	 */
	public void sendLog(){
		String filename = Environment.getExternalStorageDirectory()+"/"+getString(R.string.logfile_name);
		SongUtils.getLog(filename);

		File aFile = new File(filename);
		Intent theIntent = new Intent(Intent.ACTION_SEND);
		theIntent.setType("text/plain");
		String [] emailAddress = {"pkbevans@gmail.com", ""};
		theIntent.putExtra(Intent.EXTRA_EMAIL, emailAddress);
		theIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(aFile));
		theIntent.putExtra(Intent.EXTRA_TEXT, "Chordinator Log File");
		//next line specific to email attachments
		theIntent.putExtra(Intent.EXTRA_SUBJECT, "Sending " + aFile.getName());
		try {
			startActivity(Intent.createChooser(theIntent, "Share With...."));
		}
		catch (Exception e) {
			Log.d(TAG,"Something went wrong: "+e.getMessage());
		}
	}
}
