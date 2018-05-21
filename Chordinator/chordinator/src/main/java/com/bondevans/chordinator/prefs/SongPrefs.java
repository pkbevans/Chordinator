package com.bondevans.chordinator.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.bondevans.chordinator.ChordinatorException;
import com.bondevans.chordinator.Log;
import com.bondevans.chordinator.R;
import com.bondevans.chordinator.SongUtils;
import com.bondevans.chordinator.Statics;

import java.util.Map;
import java.util.Map.Entry;



public class SongPrefs {
	private int mTextSize=0;
	private boolean mShowGrids= false;
	private boolean mAutoScroll = false;
	private boolean mTurboScroll = false;
    private int mScrollSpeedMultiplier;
	private int mColourScheme = 0;
	private int mMode=0;
	private String mDefaultEncoding="";
	private boolean mAddDashes = false;
	private boolean mHonourLFs = false;
	private int mInlineMode = 0;

	//----------------STRINGS-------------------------------//
//	public static final String PREFS_NAME = "MyPrefsFile";	// Name of preferences file for this application
	public static final String PREF_KEY_TEXTSIZE = "TextSize";
	public static final String PREF_KEY_SONGDIR = "SongDir";
	public static final String PREF_KEY_DEFAULT_ENCODING = "DefaultEncoding";
	public static final String PREF_KEY_KEYWORDS = "KeyWords";
	public static final String PREF_KEY_DOWNLOADDIR = "downLoadDir";
	//----------------BOOLEANS-------------------------------//
	public static final String PREF_KEY_CONVERTED = "Converted";
	public static final String PREF_KEY_SHOWGRIDS = "ShowGrids";
	public static final String PREF_KEY_AUTOSCROLL = "AutoScroll";
	public static final String PREF_KEY_TURBOSCROLL = "TurboScroll";
	public static final String PREF_KEY_SHOWHELPONSTART = "ShowHelp";
	public static final String PREF_KEY_FIRSTRUN = "firstrun";
	public static final String PREF_KEY_SPLIT_SCREEN = "SplitScreen";
	public static final String PREF_KEY_ADDDASHES = "addDashes";
	public static final String PREF_KEY_HONOURLFS = "honourLFs";
	//----------------INTEGERS-------------------------------//
	public static final String PREF_KEY_SCROLLSPEED = "SPEED";
	public static final String PREF_KEY_TRANSPOSE = "TRANS";
	public static final String PREF_KEY_SORTORDER = "sortorder";
	public static final String PREF_KEY_SORTDIRECTION = "sortDir";
	public static final String PREF_KEY_TRIALVIEWCOUNT = "ViEwCnt";
	public static final String PREF_KEY_FREEVIEWCOUNT = "freeViEwCnt";
	public static final String PREF_KEY_SPLIT_PROPORTION = "splitProps";
	public static final String PREF_KEY_FIRSTRUN_VERSION = "firstRunnVers";
    public static final String PREF_KEY_INLINEMODE = "iNline";
	//----------------INTEGERS - STORED AS STRINGS-----------//
	public static final String PREF_KEY_CHORDLYRIC_MODE="Mode";
	public static final String PREF_KEY_SCROLL_DELAY="scrollDelay";
	public static final String PREF_KEY_COLOURSCHEME = "ColourScheme";
	public static final String PREF_KEY_GRID_INSTRUMENT="GridInstrument";
    public static final String PREF_KEY_SCROLL_SPEED_FACTOR = "speedFctirh";

	public final static int DEFAULTSPEED = 10;
	private static final String SETTING_SEPARATOR = "\n";
	private static final String KEYVALUE_SEPARATOR = ":";
	public static final String EXPORTFILE = ".chordinator_settings.txt";
	private static final String TAG = "SongPrefs";

    public SongPrefs(){
		// Do nothing
	}
	public int getTextSize() {
		return mTextSize;
	}

