package com.bondevans.chordinator.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.webkit.WebView;

import com.bondevans.chordinator.R;
import com.bondevans.chordinator.SongUtils;

public class HelpFragment extends DialogFragment{
	private static final String KEY_PAGE = "KEY_PAGE";
	public static final int HELP_PAGE_FILEBROWSER = 2;
	public static final int HELP_PAGE_SONGVIEWER = 3;
	public static final int HELP_PAGE_SETOPTIONS = 6;
	public static final int HELP_PAGE_MYSONGS = 7;
	public static final int HELP_PAGE_EDITSET = 8;
	public static final int HELP_PAGE_SETLIST = 9;

	private static String bold(String in){
		return BR+"<big><span style=\"font-weight: bold;\">"+in+"</span></big>"+BR;
	}
	private static String big(String in){
		return "<big>"+in+"</big>";
	}
	private final static String BR = "<br>";

	WebView webview;
	@SuppressLint("SdCardPath")
	private final static String helpTextFileBrowser="" +
			big(bold("The File Browser"))+BR+
			"Use the File Browser to locate and open song files stored on your "+
			"SD card. Clicking on a song file opens the song in the Chordinator's Song Viewer "+
			"and adds the song to the main Song List if it is in chopro format."+BR+
			"There are various Long Press options: "+BR+
			BR+
			bold("Edit")+
			"Opens the file in the Chordinator's built in Text Editor"+BR+
			bold("Open")+
			"Opens the song in the Chordinator's Song Viewer"+BR+
			bold("Add Song to SET...")+
			"Adds the song to one of your existing Set Lists or allows you to create a new Set"+BR+
			bold("Convert To ChoPro")+
			"Use this option if you have Songs in Chord-over-Lyrics format "+
			"that you would like to convert to ChoPro format (which is what The "+
			"Chordinator uses). A copy of the file is created with the "+
			"extension .csf (<span style=\"font-weight: bold;\">C</span>hordinator "+
			"<span style=\"font-weight: bold;\">S</span>ong <span style=\"font-weight: bold;\">F</span>ile) in the same folder"+BR+
			bold("Delete")+
			"Deletes the song - PERMANENTLY"+BR+
			bold("Save Copy")+
			"Saves a copy of the file"+BR+
			bold("Rename")+
			"Rename the file"+BR+
			BR+
			bold("Note")+
			"The app wont open any of the following file types:"+BR+
			".jpg\",\".mp3\",\".mov\",\".zip\",\".pdf\",\".db\",\".jpeg\",\".png\",\".3gp\",\".html\",\".htm\",\".doc\","+
			".apk\", \".m4a\"";
	
	private final static String helpTextSongViewer="" +
			big(bold("The Song Viewer"))+BR+
			"The Chordinator Song Viewer has various Long Press options:"+BR+BR+
			bold("Edit")+
			"This opens the song up in The Chordinator's built in text "+
			"editor. Any changes made are written to disk and the Song Viewer "+
			"reflects these changes immediately."+BR+
			bold("Chord Grids On/Off")+
			"Toggle displaying of Guitar Chord Grids. If ON then grids for "+
			"all chords in the song are shown at the top of the song"+BR+
			bold("Save To Text File")+
			"This option allows you to save a ChoPro song into Chords-over-lyric "+
			"format - for sharing with other applications/people."+BR+
			bold("Save to HTML")+
			"This option allows you to save a ChoPro song into Chords-over-lyric "+
			"format - for sharing with other applications/people"+BR+
			bold("Save Transposed Song")+
			"This updates the file on disk with the Transposed "+
			"chords. This option only appears if you have transposed the song."+BR+BR+
			bold("Revert to Original Key")+
			"This reverts the song back to the original key in the songfile. "+
			"This option only appears if you have transposed the song."+BR+BR+
			"There are also some Menu Options:"+BR+
			BR+
			bold("Share")+
			"This allows you to send the song to other people (e.g. email) "+
			"or other applications on the device (e.g. printer apps)"+BR+
			bold("Help")+
			"This Screen";			

	private final static String helpTextPreferences="" +
			big(bold("Preferences"))+BR+
			"The Chordinator has various options:"+BR+BR+
			bold("Auto Scroll")+
			"If this option is checked, Songs automatically start scolling when opened"+BR+
			bold("Show Chord Grids")+
			"Chord grids always shown if this option is checked."+BR+
			bold("Text Size")+
			"Select the size that suits you."+BR+
			bold("Default Encoding")+
			"Use this option if song files dont seem to displaying correctly.  It could be because they are using a non-Western European character set.  Select the encoding that works best."+BR+
			bold("Colour Scheme")+
			"Select either Black text on white background or White text on black background";

