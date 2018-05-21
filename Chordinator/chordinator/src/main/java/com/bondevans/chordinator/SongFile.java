package com.bondevans.chordinator;

import java.io.File;
import java.io.FileDescriptor;


import android.annotation.SuppressLint;


/**
 * Class representing a Chordinator Song File
 * @author Paul
 */
public class SongFile {
	private final static String TAG = "SongFile";
	private Song	theSong=new Song();
	private String	songFilePath = null;
	private String	songFile = null;
	private String	songPath = null;
	public boolean hasTitle;

	/**
	 * Constructor that takes a String representing a file/Directory path
	 * and loads the song.
	 * @param filePath File Path
	 * @param fileDescriptor FileDescriptor
	 *@param defEncoding  @throws ChordinatorException
	 */
	public SongFile(String filePath, FileDescriptor fileDescriptor, String defEncoding) throws ChordinatorException{
		songFilePath = filePath;
		File x = new File(filePath);
		songFile = x.getName();
		songPath = x.getParent();
//		Log.d(TAG, "HELLO File=["+songFile+ "] path=["+songPath+"]");
		this.setSongDetails(SongUtils.loadFile(filePath, fileDescriptor, defEncoding));
	}

	public void reloadSong(String defEncoding) throws ChordinatorException{
		setSongDetails(SongUtils.loadFile(songFilePath, null, defEncoding));
	}

	/**
	 * Returns the Song object associated with this Song File
	 * @return
	 */
	public Song getSong(){
		return theSong;
	}

	/**
	 * Returns Title of the Song
	 * @return
	 */
	public String getTitle(){
		return theSong.getTitle();
	}

	public String getTitleTitleCase(){
		return (toTitleCase(getTitle()));
	}

	public String getArtistTitleCase(){
		return (toTitleCase(removeThe(getArtist())));
	}
	
	public String getComposerTitleCase(){
		return (toTitleCase(getComposer()));
	}
	

	public String getTitleAndArtist(){
		if( !getArtist().equalsIgnoreCase("")){
			return(getTitle()+" - "+ getArtist());
		}
		else if( !getComposer().equalsIgnoreCase("")){
			return(getTitle()+" - "+ getComposer());
		}
		else{
			return(getTitle());
		}
	}

	/**
	 * Returns Artist of the Song
	 * @return
	 */
	public String getArtist(){
		return theSong.getArtist();
	}

	/**
	 * Returns Composer of the Song
	 * @return
	 */
	public String getComposer(){
		return theSong.getComposer();
	}

	/**
	 * @return the songFilePath
	 */
	public String getSongFilePath() {
		return songFilePath;
	}

	/**
	 * @param songFilePath the songFilePath to set
	 */
	public void setSongFilePath(String songFilePath) {
		this.songFilePath = songFilePath;
	}

