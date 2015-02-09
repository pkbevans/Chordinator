package com.bondevans.chordinator.db;

public class SongProvider extends ChordinatorSongProvider {
	private static final String AUTHORITY = "com.bondevans.chordinator.dim";

	@Override
	public void addUris(){
		sURIMatcher.addURI(AUTHORITY, SongDB.SONGS_BASE_PATH, SONGS);
		sURIMatcher.addURI(AUTHORITY, SongDB.SONGS_BASE_PATH + "/#", SONG_ID);
		sURIMatcher.addURI(AUTHORITY, SongDB.SONGS_BASE_PATH + "/favourites", FAVOURITES);
		sURIMatcher.addURI(AUTHORITY, SongDB.SONGS_BASE_PATH + "/recent", RECENT);
		sURIMatcher.addURI(AUTHORITY, SongDB.SONGS_BASE_PATH + "/sets", SETS);
		sURIMatcher.addURI(AUTHORITY, SongDB.SONGS_BASE_PATH + "/sets/#", SET_ID);
		sURIMatcher.addURI(AUTHORITY, SongDB.SONGS_BASE_PATH + "/setitems", SETITEMS);
		sURIMatcher.addURI(AUTHORITY, SongDB.SONGS_BASE_PATH + "/setitems/#", SETITEMS_FOR_SET); // Set items for given set
		sURIMatcher.addURI(AUTHORITY, SongDB.SONGS_BASE_PATH + "/setitem/#", SETITEM_ID);
	}
}
