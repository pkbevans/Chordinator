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
package com.bondevans.chordinator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import com.bondevans.chordinator.grids.ChordShapePainter;
import com.bondevans.chordinator.grids.ChordShapeProvider;
import com.bondevans.chordinator.prefs.SongPrefs;

import java.util.ArrayList;

@SuppressLint("DefaultLocale")
public class SongCanvas3 extends View {
	private final static String TAG = "SongCanvas";
    private static final String INLINE_CHAR_RHS = "";
    public Bitmap mBitmap=null;
	private Canvas mCanvas=null;
	private Paint mPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
	private Paint mFixedPaint=null;// = new Paint(Paint.ANTI_ALIAS_FLAG);
	private int mTextSize;
	private boolean mIsChopro=true;	// Use fixed-width font if NOT

	private Song theSong = null;
	private SongPrefs preferences;

	private boolean redraw = true;
	private int mSongHeight = 0;
	private int verticalSpace = 0; // number of pixels between lines -
	private int titleY = 0;
	private int gridY = 0;
	private int chordX = 0, chordY = 0;
	private int lyricX = 0, lyricY = 0;

	private boolean titleRequired = true;	// Don't need to print Title
	private int titleHeight = 0;
	private int chordHeight = 0;
	private int lyricHeight = 0;
	private int freeTextHeight = 0;
	private int sideMargin = 0;

	private int lastPainted=LP_NOTHING;	// What was the last item printed 1=Title, 2=Freetext, 3=Chord, 4=Lyric
	private static final int LP_NOTHING=0;	// What was the last item printed 1=Title,
	private static final int LP_TITLE=1;	// What was the last item printed 1=Title,
	private static final int LP_TEXT=2;
	private static final int LP_CHORD=3;
	private static final int LP_LYRIC=4;
	private static final int LP_GRIDS = 5;
	private static final int LP_TAB = 6;
	private static final int MODE_NORMAL = 0;
	private static final int MODE_CHORDS_ONLY = 1;
	private static final int MODE_LYRICS_ONLY = 2;

	private boolean mChorus = false;
	private boolean lyricsPainted = true;
	private boolean chordsPainted = true;
	private boolean tabPainted = false;
	private boolean initDone = false;
	private int transposeBy = 0;
	private boolean showGrids = false;

	private String lyricsBuffer = "";

	private ColourScheme col;
	private String mSongChorus="";
	private boolean mTabsSupported=false;
	private int 	mChordGridInstrument=ChordShapeProvider.INSTRUMENT_GUITAR;
	private int mMode=MODE_NORMAL;
//	public boolean mTrial=false;
	// Do we want to add " - " into words that have a chord in the middle - e.g. "Hello [A]Mum[B]my" becomes:
	//       A     B
	// Hello Mum - my
	private boolean mAddDashes = false;  
	// If we get multiple line feeds together, do we ignore the all 
	// apart from the first one or do we honour all?
	private boolean mHonourLFs = false;
	private boolean mIgnoreSpaces = false;
    private boolean mInLineMode=false;
    private Bitmap mLyricBitmap;
    private Canvas mLyricCanvas;

