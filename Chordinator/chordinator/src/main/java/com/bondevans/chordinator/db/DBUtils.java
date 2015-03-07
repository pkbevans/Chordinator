package com.bondevans.chordinator.db;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;


import com.bondevans.chordinator.ChordinatorException;
import com.bondevans.chordinator.Log;
import com.bondevans.chordinator.R;
import com.bondevans.chordinator.SongFile;
import com.bondevans.chordinator.SongUtils;
import com.bondevans.chordinator.utils.Ute;

public class DBUtils {
	private static final String TAG = "DBUtils";

	public static Uri SONG(String authority){
		return Uri.parse("content://" + authority + SongDB.CONTENT_SONGS);
	}
	public static Uri SET(String authority){
		return Uri.parse("content://" + authority + SongDB.CONTENT_SETS);
	}
	public static Uri SETITEM(String authority){
		return Uri.parse("content://" + authority + SongDB.CONTENT_SETITEMS);
	}

	/**
	 * Convenience method to add a song to the DB
	 * @param songPath
	 * @param songFile
	 * @param title
	 * @param artist
	 * @param composer
	 * @return
	 */
	public static Uri addSong(ContentResolver cr, String authority, String songPath, String songFile, String title, String artist, String composer){
		// This song isn't in the DB yet so add it in
		ContentValues values = new ContentValues();
		values.put(SongDB.COLUMN_FILE_PATH, doMnt(songPath));
		values.put(SongDB.COLUMN_FILE_NAME, songFile);
		SimpleDateFormat sdf = new SimpleDateFormat(SongDB.TIMESTAMP_FORMAT);
		values.put(SongDB.COLUMN_LAST_ACCESS, sdf.format(new Date()));
		values.put(SongDB.COLUMN_TITLE, SongFile.toTitleCase(title));
		values.put(SongDB.COLUMN_ARTIST, SongFile.toTitleCase(SongFile.removeThe(artist)));
		values.put(SongDB.COLUMN_COMPOSER, SongFile.toTitleCase(composer));
		Log.d(TAG, "HELLO addSong: ["+values.getAsString(SongDB.COLUMN_TITLE)+"]["+values.getAsString(SongDB.COLUMN_FILE_PATH)+"]["+
				values.getAsString(SongDB.COLUMN_FILE_NAME)+"]");
		return cr.insert(SONG(authority), values);
	}
	
	/**
	 * Convenience method to update a song in the DB
	 * @param cr
	 * @param authority
	 * @param songId
	 * @param title
	 * @param artist
	 * @param composer
	 */
	public static void updateSong(ContentResolver cr, String authority, long songId, String title, String artist, String composer){
		// This song already exists, so just update the last_access column
		ContentValues values = new ContentValues();//Values to update
		SimpleDateFormat sdf = new SimpleDateFormat(SongDB.TIMESTAMP_FORMAT);
		values.put(SongDB.COLUMN_LAST_ACCESS, sdf.format(new Date()));
		values.put(SongDB.COLUMN_TITLE, SongFile.toTitleCase(title));
		values.put(SongDB.COLUMN_ARTIST, SongFile.toTitleCase(SongFile.removeThe(artist)));
		values.put(SongDB.COLUMN_COMPOSER, SongFile.toTitleCase(composer));

		Log.d(TAG, "HELLO updateSong: ["+values.getAsString(SongDB.COLUMN_TITLE)+"]["+values.getAsString(SongDB.COLUMN_ARTIST)+"]");
		cr.update(Uri.parse(SONG(authority)+"/"+songId),
				values, 	// Columns to update
				null,	// where clause
				null);	// any args in the where clause
	}

	/**
	 * Convenience method to update a Set song (set_order) in the DB
	 * @param cr
	 * @param authority
	 * @param setId
	 * @param songId
	 * @param setOrder
	 */
	public static void updateSetSong(ContentResolver cr, String authority, long setId, long songId, int setOrder){
		// This song already exists, so just update the last_access column
		ContentValues values = new ContentValues();//Values to update
		values.put(SongDB.COLUMN_SET_ORDER, setOrder);
		String whereClause = SongDB.COLUMN_SONG_ID + "="+songId;

		Log.d(TAG, "HELLO updateSetSong: ["+setId+"]["+songId+"]");
		cr.update(Uri.withAppendedPath(DBUtils.SETITEM(authority), String.valueOf(setId)),
				values, 	// Columns to update
				whereClause,	// where clause
				null);	// any args in the where clause
	}

	/**
	 * DIRTY HACK!! replace any instance of "/mnt/sdcard/" with "/sdcard/" when either adding or looking up a song
	 * 2.4.0 Not doing this any more
	 * @param path
	 * @return
	 */
	public static String doMnt(String path){
		// remove any slashes on the end of the path
		Log.d(TAG, "HELLO doMnt in["+path+"]");
		if( path.endsWith("/")){
			path = path.substring(0, path.length()-1);
		}
//		HACK REMOVED
		Log.d(TAG, "HELLO doMnt out["+path+"]");
		return path;
	}
	public static long getSongIdFromPath(ContentResolver cr, String authority, String filePath){
		// separate filePath into Path+file
		File tmp = new File(filePath);
		String songFile = tmp.getName();
		String songPath = tmp.getParent();
		return getSongIdFromPath(cr, authority, songPath, songFile);
	}
	public static long getSongIdFromPath(ContentResolver cr, String authority, String songPath, String songFile){
		Log.d(TAG, "HELLO getSongIdFromPath songPath=["+doMnt(songPath)+"] songFile=["+songFile+"]");
		long song_id=0; 
		String[] projection = {SongDB.COLUMN_ID};
		String[] whereArgs = {doMnt(songPath),songFile};
		Cursor songCursor = cr.query(
				SONG(authority),
				projection, 
				SongDB.COLUMN_FILE_PATH + "=? and "+SongDB.COLUMN_FILE_NAME + "=?", 
				whereArgs, null);
		if (songCursor.moveToFirst()) {
			song_id = songCursor.getLong(0);
		}
		songCursor.close();
		Log.d(TAG, "HELLO - got song_id{"+song_id+"]");
		return song_id;
	}

