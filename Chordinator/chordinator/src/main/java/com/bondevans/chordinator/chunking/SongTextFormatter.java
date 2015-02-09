// Version History
/*	Update History 																		*/
/*  9/2/2011 - Corrected FC when saving song after edit - TargetSDK flag in manifest 	*/
/*  9/2/2011 - Amended scroll speeds - SCROLLINC from 30 to 10						 	*/ 
//	2.0.5 - Fixes FC when Editing. Supports European encoding.
//  2.0.6
// - Made "Set options" screen, scrollable since ok/cancel buttons unreachable in landscape on some screens
// - Corrected title bar to include {title} + either {artist}/{subtitle} or {composer}
// - Now pauses/resumes autoscrolling when the app loses/regains the focus
// - App keeps the screen on whenever it is displaying a song - not just when autoscrolling.
//	2.0.7
package com.bondevans.chordinator.chunking;

import com.bondevans.chordinator.Log;
import com.bondevans.chordinator.Song;
import com.bondevans.chordinator.Transpose;

public class SongTextFormatter {
	private final static String TAG = "SongTextFormatter";
	protected final static int T_TITLE = 0;
	protected final static int T_BYLINE = 1;
	protected final static int T_FREETEXT= 2;
	protected final static int T_LYRIC= 3;
	protected final static int T_CHORD= 4;
	protected static final int T_EOL_CHORDS = 5;
	protected static final int T_EOL_LYRICS = 6;
	protected static final int T_EOL = 7;

	private Song mSong = null;

	private StringBuilder mOutput= new StringBuilder();
	protected StringBuilder mLyrics= new StringBuilder();
	protected StringBuilder mChords= new StringBuilder();

	private boolean mLyricsPainted = true;
	private boolean mChordsPainted = true;
	private int mTransposeBy = 0;
	private int mLineLength=0;


	public SongTextFormatter(Song song, int transBy){
		mSong = song;
		mTransposeBy = transBy;
	}

	// Various Initialisation
	// - work out font sizes, margins, etc,
	// - set current positions for Title, Chords and Lyrics
	// every repaint
	private void initSong() {
		// This stuff gets done once every repaint
		mLyricsPainted = false;
		mChordsPainted = false;
	}

	public void formatSong(int maxLen){
		SongChunk sc;
		LineChunker lcr;
		LineChunk lc;
		// Initialise some stuff
		initSong();
		// print the Title/Artist/Composer/etc if necessary
		paintTitle();
		// Show chord grids at top of page if required

		SongChunker chunker = new SongChunker(mSong.getSongText());
		while(chunker.hasMore){
			sc=chunker.getNextChunk();
			if( sc.getFreeText().length()>0){
				// Its a free text line chunk
				paintFreeText(sc.getFreeText());
			}
			else if( sc.isEOL()){
				// Its a new line chunk
				startNewLine();
			}
			else{
				// Its a chord and/or lyric chunk
//				Log.d(TAG, "HELLO got SongChunk lyrics:["+sc.getLyricText()+"] chord:["+sc.getChordText()+"]");
				// Now get the longest Line Chunk that will fit
				lcr = new LineChunker(sc);
				int loopCount=0;
				while(lcr.hasMore){
					lc = lcr.getNextChunk(maxLen-mLineLength, loopCount);
					// Add the length of our chunk to the size so far
					mLineLength += lc.length();
//					Log.d(TAG, "HELLO got LineChunk lyrics:["+lc.getLyric()+"] chord:["+lc.getChord()+"]");
					addChunk(lc);
					if(lc.isEol){
						++loopCount;
					}
					else{
						loopCount=0;
					}
				}
			}
		}
	}

	StringBuilder mPadding = new StringBuilder("                                                                                                                      ");
	private void addChunk(LineChunk lc){
		if(lc.isEol){
			startNewLine();
		}
		else{
			// Get length of lyrics and chords
			int l= lc.getLyric().length();
			String chord = Transpose.chord(lc.getChord(), mTransposeBy);
			int c = chord.length();
			// work out the difference between the two
			int length = l>c?l-c:c-l;
			// Make sure that mPadding is at least as big as the length of padding we need
			while(length>mPadding.length()){
				mPadding.append("                          ");
			}
			// Need to pad out whichever is shortest with spaces
			if( l>c){
				// Lyrics longest so pad chords
				addLyricText(wrap(lc.getLyric(), T_LYRIC));
				addChordText(wrap(chord+mPadding.substring(0, length), T_CHORD));
			}
			else{
				// chords longest
				addLyricText(wrap(lc.getLyric()+mPadding.substring(0, length),T_LYRIC));
				addChordText(wrap(chord, T_CHORD));
			}
			// Only set ChordsPainted flag if its NOT all spaces
			if(!lc.getChord().trim().isEmpty()){
				mChordsPainted = true;
			}
			if( !lc.getLyric().trim().isEmpty()){
				mLyricsPainted = true;
			}
//			Log.d(TAG, "Chordline["+mChords+"]");
//			Log.d(TAG, "Lyrics   ["+mLyrics+"]");
		}
	}