    /**
	 * Constructor
	 */
	public SongCanvas3(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SongCanvas3(Context context, Song song, SongPrefs sp) {
		super(context);
		theSong = song;
		preferences = sp;
		setPreferences(preferences, false);
	}

	public void setupSong(Song song, SongPrefs sp) {
		theSong = song;
		preferences = sp;
		setPreferences(preferences, false);
	}

	public void setPreferences(SongPrefs sp) {
		setPreferences(sp, true);
	}
	public void setPreferences(SongPrefs sp, boolean inval) {
		preferences = sp;
		showGrids = preferences.isShowGridsOn();
		mTextSize = preferences.getTextSize();
		Log.d(TAG, "HELLO Text=["+mTextSize+"]");
		// Force repaint after preferences have been updated
		col = new ColourScheme(preferences.getColourScheme());
		Log.d(TAG, "HELLO colourScheme=["+preferences.getColourScheme()+"]");
		mMode = preferences.getMode();
		if(mMode == MODE_LYRICS_ONLY){
			// Don't add dashes if in LYRICS-only mode
			mAddDashes = false;
		}
		else{
			mAddDashes = preferences.AddDashes();
		}
		mHonourLFs = preferences.HonourLFs();
		mInLineMode = preferences.getInlineMode()==1;
		if(inval){
			initDone = false;
			//			redraw = true;
			doInvalidate();
		}
	}

	/**
	 * Force a full redraw of the current song
	 */
	public void doFullRedraw(){
		chordY=titleY=lyricY=gridY=0;
		initDone = false;
		doInvalidate();
	}
	public void doInvalidate(){
		// Have to set redraw AND set mSongHeight to zero to force full repaint/resize
		redraw = true;
		mSongHeight=0;
		invalidate();
	}

	boolean mTooBig=false;
	Rect mRect = new Rect();
	@Override
	protected void onDraw(Canvas gReal) {
		if( theSong == null){
			String s = getResources().getString(R.string.no_song);
			//			Paint p= new Paint(Paint.ANTI_ALIAS_FLAG);
			mPaint.setTextSize(mTextSize>0?mTextSize:18);
			float w = mPaint.measureText(s);
			drawSongString(gReal, s, (getWidth() - w) / 2, mPaint.getTextSize()*4,mPaint);
			return;
		}

//		Log.d(TAG,"HELLO onDraw1: [" + mSongHeight + "]");
		// Paint song onto temporary canvas and then copy it to the screen
		if (redraw ) {
            if(mSongHeight==0 || mTooBig){
				Log.d(TAG,"HELLO onDraw2: [" + mSongHeight + "]");
				// 1st time Paint song on supplied canvas to get songheight
				try{
					paintSong(gReal);
				}
				catch (OutOfMemoryError e) {
					Log.d(TAG,"HELLO OutofMemoryError1: [" + mSongHeight + "]");
					redraw=false;
					mSongHeight=getHeight();
				}
			}
			else{
				Log.d(TAG,"HELLO onDraw3: [" + mSongHeight + "]");
				// 2nd time create a bitmap+canvas big enough for the whole song
				try {
					// Create the bitmap+canvas
					createCanvas(mSongHeight);
					// write to this bitmap (via the canvas)
					paintSong(mCanvas);
					// Now copy from this canvas to the real one supplied to onDraw
					if(!gReal.getClipBounds(mRect)){
						Log.d(TAG,"HELLO getClipBounds returns FALSE!!!");
					}
					// Only copy the visible bit
					gReal.drawBitmap(mBitmap, mRect, mRect, null);
					redraw=false;
				}
				catch (OutOfMemoryError e) {
					Log.d(TAG,"HELLO OutofMemoryError3: [" + mSongHeight + "]");
					Toast.makeText(getContext(), getResources().getString(R.string.song_too_big), Toast.LENGTH_LONG).show();
					mTooBig=true;// force complete redraw
					paintSong(gReal);
				}
			}
		}
		else{
//			Log.d(TAG,"HELLO onDraw4: [" + mSongHeight + "]");
			// Copy from the bitmap to the actual screen
			if(mBitmap != null){
				if(!gReal.getClipBounds(mRect)){
					Log.d(TAG,"HELLO getClipBounds returns FALSE!!!");
				}
				// Only copy the visible bit
				gReal.drawBitmap(mBitmap, mRect, mRect, null);
			}
			else{
				drawSongString(gReal, "Song too big", 10, 100, mPaint);
			}
		}
	}

	/**
	 * Create a Canvas of the given height
	 * @param requestedHeight - requested height of bitmap
	 */
	private void createCanvas(int requestedHeight){
		Log.d(TAG,"HELLO createCanvas: [" + requestedHeight + "]");
		// See if current size is big enough - if so return
		if(mBitmap != null && mBitmap.getHeight()>=requestedHeight){
			Log.d(TAG,"HELLO createCanvas Current bitmap OK: [" + requestedHeight + "]");
			return;
		}

		if( mBitmap != null){
			Log.d(TAG,"HELLO recycling Current bitmap");
			mBitmap.recycle();
			mBitmap = null;
			System.gc();
		}

		mBitmap = Bitmap.createBitmap(getWidth(), requestedHeight, Bitmap.Config.RGB_565);
		mCanvas = new Canvas(mBitmap);
	}

    // Various Initialisation
	// - work out font sizes, margins, etc,
	// - set current positions for Title, Chords and Lyrics
	// every repaint
	private void initSong(Canvas g) {
		if(!initDone){
			// This stuff only gets done once
			// Setup up Font heights
			mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mPaint.setTextSize(mTextSize);
			// If this ISN'T chopro then use a fixed-width font
			if(!mIsChopro){
				mPaint.setTypeface(Typeface.MONOSPACE);
			}
			// Fixed width font for Tab
			mFixedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mFixedPaint.setTypeface(Typeface.MONOSPACE);
			mFixedPaint.setTextSize(mTextSize);

			if(titleRequired){
				titleHeight = (int) mPaint.getTextSize();
				titleY=titleHeight*2;
			}
			else{
				titleHeight = 0;
			}

			chordHeight = (int) mPaint.getTextSize();
			lyricHeight = (int) mPaint.getTextSize();
			freeTextHeight = (int) mPaint.getTextSize();

			// Set (fairly) random left margin
			sideMargin = (int) mPaint.measureText(".");

			//Log.d(TAG,"Font height=["+freeTextHeight+"]");

			// Base the vertical spacing on the font size
			verticalSpace = lyricHeight / 3;
			// When using arrow keys scroll by the font height
			//			scrollHeight = lyricHeight + verticalSpace;
            setupLyricCanvas(lyricHeight);
			initDone = true;
		}
		// This stuff gets done once every repaint
		if(titleRequired){
			titleY=titleHeight*2;
		}
		lyricsPainted = false;
		lyricsBuffer = "";
		mSongChorus = "";
		chordsPainted = false;
		mChorus = false;
		g.drawColor(col.background()); // Paint canvas with background colour
		mLyricCanvas.drawColor(col.background()); // Paint canvas with background colour
	}

    private void setupLyricCanvas(int lyricHeight) {
        mLyricBitmap = Bitmap.createBitmap(getWidth(), lyricHeight*2, Bitmap.Config.RGB_565);
        mLyricCanvas = new Canvas(mLyricBitmap);
    }

    /**
	 * Paints Song to given canvas
	 *
	 * @param g Canvas
	 */
	private void paintSong(Canvas g) {
		int start, end;
		boolean soc=false;
		// Initialise some stuff
		initSong(g);
		// print the Title/Artist/Composer/etc if necessary
		paintTitle(g);
		// Show chord grids at top of page if required
		if (showGrids) {
			// Get a list of all the chords in the song and paint them all
			// at the top of the page
			paintChordGrids(g, getChords());
		}

		StringBuilder cs = new StringBuilder(theSong.getSongText());

		int i=0;
		String text;
		// Loop thru songtext...
		while( i<cs.length()) {
			switch (cs.charAt(i)) {
			case '{':
				// Ignore anything between curly brackets - unless a special tag
				if(cs.substring(i).toLowerCase().startsWith("{comment:") ||
						cs.substring(i).toLowerCase().startsWith("{c:"))
				{
					text = SongUtils.tagValueX(cs.substring(i),"comment", "c", "");
					paintFreeText(g, text, false);
				}
				else if(cs.substring(i).toLowerCase().startsWith("{sob") ||
						cs.substring(i).toLowerCase().startsWith("{start_of_block"))
				{
					// Start of block - treat the same as {comment/c
					text = SongUtils.tagValueX(cs.substring(i),"start_of_block", "sob", "");
					paintFreeText(g, addDashes(text), true);
				}
				else if(cs.substring(i).toLowerCase().startsWith("{eob") ||
						cs.substring(i).toLowerCase().startsWith("{end_of_block"))
				{
					// End of block - draw a line under the preceding block
					paintFreeText(g, addDashes(""), true);
				}
				else if(cs.substring(i).toLowerCase().startsWith("{soc") ||
						cs.substring(i).toLowerCase().startsWith("{start_of_chorus"))
				{
					paintFreeText(g, getResources().getString(R.string.chorus), false);
					// Start of chorus - lyrics printed in fake bold
					mChorus = true;
					soc = true;
				}
				else if(cs.substring(i).toLowerCase().startsWith("{eoc")||
						cs.substring(i).toLowerCase().startsWith("{end_of_chorus"))
				{
					// end of chorus
					mChorus = false;
					// End of block - draw a line under the preceding block
					//					paintFreeText(g, addDashes(""), true);
				}
                else if(cs.substring(i).toLowerCase().startsWith("{inline"))
                {
                    // Switch to inline mode where the Chord sysmbols are printed in line
                    // with the lyrics, rather than on the line above.
                    mInLineMode = true;
                }
				else if(cs.substring(i).toLowerCase().startsWith("{rc") ||
						cs.substring(i).toLowerCase().startsWith("{repeat_chorus"))
				{
					// Repeat chorus - must have been previously defined with {soc}
					Log.d(TAG, "HELLO - REPEAT CHORUS");
					if(mSongChorus.length()>0){
						// Get to end of the rc tag
						int offset = cs.substring(i).indexOf("}");
						// ... and insert the Chorus that we saved previously
						if( offset > -1){
							paintFreeText(g, getResources().getString(R.string.chorus), false);
							cs.insert(i+offset+1, mSongChorus);
							mChorus = true; // switch on Chorus formatting
						}
					}
					else{
						Log.d(TAG, "HELLO - IGNORING REPEAT CHORUS");
					}
				}
				else if(cs.substring(i).toLowerCase().startsWith("{start_of_tab") ||
						cs.substring(i).toLowerCase().startsWith("{sot"))
				{
					if(mTabsSupported && mMode == MODE_NORMAL){
						// Get all the lines of tab (may be 6 lines together, which are treated as a single set)
						int x = getTab(cs.substring(i));
						if(x>-1){
							//							paintFreeText(g, addDashes("TAB"), true);
							// Paint the set of tab lines
							paintTab(g);
							// End of tab block - draw a line under the preceding block
							//							paintFreeText(g, addDashes(""), true);
							i+=x;
						}
						else{
							i++;
						}
					}
					else{
						Log.d(TAG, "IGNORING TAB");
						// Start of tab.  Find the
						// end of the tab and skip to there
						int x = cs.substring(i).indexOf("{eot}");
						if(x == -1){
							x = cs.substring(i).indexOf("{end_of_tab}");
						}
						if( x == -1){
							// There is no end of tabs tag so end of song
							i = cs.length();
							SongUtils.toast(getContext(), "Missing {eot}");
						}
						else{
							i+=x;
						}
					}
				}
				// By the time we get here i needs to be past the opening brace of the tab 
				// (or the opening brace of the matching pair)
				int tmp = cs.indexOf("}", i);
				if (tmp > -1) {
					i = tmp + 1;
				} else {
					// If there isn't a matching brace then move
					// past the opening brace
					i++;
				}
				// If we've found the start of the chorus, find and save the chorus for use later
				// with a "repeat chorus" tag
				if(soc){
					int cStart = i;
					int cEnd = cs.substring(cStart).toLowerCase().indexOf("{eoc}");
					if(cEnd == -1){
						cEnd = cs.substring(cStart).toLowerCase().indexOf("{end_of_chorus}");
					}
					if(cEnd !=-1){
						// Save chorus for use later
						mSongChorus = cs.substring(cStart, cStart+cEnd)+"{eoc}";
					}
					soc=false;
				}
				break;

				/*			case '<':
 				// THE APP NO LONGER SUPPORTS the <COMMENT> tag
				// Start of FreeText/Chorus/etc/ text
				start = ++i;
				while (i < cs.length() && cs.charAt(i) != '>') {
					++i;
				}
				end = i++;
				paintFreeText(g, cs.substring(start, end), false);
				break;
				 */			
				case '[':
					 // Start of a chord - get everything up until next ']' char
					 start = ++i;
					 while (i < cs.length() && cs.charAt(i) != ']') {
						 ++i;
					 }
					 end = i++;

					 // Ignore empty chord 
					 if(start == end ){
						 break;
					 }
					 // Ignore opening bracket without a closing bracket
					 if(end >= cs.length()){
						 i = start;
						 break;
					 }
					 String chord = cs.substring(start, end);
					 // Also need to check what comes after the chord.  If a lyric word follows
					 // directly after the chord then we need to fit that word on the same line
					 // as the chord
					 start = i;
					 int j = start;
					 // Make sure its NOT another chord directly after this chord - or a
					 // formatting tag
					 while (j < cs.length() && cs.charAt(j) != ' ' && cs.charAt(j) != '['
							 && cs.charAt(j) != '{'&& cs.charAt(j) != '\n') {
						 ++j;
					 }
					 end = j;
					 String wordAfterChord = cs.substring(start, end);

					 if(MODE_LYRICS_ONLY != mMode){
                         if(mInLineMode){
                             // Put a spaace between the chord and the following word - if there is one - otherwise just print the chord
                             paintLyrics(g,Transpose.chord(chord, transposeBy)+(wordAfterChord.isEmpty()? " ":INLINE_CHAR_RHS),wordAfterChord,"", true);
                         }
                         else {
                             paintChords(g, Transpose.chord(chord, transposeBy), wordAfterChord);
                         }
					 }
					 break;
				 case '\n':// LF
					 // Treat a line break as a line break
					 startNewLine(g);
					 ++i;
					 break;
				 default:
					 // Default Case - its just lyrics so copy everything up
					 // until next format char OR LINEFEED
					 start = i;
					 while(i < cs.length() && cs.charAt(i)!= '[' /*&& cs.charAt(i)!= '<'*/ && cs.charAt(i)!= '{' && cs.charAt(i)!= '\n' ){
						 ++i;
					 }
					 end = i;
					 // Need to check whether there is a chord in the middle of a word.  In this case
					 // We need to make sure that the word is not wrapped over 2 lines.
					 String lyricsAfterChord = "";
					 // Also need to deal with situation where chord in the middle is longer than
					 // the text underneath it which can cause the chord and the 2nd half of the word to be
					 // moved onto a new line leaving the beginning of the word stranded.
					 String chordInWord = "";

					 if (end > 0 && end < cs.length() && cs.charAt(end) == '[' // Next thing IS a chord
							 && cs.charAt(end - 1) != ' ') { // Last lyric char
						 // NOT a space
						 // See if there is a space after the chord
						 int x = i;
						 // Get to the end of the chord
						 while (x < cs.length() && cs.charAt(x++) != ']');
						 chordInWord = cs.substring(i, x);
						 //						Log.d(TAG,"HELLO: Chord in word [" + chordInWord + "]");
						 int y = x;
						 while (x < cs.length() && !SongUtils.charIsInString(cs.charAt(x), " [{") && cs.charAt(x) != '\n') {
							 // Count how many non-space, non-tag, non-CR
							 ++x;
						 }
						 lyricsAfterChord = cs.substring(y, x);
					 }
					 if(MODE_CHORDS_ONLY != mMode){
						 paintLyrics(g, cs.substring(start, end)+((!mAddDashes||lyricsAfterChord.equalsIgnoreCase(""))?"":" - "), lyricsAfterChord, removeSpaces(chordInWord), false);
					 }
					 break;
			}
		}
		// Make sure that we push the last line of lyrics up properly if necessary.
		startNewLine(g);
		// Draw a line to indicate the end
		mPaint.setColor(col.lyrics());
		g.drawLine(this.getWidth()/4, lyricY, this.getWidth()/4*3, lyricY, mPaint);
		mSongHeight = lyricY;
		mSongHeight+=lyricHeight;	// Add on a bit more to allow for the scroll stop/start button
		//		Log.d(TAG, "HELLO bitmapHeight=["+mBitmap.getHeight()+"] songHeight=["+songHeight+"]");
		//  Now we know how big the song is we can request the parent to resize.
		this.requestLayout();
	}

	final String sot="{sot}";
	final String eot="{eot}";
	final String startOfTab="{start_of_tab}";
	final String endOfTab="{end_of_tab}";
	ArrayList<String> mTabLines = new ArrayList<>();

	/**
	 * 
	 * @param song the song
	 * @return length of tab - including the {sot}/{eot}/{start_of_tab}/{end_of_tab}
	 */
	private int getTab(String song) {
		int length = 0;
		int cludge=0;
		mTabLines.clear();
		//Now get the individual tab lines into an array of strings.  There may be a single {sot}...{eot} with 
		//multiple lines in between or one for each line
		boolean done=false;
		while(!done){
			int len = getTabLine(song.substring(length));
			if(len>-1){
				length+=len;
			}
			else{
				// Something went wrong - get outta here
				return -1;
			}
			cludge = length;
			// Jump over any whitespace
			while(song.substring(length).startsWith(" "))++length;
			while(song.substring(length).startsWith("\n"))++length;
			while(song.substring(length).startsWith("\t"))++length;
			// if the next thing after any whitespace is NOT the start of another tab then give up
			if(!song.substring(length).toLowerCase().startsWith(sot)&& !song.substring(length).toLowerCase().startsWith(startOfTab)){
				done = true;
			}
		}
		Log.d(TAG, "HELLO - got ["+mTabLines.size()+"] tab lines");
		// Need to return the length of the complete set of tab lines (excluding any whitespace after the last one) -1 char so that the
		// caller can then find the last bracket... Oh dear!!!
		return (cludge -1);
	}

	private int getTabLine(String song){
		int length=-1;	// length = complete length of tab line including {sot} and {eot}
		// find end of {sot}
		// get to end of {sot}/{start_of_tab}
		int start = song.toLowerCase().indexOf(sot);
		if(start>-1){
			start+=sot.length();
		}
		if(start == -1){
			start = song.toLowerCase().indexOf(startOfTab);
			if(start>-1){
				start+=startOfTab.length();
			}
		}
		if( start > -1){
			// Got start of tab, now get the end
			// Ignore whitespace TODO - needs improvement
			while(song.charAt(start) == ' ')++start;
			while(song.charAt(start) == '\n')++start;
			while(song.charAt(start) == '\t')++start;

			int end = song.toLowerCase().indexOf(eot);
			if(end == -1){
				end = song.toLowerCase().indexOf(endOfTab);
				if( end>-1 ){
					length = end + endOfTab.length();
				}
			}
			else{
				length = end + eot.length();
			}
			if(end>-1){
				String tab = song.substring(start, end).trim();
				//				Log.d(TAG, "HELLO  GOT Tab=["+tab+"]");
				// Got it - now split the tabs by NEWLINE
				TextUtils.StringSplitter splitter = new TextUtils.SimpleStringSplitter('\n');

				// Once per string to split
				splitter.setString(tab);
				for (String s : splitter) {
					// add each line
					Log.d(TAG, "HELLO  Adding TabLine=["+s.trim()+"]");
					mTabLines.add(s.trim());
				}
			}
			else{
				length = -1;
			}
		}
		return length;
	}

	private void paintTab(Canvas g) {
		mFixedPaint.setColor(col.tab());
		Log.d(TAG, "HELLO - paintTab ["+mTabLines.size()+"] tab lines");
		startNewLine(g);

		int tabMargin = sideMargin*8;
		// calc how many chars we can fit on without wrapping
		int maxChars = (int) ((getWidth() - (2*tabMargin))/mFixedPaint.measureText("-"))/4;
		maxChars*=4;// Get back to a number divisable by 4
		// calc longest line (should be all the same but best to be safe)
		int maxLen=0;
		int i;
		for(i=0;i<mTabLines.size();i++){
			if(mTabLines.get(i).length()>maxLen){
				maxLen=mTabLines.get(i).length();
			}
		}
		Log.d(TAG, "HELLO - maxChars=["+maxChars+"] maxLen=["+maxLen+"]");
		int start=0; 
		int st; int end;
		while( start<maxLen){
			// Start new line between sections of tab
			tabPainted = true;
			startNewLine(g);
			for(i=0;i<mTabLines.size();i++){
				// Make sure start and end dont go over the length of the string
				st=start;
				end=st+maxChars;
				if(st>mTabLines.get(i).length()){
					st = mTabLines.get(i).length();
				}
				if(end>mTabLines.get(i).length()){
					end = mTabLines.get(i).length();
				}

				//				Log.d(TAG, "HELLO - painting ["+mTabLines.get(i).substring(st, end)+"] tab lines");
				// Use fixed-width font
				drawSongString(g, mTabLines.get(i).substring(st, end), tabMargin, chordY, mFixedPaint);
				tabPainted = true;
				startNewLine(g);
			}
			start += maxChars;
		}
		lastPainted = LP_TAB;
	}

	/**
	 * Adds dashes around given string to fill 2/3 of the width of the screen
	 * @param text text to add dashes to
	 * @return text+dashes
	 */
	private String addDashes(String text){
		if( text.length()>1){
			text = " "+text+" ";
		}
		int x = (int) mPaint.measureText(text);
		int y = (int) mPaint.measureText("_");
		int z = ((getWidth()*2/3)-x)/y/2;
		StringBuilder ret=new StringBuilder(text);
		while(z-->0){
			ret.insert(0, "_");
			ret.append("_");
		}
		return ret.toString();
	}
	/**
	 * paintTitle
	 *
	 * @param g Canvas
	 */
	private void paintTitle(Canvas g) {
		if (titleRequired) {
			mPaint.setColor(col.title());

			mPaint.setFakeBoldText(true);
			doTitle( g, theSong.getTitle());
			mPaint.setFakeBoldText(false);
		}
	}

	void doTitle(Canvas g, String titleText){
		//		Log.d(TAG, "HELLO doTitle:"+titleText);
		String tmp = titleText;
		int i, loop=0;
		float availableSpace = getWidth()- (sideMargin*2);
		while (mPaint.measureText(tmp) > availableSpace) {
			Log.d(TAG, "HELLO b4 findlongestword:"+tmp);
			i = findLongestWordIndex(tmp, availableSpace);
			if( i > 0 ) {
				Log.d(TAG, "HELLO i:"+i);
				// We've found the largest set of complete words that will
				// fit on the line
				// so call this function recursively with this bit...
				tmp = tmp.substring(0, i);
				doTitle(g, tmp);
				// and then chop the 1st bit out of TitleText (and remove the
				// leading space)...
				titleText = titleText.substring(i + 1);
				// ...start a new line, then go round again.
				startNewLine(g);
				tmp = titleText;
				++loop;
			}
			else if (loop > 0) {
				Log.d(TAG, "HELLO b4 findlongestString:"+tmp);
				// Need to check for situation where a word is too big to
				// fit on a whole line - eg supercalafragerlisticexpealidocious
				// so may need to split up.
				int x = findLongestStringIndex(tmp, availableSpace);
				// If tmp is longer than text (e.g. because there is something after the chord) then just use text
				if( x > titleText.length())
				{
					x = titleText.length();
				}

				tmp = tmp.substring(0, x);
				doTitle(g, tmp);
				// and then chop the 1st bit out of text...
				titleText = titleText.substring(x);
				// ...start a new line, then go round again.
				startNewLine(g);
				tmp = titleText;
			}
			else{
				Log.d(TAG, "HELLO Oh bugger:"+tmp);
				// Couldn't find a set of complete words that will fit
				// ...start a new line, then go round again.
				startNewLine(g);
				// Set loop flag so we don't get stuck in one
				++loop;
			}
		}
		// To print in the centre, get the width of the page, subtract
		// width of
		// string and divide by 2. Print at top of page with a margin
		// from the top (setup in init)
		//		Log.d(TAG, "HELLO b4 drawSongString:"+titleText);
		float w = mPaint.measureText(titleText);
		drawSongString(g, titleText, (getWidth() - w) / 2, titleY,mPaint);

		// Update Chord and lyric X and Y values - assume no freeText at
		// this stage
		titleY+=titleHeight+verticalSpace;
		lyricX = chordX = sideMargin;
		chordY = titleY + titleHeight + verticalSpace; // Gap between title and chords
		lyricY = getLyricYFromChordY(); // Gap between chords and lyrics
		lastPainted = LP_TITLE;
	}

	private void paintChordGrids(Canvas g, ArrayList<String> chords) {//throws ChordinatorException {
		mPaint.setColor(col.grids());

		// Base the maximum width of the grid on the size of the font
		int maxGridWidth = (int) mPaint.measureText("ABCDEFGHIJKL");
		ChordShapePainter cgp = new ChordShapePainter(maxGridWidth, mPaint,
				theSong.getChordGrids(), mChordGridInstrument);
		// Work out how many chord grids we can get on the width of the
		// screen
		float sw = cgp.getShapeWidth();
		int maxChords = (int) (getWidth() / sw);
		//
		gridY = titleY + titleHeight + verticalSpace;
		for (int x = chords.size(), currChord = 0; x > 0;) {
			// Work out how many grids to put on this line
			int ctl = x > maxChords ? maxChords : x;
			// Work out x coord to start at
			int gridX = (int) ((getWidth() - (ctl * sw)) / 2);
			// draw the grids
			for (int z = ctl; z > 0; z--, currChord++) {
				cgp.drawShape(g, Transpose.chord(chords.get(currChord), transposeBy), gridX, gridY);
				gridX += cgp.getShapeWidth();
			}

			// Update the y coord for the next line - add a bit of space
			// between the lines of grids
			int gridSize = (int) (cgp.getShapeHeight() + mPaint.getTextSize());
			gridY += gridSize;
			x -= ctl;
		}
		// Update Chord and lyric X and Y values - assume no freeText at
		// this stage
		chordY += gridY;
		lyricY = getLyricYFromChordY(); // Gap between chords and lyrics
		lastPainted = LP_GRIDS;
	}

	/**
	 * Paint FreeText/Chorus/etc text on its own line at left
	 */
	private void paintFreeText(Canvas g, String text, boolean centred) {
		mPaint.setColor(col.freeText()); // Blue
		if(mChorus){
			// If its the chorus - switch on special font style
			mPaint.setFakeBoldText(true);
		}
		else{
			mPaint.setFakeBoldText(false);
		}

		// If we're printing FreeText/Chorus/Intro etc we need to print
		// further down the page
		// from whatever the lowest thing is - could be either title,
		// lyrics, chords or another
		// freeText text.
		// 1st work out what was lowest printed previously.
		int y;
		if (lastPainted == LP_TITLE) {
			// Title last thing to be written - or this is the first thing .
			// Set current FreeText Y value to current Title Y +
			// verticalSpace
			y = titleY + titleHeight + verticalSpace;
		}
		else if (lastPainted == LP_NOTHING) {
			y = titleY + titleHeight + verticalSpace;
		}
		else if (lastPainted == LP_GRIDS) {
			y = gridY + verticalSpace;
		}
		else if( lastPainted == LP_TAB){
			y = chordY;
		}
		else{
			// Last thing printed was either chords or lyrics
			// Push up last lyric line if necessary.
			startNewLine(g);
			y = chordY;
		}

		// Add another bit of space
		y +=verticalSpace;

		//Log.d(TAG,"paintFreeText: <" + text + "> X=" + sideMargin + " Y=" + y);
		// Write text centred if required
		if( centred){
			drawSongString(g, text, (getWidth() - (mPaint.measureText(text))) / 2, y, mPaint);
		}
		else{
			drawSongString(g, text, sideMargin, y, mPaint);
		}
		// Update Chord and lyric X and Y values
		lyricX = chordX = sideMargin;
		chordY = y + freeTextHeight + (verticalSpace*2);
		lyricY = getLyricYFromChordY();

		lastPainted = LP_TEXT;
		lyricsPainted = false;
		chordsPainted = false;
	}

	/**
	 * Paints chord symbols on the chord line above the appropriate lyrics.
	 * This method requires chordY to be set correctly.
	 *
	 * @param g Canvas
	 * @param chord Chord to paint
	 * @param wordAfterChord lyrics after the chord
	 */
	private void paintChords(Canvas g, String chord, String wordAfterChord) {
		mPaint.setColor(col.chords());
		if(mChorus){
			// If its the chorus - switch on special font style
			mPaint.setFakeBoldText(true);
		}
		else{
			mPaint.setFakeBoldText(false);
		}

		//Log.d(TAG,"paintChords: [" + chord + "] X=" + chordX + " Y=" + chordY);
		// need to make sure we leave at least a space between chords
		chord = chord + " ";

		// X COORD - Make sure that we've got room on the line. If not start
		// new line
		float w1 = mPaint.measureText(chord); // width of chord
		float w2 = mPaint.measureText(wordAfterChord); // width of word after chord
		if (chordX + (w1 > w2 ? w1 : w2) > getWidth() - sideMargin) {
			// If no lyrics have been printed then the next chord line can
			// be pushed up below the last chord line.
			startNewLine(g);
		}

		drawSongString(g, chord, chordX, chordY, mPaint);
		// Update X position for Chords and make sure that the X position for
		// lyrics is at least up to the start of this chord.
		if (lyricX < chordX) {
			lyricX = chordX;
		}
		chordX += mPaint.measureText(chord);
		lastPainted = LP_CHORD;
		chordsPainted = true;
	}

	/**
	 * Remove any spaces from given String
	 * @param in String
	 * @return String
	 */
	private String removeSpaces(String in){
		return in.replaceAll(" ", "");
	}
	private void paintLyrics(Canvas g, String text, String textAfterChord, String chord, boolean isChord) {
		if (mIgnoreSpaces && text.trim().equalsIgnoreCase("")) {
            return;
		}
        if(isChord){
            // InLineMode and this is actually a chord
            mPaint.setColor(col.chords());
        }
        else{
		    mPaint.setColor(col.lyrics());
        }
		if(mChorus){
			// If its the chorus - switch on special font style
			mPaint.setFakeBoldText(true);
		}
		else{
			mPaint.setFakeBoldText(false);
		}
		// X COORD - Make sure that we've got room on the line. If not work out
		// how much we can fit on the line.
		String tmp;
        if(mInLineMode && !isChord){
            // If we are in inline_mode then need to fit chords in the middle of words together on a line
            tmp = text+chord+textAfterChord;
        }
        else if(mPaint.measureText(textAfterChord) > mPaint.measureText(chord)){
			tmp = text + textAfterChord;
		}
		else{
			tmp = text + chord;
		}

		int i, loop = 0;
		// Work out how much space available on the line
		float availableSpace = getWidth() - sideMargin - lyricX;
		while (mPaint.measureText(tmp) > availableSpace) {
			i = findLongestWordIndex(tmp, availableSpace);
			if( i > 0 ) {
				// We've found the largest set of complete words that will
				// fit on the line
				// so call this function recursively with this bit...
				tmp = tmp.substring(0, i);
				paintLyrics(g, tmp, "", "", isChord);
				// and then chop the 1st bit out of text (and remove the
				// leading space)...
				text = text.substring(i + 1);
				// ...start a new line, then go round again.
				startNewLine(g);
				tmp = text + textAfterChord;
				++loop;
			}
			else if (loop > 0) {
				// Need to check for situation where a word is too big to
				// fit on a whole line - eg supercalafragerlisticexpealidocious
				// so may need to split up.
				int x = findLongestStringIndex(tmp, availableSpace);
				// If tmp is longer than text (e.g. because there is something after the chord) then just use text
				if( x > text.length())
				{
					x = text.length();
				}

				tmp = tmp.substring(0, x);
				paintLyrics(g, tmp, "", "", isChord);
				// and then chop the 1st bit out of text...
				text = text.substring(x);
				// ...start a new line, then go round again.
				startNewLine(g);
				tmp = text + textAfterChord;
			}
			else{
				// Couldn't find a set of complete words that will fit
				// ...start a new line, then go round again.
				startNewLine(g);
				// Set loop flag so we don't get stuck in one
				++loop;
			}
			availableSpace = getWidth() - sideMargin - lyricX;
		}

		//		Log.d(TAG,"painting: [" + text + "] X=" + lyricX + " Y=" + lyricY);
		// Cludge to get round problems with copyArea()! - keep the lyrics
		// in case we need them later
		lyricsBuffer = lyricsBuffer.concat(text);
        // Paint to special Lyric canvas - because we may need to copy the lyric line
        // up, if there are no chords. mLyricBitmap is only the height of lyrics so
        // yCoord = lyricHeight.
//		drawSongString(g, text, lyricX, lyricY, mPaint);
		drawSongString(mLyricCanvas, text, lyricX, lyricHeight, mPaint);

		// Update X positions for lyrics and chords
		lyricX += mPaint.measureText(text);

		// Update Chord X position to be the same as next Lyric position -
		// unless the chord position is already further across than the
		// lyrics, in which case leave it. Also need to make sure that
		// there is some space between the last chord and the next one -
		// if there's not much lyrics
		if (chordX < lyricX){
			chordX = lyricX;
			lyricX = chordX;
			//			Log.d(TAG,"Adjusting  AFTER: lyricX=" + lyricX + " lyricY=" + lyricY
			//					+ " chordX=" + chordX + " chordY=" + chordY);
		}
		lastPainted = LP_LYRIC;
		lyricsPainted = true;
	}


	/**
	 * Returns index into lyrics of longest set of complete words that will
	 * fit in space. Returns -1 if no complete words will fit in.
	 * REWRITTEN to perform better on long strings.
	 *
	 * @param lyrics the lyrics
	 * @param space amount of space available
	 * @return index of longest word
	 */
	private int findLongestWordIndexOld(String lyrics, float space) {
		int i = 0;
		while( i >= 0 && (mPaint.measureText(lyrics) > space)) {
			// chop words off the end until the line will fit
			i = lyrics.lastIndexOf(' ');
			if( i >= 0 ){
				lyrics = lyrics.substring(0, i);
			}
		}
		return i;
	}

	/**
	 * Returns index into lyrics of longest set of complete words that will
	 * fit in space. Returns -1 if no complete words will fit in.
	 *
	 * @param lyrics the lyrics
	 * @param space available space
	 * @return index index of longest set of words that will fit the given space
	 */
	private int findLongestWordIndex(String lyrics, float space) {
//		Log.d(TAG, "findLongestWordIndex: ["+lyrics+"] space["+space+"]");
		// split on spaces
		String words[] = lyrics.split("\\s");
		float len=0;
		int index=0;
		float spaceLen = mPaint.measureText(" ");
		boolean first=true;

		for( String word: words){
			len += mPaint.measureText(word);
			if(len>=space){
				break;
			}
			len += spaceLen;
			if(first){
				first = false;
			}
			else{
				index+=1;   // Add one for the space between words
			}
			// add words until the string is too long to fit in
			index += word.length();
		}
		// Return index of longest word or 0 if no words fit
//		Log.d(TAG, "findLongestWordIndex: ["+index+"] ["+lyrics.substring(0,index)+"]");
		return index;
	}

	/**
	 * Returns index into word of longest string that will fit in space.
	 * Returns -1 if no chars will fit.
	 *
	 * @param word text
	 * @param space available space
	 * @return longest string that will fit in the space available
	 */
	private int findLongestStringIndex(String word, float space) {
		while( mPaint.measureText(word) > space ){
			// chop chars off the end until the line will fit
			word = word.substring(0, word.length() - 1);
		}
		return word.length();
	}

	/**
	 * Starts a new line and adjusts the X & Y values for Chords and Lyrics.
	 * If no lyrics were printed on last line then push chords up underneath
	 * last chord line. if no chords have been printed then do a bit of a
	 * cludge: - copy the lyrics on the current line up to the chord
	 * position - blank out those lyrics - adjust the chord and lyric
	 * positions based on the moved lyrics
	 */
	private void startNewLine(Canvas g) {
        boolean newWay = true;
		if( chordsPainted ){
			if( lyricsPainted ){
                // Copy contents of mLyricsBitmap over the lyric line
                if(newWay){
                    g.drawBitmap(mLyricBitmap,0,lyricY-lyricHeight, mPaint);
                }
				// Normal - both chords and lyrics painted
				chordY += chordHeight + verticalSpace + lyricHeight
						+ verticalSpace;
			}
			else{
				// Chords but no lyrics - push chords up underneath last
				// chord line
				chordY += chordHeight + verticalSpace;
			}
		}
		else{ // No chords
			if( lyricsPainted ){
				// No chords
				// re-draw current lyric line up to empty chord line....
                if(newWay){
                    // Copy contents of mLyricsBitmap up over the chord line
//                    Log.d(TAG, "startNewLine2: chordY="+lyricY);
                    g.drawBitmap(mLyricBitmap,0,chordY-lyricHeight, mPaint);
                }
                else {
                    int c = mPaint.getColor();
                    mPaint.setColor(col.lyrics());

                    //				Log.d(TAG,"Re-drawing [" + lyricsBuffer + "] at y=[" + chordY+ "]");
                    drawSongString(g, lyricsBuffer, sideMargin, chordY, mPaint);
                    // ... then blank out old lyrics
                    mPaint.setColor(col.background());
                    // left, top, right, bottom, paint
                    //				Log.d(TAG,"drawRect left=[0], top=[" + (lyricY - lyricHeight)
                    //						+ "] right=[" + getWidth() + "] bottom=[" + lyricY	+ "]");
                    g.drawRect(0, lyricY - lyricHeight, getWidth(), lyricY
                            + verticalSpace, mPaint);
                    mPaint.setColor(c);
                }
				// Adjust new Chord Y position
				chordY += lyricHeight + verticalSpace;
			}
			else if(tabPainted){
				chordY += chordHeight + verticalSpace;
			}
			else{
				// Two line feeds together
				// no chords and no lyrics and no tab - do nothing.
				//OR
				// add a blank line
				// chord line
				if(mHonourLFs && lastPainted != LP_TEXT){
					chordY += chordHeight + verticalSpace;
				}
			}
		}

		lyricX = chordX = sideMargin;
		// Lyric position always in relation to chord position
		lyricY = getLyricYFromChordY();
		// reset chord/lyric Painted flags
		lyricsPainted = false;
		lyricsBuffer = "";
		chordsPainted = false;
		tabPainted = false;
        if(newWay) {
            mLyricCanvas.drawColor(col.background()); // Paint Lyric canvas with background colour
        }
    }

	/**
	 * Calls drawString after checking whether canvas needs to be re-sized
	 *
	 * @param g Canvas
	 * @param text text to paint
	 * @param x x coordinate
	 * @param y y coordinate
     */
    private void drawSongString(Canvas g, String text, float x, float y, Paint paint){
		g.drawText(text, x, y, paint);
	}

	/** Set song to paint on the canvas
	 *
	 * @param song Song
	 */
	public void setSong(Song song) {
		theSong = song;
		//		redraw = true;
		doInvalidate();
	}

	public void setTranspose(int value) {
		transposeBy = value;
	}

	public int getTranspose() {
		return transposeBy;
	}


	/*
	 * Updates Lyric Y position in relation to current Chord Y position.
	 * Avoid code repetition.
	 */
	private int getLyricYFromChordY() {
		return( chordY + chordHeight + verticalSpace );
	}

	/**
	 * Transposes up or down depending on key pressed.
	 * @param up true to transpose up, false to transpose down
	 */
	public void transposeSong (boolean up){
		if(up){
			if (transposeBy < 11) {
				++transposeBy;
			}
			else{
				transposeBy = 0;
			}
		}
		else{
			if (transposeBy > -11) {
				--transposeBy;
			}
			else{
				transposeBy = 0;
			}
		}
		// Need to redraw completely on transpose
		//		redraw = true;
		doInvalidate();
	}
	public void setShowGrids(boolean isOn){
		showGrids = isOn;
		preferences.setShowGrids(isOn);
		// Force full repaint
		//		redraw = true;
		//		mSongHeight=0;	// We don't know the height of the new song
		doInvalidate();
	}

	private ArrayList<String> getChords() {
		ArrayList<String> theChords = new ArrayList<String>();
		int start, end;

		String cs = theSong.getSongText();

		int i = 0;
		String chord;
		// Loop thru songtext...
		if (true) {
			end = 0;
			while ((i = cs.indexOf("[", end)) >= 0) {
				start = ++i;
				if ((end = cs.indexOf("]", start)) >= 0) {
					chord = cs.substring(start, end);
					chord = Transpose.cleanUpChord(chord);
					if (!chord.equalsIgnoreCase("")
							&& !theChords.contains(chord)) {
						//						Log.d(TAG,"Adding chord[" + chord + "]");
						theChords.add(chord);
					}
				}
				else{
					// No End bracket
					break;
				}
			}
		}
		return theChords;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(mSongHeight==0?1000:mSongHeight, MeasureSpec.EXACTLY));
	}

	/**
	 * @param tabsSupported the mTabsSupported to set
	 */
	public void setTabSupported(boolean tabsSupported) {
		this.mTabsSupported = tabsSupported;
	}

	/**
	 * @param isChopro the mIsChopro to set
	 */
	public void setIsChopro(boolean isChopro) {
		this.mIsChopro = isChopro;
	}

	public void setChordGridInstrument(int chordGridInstrument) {
		this.mChordGridInstrument = chordGridInstrument;
	}
}