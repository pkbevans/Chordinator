package com.bondevans.chordinator.setlist;

public class SetSong {
	public long id;
	public String title;
	public String filePath;

	SetSong(long id, String title, String filePath){
		this.id = id;
		this.title = title;
		this.filePath = filePath;
	}
}
