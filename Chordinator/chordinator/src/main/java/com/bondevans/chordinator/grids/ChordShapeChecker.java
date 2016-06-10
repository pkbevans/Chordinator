package com.bondevans.chordinator.grids;

import com.bondevans.chordinator.Log;

import java.util.HashMap;
import java.util.Map;

public class ChordShapeChecker {
    private final static String [] rootNotes = {"C","C#","D","Eb","E","F","F#","G","Ab","A","Bb","B"};
    private static final String TAG = ChordShapeChecker.class.getSimpleName();
    private String[] intervals={"1st","b9","2nd","b3rd","3rd","4th","b5th","5th","+5th","6th","b7th","maj7th"};
    Map ints;

    public ChordShapeChecker(){
        Log.d(TAG, "Guitar Shapes");
        GuitarChordShapeProvider guitar = new GuitarChordShapeProvider("");
        checkShapes(guitar.getShapes(), guitar.STRINGS(),guitar.openNotes());
        Log.d(TAG, "Ukelele Shapes");
        UkeleleChordShapeProvider ukelele = new UkeleleChordShapeProvider("");
        checkShapes(ukelele.getShapes(), ukelele.STRINGS(),ukelele.openNotes());
        Log.d(TAG, "Mandolin Shapes");
        MandolinChordShapeProvider mandolin = new MandolinChordShapeProvider("");
        checkShapes(mandolin.getShapes(), mandolin.STRINGS(),mandolin.openNotes());
        Log.d(TAG, "Banjo Shapes");
        TenorBanjoChordShapeProvider tenorBanjo = new TenorBanjoChordShapeProvider("");
        checkShapes(tenorBanjo.getShapes(), tenorBanjo.STRINGS(),tenorBanjo.openNotes());
    }
    void checkShapes(String[][] shapes, int strings, int[] openNotes){
        setIntervalMap();
        int note, interval, root, firstFret;
        String type;
        String log;
        for(String[] shape: shapes){
            root = getRoot(shape[0]);
            type = shape[0].substring(rootNotes[root].length());
            firstFret = Integer.valueOf(shape[1].substring(strings+1));
            log = shape[0]+ ","+shape[1]+","+ rootNotes[root]+","+ type+","+firstFret;
            for(int string = 0; string< strings; string++){
                if(shape[1].substring(string, string+1).equalsIgnoreCase("x")){
                    note=interval=0;
                }
                else {
                    note = getNote(string, Integer.valueOf(shape[1].substring(string, string + 1)), firstFret, openNotes);
                    interval = getInterval(root, note);
                }
                if(!isIntervalValid(type, intervals[interval])){
                    Log.d(TAG, "BAD SHAPE! "+log+","+ (string+1)+
                            ","+ shape[1].substring(string, string+1)+
                            "," + rootNotes[(note%12)]+
                            ","+ intervals[interval]
                    );
                }
            }
        }
    }

    void setIntervalMap(){
        ints = new HashMap<>();
        // For this type, is this interval valid
        ints.put("major",   "1st 3rd 5th");
        ints.put("5",		"1st 5th");
        ints.put("13",		"1st 3rd 5th b7th 2nd 4th 6th");
        ints.put("11",		"1st 3rd 5th b7th 2nd 4th");
        ints.put("6",		"1st 3rd 5th 6th");
        ints.put("7",		"1st 3rd 5th b7th");
        ints.put("9",		"1st 3rd 5th b7th 2nd");
        ints.put("aug",	    "1st 3rd +5th");
        ints.put("dim",	    "1st b3rd b5th");
        ints.put("dim7",	"1st b3rd b5th 6th");
        ints.put("m",		"1st b3rd 5th");
        ints.put("m6",		"1st b3rd 5th 6th");
        ints.put("m7",		"1st b3rd 5th b7th");
        ints.put("m7b5",	"1st b3rd b5th b7th");
        ints.put("m9",		"1st 2nd b3rd 5th b7th");
        ints.put("maj7",	"1st 3rd 5th maj7th");
        ints.put("sus2",	"1st 2nd 5th");
        ints.put("sus4",	"1st 4th 5th");
    }
    private boolean isIntervalValid(String type, String interval) {
        String myType;
        myType = (type.isEmpty()?"major":type);
        if(ints.containsKey(myType)){
            String i= (String) ints.get(myType);
            if(i.contains(interval)){
                return true;
            }
            else{
                return false;
            }
        }
        else{
            Log.e(TAG, "OOPS ERROR!!!!!");
        }
        return false;
    }

    private int getInterval(int root, int note) {
        int a = note % 12;
        //        Log.d(TAG, "interval: "+ret+ " root:"+root+" note:"+note);
        return ((12-root)+a)%12;
    }

    private int getNote(int string, int fret, int firstFret, int[] openNotes) {
        if( firstFret == 1) {
            return openNotes[string] + fret;
        }
        else{
            return openNotes[string] + fret+(fret>0?firstFret-1:0);
        }
    }

    private int getRoot(String shape) {
        for(int root=0; root< rootNotes.length;root++){
            if(shape.length()>1 && shape.substring(0,2).equalsIgnoreCase(rootNotes[root])){
                return root;
            }
        }
        for(int root=0; root< rootNotes.length;root++){
            if(shape.length()>0 && shape.substring(0,1).equalsIgnoreCase(rootNotes[root])){
                return root;
            }
        }
        return 999;
    }
}
