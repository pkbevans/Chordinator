package com.bondevans.chordinator.setlist;

public class SetSong{
    // v6.0.0 - Removed Parcelable  - not used.
	public long id;
	public String title;
	public String artist;
	public String composer;
	public String filePath;
	public int setOrder;

	public SetSong(long id, String title, String filePath, int setOrder){
		this.id = id;
		this.title = title;
		this.filePath = filePath;
		this.setOrder = setOrder;
	}
	public SetSong(long id, String title, String artist, String composer, String filePath, int setOrder){
		this.id = id;
		this.title = title;
		this.artist = artist;
		this.composer = composer;
		this.filePath = filePath;
		this.setOrder = setOrder;
	}

	public String toString(){
		return title;
	}
}
