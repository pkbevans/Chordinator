package com.bondevans.chordinator.chunking;

public class SongChunk{
	private String mLyricText="";
	private String mChordText="";
	private String mFreeText="";
	private String mTabText="";
	private boolean isEOL = false;
	
	/**
	 * Constuctor takes text (lyrics/freetext), chord text and 
	 * boolean indicating whether there is an end of line after this chunk
	 * @param songText
	 */
	public SongChunk(String lyricText, String chordText, String freeText, String tabText, boolean eol){
		this.mLyricText=lyricText;
		this.mChordText=chordText;
		this.mFreeText=freeText;
		this.mTabText=tabText;
		this.isEOL = eol;
	}

	/**
	 * empty constructor
	 */
	public SongChunk(){
		this.mLyricText="";
		this.mChordText="";
		this.mFreeText="";
		this.mTabText="";
		this.isEOL = false;
	}
	/**
	 * @return the text
	 */
	public String getLyricText() {
		return mLyricText;
	}

	/**
	 * @param text the text to set
	 */
	public void setLyricText(String text) {
		this.mLyricText = text;
	}

	/**
	 * @return the chordText
	 */
	public String getChordText() {
		return mChordText;
	}

	/**
	 * @param chordText the chordText to set
	 */
	public void setChordText(String chordText) {
		this.mChordText = chordText;
	}

	/**
	 * @return the mLyricText
	 */
	public String getFreeText() {
		return mFreeText;
	}

	/**
	 * @param mLyricText the mLyricText to set
	 */
	public void setFreeText(String text) {
		this.mFreeText = text;
	}

	/**
	 * @return the mTabText
	 */
	public String getTabText() {
		return mTabText;
	}

	/**
	 * @param mTabText the mTabText to set
	 */
	public void setTabText(String text) {
		this.mTabText = text;
	}

	/**
	 * @return the isEOL
	 */
	public boolean isEOL() {
		return isEOL;
	}

	/**
	 * @param isEOL the isEOL to set
	 */
	public void setEOL(boolean isEOL) {
		this.isEOL = isEOL;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "SongChunk [mLyricText=" + mLyricText + ", mChordText="
				+ mChordText + ", mFreeText=" + mFreeText + ", mTabText="
				+ mTabText + ", isEOL=" + isEOL + "]";
	}
}
