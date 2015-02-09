package com.bondevans.chordinator.utils;

import java.io.File;

import java.util.HashSet;
import java.util.Set;

import com.bondevans.chordinator.Log;
import com.bondevans.chordinator.R;


import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;


public class SdCardFactory{
	private static final String TAG = SdCardFactory.class.getSimpleName();
	private static final String rawSecondaryStoragesStr = System.getenv("SECONDARY_STORAGE");
	private static Set<SdCard> cardSet;
	private static SdCard [] cardArray;

	public class SdCard{
		public String path;
		public String name;
		public SdCard(String path, String name) {
			this.path = path;
			this.name = name;
		}
	}
	/**
	 * Constructor takes context and sets up all sd cards
	 * @param context
	 */
	public SdCardFactory(Context context) {
		cardSet = new HashSet<SdCard>();
		getCards(context);
	}

	/**
	 * returns an SdCard array 
	 * @return
	 */
	public SdCard[] getSdCards(){
		return cardArray;
	}

	private void getCards(Context context){
		// Add primary external storage
		cardSet.add(new SdCard(Environment.getExternalStorageDirectory().getPath(), context.getString(R.string.sdcard)));
		// Add all secondary storages
		if(!TextUtils.isEmpty(rawSecondaryStoragesStr))
		{
			// All Secondary SD-CARDs split into array
			final String[] sdCards = rawSecondaryStoragesStr.split(File.pathSeparator);
			int x=1;
			for( String card: sdCards){
				cardSet.add(new SdCard(card, context.getString(R.string.external_card)+x));
			}
		}
		cardArray = cardSet.toArray(new SdCard[cardSet.size()]);
	}

	public boolean isPathUpFromRoot(File folder) {
		Log.L(TAG, "Folder.getPath", folder.getPath());
		for(SdCard card : getSdCards()){
			Log.L(TAG, "card.path.getParent", new File(card.path).getParent());
			if(new File(card.path).getParent().equals(folder.getPath())){
				return true;
			}
		}
		return false;
	}

	public String toDisplay(String path){
		String ret = path;
		// Replace Environment.getExternalStorageDirectory() with /sdcard for display purposes only
		for( SdCard card: getSdCards()){
			if( path.startsWith(card.path)){
				String end = path.substring(card.path.length());
				ret = card.name + (end.length()>0?end:"");
				break;
			}
		}
		return ret;
	}
	public void log(){
		for( SdCard card: getSdCards()){
			Log.d(TAG, "CARD: "+card.name+"-"+card.path);
		}
	}

	/**
	 * returns True if given path is one of the sdcard root names
	 * @param path
	 * @return
	 */
	public boolean isRoot(String path){
		for( SdCard card: getSdCards()){
			if( path.startsWith(card.name)){
				return true;
			}
		}
		return false;
	}
	/**
	 * Replaces the sdcard display name with the real path, if found, otherwise returns same path
	 * the real path.
	 * @param path
	 * @return
	 */
	public String toPath(String path) {
		String ret = path;

		for( SdCard card: getSdCards()){
			if( path.startsWith(card.name)){
				String end = path.substring(card.name.length());
				ret = card.path + (end.length()>0?end:"");
				break;
			}
		}
		return ret;
	}
}