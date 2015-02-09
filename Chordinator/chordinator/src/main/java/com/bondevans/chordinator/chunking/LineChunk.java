package com.bondevans.chordinator.chunking;


public class LineChunk {
//	private static final String TAG = "TextLineChunk";
	private String chord;
	private String lyric;
	public boolean isEol;
	
	public LineChunk(String chord, String lyric, boolean eol){
		this.chord = chord;
		this.lyric= lyric;
		this.isEol = eol;
	}
	
	public int length(){
		int mc = chord.length();
		int ml = lyric.length();
		return mc>ml?mc:ml;
	}
	
	public String getChord(){
		return chord;
	}

	public String getLyric(){
		return lyric;
	}
}
