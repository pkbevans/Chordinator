package com.bondevans.chordinator.grids;

import com.bondevans.chordinator.SongUtils;

public class ChordShapeProvider {
//	private final static String TAG = "ChordShapeProvider";
	private final static String [] roots = {"Ab","A","Bb","B","C#","C","D","Eb","E","F#","F","G"};
	private final static String [][] syns = {
			//
			{"add6","6"},
			{"4","sus4"},
			{"+","aug"}};
	public static final int INSTRUMENT_GUITAR = 1;
	public static final int INSTRUMENT_MANDOLIN = 2;
	public static final int INSTRUMENT_UKELELE = 3;
	public static final int INSTRUMENT_BANJO = 4;

    //declare and initialise two dimensional array
	// set of chords for each root
	private static String chordShape[][]=null;
	private ExtraShapeFactory extraShapes;

    /**
	 * Constructor takes an array of additional chord shapes and number of strings
	 * @param extraShapes Array of additional chord shapes
     * @param strings Number of strings on this instrument
	 */
	public ChordShapeProvider(String extraShapes, int strings){
		this.extraShapes = new ExtraShapeFactory(extraShapes, strings);
		chordShape = getShapes();
	}
	protected String[][] getShapes() {
		return null;
	}
	protected int STRINGS(){
		return 0;
	}

	protected int FRETS(){
		return 0;
	}
    protected int[] openNotes(){return new int[0];}

	public interface ShapeProvider{
		int FRETS();
		int STRINGS();
		String findShape(String chord);
        int[] openNotes();
	}
	/**
	 * Finds chord shape matching given chord name - sets shapeIndex
	 * @param chordName
	 *
	 */
	public String findShape(String chordName) {
		// Find the shape
		String shape=findShape2(getSynonym(getAltName(chordName)));

		// IF we failed to find the shape see if its called something different
		// e.g. Eb=D#, F#=Gb, etc.  NB  The original name is displayed over the
		// chord grid - this is just to reduce the number of chords we have to
		// maintain in our table.

		// If we failed to find a shape and the chord has got a bass note
		// specified (e.g. D7/C)...
		if(shape.equalsIgnoreCase("") && SongUtils.charIsInString('/', chordName)){
			// chop off the bass bit and try again
			chordName = chordName.substring(0, chordName.indexOf("/"));
			shape=findShape2(getSynonym(getAltName(chordName)));
		}
//		Log.d(TAG, "HELLO shape=["+shape+"]");
		return shape;
	}
	/**
	 * Finds chord shape matching given chord name - sets shapeIndex
	 * @param chordname
	 *
	 */
	private String findShape2(String chordname) {
		String shape="";
		// First look in the extra shapes.....
		shape = extraShapes.findShape(chordname);

		// ....then in the standard shapes if not found
		if( shape.equalsIgnoreCase("") ) {
			for(int i=0; shape.equalsIgnoreCase("") && i< chordShape.length;i++){
				if(chordname.equalsIgnoreCase(chordShape[i][0])){
					shape = chordShape[i][1];
				}
			}
		}
		return shape;
	}
	/**
	 * Returns alternative name of certain sharps and flats -e.g. returns "Abm7" if given "G#m7"
	 * Returns name input if no alternative found.
	 * @param chord
	 * @return
	 */
	private String getAltName(String chord){
		String ret = chord;
		String[][] alts={
				{"A#","Bb"},
				{"B#","C"},
				{"Cb","B"},
				{"Db","C#"},
				{"D#","Eb"},
				{"E#","F"},
				{"Fb","E"},
				{"G#","Ab"},
				{"Gb","F#"}
		};
		// Don't bother if the chord is only 1 char
		if(chord.length()>1){
			for ( int x=0; x<alts.length; x++ ){
				if(chord.substring(0, 2).equalsIgnoreCase(alts[x][0])){
					// Add the end of the chord name back on to the end of the alternative name
					return(alts[x][1].concat(chord.substring(2)));
				}
			}
		}
		return(ret);
	}
	private String getSynonym(String chord) {
//		Log.d(TAG, "HELLO getSynonym=["+chord+"]");
		String ret = chord;
		String root="";
		String type="";
		// Match to root of the chord - e.g. C F#, Bb etc
		for(String x:roots){
			if( chord.startsWith(x)){
				root = x;
				type = chord.substring(x.length());
				break;
			}
		}
//		Log.d(TAG, "HELLO root=["+root+"] type=["+type+"]");
		if( !root.equalsIgnoreCase("")){
			// Now see if the rest of the chord matches any of out known synonyms
			for( int i=0; i< syns[0].length;i++){
				if( type.equalsIgnoreCase(syns[i][0])){
					// If it does then use the standard name
					ret = root+syns[i][1];
					break;
				}
			}
		}
//		Log.d(TAG, "HELLO ret=["+ret+"]");

		return ret;
	}
}