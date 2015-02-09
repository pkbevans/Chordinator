package com.bondevans.chordinator.grids;

import java.util.Enumeration;
import java.util.Vector;

import com.bondevans.chordinator.Log;



public class ExtraShapeFactory {
	private static final String TAG = "ExtraShapeFactory";
	private Vector<ExtraShape> shapes = new Vector<ExtraShape>();
	/**
	 * Constructor takes extra chord grids for a particular song
	 * @param maxHeight
	 */
	public ExtraShapeFactory(String extraGrids){
		parseSongGrids(extraGrids, 6);
	}

	/**
	 * Constructor takes extra chord grids, and number of strings for this instrument for a particular song
	 * @param maxHeight
	 */
	public ExtraShapeFactory(String extraGrids, int strings){
		parseSongGrids(extraGrids, strings);
	}

	private void parseSongGrids(String grids, int strings){
		// Incoming grids are in the form: "chordname1:xxxxxx_y,chordname2:xxxxxx_y"
		// where x=dot fret positions and y=starting fret
		Vector<String> chords = Split( grids, ",");
//		int a=0;
		for (Enumeration<String> e = chords.elements() ; e.hasMoreElements() ;) {
			String chordDef = (String) e.nextElement();
			Vector<String> z = Split(chordDef, ":");
			if(z.size()>1){
				String name = z.elementAt(0);
				String shape = z.elementAt(1);
				if(shape.length() == (strings+2)){
					shapes.addElement(new ExtraShape(name, shape));
//					shapes.addElement(new ExtraShape((String)z.elementAt(0), (String)z.elementAt(1)));
					Log.d(TAG, "HELLO - Adding shape: "+name+":"+shape);
//				a++;
				}
				else{
					Log.d(TAG, "HELLO - ignoring shape: "+name+":"+shape);
				}
			}
		}
	}

	/**
	 * Split a string given a delimiting character
	 * @param in
	 * @param delimiter
	 * @return
	 */
	private Vector<String> Split( String in, String delimiter ){
		Vector<String> out = new Vector<String>();
		int start=0;
		int end = in.indexOf(delimiter, start);
		while( end > -1 ){
			out.addElement(in.substring(start, end));
			start = end + 1;
			end = in.indexOf(delimiter, start);
		}
		// Don't forget last bit
		if( start <= in.length()){
			out.addElement(in.substring(start));
		}
		return out;
	}

	/**
	 * Finds chord shape matching given chord name - sets shapeIndex
	 * @param chordname
	 *
	 */
	public String findShape(String chordname) {
		String shape="";
		// Look in the extra shapes..... 
		for(int i=0; shape.equalsIgnoreCase("") && i< shapes.size();i++){
			if(((ExtraShape) shapes.elementAt(i)).getName().equalsIgnoreCase(chordname)){
				shape = ((ExtraShape) shapes.elementAt(i)).getShape();
			}		
		}
		return shape;
	}

	private class ExtraShape{
		private String name;
		private String shape;
		public ExtraShape(String name, String shape){
			this.name = name;
			this.shape = shape;
		}
		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}
		/**
		 * @return the shape
		 */
		public String getShape() {
			return shape;
		}
	}
}
