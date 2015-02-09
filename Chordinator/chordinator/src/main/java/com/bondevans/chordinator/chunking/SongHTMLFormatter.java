package com.bondevans.chordinator.chunking;



import com.bondevans.chordinator.Log;
import com.bondevans.chordinator.Song;

public class SongHTMLFormatter extends SongTextFormatter {
	private final static String TAG = "SongHTMLFormatter";
	private Song mSong;

	public SongHTMLFormatter(Song song, int transBy) {
		super(song, transBy);
		this.mSong = song;
	}

	@Override
	public void appendEOL(int type){
		switch(type){
		case T_EOL_CHORDS:
			mChords.insert(0, "<tr class=\"cl\">");
			mChords.append("</tr>\n");
			break;
		case T_EOL_LYRICS:
			mLyrics.insert(0, "<tr class=\"tl\">");
			mLyrics.append("</tr>\n");
			break;
		}
	}

	@Override
	public String wrap( String txt, int type){
		String pref=""; 
		String suff=""; 
		switch(type){
		case T_TITLE:
			pref = "<style type=\"text/css\">"+
			"body { font-family: \"Trebuchet MS\", Arial, Helvetica, sans-serif; }"+
			"</style>"+	
			"<p align=\"center\"><font color=\"#000000\"><b>";
			suff = "</b></font></p>";
			break;
		case T_BYLINE:
			pref = "<p align=\"center\"><font color=\"#000000\">";
			suff = "</font></p>";
			break;
		case T_FREETEXT:
			pref = "<p><b><font color=\"#000066\">";
			suff = "</font></b></p>";
			break;
		case T_LYRIC:
			pref = "<td><font color=\"#000000\">";
			suff = "</font></td>";
			break;
		case T_CHORD:
			pref = "<td><font color=\"#FF0000\">";
			suff = "</font></td>";
			break;
		case T_EOL_CHORDS:
			pref = "<tr class=\"cl\">";
			suff = "</tr>\n";
			break;
		case T_EOL_LYRICS:
			pref = "<tr class=\"tl\">";
			suff = "</tr>\n";
			break;
		case T_EOL:
			pref = "<table cellpadding=0>";
			suff = "</table>\n";
			break;
	
		default:
				Log.d(TAG, "INVALID TYPE: "+type);
		}
//		Log.d(TAG, "HELLO wrp type="+type+" txt=["+txt+"]");
		if(txt.endsWith(" ")){
			// Replace last space with non-breaking space
			txt = txt.substring(0, txt.length()-1).concat("&nbsp;");
		}
		return pref+txt+suff;
	}

	@Override
	public String getFormattedSong(){
		return
				"<!DOCTYPE html><html><head><title>"+mSong.getTitle()+"</title></head><body>"+
				super.getFormattedSong()+
				"</body></html>";
	}
}
