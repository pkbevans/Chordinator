package com.bondevans.chordinator;

//

public class Transpose {
//	private static final String TAG = "Transpose";

	/**
	 * Transposes a chord by the given number of semi-tones
	 * @author Paul
	 *
	 */
	public static String chord (String chordIn, int transposeBy){
		String[] keys = { "C", "C#", "D", "Eb", "E", "F", "F#", "G", "Ab",
				"A", "Bb", "B", "C", "Db", "D", "D#", "E", "F", "Gb", "G",
				"G#", "A", "A#" };
		int current = -1;
		int matchLength = -1;
		String chordOut = chordIn;

		if( transposeBy != 0 ){
			// Find current place in keys
			for( int x = 0; x < keys.length; x++ ) {
				// Looking for the longest match between incoming chord and
				// the values in keys - i.e. match to F# beats F
				if( chordIn.startsWith(keys[x] )
						&& keys[x].length() > matchLength ){
					current = x;
					matchLength = keys[x].length();
				}
			}
			if (current > -1) {
				// Make sure we don't transpose below 0 and above max value
				// of keys
				if (current + transposeBy < 0) {
					current += 12;
				}else if (current + transposeBy > 12) {
					current -= 12;
				}
				// Swap current value for new one (ignore if no match found)
				chordOut = keys[current + transposeBy]
				                + chordIn.substring(matchLength);
				// Also need to check whether there is a Bass note specified
				// after the chord - If so this needs to be transposed as well
				int x = chordOut.indexOf("/");
				if (x > 0 && x < chordIn.length()) {
					// Transpose the bit after the slash
					chordOut = chordOut.substring(0, x + 1)
					+ chord(chordOut.substring(x + 1), transposeBy);
				}
//				Log.d(TAG,"Transposing: [" + chordIn + "] to [" + chordOut + "]");
			}
		}
		return chordOut;
	}
	/**
	 * Transposes an entire song by given number of semi-tones
	 * @param songIn
	 * @return
	 */
	public static String song( String songIn, int transposeBy){
		// Create a StringBuilder
		String replacement="";
		StringBuilder cs = new StringBuilder(songIn);
		// Loop thru song replacing all chords with transposed versions
		int start, end;
		int i = 0;
		String chord = null;
		// Loop thru songtext...
		if (true) {
			end = 0;
			while ((i = cs.indexOf("[", end)) >= 0) {
				start = ++i;
				if ((end = cs.indexOf("]", start)) >= 0) {
					chord = cs.substring(start, end);
					chord = cleanUpChord(chord);
					if (!chord.equalsIgnoreCase("")	) {
						// Replace chord with transposed version
						replacement = chord(chord, transposeBy);
//						Log.d(TAG,"Replacing chord[" + chord + "] with ["+replacement+"]");
						cs.replace(start, end, replacement);
					}
				}
				// Start looking for the next open bracket just after the last one
				end=start;
			}
		}
		// return new string
		return cs.toString();
	}
	/**
	 * Removes any dodgy characters from a chord (e.g. | - etc). Returns
	 * cleaned-up chord
	 *
	 * @param chord
	 * @return
	 */
	public static String cleanUpChord(String chord) {
		if(chord.equalsIgnoreCase("")){
			return "";
		}
		// Chord must start with  A-G
		if(!"ABCDEFG".contains(chord.substring(0, 1))){
			return "";
		}
		// chop off anything after a space character
		int x = chord.indexOf(" ");
		if( x >- 1 ){
			chord = chord.substring(0, x);
		}
		return chord;
	}

}