	/**
	 * paintTitle
	 *
	 * @param g
	 * @throws Exception 
	 */
	private void paintTitle() {
		// print Title at top
		addText(wrap(mSong.getTitle(), T_TITLE));
		// Print composer or artist if available or nothing
		if (mSong.getComposer().length() > 0) {
			addText(wrap("By " + mSong.getComposer(), T_BYLINE));
		}
		else if (mSong.getArtist().length() > 0) {
			addText(wrap(mSong.getArtist(), T_BYLINE));
		}
		else{
			// Do nothing
		}
	}

	static char [] EOLCHARS = {13, 10};
	static final String EOL = new String(EOLCHARS);

	/**
	 * Appends characters to either Chords or Lyrics depending on type
	 * @param type
	 */
	protected void appendEOL(int type){
		switch(type){
		case T_EOL_CHORDS:
			mChords.append(EOL);
			break;
		case T_EOL_LYRICS:
			mLyrics.append(EOL);
			break;
		}
	}

	/**
	 * Wraps given text in prefix and suffix - depending on type
	 * @param txt
	 * @param type
	 * @return
	 */
	protected String wrap( String txt, int type){
		String pref=""; 
		String suff=""; 
		switch(type){
		case T_TITLE:
			pref = "";
			suff = EOL+EOL;
			break;
		case T_BYLINE:
			pref = "";
			suff = EOL+EOL;
			break;
		case T_LYRIC:
		case T_CHORD:
			// No wrapping for lyrics and chords for TEXT format - but there is for HTML
			break;
		case T_FREETEXT:
			pref = "";
			suff = EOL+EOL;
			break;
		case T_EOL:
			pref = "";
			suff = EOL;
			break;

		default:
			Log.d(TAG, "INVALID TYPE: "+type);
		}
		Log.d(TAG, "HELLO wrp type="+type+" txt=["+txt+"]");
		return pref+txt+suff;
	}

	/**
	 * Paint FreeText/Chorus/etc text on its own line at left
	 * @throws Exception 
	 */
	private void paintFreeText(String text) {
		// Need to finish off previous chord and lyric lines and start new ones
		startNewLine();

		Log.d(TAG,"paintFreeText: [" + text + "]");
		// Add Formatting for free text
		addText(wrap(text, T_FREETEXT));
		mLyricsPainted = false;
		mChordsPainted = false;
	}

	/**
	 * Adds a chord table cell to mChords
	 * @param text
	 */
	private void addChordText(String text){
		mChords = mChords.append(text);
	}
	/**
	 * Adds a lyric table cell to mLyrics
	 * @param text
	 */
	private void addLyricText(String text){
		mLyrics = mLyrics.append(text);
	}

	/**
	 * Starts a new line and adjusts the X & Y values for Chords and Lyrics.
	 * If no lyrics were printed on last line then push chords up underneath
	 * last chord line. if no chords have been printed then do a bit of a
	 * cludge: - copy the lyrics on the current line up to the chord
	 * position - blank out those lyrics - adjust the chord and lyric
	 * positions based on the moved lyrics
	 * @throws Exception 
	 */
	private void startNewLine() {
		if( mChordsPainted ){
			if( mLyricsPainted ){
				// Normal - both chords and lyrics painted
				appendEOL(T_EOL_CHORDS);
				appendEOL(T_EOL_LYRICS);
				addText(wrap(mChords.toString()+mLyrics.toString(), T_EOL));
				Log.d(TAG, "HELLO StartNewLine: BOTH");
			}
			else{
				// Chords but no lyrics - push chords up underneath last
				// chord line
				appendEOL(T_EOL_CHORDS);
				addText(wrap(mChords.toString(), T_EOL));
				Log.d(TAG, "HELLO StartNewLine: CHORDS ONLY");
			}
		}
		else{ // No chords
			if( mLyricsPainted ){
				// No chords
				// Finish off lyric line but drop chords
				appendEOL(T_EOL_LYRICS);
				addText(wrap(mLyrics.toString(), T_EOL));
				Log.d(TAG, "HELLO StartNewLine: LYRICS ONLY");
			}
			else{
				// no chords and no lyrics - do nothing.
				Log.d(TAG, "HELLO StartNewLine: NONE");
			}
		}

		//Wrap chord and lyric lines in table
//		addText(wrap(mChords.toString()+mLyrics.toString(), T_EOL));
//		addText(mChords.toString()+mLyrics.toString());// Don't think we need to add on another EOL...
		mChords = new StringBuilder("");
		mLyrics = new StringBuilder("");
		// reset chord/lyric Painted flags
		mLyricsPainted = false;
		mChordsPainted = false;
		mLineLength = 0;
	}

	/**
	 * Appends given text to mOutput
	 *
	 * @param g
	 * @param text
	 * @param x
	 * @param y
	 * @throws Exception 
	 */
	private void addText(String text){
		//		Log.d(TAG, "HELLO addText:"+text);
		mOutput.append(text);
	}

	public String getFormattedSong(){
//		Log.d(TAG, "HELLO mOutput=["+mOutput.toString()+"]");
		return mOutput.toString();
	}
}

