package com.bondevans.chordinator.conversion;



import com.bondevans.chordinator.Log;
import com.bondevans.chordinator.Song;

public class SongConverter {
	private String intText="";
	private Song theSong;
	private String songLine[];
	private int lineType[];
	private static final int MAX_TOKENS_FOR_TEXT=3;
	public static final int TYPE_TITLE = 0;
	public static final int TYPE_CHORD = 1;
	public static final int TYPE_LYRIC = 2;
	public static final int TYPE_TEXT = 3;
	public static final int TYPE_EMPTY = 4;
	private static final String TAG = "SongConverter";
	public final String[] TypeText={"Title", "Chords", "Lyrics", "Text", "Empty"};

	/**
	 * Constructor which converts
	 */
	public SongConverter(){
		// Initialise various variables
		theSong = new Song();
		theSong.setSongText("");
	}

	public Song getSong(){
		return theSong;
	}

	public String getIntText(){
		return intText;
	}

	/**
	 * Creates and returns the Finished CSF format SongText
	 * @return
	 */
	public String createCSF(){
		StringBuilder csf= new StringBuilder();
		Log.d(TAG, "CREATING CSF");
		// First insert Title/Artist/etc stuff at start
		csf.append("{title:" + theSong.getTitle().trim() + "}\n{st:"
				+ theSong.getArtist().trim() + "}\n{composer:" + theSong.getComposer().trim()
				+"}\n");
		// loop thru the list of lines merging the chord and lyric lines
		// and putting formatting round title, artist, text etc
		for (int x=0; x< songLine.length; x++){
			Log.d(TAG,  "Line " + x + ": [" + songLine[x] + "] :"+ TypeText[lineType[x]] );
			// Ignore title line and empty lines
			switch(lineType[x]){
			case TYPE_TITLE:
				Log.d(TAG,  "TITLE");
				// Ignore
				break;
			case TYPE_EMPTY:
				Log.d(TAG,  "EMPTY");
				// IGNORE
				break;
			case TYPE_CHORD:
				Log.d(TAG,  "CHORD");
				// If its a chord line then look at the next line.  IF that is a lyric line
				// then splice them together. Otherwise put the line in with square
				// brackets round each chord.

//				 Add some spaces before the next line unless previous line was text or Title
//				if( x>0 && lineType[x-1] != TYPE_TEXT && lineType[x-1] != TYPE_TITLE){
//					csf.append(doSplice("", " "));
//				}

				if( lineType.length > x+1 && lineType[x+1] == TYPE_LYRIC){
					csf.append(doSplice(songLine[x], songLine[x+1]));
					x++;
				}
				else{
					// Chord line on its own
					csf.append(doSplice(songLine[x], ""));
				}
				break;
			case TYPE_LYRIC:
				Log.d(TAG,  "LYRIC");
				// Lyric line on its own
				csf.append(doSplice("", songLine[x]));
				break;
			case TYPE_TEXT:
				Log.d(TAG,  "TEXT");
				csf.append( "\n{c:"+songLine[x] + "}\n");
				break;
			}
		}
		theSong.setSongText(csf.toString());
		return theSong.getSongText();
	}
	/**
	 * Splices a lyric and a chord line together and puts square brackets
	 * round each chord symbol
	 * @param chord
	 * @param lyric
	 * @return
	 */
	private String doSplice(String chord, String lyric){
		StringBuilder splice = new StringBuilder(); 
		int x=0; // index into chord
		int y=0; // index into lyric

		Log.d(TAG, "Splicing :\n1234567890123456789012345678901234567890\n[" + chord + "] + \n[" + lyric + "]");
		// Loop thru chars on Chord line
		while( x< chord.length()){
			// If no chord, move lyrics into new line
			if( chord.charAt(x) == ' '){
				if(y < lyric.length()) {// Make sure we don't fall off the end of the lyrics
					splice.append(lyric.charAt(y++));
				}
				else {
					// Add spaces if no lyrics
					splice.append(" ");
				}
				++x;	// Increment chord index
			}
			else {
				// Any non-whitespace must be the start of a chord -add the
				// brackets and splice into line
				splice.append('[');
				int chordLength = 0;
				while(x < chord.length() && chord.charAt(x) != ' '){
					splice.append(chord.charAt(x++));
					chordLength++;
				}
				splice.append(']');
				// Need to add in same number of chars from the lyrics
				while( chordLength-- > 0 && y < lyric.length()){
					splice.append(lyric.charAt(y++));
				}
			}
		}
		// Add any extra lyrics ..
		while( y < lyric.length()){
			splice.append(lyric.charAt(y++));
		}

		// Add a line feed
		splice.append("\n");
		Log.d(TAG, "Spliced chords & lyrics:" + splice.toString());
		return splice.toString();
	}

	
	/**
	 * Sets the Song Title
	 * @param title
	 */
	public void setTitle( String title){
		this.theSong.setTitle(title);
	}
	/**
	 * Sets the Song Artist
	 * @param artist
	 */
	public void setArtist( String artist){
		this.theSong.setArtist(artist);
	}
	/**
	 * Sets the Song Composer
	 * @param composer
	 */
	public void setComposer( String composer){
		this.theSong.setComposer(composer);
	}

