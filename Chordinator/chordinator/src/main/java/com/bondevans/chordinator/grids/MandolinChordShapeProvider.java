package com.bondevans.chordinator.grids;

public class MandolinChordShapeProvider extends ChordShapeProvider implements ChordShapeProvider.ShapeProvider {
	private final static int strings = 4;
	//declare and initialise two dimensional array
	// set of chords for each root
	private final static String mandolinShapes[][] = {
		{"A",		"2245_1"},
		{"A11",		"xxxx_1"},
		{"A13",		"xxxx_1"},
		{"A5",		"2200_1"},
		{"A6",		"3112_4"},
		{"A7",		"2243_1"},
		{"A9",		"2343_1"},
		{"Aaug",	"2345_1"},
		{"Adim",	"2132_1"},
		{"Adim7",	"2132_1"},
		{"Am",		"2235_1"},
		{"Am6",		"2232_1"},
		{"Am7",		"2233_1"},
		{"Am7b5",	"2133_1"},
		{"Am9",		"xxxx_1"},
		{"Amaj7",	"2244_1"},
		{"Asus2",	"xxxx_1"},
		{"Asus4",	"xxxx_1"},
		{"Bb",		"1134_3"},
		{"Bb11",	"xxxx_1"},
		{"Bb13",	"xxxx_1"},
		{"Bb5",		"xxxx_1"},
		{"Bb6",		"0011_1"},
		{"Bb7",		"3354_1"},
		{"Bb9",		"3454_1"},
		{"Bbaug",	"1234_3"},
		{"Bbdim",	"3243_1"},
		{"Bbdim7",	"xxxx_1"},
		{"Bbm",		"1124_3"},
		{"Bbm6",	"3343_1"},
		{"Bbm7",	"3344_1"},
		{"Bbm7b5",	"xxxx_1"},
		{"Bbm9",	"xxxx_1"},
		{"Bbmaj7",	"3355_1"},
		{"Bbsus2",	"xxxx_1"},
		{"Bbsus4",	"xxxx_1"},
		{"B",		"2240_2"},
		{"B11",		"xxxx_1"},
		{"B13",		"xxxx_1"},
		{"B5",		"xxxx_1"},
		{"B6",		"1122_1"},
		{"B7",		"2122_1"},
		{"B9",		"4142_1"},
		{"Baug",	"1234_4"},
		{"Bdim",	"2132_3"},
		{"Bdim7",	"xxxx_1"},
		{"Bm",		"4022_1"},
		{"Bm6",		"1022_1"},
		{"Bm7",		"2022_1"},
		{"Bm7b5",	"xxxx_1"},
		{"Bm9",		"xxxx_1"},
		{"Bmaj7",	"0022_4"},
		{"Bsus2",	"xxxx_1"},
		{"Bsus4",	"xxxx_1"},
		{"C",		"0230_1"},
		{"C11",		"0330_1"},
		{"C13",		"xxxx_1"},
		{"C5",		"xxxx_1"},
		{"C6",		"5500_1"},
		{"C7",		"5213_1"},
		{"C9",		"0030_1"},
		{"Caug",	"1234_5"},
		{"Cdim",	"2132_4"},
		{"Cdim7",	"xxxx_1"},
		{"Cm",		"0133_1"},
		{"Cm6",		"5103_1"},
		{"Cm7",		"3133_1"},
		{"Cm7b5",	"xxxx_1"},
		{"Cm9",		"xxxx_1"},
		{"Cmaj7",	"5520_1"},
		{"Csus2",	"xxxx_1"},
		{"Csus4",	"xxxx_1"},
		{"C#",		"1344_1"},
		{"C#11",	"1441_1"},
		{"C#13",	"xxxx_1"},
		{"C#5",		"xxxx_1"},
		{"C#6",		"3344_1"},
		{"C#7",		"4102_2"},
		{"C#9",		"1031_3"},
		{"C#aug",	"1234_6"},
		{"C#dim",	"2132_5"},
		{"C#dim7",	"xxxx_1"},
		{"C#m",		"1244_1"},
		{"C#m6",	"1246_1"},
		{"C#m7",	"4244_1"},
		{"C#m7b5",	"xxxx_1"},
		{"C#m9",	"xxxx_1"},
		{"C#maj7",	"3001_3"},
		{"C#sus2",	"xxxx_1"},
		{"C#sus4",	"xxxx_1"},
		{"D",		"2002_1"},
		{"D11",		"0330_2"},
		{"D13",		"xxxx_1"},
		{"D5",		"xxxx_1"},
		{"D6",		"2022_1"},
		{"D7",		"2032_1"},
		{"D9",		"2450_1"},
		{"Daug",	"1234_7"},
		{"Ddim",	"2132_6"},
		{"Ddim7",	"xxxx_1"},
		{"Dm",		"2001_1"},
		{"Dm6",		"2021_1"},
		{"Dm7",		"2022_3"},
		{"Dm7b5",	"xxxx_1"},
		{"Dm9",		"xxxx_1"},
		{"Dmaj7",	"2042_1"},
		{"Dsus2",	"xxxx_1"},
		{"Dsus4",	"xxxx_1"},
		{"Eb",		"0113_1"},
		{"Eb11",	"0330_3"},
		{"Eb13",	"xxxx_1"},
		{"Eb5",		"xxxx_1"},
		{"Eb6",		"3133_1"},
		{"Eb7",		"3143_1"},
		{"Eb9",		"0111_1"},
		{"Ebaug",	"1234_8"},
		{"Ebdim",	"2132_7"},
		{"Ebdim7",	"xxxx_1"},
		{"Ebm",		"0133_3"},
		{"Ebm6",	"3132_1"},
		{"Ebm7",	"3142_1"},
		{"Ebm7b5",	"xxxx_1"},
		{"Ebm9",	"xxxx_1"},
		{"Ebmaj7",	"3153_1"},
		{"Ebsus2",	"xxxx_1"},
		{"Ebsus4",	"xxxx_1"},
		{"E",		"1220_1"},
		{"E11",		"0330_4"},
		{"E13",		"xxxx_1"},
		{"E5",		"xxxx_1"},
		{"E6",		"2022_2"},
		{"E7",		"1020_1"},
		{"E9",		"1222_1"},
		{"Eaug",	"1234_9"},
		{"Edim",	"2132_8"},
		{"Edim7",	"xxxx_1"},
		{"Em",		"0220_1"},
		{"Em6",		"4243_1"},
		{"Em7",		"0020_1"},
		{"Em7b5",	"xxxx_1"},
		{"Em9",		"xxxx_1"},
		{"Emaj7",	"1120_1"},
		{"Esus2",	"xxxx_1"},
		{"Esus4",	"xxxx_1"},
		{"F",		"5301_1"},
		{"F11",		"0330_5"},
		{"F13",		"xxxx_1"},
		{"F5",		"xxxx_1"},
		{"F6",		"2031_1"},
		{"F7",		"2131_1"},
		{"F9",		"2333_1"},
		{"Faug",	"1234_10"},
		{"Fdim",	"2132_9"},
		{"Fdim7",	"xxxx_1"},
		{"Fm",		"1331_1"},
		{"Fm6",		"1031_1"},
		{"Fm7",		"1131_1"},
		{"Fm7b5",	"xxxx_1"},
		{"Fm9",		"xxxx_1"},
		{"Fmaj7",	"2330_1"},
		{"Fsus2",	"xxxx_1"},
		{"Fsus4",	"xxxx_1"},
		{"F#",		"3442_1"},
		{"F#11",	"xxxx_1"},
		{"F#13",	"xxxx_1"},
		{"F#5",		"xxxx_1"},
		{"F#6",		"3142_1"},
		{"F#7",		"3440_1"},
		{"F#9",		"3444_1"},
		{"F#aug",	"1234_11"},
		{"F#dim",	"2132_10"},
		{"F#dim7",	"xxxx_1"},
		{"F#m",		"2442_1"},
		{"F#m6",	"2142_1"},
		{"F#m7",	"2440_1"},
		{"F#m7b5",	"xxxx_1"},
		{"F#m9",	"xxxx_1"},
		{"F#maj7",	"3441_1"},
		{"F#sus2",	"xxxx_1"},
		{"F#sus4",	"xxxx_1"},
		{"G",		"0023_1"},
		{"G11",		"xxxx_1"},
		{"G13",		"xxxx_1"},
		{"G5",		"xxxx_1"},
		{"G6",		"0020_1"},
		{"G7",		"0021_1"},
		{"G9",		"0025_1"},
		{"Gaug",	"0123_1"},
		{"Gdim",	"2132_11"},
		{"Gdim7",	"xxxx_1"},
		{"Gm",		"0013_1"},
		{"Gm6",		"0010_1"},
		{"Gm7",		"0011_1"},
		{"Gm7b5",	"xxxx_1"},
		{"Gm9",		"xxxx_1"},
		{"Gmaj7",	"0022_1"},
		{"Gsus2",	"xxxx_1"},
		{"Gsus4",	"xxxx_1"},
		{"Ab",		"1134_1"},
		{"Ab11",	"xxxx_1"},
		{"Ab13",	"xxxx_1"},
		{"Ab5",		"xxxx_1"},
		{"Ab6",		"1131_1"},
		{"Ab7",		"1132_1"},
		{"Ab9",		"4303_3"},
		{"Abaug",	"1234_1"},
		{"Abdim",	"1021_1"},
		{"Abdim7",	"xxxx_1"},
		{"Abm",		"1124_1"},
		{"Abm6",	"1121_1"},
		{"Abm7",	"1122_1"},
		{"Abm7b5",	"xxxx_1"},
		{"Abm9",	"xxxx_1"},
		{"Abmaj7",	"1133_1"},
		{"Absus2",	"xxxx_1"},
		{"Absus4",	"xxxx_1"},
	};
	/**
	 * Constructor takes height in points of grid
	 * @param maxHeight
	 */
	public MandolinChordShapeProvider(String songGrids){
		// Ignore any extra shapes as these will only work for Guitar
		super(songGrids, strings);	
	}
	/* (non-Javadoc)
	 * @see com.bondevans.chordinator.grids.ChordShapeProvider#getShapes()
	 */
	@Override
	public String[][] getShapes() {
		return mandolinShapes;
	}
	/* (non-Javadoc)
	 * @see com.bondevans.chordinator.grids.ChordShapeProvider#getStrings()
	 */
	@Override
	public int STRINGS() {
		return strings;
	}
	/* (non-Javadoc)
	 * @see com.bondevans.chordinator.grids.ChordShapeProvider#FRETS()
	 */
	@Override
	public int FRETS() {
		// Need 6 frets (actuall 5) for Mandolin
		return 6;
	}
}






