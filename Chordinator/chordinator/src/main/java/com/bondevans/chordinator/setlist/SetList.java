package com.bondevans.chordinator.setlist;

import java.io.File;

import java.util.Vector;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;

import android.widget.Toast;

import com.bondevans.chordinator.ChordinatorException;
import com.bondevans.chordinator.Log;
import com.bondevans.chordinator.SongUtils;
import com.bondevans.chordinator.Statics;
import com.bondevans.chordinator.db.DBUtils;
import com.bondevans.chordinator.db.SongDB;
import com.bondevans.chordinator.utils.Ute;

public class SetList {
	public static final String TAG = "SetList";
	public static final String SETLIST_PREFIX = "SETLIST-";
	public static final String SETLIST_SEPARATER = "\n";
	private String mSetName;
	private String mSetListPath;
	private Vector<String> mSongPaths = new Vector<String>();
	private int mCurrentSong=0;
	private String mAuthority;
	
	/**
	 * Constructor takes a FILE and loads up the set list from it.  The set list is simply a
	 * list of song file paths. Throws an exception if the file is in the wrong format
	 * @param setListFile
	 */
	public SetList (String authority, File setListFile)throws Exception{
		Log.d(TAG, "HELLO SetList2");
		mAuthority = authority;
		loadSet(setListFile);
		// Throw an exception if there is a problem loading the file
	}
	
	@SuppressLint("SdCardPath")
	private void loadSet(File setListFile) throws Exception{
		mSetListPath = setListFile.getPath();
		mSetName = setListFile.getName().substring(SETLIST_PREFIX.length());
		Log.d(TAG, "HELLO SetList2 setName=["+mSetName+"] setListPath=["+mSetListPath+"]");
		String set = SongUtils.loadFile(setListFile.getPath(),"");
		// read each line and add to Vector
		String [] theSongs = set.split(SETLIST_SEPARATER);
		for( String filePath :theSongs){
			// Remove any line feeds/carriage returns etc
			if( filePath.trim().compareToIgnoreCase("") != 0){
				mSongPaths.add(DBUtils.doPath(filePath,"/sdcard", Environment.getExternalStorageDirectory().getPath()));
			}
		}
		dbListSongs();
	}
	/**
	 * Constructor takes a string path to the set
	 * @param setListFile
	 * @throws Throwable
	 */
	public SetList(String authority, String setListFile) throws Throwable{
		Log.d(TAG, "HELLO SetList1");
		mAuthority = authority;
		loadSet(new File(setListFile));
	}
	
	/**
	 * Constructor takes set_id and loads up a set from the DB
	 * @param setId
	 */
	public SetList(ContentResolver cr, String authority, long setId, String setName){
		mSetName = setName;
		mSetListPath = Statics.CHORDINATOR_DIR+SETLIST_PREFIX+setName;
		mAuthority = authority;
		String [] projection = {SongDB.COLUMN_SONG_ID};
		// create cursor to get all the set items in the correct order
		Cursor setItemCursor = cr.query(
				Uri.withAppendedPath(DBUtils.SETITEM(mAuthority),
						String.valueOf(setId)), projection, null, null, SongDB.COLUMN_SET_ORDER + " ASC");

		// load them into the array
		while(setItemCursor.moveToNext()){
			mSongPaths.add(getSong(cr, Long.parseLong(setItemCursor.getString(0))));
		}
		setItemCursor.close();
	}
	private String getSong(ContentResolver cr, long songId) {
		String ret = "";
		// 	Get songFilePath+songFileName for given song_id
		String [] projection = {SongDB.COLUMN_FILE_PATH, SongDB.COLUMN_FILE_NAME, SongDB.COLUMN_TITLE};
		// create cursor to get all the set items in the correct order
		Cursor songCursor = cr.query(
				Uri.withAppendedPath(DBUtils.SONG(mAuthority),
						String.valueOf(songId)), projection, null, null, null);
		
		if( songCursor.moveToFirst()){
			ret = Ute.doPath(songCursor.getString(0),songCursor.getString(1));
		}
		else{
			Log.e(TAG, "OOPS - Can't get song:["+songId+"]");
		}
		songCursor.close();
		return ret;
	}

	/**
	 * Constructor creates empty Set list
	 */
	public SetList(){
		mSetName="";
		mSetListPath="";
		mAuthority="";
	}

	/**
	 * Return an array containing all of the songs
	 * @return
	 */
	public String [] getSongs(){
		return (String[]) mSongPaths.toArray();
	}
	
	/**
	 * @return the setName
	 */
	public String getSetName() {
		return mSetName;
	}

	//
	public String getFirstSongString() throws ChordinatorException{
		return getFirstSong().getName();
	}
	public String getLastSongString() throws ChordinatorException{
		return getLastSong().getName();
	}
	public String getNextSongString(){
		return getNextSong().getName();
	}
	public String getPrevSongString(){
		return getPrevSong().getName();
	}

