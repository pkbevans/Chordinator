package com.bondevans.chordinator.trial;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.bondevans.chordinator.prefs.SongPrefs;

public class Trial {
	private final static int MAX_TRIAL_VIEWS=30;
	private final static int MAX_FREE_VIEWS=5;

	public static boolean maxViewsReached(Context context, String prefKey, boolean increment){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

		// Get the current count
		int views = settings.getInt(prefKey, 0);
		if(increment){
			SharedPreferences.Editor editor = settings.edit();
			// Increment the view count
			editor.putInt(prefKey, ++views);
			// Commit edits
			editor.commit();
		}
		// return true if max views exceeded, else false
		if(prefKey.equalsIgnoreCase(SongPrefs.PREF_KEY_TRIALVIEWCOUNT) && views>=MAX_TRIAL_VIEWS){
			// Trial over
			return true;
		}
		else if(prefKey.equalsIgnoreCase(SongPrefs.PREF_KEY_FREEVIEWCOUNT) && views>=MAX_FREE_VIEWS){
			// Trial over
			return true;
		}
		return false;
	}

	public static void resetViewCount(Context context){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		// Increment the view count
		editor.putInt(SongPrefs.PREF_KEY_FREEVIEWCOUNT, 0);
		// Don't forget to commit your edits!!!
		editor.commit();
	}
	/*
	private static boolean finishedTrial(Context context) {
		// Get View Count
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		int trialViews = settings.getInt(SongPrefs.PREF_KEY_TRIALVIEWCOUNT, 0);

		if( trialViews>MAX_TRIAL_VIEWS){
			// Trial over
			return true;
		}
		// Not over
		return false;
	}
	 */
	/*
	private static boolean timeToPromotePaidApp(Context context) {
		// Get View Count
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		int freeViews = settings.getInt(SongPrefs.PREF_KEY_FREEVIEWCOUNT, 0);

		if( freeViews>MAX_FREE_VIEWS){
			// Time to annoy the user
			return true;
		}
		return false;
	}
	*/
}
