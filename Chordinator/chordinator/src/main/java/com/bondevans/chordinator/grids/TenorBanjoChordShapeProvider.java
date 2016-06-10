package com.bondevans.chordinator.grids;

public class TenorBanjoChordShapeProvider extends ChordShapeProvider implements ChordShapeProvider.ShapeProvider {
	private final static int strings = 4;
    //declare and initialise two dimensional array
	// set of chords for each root
	private final static String tenorBanjoShapes[][] = {
            {"A",		"1220_1"},
            {"A11",		"1200_1"},
            {"A13",		"4244_1"},
            {"A5",		"4220_1"},
            {"A6",		"4244_1"},
            {"A7",		"1020_1"},
            {"A9",		"1222_1"},
            {"Aaug",	"1230_1"},
            {"Adim",	"1210_1"},
            {"Adim7",	"3243_1"},
            {"Am",		"0220_1"},
            {"Am6",		"4243_1"},
            {"Am7",		"0020_1"},
            {"Am7b5",	"0010_1"},
            {"Am9",		"0450_1"},
            {"Amaj7",	"1120_1"},
            {"Asus2",	"4420_1"},
            {"Asus4",	"2220_1"},
            {"Bb",		"5301_1"},
            {"Bb11",	"3301_1"},
            {"Bb13",	"2001_1"},
            {"Bb5",		"x331_1"},
            {"Bb6",		"2031_1"},
            {"Bb7",		"2131_1"},
            {"Bb9",		"2333_1"},
            {"Bbaug",	"2341_1"},
            {"Bbdim",	"1321_1"},
            {"Bbdim7",	"1021_1"},
            {"Bbm",		"1331_1"},
            {"Bbm6",	"1031_1"},
            {"Bbm7",	"1131_1"},
            {"Bbm7b5",	"1121_1"},
            {"Bbm9",	"4124_5"},
            {"Bbmaj7",	"2330_1"},
            {"Bbsus2",	"0331_1"},
            {"Bbsus4",	"3331_1"},
            {"B",		"3442_1"},
            {"B11",		"3422_1"},
            {"B13",		"3112_1"},
            {"B5",		"x442_1"},
            {"B6",		"3142_1"},
            {"B7",		"3440_1"},
            {"B9",		"3444_1"},
            {"Baug",	"3012_1"},
            {"Bdim",	"3432_1"},
            {"Bdim7",	"2132_1"},
            {"Bm",		"2442_1"},
            {"Bm6",		"2142_1"},
            {"Bm7",		"2440_1"},
            {"Bm7b5",	"2232_1"},
            {"Bm9",		"1202_1"},
            {"Bmaj7",	"3441_1"},
            {"Bsus2",	"3111_4"},
            {"Bsus4",	"4442_1"},
            {"C",		"0023_1"},
            {"C11",		"4033_1"},
            {"C13",		"0020_1"},
            {"C5",		"0053_1"},
            {"C6",		"0020_1"},
            {"C7",		"0021_1"},
            {"C9",		"0025_1"},
            {"Caug",	"0123_1"},
            {"Cdim",	"2321_3"},
            {"Cdim7",	"3243_1"},
            {"Cm",		"0013_1"},
            {"Cm6",		"0010_1"},
            {"Cm7",		"0011_1"},
            {"Cm7b5",	"3343_1"},
            {"Cm9",		"2313_1"},
            {"Cmaj7",	"0022_1"},
            {"Csus2",	"0003_1"},
            {"Csus4",	"0033_1"},
            {"C#",		"1134_1"},
            {"C#11",	"2311_4"},
            {"C#13",	"1131_1"},
            {"C#5",		"11xx_1"},
            {"C#6",		"1131_1"},
            {"C#7",		"1132_1"},
            {"C#9",		"3434_1"},
            {"C#aug",	"1234_1"},
            {"C#dim",	"1021_1"},
            {"C#dim7",	"4354_1"},
            {"C#m",		"1124_1"},
            {"C#m6",	"1121_1"},
            {"C#m7",	"1122_1"},
            {"C#m7b5",	"1022_1"},
            {"C#m9",	"3424_1"},
            {"C#maj7",	"1133_1"},
            {"C#sus2",	"111x_1"},
            {"C#sus4",	"3331_4"},
            {"D",		"1134_2"},
            {"D11",		"2040_1"},
            {"D13",		"2440_1"},
            {"D5",		"2200_1"},
            {"D6",		"2440_1"},
            {"D7",		"2243_1"},
            {"D9",		"2243_1"},
            {"Daug",	"1234_2"},
            {"Ddim",	"2132_1"},
            {"Ddim7",	"2132_1"},
            {"Dm",		"1124_2"},
            {"Dm6",		"2232_1"},
            {"Dm7",		"2233_1"},
            {"Dm7b5",	"2133_1"},
            {"Dm9",		"2313_3"},
            {"Dmaj7",	"2244_1"},
            {"Dsus2",	"2220_1"},
            {"Dsus4",	"2000_1"},
            {"Eb",		"3011_1"},
            {"Eb11",	"2011_6"},
            {"Eb13",	"0011_1"},
            {"Eb5",		"3311_1"},
            {"Eb6",		"0011_1"},
            {"Eb7",		"1132_3"},
            {"Eb9",		"1132_3"},
            {"Ebaug",	"3012_1"},
            {"Ebdim",	"3243_1"},
            {"Ebdim7",	"3243_1"},
            {"Ebm",		"334x_1"},
            {"Ebm6",	"3343_1"},
            {"Ebm7",	"3344_1"},
            {"Ebm7b5",	"3244_1"},
            {"Ebm9",	"2313_4"},
            {"Ebmaj7",	"1133_3"},
            {"Ebsus2",	"3331_1"},
            {"Ebsus4",	"3111_1"},
            {"E",		"x122_1"},
            {"E11",		"1130_4"},
            {"E13",		"1122_1"},
            {"E5",		"4422_1"},
            {"E6",		"1122_1"},
            {"E7",		"2122_1"},
            {"E9",		"1212_6"},
            {"Eaug",	"0123_1"},
            {"Edim",	"x021_1"},
            {"Edim7",	"1021_1"},
            {"Em",		"4022_1"},
            {"Em6",		"1022_1"},
            {"Em7",		"2022_1"},
            {"Em7b5",	"2021_1"},
            {"Em9",		"1012_4"},
            {"Emaj7",	"3122_1"},
            {"Esus2",	"4442_1"},
            {"Esus4",	"4420_1"},
            {"F",		"0230_1"},
            {"F11",		"0330_1"},
            {"F13",		"2230_1"},
            {"F5",		"0311_3"},
            {"F6",		"1100_5"},
            {"F7",		"3233_1"},
            {"F9",		"3030_1"},
            {"Faug",	"1230_1"},
            {"Fdim",	"5430_1"},
            {"Fdim7",	"2132_1"},
            {"Fm",		"0133_1"},
            {"Fm6",		"2133_1"},
            {"Fm7",		"3133_1"},
            {"Fm7b5",	"3132_1"},
            {"Fm9",		"1022_5"},
            {"Fmaj7",	"4230_1"},
            {"Fsus2",	"0033_1"},
            {"Fsus4",	"0331_1"},
            {"F#",		"1344_1"},
            {"F#11",	"x342_1"},
            {"F#13",	"3344_1"},
            {"F#5",		"3311_4"},
            {"F#6",		"3344_1"},
            {"F#7",		"4344_1"},
            {"F#9",		"1212_8"},
            {"F#aug",	"234x_1"},
            {"F#dim",	"0343_1"},
            {"F#dim7",	"3243_1"},
            {"F#m",		"1244_1"},
            {"F#m6",	"3240_1"},
            {"F#m7",	"4244_1"},
            {"F#m7b5",	"4243_1"},
            {"F#m9",	"2313_7"},
            {"F#maj7",	"5344_1"},
            {"F#sus2",	"6664_1"},
            {"F#sus4",	"6444_1"},
            {"G",		"2002_1"},
            {"G11",		"0002_1"},
            {"G13",		"2022_1"},
            {"G5",		"3001_5"},
            {"G6",		"2022_1"},
            {"G7",		"2032_1"},
            {"G9",		"5450_1"},
            {"Gaug",	"3012_1"},
            {"Gdim",	"10x2_1"},
            {"Gdim7",	"1021_1"},
            {"Gm",		"2001_1"},
            {"Gm6",		"2021_1"},
            {"Gm7",		"2031_1"},
            {"Gm7b5",	"1031_1"},
            {"Gm9",		"5350_1"},
            {"Gmaj7",	"2042_1"},
            {"Gsus2",	"2000_1"},
            {"Gsus4",	"0003_1"},
            {"Ab",		"0113_1"},
            {"Ab11",	"1113_1"},
            {"Ab13",	"0133_1"},
            {"Ab5",		"311x_1"},
            {"Ab6",		"3133_1"},
            {"Ab7",		"2122_5"},
            {"Ab9",		"0111_1"},
            {"Abaug",	"0123_1"},
            {"Abdim",	"0103_1"},
            {"Abdim7",	"2132_1"},
            {"Abm",		"3112_1"},
            {"Abm6",	"3132_1"},
            {"Abm7",	"3142_1"},
            {"Abm7b5",	"3132_4"},
            {"Abm9",	"2313_9"},
            {"Abmaj7",	"3153_1"},
            {"Absus2",	"3111_1"},
            {"Absus4",	"111x_1"},
    };

    private int[] openNotes = {48,55,62,69};    // C G D A

	/**
	 * Constructor takes height in points of grid
	 * @param songGrids Additional user-defined grids for this song
	 *                  (Not used for Tenor Banjo)
	 */
	public TenorBanjoChordShapeProvider(String songGrids){
		// Ignore any extra shapes as these will only work for Guitar
		super(songGrids, strings);
    }
	/* (non-Javadoc)
	 * @see com.bondevans.chordinator.grids.ChordShapeProvider#getShapes()
	 */
	@Override
	public String[][] getShapes() {
		return tenorBanjoShapes;
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
		// Need 7 frets (actually 6) for tenor Banjo
		return 7;
	}
    @Override
    public int[] openNotes() {
        return openNotes;
    }
}






