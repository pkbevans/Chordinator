package com.bondevans.chordinator.chunking;



import com.bondevans.chordinator.Log;
import com.bondevans.chordinator.Song;

/**
 * This class is used to format a chords-over-lyrics song file to html
 * 
 * @author Paul
 *
 */
public class TextSongHTMLFormatter extends SongTextFormatter {
	private final static String TAG = "TextSongHTMLFormatter";
	private String fileName;

	public TextSongHTMLFormatter(Song song, int transBy, String fileName) {
		super(song, transBy);
		this.fileName = fileName;
	}

	/**
	 * Appends characters to either Chords or Lyrics depending on type
	 * @param type
	 */
	@Override
	public void appendEOL(int type){
		switch(type){
		case T_EOL_CHORDS:
			mChords.append(EOL);
			break;
		case T_EOL_LYRICS:
			mLyrics.append(EOL);
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
	@Override
	public String wrap( String txt, int type){
		String pref=""; 
		String suff=""; 
		switch(type){
		case T_TITLE:
			pref = "";
			suff = EOL;
			break;
		case T_BYLINE:
			pref = "";
			suff = EOL;
			break;
		case T_LYRIC:
		case T_CHORD:
			// No wrapping for lyrics and chords for TEXT format - but there is for HTML
			break;
		case T_FREETEXT:
			pref = "";
			suff = EOL;
			break;
		case T_EOL:
			pref = "";
			suff = EOL;
			break;

		default:
			Log.d(TAG, "INVALID TYPE: "+type);
		}
		Log.d(TAG, "HELLO wrap type="+type+" txt=["+txt+"]");
		return pref+txt+suff;
	}

	@Override
	public String getFormattedSong(){
		return 
				"<!DOCTYPE html><html><head><title>"+fileName+"</title></head><body><pre>"+
				super.getFormattedSong()+ 
				"</pre></body></html>";
	}
}