	/**
	 * Converts from mono-spaced format to intermediate csf format - which
	 * is essentially assigning a type to each line of the song.
	 */
	public void convertToIntermediateFormat(String inputText){
		boolean emptyLine = false;
		intText = "";
		// Read thru each line and try and work out what type it is (title/lyrics/chords/text)
		// First split the text up into lines (an array of Strings)..
		songLine = inputText.split("\n");
		lineType = new int[songLine.length];
		// Now split each line into items - split on white space - and see
		// if each one looks like a chord.
		for (int x=0; x< songLine.length; x++){
			// Trim off any trailing spaces 1st
			Log.d(TAG,  "Line " + x + ": [" + songLine[x] + "]");
			int i = songLine[x].length()-1;
			while(i>1 && songLine[x].charAt(i) == ' ')--i;

			songLine[x] = songLine[x].substring(0, i+1);
			Log.d(TAG,  "Line " + x + ": [" + songLine[x] + "]");

			// Split the line into items based on whitespace
			String item[] = songLine[x].split("\\s");

			// If its ALL whitespace then special handling required
			if( songLine[x].trim().equalsIgnoreCase("")){
				emptyLine = true;
				Log.d(TAG, "EMPTY LINE");
			}
			else{
				emptyLine = false;
			}

			// If we find 5 in a row that look like chords then assume
			// the whole line contains chords. Likewise if we find 2 non-blank items
			// that are not chords then its probably not a chord line.
			int chordCount=0;
			for (int y=0; chordCount > -2 && chordCount < 5 && y < item.length; y++){
				if(item[y].length() == 0){
					// Ignore
				}else if(isChord(item[y])){
					Log.d(TAG, "Item " + y + ": [" + item[y] + "] - Chord");
					++chordCount;
				}else{
					Log.d(TAG, "Item " + y + ": [" + item[y] + "] - NOT Chord");
					--chordCount;
				}
			}
			if(chordCount > 0){
				// Definitely looks like Chord line
				lineType[x] = TYPE_CHORD;
			}
			else if(emptyLine){
				// Might be either just padding or may be a chord line with no chords on it.
				// Doesn't really matter - ignore it.
				lineType[x] = TYPE_EMPTY;
			}
			else{
				// Definitely not a Chord line, so now need to work out
				// whether its TITLE/Lyrics or text.

				//If its the first line then - assume its the title
				if( x == 0){
					lineType[x] = TYPE_TITLE;
					theSong.setTitle(songLine[x]);
				}
				else if(isText(songLine[x])){
					// If the text is something obvious like Verse/Chorus/INtro/etc then assume that
					// it is text
					lineType[x] = TYPE_TEXT;
				}
				// If the previous line was a chord line or an empty line then assume that this
				// is a lyric line
				else if(x>0 && (lineType[x-1] == TYPE_CHORD ||lineType[x-1] == TYPE_EMPTY )){
					lineType[x] = TYPE_LYRIC;
				}
				// If there are more than x elements - assume its lyrics
				else if (item.length > MAX_TOKENS_FOR_TEXT){
					lineType[x] = TYPE_LYRIC;
				}
				// Otherwise its probably just text
				else{
					lineType[x] = TYPE_TEXT;
				}
			}
			intText = intText.concat(TypeText[lineType[x]] + ": " + songLine[x]+ "\n");
			Log.d(TAG,  "Line [" + x + "] is a "+ TypeText[lineType[x]] + "line");
		}
	}

