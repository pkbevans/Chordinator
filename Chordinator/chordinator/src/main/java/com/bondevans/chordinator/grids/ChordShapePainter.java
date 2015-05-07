package com.bondevans.chordinator.grids;

import com.bondevans.chordinator.Log;
import com.bondevans.chordinator.grids.ChordShapeProvider.ShapeProvider;

import android.graphics.Canvas;
import android.graphics.Paint;


public class ChordShapePainter {
	private final static String TAG = "ChordGridPainter";
	private Paint mPaint;

	private int gridHeight;		// Max height of grid - doesn't include Chord name above
	private int gridWidth;		// Max width of grid - doesn't include the fret to the side
	private int hSpace;			// space between the strings
	private int vSpace;			// space between the frets

	public final static int STRING_WIDTH = 1;	// Must be ODD number!!
	public static int FRETS = 5;
	public final static int FRET_HEIGHT = 1;
	private ShapeProvider mShapes;
	/**
	 * Constructor takes height in points of grid
	 * @param maxHeight
	 */
	public ChordShapePainter(int maxWidth, Paint paint, String songGrids, int instrument){
		//gridHeight = maxHeight; // Start with the maximum height specified - this will probably be re-calculated
		mPaint = paint;
		switch (instrument){
		case ChordShapeProvider.INSTRUMENT_GUITAR:
			mShapes = (ShapeProvider)new GuitarChordShapeProvider(songGrids);
			break;
		case ChordShapeProvider.INSTRUMENT_MANDOLIN:
			mShapes = (ShapeProvider)new MandolinChordShapeProvider(songGrids);
			break;
		case ChordShapeProvider.INSTRUMENT_UKELELE:
			mShapes = (ShapeProvider)new UkeleleChordShapeProvider(songGrids);
			break;
		case ChordShapeProvider.INSTRUMENT_BANJO:
				mShapes = (ShapeProvider)new TenorBanjoChordShapeProvider(songGrids);
				break;
		}
		FRETS = mShapes.FRETS();
		initGrid(maxWidth);
	}

	private void initGrid(int maxWidth){
		// Need to allow some space around the grid - see getShapeWidth()
		maxWidth -= mPaint.measureText("XXXXX");
		// Work out the Horizontal space between the strings based on a maximum width.
		// width is calculated from height based on a proportion of height:width => 3:2
		hSpace = (maxWidth-(mShapes.STRINGS()*STRING_WIDTH))/(mShapes.STRINGS()-1);
		// recalculate actual width of grid (=< height)
		gridWidth = (hSpace*(mShapes.STRINGS()-1))+(mShapes.STRINGS()*STRING_WIDTH);
		// Make it a square grid
		int maxHeight = gridWidth;
		// Work out the Horizontal space between the strings based on a maximum height
		vSpace = (maxHeight-(FRETS*FRET_HEIGHT))/(FRETS-1);
		// recalculate actual height of grid (=< height)
		gridHeight = (vSpace*(FRETS-1))+(FRETS*FRET_HEIGHT);
		Log.d(TAG,"initGrid width=["+gridWidth+"] hspace = ["+hSpace+"] height=["+gridHeight+"] vspace = ["+vSpace+"]");
	}
	/*
	 * Update current Paint.
	 */
	public void setPaint(Paint paint){
		mPaint = paint;
	}
	/**
	 * Returns the height of the complete shape including the chord name above the grid - this will be the same
	 * for all chords
	 * @return
	 */
	public int getShapeHeight(){
		return (int) ((mPaint.getTextSize()*2.5) + gridHeight);
	}

	/**
	 * Returns the maximum width of the complete shape including space either side.
	 * This width allows a bit of space so that grids can be placed next to each other.
	 * This will be the same for all chords
	 * @return
	 */
	public int getShapeWidth(){
		return (int) (gridWidth + mPaint.measureText("XXXXX"));
	}


