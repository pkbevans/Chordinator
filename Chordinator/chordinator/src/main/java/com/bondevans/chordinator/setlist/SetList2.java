package com.bondevans.chordinator.setlist;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import com.bondevans.chordinator.ChordinatorException;
import com.bondevans.chordinator.Log;
import com.bondevans.chordinator.db.DBUtils;
import com.bondevans.chordinator.db.SongDB;
import com.bondevans.chordinator.utils.Ute;

public class SetList2 {
	private static final String TAG = "SetList2";
	private List<SetSong> 		mSongs = new ArrayList<SetSong>();
	private int 				mCurrentSong=0;

	/**
	 * Constructor takes setId and loads up a set from the DB
	 * @param cr ContentResolver
	 * @param authority Database authority (based on app version
	 * @param setId The set to be loaded up
	 */
	public SetList2(ContentResolver cr, String authority, long setId){
		String [] projection = {SongDB.COLUMN_SONG_ID, SongDB.COLUMN_TITLE, SongDB.COLUMN_ARTIST, SongDB.COLUMN_COMPOSER,
				SongDB.COLUMN_FILE_PATH, SongDB.COLUMN_FILE_NAME, SongDB.COLUMN_SET_ORDER};
		// create cursor to get all the set items in the correct order
		Cursor setItemCursor = cr.query(
				Uri.withAppendedPath(DBUtils.SETITEM(authority),
						String.valueOf(setId)), projection, null, null, SongDB.COLUMN_SET_ORDER + " ASC");

		// load them into the array
		while(setItemCursor.moveToNext()){
			Log.d(TAG, "HELLO adding song["+setItemCursor.getString(1)+"] for set["+setId+"] set_order=["+setItemCursor.getString(6)+"]");
			mSongs.add(new SetSong(setItemCursor.getLong(0),    // id
					setItemCursor.getString(1), // title
					setItemCursor.getString(2), // artist
					setItemCursor.getString(3), // composer
					Ute.doPath(setItemCursor.getString(4),setItemCursor.getString(5)),  //filepath
					setItemCursor.getInt(6)));  // set order
		}
		mCurrentSong = -1;	// Make sure that getNext/getPrev get the correct song 
		// Close the query
		setItemCursor.close();
	}

	public int size(){
		return mSongs.size();
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
	 * Set the current Song to given id
	 * @param id id of Song
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

	/**
	 * Remove song at given position
	 * @param index position to remove
	 */
	public void removeSong(int index){
		mSongs.remove(index);
	}

	/**
	 * Returns an array of the songs
	 * @return an array of the songs
	 */
	public List<SetSong> getSongs(){
		return mSongs;
	}

	/**
	 * Returns an array of the songs - filtered to match the given filter
	 * @param filter text to filter on
	 * @return a list of songs that match the filter
	 */
	public List<SetSong> getFilteredSongs(String filter){
		List<SetSong> filteredSongs = new ArrayList<SetSong>();
		for(int i=0;i<mSongs.size();i++){
			SetSong song = mSongs.get(i);
			String regex = ".*"+filter.toLowerCase()+".*";
			if(song.title.toLowerCase().matches(regex)||
					song.artist.toLowerCase().matches(regex)||
					song.composer.toLowerCase().matches(regex)
					){
				Log.d(TAG, "MATCHED " + song.title + " to " + filter);
				filteredSongs.add(song);
			}
		}
		return filteredSongs;
	}

	public void setSongs(List<SetSong> songs){
		mSongs = songs;
	}
}