	//
	public File getFirstSong() throws ChordinatorException{
		if(mSongPaths.size()==0){
			throw new ChordinatorException("Empty set");
		}
		mCurrentSong=0;
		return new File(mSongPaths.get(mCurrentSong));
	}
	public File getLastSong() throws ChordinatorException{
		if(mSongPaths.size()==0){
			throw new ChordinatorException("Empty set numpty!");//Resources.getSystem().getString(R.string.empty_set));
		}
		mCurrentSong = mSongPaths.size()-1;
		return new File(mSongPaths.get(mCurrentSong));
	}
	public File getNextSong(){
		if(++mCurrentSong>=mSongPaths.size()){
			mCurrentSong=0;
		}
		return new File(mSongPaths.get(mCurrentSong));
	}
	public File getPrevSong(){
		if(--mCurrentSong<0){
			mCurrentSong=mSongPaths.size()-1;
		}
		return new File(mSongPaths.get(mCurrentSong));
	}
	public void addSong(String songPath){
		mSongPaths.add(songPath);
		dbListSongs();
	}
	
	/**
	 * Writes out set list to file on disk
	 */
	public void writeSetList(){
		if( mSetListPath.equalsIgnoreCase("")){
			return;
		}
		//Write out to file in a separate thread
		//		new Thread(new Runnable(){
		//			public void run(){
		try {
			Log.d(TAG,"HELLO saving file: "+mSetListPath);
			SongUtils.writeFile(mSetListPath, toString());
		} 
		catch (SecurityException e){
			errMsgToast(e.getMessage());
		} 
		catch (Exception e) {
			errMsgToast(e.getMessage());
		}
		//			}
		//		}).start();
	}
	private void errMsgToast(String msg){
		Toast.makeText(null, msg, Toast.LENGTH_LONG).show();
	}

	public int size(){
		return mSongPaths.size();
	}
	/**
	 * Move song in specified position (first = zero) up by one position
	 * 
	 * @param position
	 */
	public void moveUp(int position){
		// Do nothing if position=0
		if(position>0 && position < mSongPaths.size()){
			// Get current contents of new position
			String tmp = mSongPaths.get(position-1);
			// Copy specified song to new position
			mSongPaths.setElementAt(mSongPaths.get(position), position-1);
			// COpy old contents to old position
			mSongPaths.setElementAt(tmp, position);
		}
		dbListSongs();
	}
	/**
	 * Move song in specified position (first = zero) up by one position
	 * 
	 * @param position
	 */
	public void moveDown(int position){
		// Do nothing if position>=songPaths.size()
		if(position>=0 && position < mSongPaths.size()-1){
			// Get current contents of new position
			String tmp = mSongPaths.get(position+1);
			// Copy specified song to new position
			mSongPaths.setElementAt(mSongPaths.get(position), position+1);
			// Copy old contents to old position
			mSongPaths.setElementAt(tmp, position);
		}
		dbListSongs();
	}
	/**
	 * Delete the song at given position
	 * @param position
	 */
	public void delete(int position){
		mSongPaths.remove(position);
		dbListSongs();
	}
	private void dbListSongs(){
		int i=0;
		while (i<mSongPaths.size()){
			Log.d(TAG, "HELLO POS ["+i+"]["+mSongPaths.elementAt(i));
			++i;
		}
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String ret="";
		int i=0;
		if( mSongPaths.size()==0){
			ret = "";
		}
		else if( mSongPaths.size() == 1){
			ret = mSongPaths.firstElement();
		}
		else{
			// MOre than 1 song in set list so need separaters
			while (i<mSongPaths.size()-1){
				ret = ret.concat(mSongPaths.elementAt(i)+SetList.SETLIST_SEPARATER);
				++i;
			}
			ret = ret.concat(mSongPaths.elementAt(i));
		}
//		Log.d(TAG, "HELLO returning ["+ret+"]");
		return ret;
	}

	/**
	 * Import old-style setlist from SETLIST file to new-style DB set
	 * @param filePath
	 */
	public void importSet(ContentResolver cr){
		// Create SET in DB with name from File
		Log.d(TAG, "importSet; ["+this.mSetName+"]");
		long setId = DBUtils.createSet(cr, mAuthority, mSetName);
		int i=0;
		while (i<mSongPaths.size()){
			Log.d(TAG, "HELLO POS ["+i+"]["+mSongPaths.elementAt(i));
			// Find each song by filepath to get the ID
			long songId=0;
			songId = DBUtils.getSongIdFromPath(cr, mAuthority, mSongPaths.elementAt(i));
			if(songId != 0){
				// Add song to set just created.
				try {
					DBUtils.addSongToSet(cr, mAuthority, setId, songId);
				} catch (ChordinatorException e) {
					// Ignore Already-in-set errors
				}
			}
			++i;
		}
	}

	/**
	 * @return the mSetListPath
	 */
	public String getSetListPath() {
		return mSetListPath;
	}
}
