package com.bondevans.chordinator.db;

import com.bondevans.chordinator.Log;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class SongDB extends SQLiteOpenHelper {
	private static final String TAG = "SongDB";
	public static final String DATABASE_NAME = "songdata.db";
    public static final String TABLE_SONG = "song";
    public static final String TABLE_RECENT = "recent";
    public static final String TABLE_FAVOURITE = "favourite";
    public static final String TABLE_SETLIST = "setlist";
    public static final String TABLE_SETITEM = "setitem";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_FILE_PATH = "file_path";
	public static final String COLUMN_FILE_NAME = "file_name";
	public static final String COLUMN_TITLE = "title";
	public static final String COLUMN_ARTIST = "artist";
	public static final String COLUMN_COMPOSER = "composer";
	public static final String COLUMN_FAV = "fav";
	public static final String COLUMN_LAST_ACCESS = "last_access";
	public static final String COLUMN_SETLIST_ID= "setlist_id";
	public static final String COLUMN_SONG_ID= "song_id";
	public static final String COLUMN_SET_ORDER = "set_order";
	public static final String COLUMN_SET_NAME = "set_name";
    public static final String TIMESTAMP_FORMAT="yyyy-MM-dd HH:mm:ss";

    public static final String SONGS_BASE_PATH = "songs";
	public static final String CONTENT_SONGS = "/" + SONGS_BASE_PATH;
	public static final String CONTENT_SETS =  "/" + SONGS_BASE_PATH+ "/sets";
	public static final String CONTENT_SETITEMS = "/" + SONGS_BASE_PATH+ "/setitems";

	private static final int DATABASE_VERSION = 5;

	private static String FOREIGN_KEYS_ON = "PRAGMA foreign_keys = ON;";
	// Database creation sql statement
	private static final String CREATE_SONG = "create table song "+
			"("+COLUMN_ID+" integer primary key autoincrement, "+ 
			COLUMN_FILE_PATH+" text not null, "+
			COLUMN_FILE_NAME+" text not null, "+
			COLUMN_TITLE+" text not null, "+
			COLUMN_ARTIST+" text, "+
			COLUMN_COMPOSER+" text,"+
			// FAV IS INTEGER - 0= Not favourite, 1=IS Favourite
			COLUMN_FAV+" INTEGER not null default 0,"+
			COLUMN_LAST_ACCESS+" TEXT not null default CURRENT_TIMESTAMP"+
			");";

	private static final String CREATE_SETLIST = "CREATE TABLE "+TABLE_SETLIST+
			"("+COLUMN_ID+" integer primary key autoincrement,"+
			COLUMN_SET_NAME+" NOT NULL);";
	private static final String CREATE_SETITEM = "CREATE TABLE "+TABLE_SETITEM+
		"("+COLUMN_ID+" integer primary key autoincrement, "+
		COLUMN_SETLIST_ID+" INTEGER NOT NULL, "+
		COLUMN_SET_ORDER+" INTEGER NOT NULL, "+
		COLUMN_SONG_ID+" INTEGER NOT NULL, "+
		"FOREIGN KEY ("+COLUMN_SETLIST_ID+") REFERENCES "+TABLE_SETLIST+"("+COLUMN_ID+"), "+
		"FOREIGN KEY ("+COLUMN_SONG_ID+") REFERENCES "+TABLE_SONG+"("+COLUMN_ID+"));";
	
	public SongDB(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// Method is called during creation of the database
	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(FOREIGN_KEYS_ON);
		Log.d(TAG, "Creating SONG table: ["+ CREATE_SONG+"]");
		database.execSQL(CREATE_SONG);
		Log.d(TAG, "Creating SETLIST table: ["+ CREATE_SETLIST+"]");
		database.execSQL(CREATE_SETLIST);
		Log.d(TAG, "Creating SETITEM table: ["+ CREATE_SETITEM+"]");
		database.execSQL(CREATE_SETITEM);
	}

	// Method is called during an upgrade of the database, e.g. if you increase
	// the database version
	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		Log.d(TAG,
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will delete everything!!!");
		database.execSQL("DROP TABLE IF EXISTS song");
		database.execSQL("DROP TABLE IF EXISTS recent");
		database.execSQL("DROP TABLE IF EXISTS favourite");
		database.execSQL("DROP TABLE IF EXISTS setlist");
		database.execSQL("DROP TABLE IF EXISTS setitem");
		onCreate(database);
	}
}