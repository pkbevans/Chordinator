package com.bondevans.chordinator;

/**
 *
 * @author Paul
 */
public class Song {
	private String Title;
	private String Composer;
	private String Artist;
	private String SongText;
	private String ChordGrids;
	
	// Constructor
	public Song(String title, String composer, String artist, String songtext, String chordGrids){
		Title = title;
		Composer = composer;
		Artist = artist;
		SongText = songtext;
		ChordGrids = chordGrids;
	}

	/**
	 * Creates an empty Song with all values initialised
	 */
	public Song(){
		// Empty constructor - may want to create an instance and fill it up later
		Title = "";
		Artist = "";
		Composer = "";
		SongText = "";
		ChordGrids = "";
	}
	public void setTitle(String title){
		Title = title;
	}
	public void setComposer(String composer){
		Composer = composer;
	}
	public void setArtist(String artist){
		Artist = artist;
	}
	public void setSongText(String songtext){
		SongText = songtext;
	}
	public String getTitle(){
		return Title;
	}
	public String getComposer(){
		return Composer;
	}
	public String getArtist(){
		return Artist;
	}
	public String getSongText(){
		return SongText;
	}
	/**
	 * @return the chordGrids
	 */
	public String getChordGrids() {
		return ChordGrids;
	}

	/**
	 * @param chordGrids the chordGrids to set
	 */
	public void setChordGrids(String chordGrids) {
		this.ChordGrids = chordGrids;
	}

	public String toString(){
		return "Title: " + Title+ ", Artist: " + Artist+ ", Composer: "+Composer+ ", SongText: " + SongText;
	}
}