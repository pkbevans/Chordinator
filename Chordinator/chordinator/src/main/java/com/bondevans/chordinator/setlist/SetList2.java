package com.bondevans.chordinator.setlist;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;


import com.bondevans.chordinator.ChordinatorException;
import com.bondevans.chordinator.Log;
import com.bondevans.chordinator.db.DBUtils;
import com.bondevans.chordinator.db.SongDB;
import com.bondevans.chordinator.utils.Ute;

public class SetList2 {
	public static final String 	TAG = "SetList2";
	private String 				mSetName;
	private long				mSetId;
	private List<SetSong> 		mSongs = new ArrayList<SetSong>();
	private int 				mCurrentSong=0;
	private String 				mAuthority;
	
	/**
	 * Constructor takes set_id and loads up a set from the DB
	 * @param set_id
	 */
	public SetList2(ContentResolver cr, String authority, long set_id){
		mSetId = set_id;
		mAuthority = authority;
		String [] projection = {SongDB.COLUMN_SONG_ID, SongDB.COLUMN_TITLE, SongDB.COLUMN_FILE_PATH, 
				SongDB.COLUMN_FILE_NAME};
		// create cursor to get all the set items in the correct order
		Cursor setItemCursor = cr.query(
				Uri.withAppendedPath(DBUtils.SETITEM(mAuthority),
						String.valueOf(set_id)), projection, null, null, SongDB.COLUMN_SET_ORDER + " ASC");

		// load them into the array
		while(setItemCursor.moveToNext()){
			Log.d(TAG, "HELLO adding song["+setItemCursor.getString(1)+"] for set["+set_id+"]");
			mSongs.add(new SetSong(setItemCursor.getLong(0), setItemCursor.getString(1), Ute.doPath(setItemCursor.getString(2),setItemCursor.getString(3))));
		}
		mCurrentSong = -1;	// Make sure that getNext/getPrev get the correct song 
		// Close the query
		setItemCursor.close();
	}

	/**
	 * Constructor creates empty Set list
	 */
	public SetList2(){
		mSetName="";
	}

	/**
	 * @return the setName
	 */
	public String getSetName() {
		return mSetName;
	}

	public String getFirstTitle()throws ChordinatorException{
		if(mSongs.size()==0){
			throw new ChordinatorException("EMPTY SET");
		}
		return mSongs.get(0).title;
	}
	public String getNextTitle() throws ChordinatorException{
		if(mSongs.size()==0){
			throw new ChordinatorException("EMPTY SET");
		}
		if(++mCurrentSong >= mSongs.size()){
			mCurrentSong=0;
		}
		return mSongs.get(mCurrentSong).title;
	}
	public void writeSetList(ContentResolver cr){
		Log.d(TAG, "HELLO - writing Set to DB");
		// Delete all set items first
		cr.delete( Uri.withAppendedPath(
				DBUtils.SETITEM(mAuthority), String.valueOf(mSetId)), 
						null, null);

		// then iterate thru new list and give each one a set_order
		int i=0;
		while (i<mSongs.size()){
			Log.d(TAG, "HELLO Add song ["+mSongs.get(i).title+"] set_order=["+i+"]");
			ContentValues values = new ContentValues();
			values.put(SongDB.COLUMN_SETLIST_ID, mSetId);
			values.put(SongDB.COLUMN_SONG_ID, mSongs.get(i).id);
			values.put(SongDB.COLUMN_SET_ORDER, i);

			cr.insert(DBUtils.SETITEM(mAuthority), values);
			++i;
		}
		dbListSongs();
	}

	public int size(){
		return mSongs.size();
	}
	/**
	 * Move song in specified position (first = zero) up by one position
	 * 
	 * @param position
	 */
	public void moveUp(int position){
		// Do nothing if position=0
		if(position>0 && position < mSongs.size()){
			// Get current contents of new position
			SetSong tmp = mSongs.get(position-1);
			// Copy specified song to new position
			mSongs.set(position-1, mSongs.get(position));
			// COpy old contents to old position
			mSongs.set(position, tmp);
		}
		dbListSongs();
	}
	/**
	 * Move song in specified position (first = zero) up by one position
	 * 
	 * @param position
	 */
	public void moveDown(int position){
		// Do nothing if position>=songs.size()
		if(position>=0 && position < mSongs.size()-1){
			// Get current contents of new position
			SetSong tmp = mSongs.get(position+1);
			// Copy specified song to new position
			mSongs.set(position+1, mSongs.get(position));
			// Copy old contents to old position
			mSongs.set(position, tmp);
		}
		dbListSongs();
	}
	/**
	 * Delete the song at given position
	 * @param position
	 */
	public void delete(int position){
		mSongs.remove(position);
		dbListSongs();
	}
	private void dbListSongs(){
		int i=0;
		while (i<mSongs.size()){
			Log.d(TAG, "HELLO POS ["+i+"]["+mSongs.get(i).title+"]");
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
		if( mSongs.size()==0){
			ret = "";
		}
		else if( mSongs.size() == 1){
			ret = ""+mSongs.get(0).title;
		}
		else{
			// MOre than 1 song in set list so need separaters
			while (i<mSongs.size()-1){
				ret = ret.concat(mSongs.get(i).title+",");
				++i;
			}
			ret = ret.concat(""+mSongs.get(i).title);
		}
//		Log.d(TAG, "HELLO returning ["+ret+"]");
		return ret;
	}
	
	/**
	 * Set the current Song to the Song from @param id 
	 * @param id
	 */
	public void setCurrentSong(long id){
		int i=0;
		while (i<mSongs.size()){
			Log.d(TAG, "HELLO POS ["+i+"]["+mSongs.get(i).title+"]");
			if(mSongs.get(i).id == id){
				mCurrentSong = i;
				return;
			}
			++i;
		}
	}

	/**
	 * Set the current Song to the Song at @param position
	 * @param position
	 */
	public void setCurrentSong(int position){
		mCurrentSong = position;
	}

	/**
	 * Return the current song position
	 * @return
	 */
	public int getCurrentSong(){
		return mCurrentSong;
	}
	
	public SetSong getCurrentSetSong(){
		return mSongs.get(mCurrentSong);
	}
	public SetSong getNextSong()throws ChordinatorException{
		if(mSongs.size()==0){
			throw new ChordinatorException("EMPTY SET");
		}
		if(++mCurrentSong >= mSongs.size()){
			mCurrentSong=0;
		}
		return mSongs.get(mCurrentSong);
	}
	public SetSong getPrevSong()throws ChordinatorException{
		if(mSongs.size()==0){
			throw new ChordinatorException("EMPTY SET");
		}
		if(--mCurrentSong < 0){
			mCurrentSong=mSongs.size()-1;
		}
		return mSongs.get(mCurrentSong);
	}

	public void moveSong(int fromIndex, int toIndex){
		// Get the song from the from location
		SetSong songToMove = mSongs.get(fromIndex);
		// remove it
		mSongs.remove(fromIndex);
		// then insert it into the to location
		mSongs.add(toIndex, songToMove);
	}
	public void removeSong(int index){
		mSongs.remove(index);
	}	
}
