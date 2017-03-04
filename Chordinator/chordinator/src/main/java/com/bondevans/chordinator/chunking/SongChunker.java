package com.bondevans.chordinator.chunking;


import android.annotation.SuppressLint;

import com.bondevans.chordinator.Log;
import com.bondevans.chordinator.SongUtils;

public class SongChunker {
	private static final String TAG = "SongChunker";
	private String mSongText="";
	private StringBuilder mCs;
	private int ind=0;	
	public boolean hasMore;
	private String mSongChorus;
	
	/**
	 * Constuctor takes song text as String
	 * @param songText
	 */
	public SongChunker(String songText){
		mSongText=songText;
		mCs = new StringBuilder(mSongText);
		hasMore = true;
	}

	@SuppressLint("DefaultLocale")
	public SongChunk getNextChunk(){
		int start, end;
		String lyric="";
		hasMore=true;
		boolean soc=false;

		// Loop thru songtext...
		while( ind<mCs.length() ) {
			SongChunk sc= new SongChunk();
			switch (mCs.charAt(ind)) {
				case '{':
					boolean gotSomething=false;
					// Ignore anything between curly brackets - unless a special tag
					if(mCs.substring(ind).toLowerCase().startsWith("{comment:") ||
							mCs.substring(ind).toLowerCase().startsWith("{c:"))
					{
						String text = SongUtils.tagValueX(mCs.substring(ind),"comment", "c", "");
						Log.d(TAG, "HELLO - FREETEXT:["+text+"]");
						sc.setFreeText(text);
						gotSomething=true;
//						Log.d(TAG, "adding:"+sc.toString());
					}
					else if(mCs.substring(ind).toLowerCase().startsWith("{soc") ||
							mCs.substring(ind).toLowerCase().startsWith("{start_of_chorus"))
					{
						// Start of chorus
						sc.setFreeText("Chorus");	// TODO - remove hardcoding
						gotSomething=true;
						soc = true;
					}
					else if(mCs.substring(ind).toLowerCase().startsWith("{eoc")||
							mCs.substring(ind).toLowerCase().startsWith("{end_of_chorus"))
					{
						// end of chorus - IGNORE
					}
					else if(mCs.substring(ind).toLowerCase().startsWith("{rc}")||
							mCs.substring(ind).toLowerCase().startsWith("{repeat_chorus"))
					{
						Log.d(TAG, "HELLO - REPEAT CHORUS");
						// Repeat chorus - must have been previously defined with {soc}/{eoc}
						if(mSongChorus != null && mSongChorus.length()>0){
							// Get to end of the rc tag
							int offset = mCs.substring(ind).indexOf("}");
							// ... and insert the Chorus that we saved previously
							if( offset > -1){
								sc.setFreeText("Chorus");	// TODO - remove hardcoding
								gotSomething=true;
								mCs.insert(ind+offset+1, mSongChorus);
							}
						}
						else{
							Log.d(TAG, "HELLO - IGNORING REPEAT CHORUS");
						}
					}
					else if(mCs.substring(ind).toLowerCase().startsWith("{start_of_tab") ||
							mCs.substring(ind).toLowerCase().startsWith("{sot"))
					{
						// Start of tabs, which chordinator treats as.  Find the
						// end of the tabs and skip to there
						int x = mCs.substring(ind).indexOf("{eot}");
						if(x == -1){
							x = mCs.substring(ind).indexOf("{end_of_tab}");
						}
						if( x == -1){
							// There is no end of tabs tag so end of song
							ind = mCs.length();
						}
						else{
							ind+=x;
						}
					}
					// By the time we get here i needs to be past the opening brace of the tab 
					// (or the opening brace of the matching pair)
					int tmp = mCs.indexOf("}", ind);
					if (tmp > -1) {
						ind = tmp + 1;
					} else {
						// If there isn't a matching brace then move
						// past the opening brace
						ind++;
					}
					// If we've found the start of the chorus, find and save the chorus for use later
					// with a "repeat chorus" tag
					if(soc){
						int cStart = ind;
						int cEnd = mCs.substring(cStart).toLowerCase().indexOf("{eoc");
						if(cEnd == -1){
							cEnd = mCs.substring(cStart).toLowerCase().indexOf("{end_of_chorus");
						}
						Log.L(TAG, "cStart", ""+cStart);
						Log.L(TAG, "cEnd", ""+cEnd);
						if(cEnd !=-1){
							// 	Save chorus for use later
							mSongChorus = mCs.substring(cStart, cStart+cEnd)+"{eoc}";
							Log.d(TAG, "HELLO CHORUS: "+mSongChorus);
						}
					}

					if(gotSomething){
						return (sc);
					}
					break;

				case '[':
					// Start of a chord - get everything up until next ']' char
					start = ++ind;
					while (ind < mCs.length() && mCs.charAt(ind) != ']') {
						++ind;
					}
					end = ind++;
					String chord = mCs.substring(start, end);

					// Get the lyrics that go with this chord
					// Or its an EOL or its a freetext.  
					start = ind;
					while (ind < mCs.length() /* && cs.charAt(ind) != '<' */ && mCs.charAt(ind) != '['
						&& mCs.charAt(ind) != '{'&& mCs.charAt(ind) != '\n') {
						++ind;
					}
					end = ind;
				   	// Need to check whether there is a chord in the middle of a word.  
					// If so put a hyphen on the end of the first half
					lyric = mCs.substring(start, end);
					if(isChordInWord(lyric, mCs.substring(end, end+10<mCs.length()?end+10:mCs.length()))){
						lyric = lyric.concat("-");
					}
					// also need to check whether there is another chord straight after this one.  If so
					// add a space to the chord
					else if(isChordNext(mCs.substring(end, end+10<mCs.length()?end+10:mCs.length()))){
//						Log.d(TAG, "HELLO CHORDISNEXT");
						chord = chord.concat(" ");
					}

					sc.setChordText(chord);
					sc.setLyricText(lyric);
//					Log.d(TAG, "adding:"+sc.toString());
					return (sc);
				case '\n':// LF
					// Treat a line break as a line break
					sc.setEOL(true);
//					Log.d(TAG, "adding:"+sc.toString());
					++ind;
					return (sc);
				default:
					// Default Case - its just lyrics so copy everything up
					// until next format char OR LINEFEED
					start = ind;
					while(ind < mCs.length() && mCs.charAt(ind)!= '[' /* && cs.charAt(ind)!= '<' */ 
						&& mCs.charAt(ind)!= '{' && mCs.charAt(ind)!= '\n' ){
						++ind;
					}
					end = ind;
				   	// Need to check whether there is a chord in the middle of a word.  
					// If so put a hyphen on the end of the first half
					lyric = mCs.substring(start, end);

					if(isChordInWord(lyric, mCs.substring(end, end+10<mCs.length()?end+10:mCs.length()))){
						lyric = lyric.concat("-");
					}
					sc.setLyricText(lyric);
//					Log.d(TAG, "adding:"+sc.toString());
					return (sc);
			}// End of switch
		} // End of while loop
		hasMore=false;
		return (new SongChunk("","","","",true));
	} //end of method

	private boolean isChordNext(String nextBit) {
		if(nextBit.trim().startsWith("[")){
			return true;
		}
		return false;
	}

	private boolean isChordInWord(String lyric, String nextBit){
//		Log.d(TAG, "HELLO chordinword=["+lyric+"]["+nextBit+"]");
		boolean ret = true;
		if( nextBit.startsWith("[") &&	// Next thing IS a chord
				lyric.length()>0 &&		// we actually have some lyrics
				!lyric.endsWith(" ") &&	// Last lyric NOT a space
				!lyric.endsWith("-")){	// Last lyric char NOT a dash
			// Need to check whether the next thing after the chord is a lyric char
			int i = nextBit.indexOf("]");
			if(i>=0 && i<(nextBit.length()-1) && !SongUtils.charIsInString(nextBit.charAt(i+1)," [\n")){
				ret = true;
			}
			else{
				ret = false;
			}
		}
		else{
			ret = false;
		}
//		Log.d(TAG, "HELLO chordinword="+(ret?"TRUE":"FALSE"));
		return ret;
	}
}