	public void setTextSize(int textSize) {
		mTextSize = textSize;
	}
	public void setShowGrids(boolean showGrids) {
		this.mShowGrids = showGrids;
	}
	public boolean isShowGridsOn() {
		return mShowGrids;
	}
	public int getColourScheme() {
		return mColourScheme;
	}
	public void setColourScheme(int scheme) {
		mColourScheme = scheme;
	}
	/**
	 * @return the autoScroll
	 */
	public boolean isAutoScrollOn() {
		return mAutoScroll;
	}
	/**
	 * @param autoScroll the autoScroll to set
	 */
	public void setAutoScroll(boolean autoScroll) {
		this.mAutoScroll = autoScroll;
	}
	/**
	 * @return the turboScroll
	 */
	public boolean isTurboScrollOn() {
		return mTurboScroll;
	}
	/**
	 * @param turboScroll the turboScroll to set
	 */
	public void setTurboScroll(boolean turboScroll) {
		this.mTurboScroll = turboScroll;
	}
	//	public void setFullScreen(boolean fullScreen) {
	//		this.fullScreen = fullScreen;
	//	}
	//	public boolean isFullScreen() {
	//		return fullScreen;
	//	}
	public void setDefaultEncoding(String defaultEncoding) {
		this.mDefaultEncoding = defaultEncoding;
	}
	public String getDefaultEncoding() {
		return mDefaultEncoding;
	}
	public int getMode() {
		return mMode;
	}
	public void setMode(int mode) {
		this.mMode = mode;
	}
	/**
	 * @return mAddDashes
	 */
	public boolean AddDashes() {
		return mAddDashes;
	}
	/**
	 * @param addDashes the mAddDashes to set
	 */
	public void setAddDashes(boolean addDashes) {
		this.mAddDashes = addDashes;
	}
	/**
	 * @return mHonourLFs
	 */
	public boolean HonourLFs() {
		return mHonourLFs;
	}
    /**
     * @param honourLFs the mHonourLFs to set
     */
    public void setHonourLFs(boolean honourLFs) {
        this.mHonourLFs = honourLFs;
    }
    /**
     * @param inlineMode in line mode
     */
	public void setInlineMode(int inlineMode) {
		Log.d(TAG, "Setting inline mode="+inlineMode);
        this.mInlineMode = inlineMode;
	}
    /**
     * @return mInlineMode
     */
    public int getInlineMode() {return this.mInlineMode;}
	public static void exportSettings(Context context){
		Log.d(TAG, "HELLO exportPrefs1");
		// Delete any scroll-speed entries with the default value
		deleteDefaultSpeedEntries(context);
		// Open up the new-style preferences
		SharedPreferences newSettings = PreferenceManager.getDefaultSharedPreferences(context);
		// Create a string containing each pref on a separate line.
		StringBuilder output= new StringBuilder();
		// Read all preferences
		Log.d(TAG, "HELLO exportPrefs3");
		Map<String, ?> prefs;
		prefs = newSettings.getAll();
		Log.d(TAG, "HELLO exportPrefs4");
		for( Entry<String, ?> x:prefs.entrySet()){
			// write each one out into the file
			// Ignore some settings
			if(x.getKey().equalsIgnoreCase(SongPrefs.PREF_KEY_FIRSTRUN)||
					x.getKey().equalsIgnoreCase(PREF_KEY_CONVERTED)||
					x.getKey().equalsIgnoreCase(PREF_KEY_SORTORDER)||
					x.getKey().equalsIgnoreCase(PREF_KEY_SORTDIRECTION)||
					x.getKey().equalsIgnoreCase(PREF_KEY_SONGDIR)||
					x.getKey().equalsIgnoreCase(PREF_KEY_TRIALVIEWCOUNT)||
					x.getKey().equalsIgnoreCase(PREF_KEY_FREEVIEWCOUNT)||
					x.getKey().equalsIgnoreCase(PREF_KEY_SPLIT_SCREEN)||
					x.getKey().equalsIgnoreCase(PREF_KEY_FIRSTRUN_VERSION)||
					x.getKey().equalsIgnoreCase(PREF_KEY_SHOWHELPONSTART)){
				// IGNORE
                Log.d(TAG, "HELLO Ignoring");
			}
			else{
				Log.d(TAG, "Exporting: ["+x.getKey()+"]["+x.getValue()+"]");
				output.append(x.getKey()+KEYVALUE_SEPARATOR+x.getValue()+SETTING_SEPARATOR);
			}
		}
		Log.d(TAG, "HELLO exportPrefs5");
		// Write out prefs to a file
		try {
			SongUtils.writeFile(Statics.CHORDINATOR_DIR+EXPORTFILE, output.toString());
			SongUtils.toast(context, context.getString(R.string.settings_exported)+"\n"+ Statics.CHORDINATOR_DIR+EXPORTFILE);
		} 
		catch (ChordinatorException e) {
			SongUtils.toast(context, context.getString(R.string.export_failed));
			e.printStackTrace();
		}
		Log.d(TAG, "HELLO exportPrefs6");
	}

