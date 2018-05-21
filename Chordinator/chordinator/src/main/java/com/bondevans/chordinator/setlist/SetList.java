package com.bondevans.chordinator.setlist;

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

import java.io.File;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SetList {
	public static final String TAG = "SetList";
	public static final String SETLIST_PREFIX = "SETLIST-";
	private static final String SETLIST_SEPARATER = "\n";
    private static final String SETLIST_SUFFIX = ".txt";
    private String mSetName;
	private String mSetListPath;
	private Vector<String> mSongPaths = new Vector<>();
	private int mCurrentSong=0;
	private String mAuthority;
    private static final CharSequence DROPBOX = "com.dropbox.android";
    private boolean mFoundDropBox=false;
    private static String mDropBoxFolder = null;
    private static Pattern mPathPattern = Pattern.compile("/scratch/.*",
            Pattern.DOTALL|Pattern.CASE_INSENSITIVE);

	/**
	 * Constructor takes a FILE and loads up the set list from it.  The set list is simply a
	 * list of song file paths. Throws an exception if the file is in the wrong format
	 * @param authority Authority
	 * @param setListFile Set
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
        // Remove .txt from set name if present in file
        mSetName=mSetName.replace(SETLIST_SUFFIX,"");
		Log.d(TAG, "HELLO SetList2 setName=["+mSetName+"] setListPath=["+mSetListPath+"]");
		String set = SongUtils.loadFile(setListFile.getPath(), null, "");
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
	 * @param setListFile Set list file
	 * @throws Throwable
	 */
	public SetList(String authority, String setListFile) throws Throwable{
		Log.d(TAG, "HELLO SetList1");
		mAuthority = authority;
		loadSet(new File(setListFile));
	}
	
	/**
	 * Constructor takes set_id and loads up a set from the DB
	 * @param setId Set ID
	 */
	public SetList(ContentResolver cr, String authority, long setId, String setName){
		mSetName = setName;
		mSetListPath = Statics.CHORDINATOR_DIR+SETLIST_PREFIX+setName+SETLIST_SUFFIX;
		mAuthority = authority;
		String [] projection = {SongDB.COLUMN_SONG_ID};
		// create cursor to get all the set items in the correct order
		Cursor setItemCursor = cr.query(
				Uri.withAppendedPath(DBUtils.SETITEM(mAuthority),
						String.valueOf(setId)), projection, null, null, SongDB.COLUMN_SET_ORDER + " ASC");

		// load them into the array
        if (setItemCursor != null) {
            while(setItemCursor.moveToNext()){
                mSongPaths.add(getSong(cr, Long.parseLong(setItemCursor.getString(0))));
            }
            setItemCursor.close();
        }
	}
	private String getSong(ContentResolver cr, long songId) {
		String ret = "";
		// 	Get songFilePath+songFileName for given song_id
		String [] projection = {SongDB.COLUMN_FILE_PATH, SongDB.COLUMN_FILE_NAME, SongDB.COLUMN_TITLE};
		// create cursor to get all the set items in the correct order
		Cursor songCursor = cr.query(
				Uri.withAppendedPath(DBUtils.SONG(mAuthority),
						String.valueOf(songId)), projection, null, null, null);

        if (songCursor != null) {
            if( songCursor.moveToFirst()){
                ret = Ute.doPath(songCursor.getString(0),songCursor.getString(1));
            }
            else{
                Log.e(TAG, "OOPS - Can't get song:["+songId+"]");
            }
            songCursor.close();
        }
		return ret;
	}

	/**
	 * Return an array containing all of the songs
	 * @return All of the songs
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
    String getFirstSongString() throws ChordinatorException{
		return getFirstSong().getName();
	}
	String getNextSongString(){
		return getNextSong().getName();
	}

	//
	public File getFirstSong() throws ChordinatorException{
		if(mSongPaths.size()==0){
			throw new ChordinatorException("Empty set");
		}
		mCurrentSong=0;
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
		} catch (Exception e) {
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
	 * @param position Position
	 */
    void moveUp(int position){
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
	 * @param position Position
	 */
    void moveDown(int position){
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
	 * @param position Position
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
	 * @param cr Content resolver
	 */
	public void importSet(ContentResolver cr){
		// Create SET in DB with name from File
		Log.d(TAG, "importSet; ["+this.mSetName+"]");
		long setId = DBUtils.createSet(cr, mAuthority, mSetName);
		int i=0;
		while (i<mSongPaths.size()){
			Log.d(TAG, "HELLO POS ["+i+"]["+mSongPaths.elementAt(i));
			// Find each song by filepath to get the ID
			long songId = DBUtils.getSongIdFromPath(cr, mAuthority, mSongPaths.elementAt(i));
            if(songId == 0){
                // If song not found and it is a DropBox song, check if the dropbox folder has changed
                // slightly and if so, see if we have the same song in a slightly different path
                if(mSongPaths.elementAt(i).contains(DROPBOX) && findDropBoxFolder()){
                    songId = DBUtils.getSongIdFromPath(cr, mAuthority, mDropBoxFolder+ dropBoxSongPath(mSongPaths.elementAt(i)));
                }
                else if(mSongPaths.elementAt(i).contains(Statics.CHORDINATOR)){
                    // If its in the ..../chordinator/ folder swap out the /sdcard/ bit and see if its in there
                    songId = DBUtils.getSongIdFromPath(cr, mAuthority, tweekChordinatorPath(mSongPaths.elementAt(i)));
                }
            }
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

    private String tweekChordinatorPath(String path) {
        Log.d(TAG, "tweekChordinatorPath:"+path);
        path=path.replaceFirst("/.*"+Statics.CHORDINATOR, Statics.CHORDINATOR_DIR);
        Log.d(TAG, "tweekChordinatorPath:"+path);
        return path;
    }

    /**
     * @return the mSetListPath
     */
    String getSetListPath() {
        return mSetListPath;
    }

    /**
     * Extract the end of the path after the /sdcard/android/com.dropbox.android/files/uXXXXXX/ bit
     * @param path Path
     * @return Returns the song bit at the end
     */
    private static String dropBoxSongPath(String path){
        String ret="";

        Matcher m = mPathPattern.matcher(path);
        if (m.find()) {
            ret = m.group(0); // Access the submatch
            Log.d(TAG, "dropBoxSongPath=["+ret+"]");
        }
        return ret;
    }

    /**
     * Find the local DropBox folder on the  sdcard where DB keeps the synced files
     * Should be: /{path to sdcard}/Android/data/comd.dropbox.android/files/uXXXX/scratch/
     * @return Returns the DropBox folder
     */
    private boolean findDropBoxFolder() {
        if(mFoundDropBox){
            // We looked but did we find it?
            return mDropBoxFolder != null;
        }
        else
        {
            // We haven't looked yet
            mFoundDropBox=true;// Only look once - not every time
            // First get the sdcard folder
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                            "/Android/data");
            // then look for DROPBOX in there
            for (File x: file.listFiles()) {
                if(x.isDirectory() && x.getAbsolutePath().contains(DROPBOX)){
                    Log.d(TAG, "HELLO FOUND DropBox" + x.getAbsolutePath());
                    // Now find the /files/ folder
                    File y = new File(x.getAbsolutePath()+"/files/");
                    if(y.exists()&& y.isDirectory()){
                        for(File z: y.listFiles()){
                            // Now find the /uXXXXXX/ folder - assume its the only folder in here
                            if(z.isDirectory()){
                                Log.d(TAG, "Got DropBox folder: ["+z.getAbsolutePath()+"]");
                                mDropBoxFolder=z.getAbsolutePath();
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}