	/**
	 * Sets up the Song details from the contents of the Song File
	 * @param encContents
	 */
	private void setSongDetails(String encContents){
	/*
	 * 		ChordPro Formatting supported by Chordinator
	 * 		{title: title string} ({t:string})
	 * 		{subtitle: subtitle string} ({st:string})  - same as {artist=}
	 *		{start_of_chorus} ({soc})  - switches on printing in fake bold
	 *		{end_of_chorus} ({eoc}) - switches off printing in fake bold
	 *		{comment: string} ({c:string})  - same as <string>
	 *		{start_of_tab} ({sot}) / {end_of_tab} ({eot})  - everything between them IGNORED
	 *		{guitar_comment: string} ({gc:string}) - IGNORED
	 *		{new_song} ({ns}) - IGNORED
	 *		{new_page} ({np}) - IGNORED
	 *		{new_physical_page} ({npp}) - IGNORED
	 *		{column_break} ({colb}) - IGNORED
	 *		{data_abc: xyz} ({d_abc:xyz}) - IGNORED
	 *		{footer: xyz} ({f:xyz}) - IGNORED
	 *		{key: xyz} ({k:xyz})  - IGNORED
	 *		{define:.......} - chord grid definitions - converted to chordinator format
	 *		# - lines beginning with # are comments and should be ignored
	 */

		/* - ENCRYTPTED SONG FILES
		String EncryptionKey = "BIGBOLLOX";	// TODO - remove this key
		// Decode the encrypted file
		System.out.println("HELLO 1 encContents=["+encContents+"]");
		DesEncrypter de = new DesEncrypter(EncryptionKey);
		String decContents = de.decrypt(encContents);
		System.out.println("HELLO 2 decContents=["+decContents+"]");
		*/
		String decContents = encContents;
		// Replace any tabs with a single space
		decContents = decContents.replaceAll("[\t]", " ");
		// Replace any DOS-style CR/LF pairs with a single UNIX-style LF
		decContents = decContents.replaceAll("\r\n", "\n");
		// Replace any MAC-style CRs with a single UNIX-style LF
		decContents = decContents.replaceAll("[\r]", "\n");
		// Remove any comment lines - any NEW lines starting with # up until
		// the next new line character(s). Replace with single LF
		decContents = decContents.replaceAll("[\n]#.*[\n]", "\n");
		// Also need to allow for last line being a comment line
		decContents = decContents.replaceAll("[\n]#.*","");
		// TODO REMOVE THIS
		int x=0;
		while ( x<decContents.length()){
			int y=decContents.charAt(x);
			if(y==146){
//				Log.d(TAG, "HELLO: x="+x+" char ["+decContents.charAt(x)+"] byte=["+y+"]");
			}
			x++;
		}
		char oldChar = 146;
		decContents.replace(oldChar, '\'');
		// Replace any old-style angled-bracket free texts with new-style (proper ChoPro) {c:xxxxxx}
		decContents = decContents.replaceAll("<([^>]*)>", "{c:$1}");
		
		// Examine Contents for Title/Artist/Composer details.
		// These details should be at the start of the file in the format:
		// {title:Hotlove}{artist:Paul Evans}{composer:Paul Evans}
		String title = SongUtils.tagValue(decContents, "title","t","");
		if(title.equalsIgnoreCase("")){
			hasTitle=false;
		}
		else{
			theSong.setTitle(title);
			hasTitle=true;
		}

		String substr = SongUtils.tagValue(decContents, "artist","","");
		if( !substr.equals("") ){
				theSong.setArtist(substr);
		}
		else{
			// If there isn't an artist tag then see if the ChordPro subtitle tag is present
			theSong.setArtist(SongUtils.tagValue(decContents, "subtitle", "st", ""));
		}

		theSong.setComposer(SongUtils.tagValue(decContents, "composer"));

		// Convert any ChordPro-style chord definitions into internal format
		// ChordPro format = {define: E5 base-fret 7 frets 0 1 3 3 x x} - although "base-fret" and "frets"
		// not always present.
		String grids = SongUtils.tagValue(decContents, "chordgrids","","");
		// If there are some grids defined in the {chordgrids:} tag AND
		// there are some more in the {define:} tags then need to add them
		// together with appropriate commas....
		boolean comma = (grids.length()>0)?true:false;
		// Get each {define:...} tag
		int start=0;
		start = decContents.indexOf("{define:", start);
		while(start>-1){
			String define = SongUtils.tagValue(decContents.substring(start), "define").trim();
			// Split each one into its elements
			String defBits[] = define.split("\\s+");
			// Put it back in the right order
			// Make sure we've got enough bits
			String grid = "";
			if(defBits.length>=8){
				// Assume that the last 6 bits are the 6 string values
				grid = defBits[0]+":"+defBits[defBits.length-6]+defBits[defBits.length-5]+defBits[defBits.length-4]+
				defBits[defBits.length-3]+defBits[defBits.length-2]+defBits[defBits.length-1]+"_"+defBits[defBits.length==10?2:1];
				Log.d(TAG, "HELLO Chord grid=["+grid+"]");
				if(comma){
					grids = grids.concat(","+grid);
				}
				else{
					grids = grids.concat(grid);
					comma = true; // need a comma next time
				}
			}
			else{
				Log.d(TAG, "HELLO Invalid Define tag");
			}

			start = decContents.indexOf("{define:", start+1);
		}
		theSong.setChordGrids(grids);
		theSong.setSongText(decContents);
		// Set up various flags regarding the contents of the song
		setFlags(decContents);
	}
	private void setFlags(String decContents) {
		// TODO Auto-generated method stub
		
	}
	public String getSongFile() {
		return songFile;
	}
	public String getSongPath() {
		return songPath;
	}
	public static final void doTests(){
		convAngledBrackets(test1);
		convAngledBrackets(test2);
		convAngledBrackets(test3);
		convAngledBrackets(test4);
	}
	public static final String test1="<Chorus>Mary had a little lamb in a field next to the river";
	static final String test2="Mary had a little lamb<POOPOO> in a field next to the river<Chorus>";
	static final String test3="Mary had a little <Chorus>lamb in a field next <hello. Mum!!> to the river";
	static final String test4="Mary <BLOBby>had a little lamb in a < >field next to the river";

	private final static void convAngledBrackets(String song){
		song = song.replaceAll("<([^>]*)>", "{c:$1}");
		Log.d(TAG, "HELLO: convAngledBrackets["+song+"]");
	}
	
	@SuppressLint("DefaultLocale")
	public static String toTitleCase(String in){
		Log.d(TAG, "HELLO toTitleCase in["+in+"]");
		StringBuilder out = new StringBuilder("");
		// Split the line into items based on whitespace
		// TODO - also split on / \ - etc
		String words[] = in.toLowerCase().trim().split("\\s");
		for(String word: words){
			out.append(word.substring(0, (word.length()>0?1:0)).toUpperCase()+(word.length()>1?word.substring(1):"")+" ");
		}
		// Look out for special case - Mccartney - restore to McCartney
		String ret = out.toString().trim().replaceAll("Mcc", "McC");
//		Log.d(TAG, "HELLO toTitleCase out["+ret+"]");
		return ret;
	}
	public static String removeThe(String artist){
		String [] thes={"the "};
		// If the string starts with "the" remove it
		for(String x:thes){
			if(artist.regionMatches(true, 0, x, 0, x.length())){
				return artist.substring(x.length());
			}
		}
		return artist;
	}

}