	public String[] getLines(){
		return songLine;
	}

	public int[] getTypes(){
		return lineType;
	}
	/**
	 *	 Sets Line to given type (CHORD/LYRIC/TEXT)
	 * @param line
	 * @param type
	 */
	public void setLineType(int line, int type){
		lineType[line] = type;
	}
	/**
	 * Checks for obvious TEXT keywords like Verse/Chorus/Intro etc at start of line
	 * @param text
	 * @return
	 */
	private boolean isText( String text){
		boolean ret = false;
		String keyWord[] = {"verse", "chorus", "intro","other bit", "instrumental","middle", "outro", "solo", "bridge"};
		for(int i=0; ret == false && i < keyWord.length; i++){
			ret = text.toLowerCase().startsWith(keyWord[i]);
		}
		return ret;
	}
	
	private static String songLine2[];
	/**
	 * Checks the input text for lines containing chords
	 * @param inputText
	 * @return
	 */
	public static boolean containsChordLines(String inputText, int maxLines){
		// Read thru each line and try and work out if it contains any definite chord lines
		// First split the text up into lines (an array of Strings)..
		songLine2 = inputText.split("\n");
		int i=0;
		for (String xx: songLine2){
			if(isChordLine(xx)){
				Log.d(TAG, "HELLO FOUND CHORD LINE: "+xx);
				return true;
			}
			if(++i>maxLines){
				return false;
			}
				
		}
		return false;
	}
	public static boolean isChordLine(String songLine){
		// Split the line into items based on whitespace
		String item[] = songLine.split("\\s");
		// If we find 5 in a row that look like chords then assume
		// the whole line contains chords. Likewise if we find 2 non-blank items
		// that are not chords then its probably not a chord line.
		int chordCount=0;
		for (int y=0; chordCount > -2 && chordCount < 5 && y < item.length; y++){
			if(item[y].length() == 0){
				// Ignore
			}else if(isChord(item[y])){
				Log.d(TAG, "Item " + y + ": [" + item[y] + "] - Chord");
				++chordCount;
			}else{
				Log.d(TAG, "Item " + y + ": [" + item[y] + "] - NOT Chord");
				--chordCount;
			}
		}
		return (chordCount>0?true:false);

	}
	/** Tests a string to see whether it looks like a chord
	 *
	 * @param item
	 * @return
	 */
	private static boolean isChord(String text){
		boolean ischord=true;
		int arbitaryNumber = 9;	// Maximum length of a chord

		// Assume any long items are not chords
		if( text.length()> arbitaryNumber)
			ischord = false;

		// Ignore case
		String item = text.toUpperCase();
		char x[] = item.toCharArray();
		for( int i=0; ischord == true && i < x.length;i++){
			switch(i){
			case 0:// 1st char must be C-B
				if( !isCharIn(x[i], "CDEFGAB")){
					// Not a chord so break and return false
					ischord = false;
				}
				break;
			case 1: // 2nd char must be .....
				if( !isCharIn(x[i], "ABDMS1579/#")){
					// Not a chord so break and return false
					ischord = false;
				}
				break;
			default:	// Once we've got here look for no-nos
				if( isCharIn(x[i], "HIKLNPQRTVWXYZ,.<>?;':@~[]{}=_*&^%$£\"!|\\")){
					// Not a chord so break and return false
					ischord = false;
				}
				break;
			}
		}

		return ischord;
	}
	/**
	 * Checks whether given char is in given String
	 * @param x
	 * @param y
	 * @return
	 */
	private static boolean isCharIn( char x, String y){
		boolean ret = false;
		for(int i=0;i< y.length();i++){
			if(x == y.charAt(i)){
				ret = true; break;
			}
		}
		return ret;
	}
//	static class Log {
//		static void d(String tag, String text){
//			System.out.println(TAG+" : "+text);
//		}
//	}
}