	private final static String helpTextSongList=""+
			big(bold("Song List Screen"))+BR+
			"The Song List screen shows all your songs. You can sort by Song Title, Artist, Recently viewed, or "+
			"showing favourites first."+BR+
			"There are are various menu options from this screen:"+BR+
			bold("Change Sort Order")+
			"Sorting of the songs can be be by Song Title, Artist, Most recently viewed or by favourites."+BR+
			bold("Select SET")+
			"Select a SET to work with"+BR+
			bold("File Browser")+
			"Locate songs on your disk using the file browser from the \"Standard\" Chordinator app."+BR+
			bold("Search Internet for Songs")+
			"Search for songs from the Internet"+BR+
			bold("Scan for Songs")+
			"Search for songs files on your disk. Any new chopro files will be added to the Song List screen"+BR+
			bold("Delete all Songs")+
			"Delete all songs from the Song List and any SETs."+BR+
			bold("Preferences")+
			"Set various options for the app."+BR+
			bold("About..")+
			"Version number and latest updates to the app."+BR+
			BR+
			"There are various long press options:"+BR+BR+
			bold("Edit")+
			"Opens the file in the Chordinator's built in Text Editor"+BR+
			bold("Add to Set List...")+
			"Adds the song to one of your existing Set Lists or allows you to create a new Set"+BR+
			bold("Delete Song")+
			"Deletes the song from this listing.  This does NOT delete the file from your sdcard"+BR+
			bold("Show Details")+
			"Displays the file location of this song."+BR+
			bold("Goto File Location")+
			"Opens the File Browser at the folder containing this song."+BR+
			bold("Share...")+
			"Share the song via email/etc"+BR+
			"";
	private final static String helpTextSetlist=""+
			big(bold("Set List Screen"))+BR+
			"The Set List screen shows the songs in the chosen SET."+BR+
			"There are are various menu options from this screen:"+BR+
			bold("Edit SET")+
			"Change the order of the songs in the current set and to and/delete songs"+BR+
			bold("Add Songs")+
			"Add songs to the current SET"+BR+
			bold("Delete SET")+
			"Delete the current SET"+BR+
			bold("Export SET")+
			"Export the SET to a SETLIST file in the /sdcard/chordinator/ folder. Useful as a backup.  You can import a SETLIST file by doing a long press on the file on the File Browser screen."+BR+BR+
			"There are various long press options from the song list:"+BR+
			bold("Edit")+
			"Opens the file in the Chordinator's built in Text Editor"+BR+
			bold("Delete")+
			"Deletes the song from this listing.  This does NOT delete the file from your sdcard"+BR+
			"";

	private final static String helpTextEditSet=""+
			big(bold("Edit Set"))+BR+
			"The Edit Set screen allows you to re-arrange the order of the songs in your set.  Hold the "+
			"icon on the left of each song and drag and drop the song to the new position.  You can also "+
			"delete songs from the set by holding the icon and swiping to the right.  Press the \"Done\" "+
			"button to save your changes."+BR+
			bold("Add Songs")+
			"Lists all of your songs and allows you to choose which songs to add to this set"+BR+
			"";


	public static HelpFragment newInstance(int whichPage) {
		HelpFragment frag = new HelpFragment();
		Bundle args = new Bundle();
		args.putInt(KEY_PAGE, whichPage);
		frag.setArguments(args);
		return frag;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		webview = new WebView(getActivity());

		int page = getArguments().getInt(KEY_PAGE);

		String helpText="";
		switch (page){
//		case HELP_PAGE_MAIN:
//			helpText=helpTextMain;
//			break;
		case HELP_PAGE_FILEBROWSER:
			helpText=helpTextFileBrowser;
			break;
		case HELP_PAGE_SONGVIEWER:
			helpText=helpTextSongViewer;
			break;
		case HELP_PAGE_SETOPTIONS:
			helpText=helpTextPreferences;
			break;
		case HELP_PAGE_MYSONGS:
			helpText=helpTextSongList;
			break;
		case HELP_PAGE_EDITSET:
			helpText=helpTextEditSet;
			break;
		case HELP_PAGE_SETLIST:
			helpText=helpTextSetlist;
			break;
		default:
			SongUtils.toast(getActivity(), "UNKNOWN HELP PAGE");
		}
		webview.loadData(helpText,"text/html", null);
		return new AlertDialog.Builder(getActivity())
		.setTitle(getString(R.string.help_screen_title))
		.setView(webview)
		.create();
	}
}