	public static long createSet(ContentResolver cr, String authority, String setName){
		ContentValues values = new ContentValues();
		values.put(SongDB.COLUMN_SET_NAME, setName);
		Uri set = cr.insert(SET(authority), values);
		return ContentUris.parseId(set);
	}
	public static void addSongToSet(ContentResolver cr, String authority, long set_id, long song_id) throws ChordinatorException{
		Log.d(TAG, "HELLO AddSongToSet["+set_id+"]["+song_id+"]");
		// Check whether it is already in the set
		String [] selectionArgs = new String [] {""+song_id};
		String [] projection = new String [] {SongDB.TABLE_SONG+"."+SongDB.COLUMN_TITLE};

		Cursor setitem = cr.query(Uri.withAppendedPath(SETITEM(authority),
				String.valueOf(set_id)), projection, SongDB.COLUMN_SONG_ID+"=?", selectionArgs, null);
		if (setitem.moveToFirst()) {
			Log.d(TAG, "HELLO - ["+setitem.getString(0)+"] ALDREADY IN SET");
			setitem.close();
			throw new ChordinatorException("Song already in set");
		}
		else{
			setitem.close();
			ContentValues values = new ContentValues();
			values.put(SongDB.COLUMN_SETLIST_ID, set_id);
			values.put(SongDB.COLUMN_SONG_ID, song_id);
			values.put(SongDB.COLUMN_SET_ORDER, getMaxSetOrder(cr, authority, set_id)+1);// TODO HARDCODED
			Log.d(TAG, "HELLO - adding song to SET");

			cr.insert(SETITEM(authority), values);
		}
	}

	public static int deleteSongFromSet(ContentResolver cr, String authority, long setId, long songId){
		int rows = cr.delete(
				Uri.withAppendedPath(DBUtils.SETITEM(authority), String.valueOf(setId)),
				SongDB.COLUMN_SONG_ID + "="+songId, null);
		return rows;
	}

	public static int getMaxSetOrder(ContentResolver cr, String authority, long set_id) {
		int ret=-1;
		String[] projection = new String [] {"MAX(set_order)"};
		Cursor max = cr.query(Uri.withAppendedPath(SETITEM(authority), String.valueOf(set_id)), projection, null, null, null);
		if (max.moveToFirst()) {
			String m = max.getString(0);
			Log.d(TAG, "HELLO - GOT MAX[" + m + "]");
			max.close();
			try{
				ret = Integer.parseInt(m);
			}
			catch(NumberFormatException e){
				// do nothing (ret = -1)
			}
		}
		return ret;
	}
	public static int deleteSongByFile(ContentResolver cr, String authority, String filePath, String fileName){
		String [] selectionArgs = new String [] {doMnt(filePath), fileName};

		int rows = cr.delete(
				SONG(authority), 
				SongDB.COLUMN_FILE_PATH+"=? and "+SongDB.COLUMN_FILE_NAME+"=?", 
				selectionArgs);
		Log.d(TAG, "HELLO - Deleting Song: ["+doMnt(filePath)+"]["+fileName+"] rows=["+rows+"]");
		return rows;
	}

	public static void updateSongPaths(ContentResolver cr, String authority, String oldPath, String newPath) throws ChordinatorException{
		Log.d(TAG, "HELLO updateSongPaths["+oldPath+"]["+newPath+"]");
		long songId;
		String title;
		String path;
		String [] projection = new String [] {	SongDB.TABLE_SONG+"."+SongDB.COLUMN_ID,
												SongDB.TABLE_SONG+"."+SongDB.COLUMN_TITLE,
												SongDB.TABLE_SONG+"."+SongDB.COLUMN_FILE_PATH};

		Cursor allSongs = cr.query(SONG(authority), projection, null, null, null);
		while(allSongs.moveToNext()){
			// see if the current path contains "oldPath" ("/sdcard"), then replace it with "newPath" and update
			ContentValues values = new ContentValues();//Values to update
			songId = allSongs.getLong(0);
			title = allSongs.getString(1);
			Log.L(TAG, "TITLE", title);
			path = allSongs.getString(2);
			values.put(SongDB.COLUMN_FILE_PATH, doPath(path, oldPath, newPath ));

			Log.d(TAG, "HELLO updateSong: ["+values.getAsString(SongDB.COLUMN_TITLE)+"]["+values.getAsString(SongDB.COLUMN_ARTIST)+"]");
			cr.update(Uri.parse(SONG(authority)+"/"+songId),
					values, 	// Columns to update
					null,	// where clause
					null);	// any args in the where clause
		}
	}

	public static String doPath(String path, String oldCard, String newCard ) {
		String updatedPath=path;
		// replace first instance of oldCard in path with newCard - but only if its at the beginning of the string
		if(updatedPath.startsWith(oldCard)){
			updatedPath = updatedPath.replaceFirst("\\A"+oldCard, newCard);
		}
		Log.d(TAG, "HELLO B4=["+path+"] AFTER["+updatedPath+"]");
		return updatedPath;
	}
}
