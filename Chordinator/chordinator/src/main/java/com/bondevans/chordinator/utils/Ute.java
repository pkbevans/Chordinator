package com.bondevans.chordinator.utils;

import com.bondevans.chordinator.prefs.SongPrefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class Ute {
	/**
	 * Concatenate file path and file name, adding slash if necessary
	 * @param path
	 * @param file
	 * @return
	 */
	public static String doPath(String path, String file){
		if(path.endsWith("/")){
			return (path+file);
		}
		else{
			return (path+"/"+file);
		}
	}
	public static int getColourScheme(Context context){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		return Integer.parseInt(settings.getString(SongPrefs.PREF_KEY_COLOURSCHEME, "1"));
	}
}