	public int drawShape(Canvas g, String chord, int x, int y ){
		// Find chord shape from given name
		String shape = mShapes.findShape(chord);
		//		Log.d(TAG,"drawShape: [" + chord+"]["+ (shape.equalsIgnoreCase("")?"Unknown": shape)+"]");

		// Draw the grid centred between the x value supplied and the getShapeWidth() specified -
		// need to adjust x value accordingly
		x += (getShapeWidth() - gridWidth) / 2;

		// Write out the chord name centred above the grid
		g.drawText(chord,x+((gridWidth - mPaint.measureText(chord))/2), y, mPaint);
		// Move yPos (start of Grid) down below chord name (allow for xs and zeros above strings)
		int gridTopY = (int) (y + (mPaint.getTextSize()*1.5));

		// Draw the grid
		drawGrid(g, (int)x, (int)gridTopY);
		if(shape.equalsIgnoreCase("")){
			// UNKNOWN chord so say so - centred across 2nd fret
			g.drawText("Unknown",
					x+((gridWidth - mPaint.measureText("Unknown"))/2),
					gridTopY + (gridHeight - mPaint.getTextSize())/2,mPaint);
		}
		else{
			// Draw the first fret position - next to first fret - unless its 1
			int firstFret = Integer.valueOf(shape.substring(mShapes.STRINGS()+1));
			if(firstFret>1){
				int ffy = fretPos(gridTopY,1)-((fretPos(gridTopY,1)-gridTopY)/2)+(int)(mPaint.getTextSize()/2);
				g.drawText(""+ firstFret, x + gridWidth + vDotSize(), ffy, mPaint);
			}
			// Draw the dots
			for (int i=0; i<mShapes.STRINGS(); i++){
				int dotPos = shape.charAt(i);

				if( dotPos == 'N'|| dotPos == 'n' || dotPos == 'x' || dotPos == 'X'){
					// Draw an x above the string
					g.drawText("x",stringPos(x,i)-(mPaint.measureText("x")/2), y+mPaint.getTextSize(), mPaint);
				}
				else if( dotPos>'0'){
					// Draw a "dot" centred on the string and centred between the frets
					dotPos -= 48;
					g.drawCircle(stringPos(x,i),
							gridTopY+((dotPos-1)*FRET_HEIGHT)+(dotPos*vSpace)-(vSpace/2),
							vDotSize(),
							mPaint);
				}
				else {
					// Its a zero (open string) so print 'o' above fretboard
					g.drawText("o",stringPos(x,i)-(mPaint.measureText("o")/2), y+mPaint.getTextSize(), mPaint);
				}
			}
		}
		return (int) (gridTopY + gridHeight);
	}

	private void drawGrid(Canvas g, int x, int yPos){
		// Draw the strings
		for (int i=0; i<mShapes.STRINGS(); i++){
			g.drawLine(stringPos(x,i),yPos, stringPos(x,i),yPos+gridHeight-1, mPaint);
			//Log.d(TAG,"String:(" + stringPos(x,i) + "," + yPos + ") to (" + (stringPos(x,i) + "," + (yPos+gridHeight-1) + ")"));
		}
		// Draw the frets
		for (int i=0; i<FRETS; i++){
			g.drawLine(x-1,fretPos(yPos,i), x+gridWidth, fretPos(yPos,i), mPaint);
			//Log.d(TAG,"Fret: (" + (x-1) + "," + fretPos(yPos,i) + ") to (" + (x+gridWidth+1) + "," + fretPos(yPos,i) + ")");
		}
	}
	/**
	 * Returns position of given string (0-5) from a start point of x
	 * @param x
	 * @param string
	 * @return
	 */
	private int stringPos(int x, int string){
		return(x+(string*STRING_WIDTH)+(string*hSpace));
	}
	/**
	 * Returns position of Fret (0-5) from a start point of y
	 * @param y
	 * @param fret
	 * @return
	 */
	private int fretPos(int y, int fret){
		return (y+(fret*FRET_HEIGHT)+(fret*vSpace));
	}

	private int vDotSize(){
		return (int) (vSpace/2.5);
	}
}