	private static void deleteDefaultSpeedEntries(Context context){
		Log.d(TAG, "HELLO deleteDefaultSpeedEntries");
		// Open up the new-style preferences
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		// Read all preferences
		Map<String, ?> prefs;
		prefs = settings.getAll();
		for( Entry<String, ?> x:prefs.entrySet()){
			if(x.getKey().endsWith(SongPrefs.PREF_KEY_SCROLLSPEED)){
				String value = ""+x.getValue();
				if(Integer.parseInt(value) == DEFAULTSPEED){
					Log.d(TAG, "HELLO - deleting ["+x.getKey()+"]");
					editor.remove(x.getKey());
				}
			}
		}
		editor.commit();
	}
	public static void importSettings(Context context) {
		// Open import file
		String imports="";
		try {
			imports = SongUtils.loadFile(Statics.CHORDINATOR_DIR+EXPORTFILE,null, null);
		} 
		catch (ChordinatorException e) {
			SongUtils.toast(context, context.getString(R.string.import_failed)+Statics.CHORDINATOR_DIR+EXPORTFILE);
			e.printStackTrace();
			return;
		}
		// Get each line
		String [] impSettings = imports.split(SETTING_SEPARATOR);
		// Open a preferences editor
		SharedPreferences newSettings = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = newSettings.edit();

		// Delete sortorder/direction settings if present
		editor.remove(PREF_KEY_SORTORDER);
		editor.remove(PREF_KEY_SORTDIRECTION);

		for(String setting: impSettings){
			Log.d(TAG, "HELLO importing setting ["+setting+"]");
			// For each line in the file separate into key| value
			String [] keyValue = setting.split(KEYVALUE_SEPARATOR);

			if(keyValue.length>1){
				Log.d(TAG, "HELLO key["+keyValue[0]+"] value["+keyValue[1]+"]");
				// Ignore some settings
				if(keyValue[0].equalsIgnoreCase(SongPrefs.PREF_KEY_FIRSTRUN)||
						keyValue[0].equalsIgnoreCase(PREF_KEY_SPLIT_SCREEN)||
						keyValue[0].equalsIgnoreCase(PREF_KEY_TRIALVIEWCOUNT)||
						keyValue[0].equalsIgnoreCase(PREF_KEY_FREEVIEWCOUNT)||
						keyValue[0].equalsIgnoreCase(PREF_KEY_SHOWHELPONSTART)){
					// IGONORE
				}
				// Boolean values
				else if(keyValue[0].equalsIgnoreCase(PREF_KEY_SHOWGRIDS)|| 
						keyValue[0].equalsIgnoreCase(PREF_KEY_ADDDASHES)||
						keyValue[0].equalsIgnoreCase(PREF_KEY_HONOURLFS)||
						keyValue[0].equalsIgnoreCase(PREF_KEY_TURBOSCROLL)||
						keyValue[0].equalsIgnoreCase(PREF_KEY_AUTOSCROLL)){
					Log.d(TAG, "HELLO BOOLEAN key["+keyValue[0]+"] value["+keyValue[1]+"]");

					editor.putBoolean(keyValue[0], keyValue[1].contentEquals("true")?true:false);
				}
				// Int values
				else if(keyValue[0].endsWith(PREF_KEY_SCROLLSPEED) ||
						keyValue[0].endsWith(PREF_KEY_TRANSPOSE) ||
						keyValue[0].endsWith(PREF_KEY_SPLIT_PROPORTION) ||
						keyValue[0].endsWith(PREF_KEY_SORTORDER) ||
//						keyValue[0].endsWith(PREF_KEY_SCROLL_SPEED_FACTOR) ||
						keyValue[0].endsWith(PREF_KEY_SORTDIRECTION)
						){
					editor.putInt(keyValue[0], Integer.parseInt(keyValue[1]));
				}
				// String Values
				else{
					Log.d(TAG, "HELLO STRING key["+keyValue[0]+"] value["+keyValue[1]+"]");
					editor.putString(keyValue[0], keyValue[1]);
				}
			}
		}
		// Commit edits
		editor.commit();
		SongUtils.toast(context, context.getString(R.string.settings_imported));
	}

    public void setScrollSpeedMultiplier(int i) {
        mScrollSpeedMultiplier=i;
    }

    public int getScrollSpeedMultiplier() {
        return mScrollSpeedMultiplier;
    }
}
